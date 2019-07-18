import superagent from "superagent";

// TODO: get from the backend API endpoint
export async function getCountries() {
    const res = await superagent.get("https://restcountries.eu/rest/v2/all");
    return res.body;
}