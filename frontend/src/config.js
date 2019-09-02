const env = process.env.NODE_ENV;
const REMOTE_URL = "http://csse-s302g5.canterbury.ac.nz";
const GOOGLE_MAPS_KEY = process.env.VUE_APP_GOOGLE_MAP_API;

let websocketUrl = "";
let backendUrl = "";
let webrtcUrl = "";

switch (env) {
  case "production":
    backendUrl = `${REMOTE_URL}:443`;
    websocketUrl = "wss://csse-s302g5.canterbury.ac.nz:443/ws";
    webrtcUrl = "https://csse-s302g5.canterbury.ac.nz:8089/janus";
    break;
  case "staging":
    backendUrl = `${REMOTE_URL}:8443`;
    websocketUrl = "wss://csse-s302g5.canterbury.ac.nz:8443/ws";
    webrtcUrl = "https://csse-s302g5.canterbury.ac.nz:8088/janus";
    break;
  default:
    backendUrl = "http://localhost:9000";
    websocketUrl = "ws://localhost:9000/ws";
    webrtcUrl = "http://rafaelgoesmann.com:8088/janus";
    break;
}

export default {
  backendUrl,
  GOOGLE_MAPS_KEY,
  websocketUrl,
  webrtcUrl,
};
