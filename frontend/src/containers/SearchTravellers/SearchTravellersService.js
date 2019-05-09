import superagent from "superagent";
import { endpoint } from "../../utils/endpoint";

/**
 * A function that gets the list of users with their given information.
 * @return the traveller data
 */
export async function requestTravellers(queries) {
    const res = await superagent.get(endpoint("/users/search"))
    .query(queries)
    .set("Authorization", localStorage.getItem("authToken"));

    return res.body;
}