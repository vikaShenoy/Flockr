import superagent from "superagent";
import { endpoint } from "../../../utils/endpoint";

/**
 * Delete Function for a photo.
 * Sends a request to the server to delete a users photo.
 */
export async function deleteUserPhoto(photo) {
  const response = await superagent.delete(endpoint(`/users/photos/${photo.photoId}`))
      .set("authorization", localStorage.getItem("authToken"));
  return response.body;
}

/**
 * Sets profile picture back to old profile picture
 * @param {Object} photo photo to swap back to
 */
export async function setProfilePictureToOldPicture(photo) {
  const userId = localStorage.getItem("userId");
  await superagent.put(endpoint(`/users/${userId}/profilephoto/${photo.photoId}/undo`))
    .set("Authorization", localStorage.getItem("authToken"));
}


