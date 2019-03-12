import config from "../config";

/**
 * Gets the full URL from a path
 * @param {string} path - The path of the URL
 * @returns {string} The full URL of the endpoint
 */
export function endpoint(path) {
  return `${config.backendUrl}/api${path}`;
}
