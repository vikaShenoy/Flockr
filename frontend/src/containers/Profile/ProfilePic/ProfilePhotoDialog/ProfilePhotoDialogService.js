import superAgent from 'superagent';
import {endpoint} from "../../../../utils/endpoint";


export async function sendProfilePicture(userId, imageData) {
  let formData = new FormData();
  formData.append("isPublic", true);
  formData.append("isPrimary", true);
  formData.append("image", imageData);

  return await superAgent.post(endpoint(`/users/${userId}/photos`))
      .set("Authorization", localStorage.getItem("authToken"))
      .send(formData);
}