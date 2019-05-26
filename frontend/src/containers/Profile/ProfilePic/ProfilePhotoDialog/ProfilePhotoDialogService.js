import superAgent from 'superagent';
import {endpoint} from "../../../../utils/endpoint";

/**
 * Uploads a user's profile picture
 * @param {number} userId The user ID to upload the photo to
 * @param {Blob} imageData The image to upload
 */
export async function sendProfilePicture(userId, imageData) {
  let formData = new FormData();
  formData.append("isPublic", true);
  formData.append("isPrimary", true);
  formData.append("image", imageData);

  const res = await superAgent.post(endpoint(`/users/${userId}/photos`))
      .set("Authorization", localStorage.getItem("authToken"))
      .send(formData);

  return res.body;
}