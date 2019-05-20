import superAgent from 'superagent';
import {endpoint} from '../../../utils/endpoint';

/**
 * Gets a JSON array containing a users photo details from the server.
 *
 * @param userId the id of the user
 * @param maxNumberOfPhotos the maximum number of photos to retrieve
 * @returns {Promise<Array<JSON>>}
 * @throws 404 - Not Found when the user does not exist
 * @throws 401 - Unauthorized when the current user is not logged in
 */
export async function getPhotos(userId, maxNumberOfPhotos) {
  let path = endpoint(`/users/${userId}/photos`);
  let response = await superAgent.get(path)
      .set("authorization", localStorage.getItem("authToken"));
  return convertPhotoDetails(response.body, maxNumberOfPhotos);
}

/**
 * Converts the isPrimary field of the photo details to a boolean value.
 *
 * @param photos the array of photo details
 * @param maxNumberOfPhotos the number of photos to restrict the returned array to.
 * @returns {Array<JSON>} the array of photo details with proper booleans for isPrimary.
 */
function convertPhotoDetails(photos, maxNumberOfPhotos) {
  let photosToShow = [];
  for (let i = 0; i < photos.length && maxNumberOfPhotos > photosToShow.length; i++) {
    photos[i].isPrimary = photos[i].isPrimary === "true";
    photos[i].isPublic = photos[i].isPublic === "true";
    // if (photos[i].isPublic) {
      photosToShow.push(photos[i]);
    // }
  }
  return addPhotoEndpoints(photosToShow);
}

/**
 * Sets the endpoint and thumbEndpoint for each photo
 *
 * @param photosDetails an array containing details of each photo available from a user.
 * @return {Array<JSON>} the photosDetails array with added properties for endpoints on each one.
 */
let addPhotoEndpoints = function(photosDetails) {
  for (let i = 0; i < photosDetails.length; i++) {
    photosDetails[i].thumbEndpoint = endpoint(`/users/photos/${photosDetails[i]["photoId"]}/thumbnail?Authorization=${localStorage.getItem("authToken")}`);
  }
  return photosDetails;
};