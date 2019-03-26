import superagent from "superagent";
import { endpoint } from "../../utils/endpoint";

/**
 * A function that gets the list of users with their given information.
 */
export async function requestTravellers(queries) {
    const res = await superagent.get(endpoint("/travellers"))
    .query(queries)
    .set("Authorization", localStorage.getItem("auth"));

    return res.body;
}