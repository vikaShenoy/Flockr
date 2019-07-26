import superagent from "superagent";
import {endpoint} from '../../utils/endpoint';

// TODO: get from the backend API endpoint
export async function getCountries() {
    const res = await superagent.get(endpoint('/destinations/countries'))
        .set("Authorization", localStorage.getItem("authToken"));
    return res.body;
}