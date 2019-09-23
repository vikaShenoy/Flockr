import Sortable from "sortablejs";
import superagent from "superagent";
import moment from "moment";
import config from "../../../../config"


/**
 * Sorts the timeline of a trip.
 *
 * @param timeline {Object} the timeline.
 * @param updateOrder {Object} the update order.
 */
export function sortTimeline(timeline, updateOrder) {
  new Sortable(timeline, {
    group: 'nested',
    animation: 500,
    fallbackOnBody: true,
    swapThreshold: 0.65,
    onEnd: (event) => {
      const oldParentTripNodeId = Number(event.from.getAttribute("data-trip-node-id"));
      const newParentTripNodeId = Number(event.to.getAttribute("data-trip-node-id"));
      const newIndex = event.newIndex;
      const oldIndex = event.oldIndex;

      const indexes = {
        oldParentTripNodeId,
        newParentTripNodeId,
        newIndex,
        oldIndex
      };

      updateOrder(indexes);
    }
  });

  timeline.setAttribute("has-been-sorted", true);
}

/**
 * Gets the timezone offset from UTC given a latitude and longitude.
 *
 * @param lat {Number} the latitude.
 * @param long {Number} the longitude.
 * @return {Promise<Object>} the timezone details.
 */
export async function getTimezoneOffset(lat, long) {
  const timestamp = moment().utc();
  const endpoint = `https://maps.googleapis.com/maps/api/timezone/json?location=${lat},${long}&timestamp=${timestamp}&key=${config.GOOGLE_MAPS_KEY}`;
  const res = await superagent.get(endpoint);

  return res.body;
}