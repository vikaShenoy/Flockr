import superagent from 'superagent';

import {endpoint} from '../../utils/endpoint';

/**
 * Gets a user by ID
 * @param {number} userId The ID of the user to get
 * @return The user object
 */
export async function getUser(userId) {
  const authToken = localStorage.getItem('authToken');

  
  const userPromise = superagent(endpoint(`/users/${userId}`))
                  .set('Authorization', authToken);

  const photosPromise = superagent(endpoint(`/users/${userId}/photos`))
                  .set('Authorization', authToken);

  const [user, photos] = await Promise.all([userPromise, photosPromise]);

  user.body.personalPhotos = addEndpoints(photos.body);
  return user.body;
}

/**
 * Adds the endpoint and thumbEndpoint field for photos to each photo in the given list of photos.
 *
 * @param photos {Array} the array of photos to be modified.
 * @return {Array} the photos array after the fields have been added.
 */
const addEndpoints = function(photos) {
  photos.map((photo) => {
    photo.endpoint = endpoint(`/users/photos/${photo["photoId"]}?Authorization=${localStorage.getItem("authToken")}`);
    photo.thumbEndpoint = endpoint(`/users/photos/${photo["photoId"]}/thumbnail?Authorization=${localStorage.getItem("authToken")}`);
  });
  return photos;
};