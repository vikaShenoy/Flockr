import superAgent from 'superagent';

import {endpoint} from '../../utils/endpoint';

/**
 * Gets the details of the available photos from the given user.
 *
 * @param userId the id of the user to get photos from
 * @return {Promise<*>}
 */
export async function getPhotosForUser(userId) {
  let response = await superAgent.get(endpoint(`/users/${userId}/photos`))
      .set("authorization", localStorage.getItem("authToken"));
  return addExtras(response.body);
}

/**
 * Sets the endpoint and thumbEndpoint for each photo
 *
 * @param photosDetails an array containing details of each photo available from a user.
 * @return the photosDetails array with added properties for endpoints on each one.
 */
let addExtras = function(photosDetails) {
  for (let i = 0; i < photosDetails.length; i++) {
    photosDetails[i].endpoint = endpoint(`/users/photos/${photosDetails[i]["photoId"]}?Authorization=${localStorage.getItem("authToken")}`);
    photosDetails[i].thumbEndpoint = endpoint(`/users/photos/${photosDetails[i]["photoId"]}/thumbnail?Authorization=${localStorage.getItem("authToken")}`);
    photosDetails[i].deleteFunction = async function() {
      const response = await superAgent.delete(endpoint(`/users/photos/${photosDetails[i].photoId}`))
          .set("authorization", localStorage.getItem("authToken"));
      return response.body;
    };
  }
  return photosDetails;
};

/**
 * Sends a request to the server to update the permission of the photo
 *
 * @param newValue the new value of public
 * @param photoId the id of the photo to be updated
 * @return {Promise<POJO>} the photo object after updating
 */
export async function updatePhotoPermissions(newValue, photoId) {
  const response = await superAgent.patch(endpoint(`/users/photos/${photoId}`))
      .set("authorization", localStorage.getItem("authToken"))
      .send({
        "isPublic": newValue,
        "isPrimary": false
      });

  return response.body;
}