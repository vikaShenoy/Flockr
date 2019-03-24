import superagent from "superagent";
import { endpoint } from "../../../utils/endpoint";


export const rules = {
  required: field => !!field || "Field is required",
  noNumbers: field => !/\d/.test(field) || "No Numbers Allowed"
};

export function updateBasicInfo(userId, basicInfo) {
  return superagent.patch(endpoint(`/travellers/${userId}`))
  .set("Authorization", localStorage.getItem("authToken"))
  .send(basicInfo);
}

