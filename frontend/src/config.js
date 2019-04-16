const env = process.env.NODE_ENV;

let backendUrl = "";

switch (env) {
  case "production":
    backendUrl = "http://localhost:443";
    break;
  case "staging":
    backendUrl = "http://localhost:8443";
    break;
  default:
    backendUrl = "http://localhost:9000";
    break;
}

export default {
  backendUrl
};