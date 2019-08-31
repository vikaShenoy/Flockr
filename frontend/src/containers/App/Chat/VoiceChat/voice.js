import Janus from "./janus";
import config from "../../../../config";
import { EventEmitter } from "events";
import UserStore from "../../../../stores/UserStore";
import superagent from "superagent";
import { endpoint } from "../../../../utils/endpoint";

const server = config.webrtcUrl;


/**
 * A class responsible with sending and receiving web rtc data
 * from the janus server
 *
 */
export class VoiceChat extends EventEmitter {
  // The communication medium on what to send/receiver messages from
  channel;
  // Session ID of the current user
  sessionId;
  // The voice chat room that the user is connected to
  room;
  // Defines if webrtc is up or not
  webrtcUp = false;
  // Uniquely identifies a user before a session is createdd
  opaqueId = `session-${Janus.randomString(12)}`;
  // Janus instance which can be useful for destroying the session
  janus;
  // ID of the instance of the created audioroom plugin
  pluginHandleId;

  /**
   * Establishes communication with janus server.
   */
  constructor() {
    super();
    Janus.init({
      debug: "all",
      callback: () => {
        // Create session
        this.janus = new Janus({
          server: server,
          success: () => {
            // Attach to Audio Bridge test plugin
            this.janus.attach({
              plugin: "janus.plugin.audiobridge",
              opaqueId: "hello " + this.opaqueId,
              success: channel => {
                this.sessionId = channel.session.getSessionId();
                this.pluginHandleId = channel.id;
                this.channel = channel;
              },
              error: this.handleError,
              onmessage: this.messageReceived,
              onlocalstream: stream => {
                this.emit("youConnected", stream);
              },
              onremotestream: stream => {
                console.log("Remote stream found");
                this.emit("remoteUserConnected", stream);
              },
              oncleanup: () => (this.webrtcUp = false),
            });
          },
          error: this.handleError,
          destroyed: this.handleError,
        });
      },
    });
  }

  /**
   * Joins a voice room. Will be called when user wants to
   * participate in voice chat
   */
  joinRoom = async (chatGroupId) => {
    const {room, token} = await this.getRoomDetails(chatGroupId);
    const message = {
      request: "join",
      room: room,
        token: token,
      display: UserStore.data.name,
    };
    this.channel.send({ message });
  };

  /**
   * Gets the token for a room
   */
  getRoomDetails = async (chatGroupId) => {
    try {
      const res = await superagent.post(endpoint(`/chats/${chatGroupId}/join`))
        .send({
          sessionId: this.sessionId,
          pluginHandleId: this.pluginHandleId
        })
        .set("Authorization", localStorage.getItem("authToken"));

      return res.body;

    } catch (e) {
      console.log(e);
      this.handleError(e);
    }
  }

  /**
   * Handles when a message has been received. This can either be if you have joined
   * the room or if another user has
   * @param {Object} msg The incoming message
   * @param {} jsep Incoming session establishment message
   */
  messageReceived = (message, sessionMessage) => {
    const event = message.audiobridge;
    Janus.debug("Event: " + event);
    if (event) {
      if (event === "joined") {
        // Successfully joined, negotiate WebRTC now
        this.myid = message.id;

        if (!this.webrtcUp) {
          this.webrtcUp = true;
          // Publish our stream
          this.createOffer();
        }

        // If there are already participants when joining, then emit up
        if (message.participants) {
          this.emit("participants", message.participants);
        }
      } else if (event === "roomchanged") {
        Janus.debug("The room has changed");
      } else if (event === "destroyed") {
        // The room has been destroyed
        Janus.warn("The room has been destroyed!");
      } else if (event === "event") {
        if (message.participants) {
          this.emit("participants", message.participants);
        } else if (message.leaving) {
          const userId = message.leaving;
          this.emit("participantLeft", userId);
        }
      }
    }

    if (sessionMessage) {
      this.channel.handleRemoteJsep({ jsep: sessionMessage });
    }
  };

  /**
   * Unmutes the users mic
   */
  unmute = session => {
    const publish = { request: "configure", muted: false };
    this.channel.send({ message: publish, jsep: session });
  };

  /**
   * Once the user has joined the room, they can create an offer to start
   * sending their audio data
   */

  createOffer = () => {
    this.channel.createOffer({
      media: { video: false }, // This is an audio only room
      success: this.unmute,
      error: this.handleError,
    });
  };

  /**
   * Handles any error caused by janus
   * @param {Error} error the error to handle
   */
  handleError = error => {
    this.emit("error", error);
  };

  /**
   * Destroy session
   */
  destroySession = () => {
    this.janus.destroy();
    alert("Did I destroy");
  };
}
