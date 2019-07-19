import superagent from "superagent";
import { endpoint } from "../../utils/endpoint";

/**
 * Takes a list of destination photos and adds strings for the two endpoints to retrieve the photo and it's thumbnail.
 *
 * @param {Array<object>} photos the list of photos.
 * @return {Array<object>} the list of photos with the endpoints.
 */
const addPhotoUrls = function(photos) {
  const authToken = localStorage.getItem("authToken");

  return photos.map(photo => ({
    ...photo, 
    endpoint: endpoint(`/users/photos/${photo.personalPhoto.photoId}?Authorization=${authToken}`),
    thumbEndpoint: endpoint(`/users/photos/${photo.personalPhoto.photoId}/thumbnail?Authorization=${authToken}`)
  }));
};

/**
 * Gets a destination 
 * @param {number} destinationId The destination to get
 * @returns {object} the destination
 */
export async function getDestination(destinationId) {
  const res = await superagent.get(endpoint(`/destinations/${destinationId}`))
    .set("Authorization", localStorage.getItem("authToken")); return res.body; 
}

/**
 * Sends a get request to the server to retrieve the photos for a destination.
 * Then calls the addEndpoints function to add the photo endpoints to each photo object.
 *
 * @param destinationId {Number} the id of the destination.
 * @return {Promise<Array<object>>} the list of photos.
 */
export async function getDestinationPhotos(destinationId) {
  const res = await superagent(endpoint(`/destinations/${destinationId}/photos`))
      .set("Authorization", localStorage.getItem("authToken"));

  return addPhotoUrls(res.body);
}

/**
 * Delete a destination from the database
 * @param destinationId int id of the destination
 * @returns {Promise<void>} contains nothing
 */
export async function deleteDestination(destinationId) {
  await superagent.delete(endpoint(`/destinations/${destinationId}`))
    .set("Authorization", localStorage.getItem("authToken"));
}

/**
 * Sends a delete request to the server to remove the association of a photo from a destination.
 *
 * @param destinationId {Number} the id of the destination.
 * @param photoId {Number} the id of the photo.
 * @return {Promise<*>} the response body.
 */
export async function removePhotoFromDestination(destinationId, photoId) {
  const response = await superagent.delete(endpoint(`/destinations/${destinationId}/photos/${photoId}`))
      .set("Authorization", localStorage.getItem("authToken"));

  return response.body;
}


