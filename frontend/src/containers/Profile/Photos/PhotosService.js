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
  let path;
  if (maxNumberOfPhotos) {
    path = endpoint(`travellers/${userId}/photos?maxNumberOfPhotos=${maxNumberOfPhotos}`);
  } else {
    path = endpoint(`travellers/${userId}/photos`);
  }

  let response = await superAgent.get(path)
      .set("authorization", localStorage.getItem("authToken"));

  return convertPhotoDetails(response.body);
}

/**
 * Converts the isPrimary field of the photo details to a boolean value.
 *
 * @param photos the array of photo details
 * @returns Array<JSON> the array of photo details with proper booleans for isPrimary.
 */
function convertPhotoDetails(photos) {
  for (let i = 0; i < photos.length; i++) {
    photos[i].isPrimary = photos[i].isPrimary === "true";
  }
  return photos;
}