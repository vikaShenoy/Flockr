import superAgent from 'superagent';
import {endpoint} from "../../../../utils/endpoint";


export async function sendProfilePicture(userId, imageData) {
  let formData = new FormData();
  formData.append("public", true);
  formData.append("primary", true);
  formData.append("image", imageData);

  const res = await superAgent.post(endpoint(`/users/${userId}/photos`))
      .set("Authorization", localStorage.getItem("authToken"))
      .send(formData);

  return res.body;
}