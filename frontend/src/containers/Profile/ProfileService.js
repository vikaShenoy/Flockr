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

  user.body.personalPhotos = photos.body;
  return user.body;
}