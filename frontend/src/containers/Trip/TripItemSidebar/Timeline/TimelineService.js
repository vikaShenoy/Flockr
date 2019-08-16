import Sortable from "sortablejs"

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
}