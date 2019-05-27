import superagent from "superagent";
import { endpoint } from "../../../utils/endpoint";


export async function getDestinationPhotos(destinationId) {
  // const res = await superagent(endpoint(`destinations/${destinationId}/photos`)); 

  // return addEndpoints(res.body);

  // Returning mock data while endpoint is being developed
  return [
    {
      photoId: 246,
      isPrimary: true,
      isPublic: false,
      filenameHash: "something.jpg",
      thumbnailName: "something.png",
      endpoint: `http://localhost:9000/api/users/photos/246?Authorization=${localStorage.getItem("authToken")}`,
      thumbEndpoint: `http://localhost:9000/api/users/photos/246/thumbnail?Authorization=${localStorage.getItem("authToken")}`,
      ownerId: 108
    },
    {
      photoId: 248,
      isPrimary: true,
      isPublic: true,
      filenameHash: "something.jpg",
      thumbnailName: "something.png",
      endpoint: `http://localhost:9000/api/users/photos/248?Authorization=${localStorage.getItem("authToken")}`,
      thumbEndpoint: `http://localhost:9000/api/users/photos/248/thumbnail?Authorization=${localStorage.getItem("authToken")}`,
      ownerId: 108
    },
  ];
}

/**
 * Takes a list of destination photos and adds strings for the two endpoints to retrieve the photo and it's thumbnail.
 *
 * @param photos {Array} the list of photos.
 * @return {Array} the list of photos with the endpoints.
 */
const addEndpoints = function(photos) {
  photos.map((photo) => {
    photo.endpoint = endpoint(`/users/photos/${photo.photoId}?Authorization=${localStorage.getItem("authToken")}`);
    photo.thumbEndpoint = endpoint(`/users/photos/${photo.photoId}/thumbnail?Authorization=${localStorage.getItem("authToken")}`);
  });
  return photos;
};