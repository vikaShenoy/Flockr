// const env = process.env.NODE_ENV;
// const REMOTE_URL = "http://csse-s302g5.canterbury.ac.nz";
const GOOGLE_MAPS_KEY = "AIzaSyDCyC_ON0GgSmJIfPlL7oke5PZgbR8TLbg";

// let backendUrl = "";
//
// switch (env) {
//   case "production":
//     backendUrl = `${REMOTE_URL}:443`;
//     break;
//   case "staging":
//     backendUrl = `${REMOTE_URL}:8443`;
//     break;
//   default:
//     backendUrl = "http://localhost:9000";
//     break;
// }

let backendUrl = "http://localhost:9000";

export default {
  backendUrl,
  GOOGLE_MAPS_KEY,
};
