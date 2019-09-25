import superagent from "superagent";
import { endpoint } from "../../../utils/endpoint";


export const rules = {
  required: field => !!field || "Field is required",
  noNumbers: field => !/\d/.test(field) || "No Numbers Allowed"
};

/**
 * Updates the user's information
 * @param userId the user id of the user that the information is being updated
 * @param basicInfo the basic information to be updated
 * @returns {*}
 */
export function updateBasicInfo(userId, basicInfo) {
  return superagent.patch(endpoint(`/users/${userId}`))
  .set("Authorization", localStorage.getItem("authToken"))
  .send(basicInfo);
}

