const env = process.env.NODE_ENV;
const REMOTE_URL = "http://csse-s302g5.canterbury.ac.nz";
// My own key for now until we get one from tutors
const GOOGLE_MAPS_KEY = "AIzaSyDPP4vfOEfKA0ks04NxKvy746UfOAawDRE";

let backendUrl = "";

switch (env) {
  case "production":
    backendUrl = `${REMOTE_URL}:443`;
    break;
  case "staging":
    backendUrl = `${REMOTE_URL}:8443`;
    break;
  default:
    backendUrl = "http://localhost:9000";
    break;
}

export default {
  backendUrl,
  GOOGLE_MAPS_KEY
};
