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

/**
 * Undo an image upload by calling soft delete endpoint.
 * @param {*} imageId id of the image to soft delete. 
 */
export async function undoImageUpload(image) {
  const response = await superAgent.delete(endpoint(`/users/photos/${image.imageId}`))
      .set("authorization", localStorage.getItem("authToken"));
  return response.body;
}

/**
 * Bring back image by undoing soft delete.
 * @param {*} imageId image to un-soft delete (re-upload)
 */
export async function redoImageUpload(image) {
  const response = await superAgent.put(endpoint(`/users/photos/${image.imageId}/undodelete`))
        .set("authorization", localStorage.getItem("authToken"));
  return response.body;
}
