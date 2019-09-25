import superagent from "superagent";
import {endpoint} from "../../../utils/endpoint";

/**
 * Gets all the possible nationalities
 * @returns {Promise<*>} all the nationalities in the db
 */
export async function getNationalities() {
  const res = await superagent.get(endpoint("/users/nationalities"));
  return res.body;
}

/**
 * Updates the nationalities of the user based on what the user has chosen
 * @param userId the id of the user
 * @param nationalityIds the nationality of the user
 * @returns {*}
 */
export function updateNationalities(userId, nationalityIds) {
  const authToken = localStorage.getItem("authToken");
  return superagent.patch(endpoint(`/users/${userId}`))
      .set("Authorization", authToken)
      .send({nationalities: nationalityIds});
}