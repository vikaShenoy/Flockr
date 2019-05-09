import superagent from 'superagent';

import {endpoint} from '../../utils/endpoint';

/**
 * Gets a user by ID
 * @param {number} userId The ID of the user to get
 * @return The user object
 */
export async function getUser(userId) {
  const authToken = localStorage.getItem('authToken');
  const res = await superagent(endpoint(`/users/${userId}`))
                  .set('Authorization', authToken);
  return res.body;
}