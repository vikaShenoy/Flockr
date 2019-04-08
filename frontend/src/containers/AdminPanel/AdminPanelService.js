import superagent from "superagent";
import { endpoint } from "../../utils/endpoint";


/**
 * Make API call to delete the given users
 * @param {number[]} userIds the ids of the users we want to delete
 */
export async function deleteUsers(userIds) {
    const authToken = localStorage.getItem("authToken");
    throw new Error('API call to delete give users to be implemented');
}