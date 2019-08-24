import Janus from "./janus";
import config from "../../config";

const server = config.janusUrl;
let mixertest = null;
let opaqueId = "audiobridgetest-" + Janus.randomString(12);

let myroom = 1234; // Demo room
let myid = null;
let webrtcUp = false;

export function initJanus(remoteStreamAdded) {
  Janus.init({
    debug: "all",
    callback: () => {
      console.log("Did I make it here");
      // Make sure the browser supports WebRTC
      if (!Janus.isWebrtcSupported()) {
        Janus.debug("No web rtc support");
        return;
      }

      // Create session
      const janus = new Janus({
        server: server,
        success: function() {
          // Attach to Audio Bridge test plugin
          janus.attach({
            plugin: "janus.plugin.audiobridge",
            opaqueId: opaqueId,
            success: function(pluginHandle) {
              console.log("Did I make it here");
              mixertest = pluginHandle;
              Janus.log("Plugin attached! (" + mixertest.getPlugin() + ", id=" + mixertest.getId() + ")");
              // Prepare the username registration
            },
            error: function(error) {
              console.log("Hello?");
              Janus.error("  -- Error attaching plugin...", error);
            },
            consentDialog: function(on) {
              Janus.debug("Consent dialog should be " + (on ? "on" : "off") + " now");
            },
            onmessage: function(msg, jsep) {
              Janus.debug(" ::: Got a message :::");
              Janus.debug(msg);
              var event = msg["audiobridge"];
              Janus.debug("Event: " + event);
              if (event != undefined && event != null) {
                if (event === "joined") {
                  // Successfully joined, negotiate WebRTC now
                  myid = msg["id"];
                  Janus.log("Successfully joined room " + msg["room"] + " with ID " + myid);
                  if (!webrtcUp) {
                    webrtcUp = true;
                    // Publish our stream
                    mixertest.createOffer({
                      media: { video: false }, // This is an audio only room
                      success: function(jsep) {
                        Janus.debug("Got SDP!");
                        Janus.debug(jsep);
                        var publish = { request: "configure", muted: false };
                        mixertest.send({ message: publish, jsep: jsep });
                      },
                      error: function(error) {
                        Janus.error("WebRTC error:", error);
                      },
                    });
                  }
                  // Any room participant?
                  if (msg["participants"] !== undefined && msg["participants"] !== null) {
                    var list = msg["participants"];
                    Janus.debug("Got a list of participants are :");
                    console.log("The list is : "); 
                    console.log(list);
                    Janus.debug(list);
                    for (var f in list) {
                      var id = list[f]["id"];
                      var display = list[f]["display"];
                      var setup = list[f]["setup"];
                      var muted = list[f]["muted"];
                      Janus.debug("  >> [" + id + "] " + display + " (setup=" + setup + ", muted=" + muted + ")");
                      Janus.debug("Participants should be added");

                      if (muted === true || muted === "true") Janus.debug("muted is true");
                      else Janus.debug("unmuted");

                      if (setup === true || setup === "true") Janus.debug("setup is true");
                      else Janus.debug("setup is false");
                    }
                  }
                } else if (event === "roomchanged") {
                  Janus.debug("The room has changed");
                } else if (event === "destroyed") {
                  // The room has been destroyed
                  Janus.warn("The room has been destroyed!");
                } else if (event === "event") {
                  if (msg["participants"] !== undefined && msg["participants"] !== null) {
                    const participants = msg["participants"];
                    Janus.debug("Got a list of participants:");
                    Janus.debug(participants);
                  }
                }
              }

              if (jsep !== undefined && jsep !== null) {
                Janus.debug("Handling SDP as well...");
                Janus.debug(jsep);
                mixertest.handleRemoteJsep({ jsep: jsep });
              }
            },
            onlocalstream: function(stream) {
              Janus.debug(" ::: Got a local stream :::");
              Janus.debug(stream);
            },
            onremotestream: function(stream) {
              console.log("I got a remote stream");
              const audioElem = document.createElement("audio");
              audioElem.class = "rounded centered";
              audioElem.autoplay = true;
              audioElem.id = "roomaudio";
              document.querySelector('#mixedaudio').appendChild(audioElem);
              Janus.attachMediaStream(document.querySelectorAll('#roomaudio')[0], stream);
              // remoteStreamAdded(stream, ); 
            },
            oncleanup: function() {
              webrtcUp = false;
              Janus.log(" ::: Got a cleanup notification :::");
            },
          });
        },
        error: function(error) {
          Janus.error(error);
        },
        destroyed: function() {
          window.location.reload();
        },
      });
    },
  });
}

export function addToRoom() {
  const register = {
    request: "join",
    room: myroom,
    display: "raf",
  };
  mixertest.send({ message: register });
}
