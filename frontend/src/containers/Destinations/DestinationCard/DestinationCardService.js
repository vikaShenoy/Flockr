import superagent from "superagent";
import { endpoint } from "../../../utils/endpoint";


export async function getDestinationPhotos(destinationId) {
  // const res = await superagent(endpoint(`destinations/${destinationId}/photos`)); 

  // return res.body;

  // Returning mock data while endpoint is being developed
  return [
    {
      photoId: 1,
      filenameHash: "something.jpg",
      thumbnailName: "something.png"
    },
    {
      photoId: 2,
      filenameHash: "something.jpg",
      thumbnailName: "something.png"
    },
  ];
}