/**
 * Contains reusability utility functions for photo functionality
 */

 import { endpoint } from "./endpoint";

/**
 * Gets a photo URL based on a photoId
 * @param {numberj} photoId the photoId of the photo
 * @returns {string} the URL of the photo
 */
 export function getPhotoUrl(photoId) {
    return endpoint(`/users/photos/${photoId}`); 
 }

 /**
  * Gets a photo thumbnail URL based on a photo ID
  * @param {number} photoId the photoId of the photo
  * @returns {string} the URL of the photo
  */
 export function getThumbnailUrl(photoId) {
   return endpoint(`/users/photos/${photoId}/thumbnail`);
 }