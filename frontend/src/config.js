const env = process.env.NODE_ENV;
const REMOTE_URL = "https://csse-s302g5.canterbury.ac.nz";
const GOOGLE_MAPS_KEY = process.env.VUE_APP_GOOGLE_MAP_API;


let backendUrl = "";
let websocketUrl = "";

switch (env) {
  case "production":
    backendUrl = `${REMOTE_URL}:443`;
    websocketUrl = `wss://csse-s302g5.canterbury.ac.nz:9000/ws?Authorization=${localStorage.getItem("authToken")}`;
    break;
  case "staging":
    backendUrl = `${REMOTE_URL}:8443`;
    websocketUrl = `wss://csse-s302g5.canterbury.ac.nz:9000/ws?Authorization=${localStorage.getItem("authToken")}`;
    break;
  default:
    backendUrl = "http://localhost:9000";
    websocketUrl = `ws://localhost:9000/ws?Authorization=${localStorage.getItem("authToken")}`;
    break;
}

export default {
  backendUrl,
  GOOGLE_MAPS_KEY,
  websocketUrl
};
