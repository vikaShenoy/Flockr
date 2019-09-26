import superagent from "superagent";
import { endpoint } from "../../../utils/endpoint";
import moment from "moment";


export const rules = {
  required: field => !!field || "Field is required",
  noNumbers: field => !/\d/.test(field) || "No Numbers Allowed",
  onlyLetters: field => /^[A-Za-z ]+$/.test(field) || field === "" || "Must only contain alphabetical letters",
  AtLeast13YearsOld: field => moment(field, 'YYYY-MM-DD') < moment().subtract(13, "years") || "Must be at least 13 years old and in DD/MM/YYYY format",

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

