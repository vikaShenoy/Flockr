import superagent from "superagent";
import { endpoint } from "../../../utils/endpoint";

/**
 * Sends a get request to the server to retrieve the photos for a destination.
 * Then calls the addEndpoints function to add the photo endpoints to each photo object.
 *
 * @param destinationId {Number} the id of the destination.
 * @return {Promise<Array<POJO>>} the list of photos.
 */
export async function getDestinationPhotos(destinationId) {
  const res = await superagent(endpoint(`/destinations/${destinationId}/photos`))
      .set("Authorization", localStorage.getItem("authToken"));

  return addEndpoints(res.body);
}

/**
 * Sends a delete request to the server to remove the association of a photo from a destination.
 *
 * @param destinationId {Number} the id of the destination.
 * @param photoId {Number} the id of the photo.
 * @return {Promise<*>} the response body.
 */
export async function removePhotoFromDestination(destinationId, photoId) {
  const response = superagent.delete(endpoint(`/destinations/${destinationId}/photos/${photoId}`))
      .set("Authorization", localStorage.getItem("authToken"));

  return response.body;
}

/**
 * Takes a list of destination photos and adds strings for the two endpoints to retrieve the photo and it's thumbnail.
 *
 * @param photos {Array<POJO>} the list of photos.
 * @return {Array<POJO>} the list of photos with the endpoints.
 */
const addEndpoints = function(photos) {
  photos.map((photo) => {
    photo["endpoint"] = endpoint(`/users/photos/${photo.personalPhoto.photoId}?Authorization=${localStorage.getItem("authToken")}`);
    photo["thumbEndpoint"] = endpoint(`/users/photos/${photo.personalPhoto.photoId}/thumbnail?Authorization=${localStorage.getItem("authToken")}`);
  });
  return photos;
};