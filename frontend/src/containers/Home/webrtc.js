import Janus from "./janus";
import config from "../../config";
import { EventEmitter } from "events";
import UserStore from "../../stores/UserStore";
const server = config.webrtcServer;

/**
 * A class responsible with sending and receiving web rtc data
 * from the janus server
 *
 */
export class VoiceChat extends EventEmitter {
  // The communication medium on what to send/receiver messages from
  channel;
  // Session ID of the current user
  sessionid;
  // The voice chat room that the user is connected to
  room;
  // Defines if webrtc is up or not
  webrtcUp = false;
  // Uniquely identifies a user before a session is createdd
  opaqueId = `session-${Janus.randomString(12)}`;
  // Janus instance which can be useful for destroying the session
  janus;

  /**
   * Establishes communication with janus server.
   * @param {room} room The room that the user is currently in
   */
  constructor(room) {
    super();
    this.room = room;

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
                this.channel = channel;
              },
              error: this.handleError,
              onmessage: this.messageReceived,
              onlocalstream: stream => {
                this.emit("youConnected", stream);
              },
              onremotestream: stream => {
                console.log("Remote stream found");
                this.emit("remoteUserConnected", stream)
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
  joinRoom = () => {
    console.log("I requested to join a room");
    const message = {
      request: "join",
      room: this.room,
      display: UserStore.data.name,
    };
    this.channel.send({ message });
  };

  /**
   * Handles when a message has been received. This can either be if you have joined
   * the room or if another user has
   * @param {Object} msg The incoming message
   * @param {} jsep Incoming session establishment message
   */
  messageReceived = (message, sessionMessage) => {
    console.log("I recieved a message");
    console.log(message);
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

    console.log("session message is: " + sessionMessage);
    if (sessionMessage) {
      console.log("I am handling a remote jsep");
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
  handleError = (error) => {
    this.emit("error", error);
  }

  /**
   * Destroy session
   */
  destroySession = () => {
    this.janus.destroy();
    alert("Did I destroy");
  }

   


}
