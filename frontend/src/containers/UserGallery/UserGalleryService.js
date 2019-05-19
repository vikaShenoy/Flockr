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
  return addPhotoEndpoints(response.body);
}

/**
 * Sets the endpoint and thumbEndpoint for each photo
 *
 * @param photosDetails an array containing details of each photo available from a user.
 * @return the photosDetails array with added properties for endpoints on each one.
 */
let addPhotoEndpoints = function(photosDetails) {
  for (let i = 0; i < photosDetails.length; i++) {
    photosDetails[i].endpoint = endpoint(`/user/photos/${photosDetails[i]["photoId"]}`)
    photosDetails[i].thumbEndpoint = endpoint(`/user/photos/${photosDetails[i]["photoId"]}/thumbnail`)
  }
  return photosDetails;
};