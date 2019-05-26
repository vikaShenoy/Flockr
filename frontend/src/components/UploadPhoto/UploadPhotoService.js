import superagent from "superagent";
import { endpoint } from "../../utils/endpoint";

/**
 * Uploads a personal photo
 * @param {File} imageFile The photo to upload
 * @param {boolean} isPublic Identifies if the photo should be public
 * @param {boolean} isPrimary Identifies if the photo should be primary
 * @param {number} userId The user ID that the photo belongs to
 * @returns {Object} the image that has been uploaded
 */
export async function uploadImage(imageFile, isPublic, userId) {
  const res = await superagent.post(endpoint(`/users/${userId}/photos`))
    .set("Authorization", localStorage.getItem("authToken"))
    .field("isPublic", isPublic)
    .field("isPrimary", false)
    .attach("image", imageFile);

  return res.body;
}
