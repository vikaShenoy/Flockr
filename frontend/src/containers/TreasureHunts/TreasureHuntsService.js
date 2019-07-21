import superagent from "superagent";
import {endpoint} from "../../utils/endpoint.js"

/**
 * Get a list of all public destinations
 * @returns the list of public destinations
 */
export async function getPublicDestinations() {
    const res = await superagent.get(endpoint("/destinations"))
        .set("Authorization", localStorage.getItem("authToken"));

    return res.body;
}

export async function getAllTreasureHunts() {
    let token = localStorage.getItem("authToken");

    const res = await superagent.get(endpoint("/treasurehunts"))
        .set("Authorization", token)

    return res.body;

}

export async function createTreasureHunt(treasureHunt) {
    let token = localStorage.getItem("authToken");
    let userId = localStorage.getItem("userId");

    const res = await superagent.post(endpoint(`/users/${userId}/treasurehunts`))
        .set("Authorization", token)
        .send(treasureHunt);

    return res.body;
}