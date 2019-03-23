import superagent from "superagent";
import {endpoint} from "../../../utils/endpoint";


export async function getNationalities() {
  const res = await superagent.get(endpoint("/travellers/nationalities"));
  return res.body;
}

export function updateNationalities(userId, nationalityIds) {
  const authToken = localStorage.getItem("authToken");
  return superagent.patch(endpoint(`/travellers/${userId}`))
      .set("Authorization", authToken)
      .send({nationalities: nationalityIds});
}