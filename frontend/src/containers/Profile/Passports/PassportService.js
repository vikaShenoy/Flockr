import superagent from "superagent";
import { endpoint } from "../../../utils/endpoint";

/**
 * Updates a users passports
 * @param {number} travellerId 
 * @param {number[]} passportIds 
 */
export function updatePassports(userId, passportIds) {
  const authToken = localStorage.getItem("authToken");

  return superagent.patch(endpoint(`/users/${userId}`))
                              .set("Authorization", authToken)
                              .send({passports: passportIds});
}

/**
 * Get all the available passports
 * @return {Promise<Array<Object>>} an array of passport objects, as per the API spec
 */
export async function getPassports() {
  const res = await superagent.get(endpoint("/users/passports"));
  return res.body;
}