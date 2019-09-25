import superagent from "superagent";
import {endpoint} from '../../utils/endpoint';

/**
 * Call the backend endpoint to retrieve a list of destination countries.
 * @returns {Promise<*>} containing list of destination countries.
 */
export async function getCountries() {
    const res = await superagent.get(endpoint('/destinations/countries'))
        .set("Authorization", localStorage.getItem("authToken"));
    return res.body;
}