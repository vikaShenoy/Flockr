import superagent from "superagent";
import { endpoint } from "../../../../../utils/endpoint";

export function uploadImage(imageFile, isPublic, userId) {
  const formData = new FormData();
  formData.append("image", imageFile);
  formData.append("isPublic", isPublic);
  
  return superagent.post(endpoint(`users/${userId}/photos`))
    .set("Authorization", localStorage.getItem("authToken"))
    .field("isPublic", isPublic)
    .attach("photo", imageFile);
}
