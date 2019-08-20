import superagent from "superagent";
import { endpoint } from "../../utils/endpoint";
import moment from "moment";

/**
 * Sends a request to get a trip
 * @param {number} userId
 * @param {number} tripId
 */
export async function getTrip(tripId) {
  const userId = localStorage.getItem("userId");
  const res = await superagent.get(endpoint(`/users/${userId}/trips/${tripId}`)).set("Authorization", localStorage.getItem("authToken"));
  return res.body;
}

function formatTime(time) {
  return moment.utc(time.as("milliseconds")).format("HH:mm");
}

export async function getTrips() {
  const userId = localStorage.getItem("userId");
  const res = await superagent.get(endpoint(`/users/${userId}/trips`)).set("Authorization", localStorage.getItem("authToken"));
  return res.body;
}

/**
 * Transform/format a trip response object.
 * @param {Object} trip The trip to transform
 * @return {Object} The transformed trip
 */

export function transformTripNode(tripNode) {
  const transformedTripNode = {
    tripNodeId: tripNode.tripNodeId,
    name: tripNode.name,
    nodeType: tripNode.nodeType,
    arrivalDate: !tripNode.arrivalDate ? null : moment(tripNode.arrivalDate).format("YYYY-MM-DD"),
    arrivalTime: !tripNode.arrivalTime ? null : formatTime(moment.duration(tripNode.arrivalTime, "minutes")),
    departureDate: !tripNode.departureDate ? null : moment(tripNode.departureDate).format("YYYY-MM-DD"),
    departureTime: !tripNode.departureTime ? null : formatTime(moment.duration(tripNode.departureTime, "minutes")),
  };

  if (tripNode.nodeType === "TripComposite") {
    transformedTripNode.tripNodes = tripNode.tripNodes.map(currentTripNode => transformTripNode(currentTripNode));
    transformedTripNode.users = tripNode.users; 
    transformedTripNode.isShowing = false;
    transformedTripNode.isSubTrip = true;
  } else {
    transformedTripNode.destination = tripNode.destination;
    // For consistency reasons, set tripNodes to empty list
    transformedTripNode.tripNodes = [];
  }

  return transformedTripNode;
}

/**
 * Checks if destinations are contiguious
 * @param {Array} tripDestinations The list of destinations to swap
 * @returns {boolean} True if the destinations are contigious, false otherwise
 */
export function contiguousDestinations(tripDestinations) {
  let oldDestinationId = tripDestinations[0].destination.destinationId;
  for (const tripDestination of tripDestinations.slice(1)) {
    if (tripDestination.destination.destinationId === oldDestinationId) {
      return true;
    }
    oldDestinationId = tripDestination.destination.destinationId;
  }

  return false;
}

/**
 * Determine whether a trip node is a destination leaf
 * @param {Object} tripNode the trip node
 * @returns {Boolean} whether a trip node is a destination leaf
 */
const isNodeDestinationLeaf = tripNode => tripNode.nodeType == "TripDestinationLeaf";

/**
 * Determine whether the nodes have the same destinations
 * @param {Object} nodeA the first node
 * @param {Object} nodeB the second node
 * @returns {Boolean} whether the nodes have the same destinations
 */
const nodesHaveSameDestinations = (nodeA, nodeB) => nodeA.destination.destinationId === nodeB.destination.destinationId;

/**
 * Checks if destinations are contiguous after they have been swapped
 * @param {Object} reorderedCopiedNodes object containing the nodes that changed, and whether
 * the node that moved stayed in the same parent node
 * @returns {Boolean} true if there are contiguous trip destinations, false otherwise
 */
export function contiguousReorderedDestinations(reorderedCopiedNodes) {
  const { reorderedSourceTripNode, reorderedTargetTripNode, stayedInSourceTripNode } = reorderedCopiedNodes;
  let contiguousDestinationFound = tripNodeHasContiguousDestinations(reorderedSourceTripNode);
  if (!stayedInSourceTripNode) {
    contiguousDestinationFound = contiguousDestinationFound || tripNodeHasContiguousDestinations(reorderedTargetTripNode);
  }
  return contiguousDestinationFound;
}

function tripNodesLessThan2Nodes(tripNode) {
  return tripNode.tripNodes && tripNode.tripNodes.length < 2;
}

export function tripNodesLessThan2(reorderedCopiedNodes) {
  const { reorderedSourceTripNode, reorderedTargetTripNode, stayedInSourceTripNode } = reorderedCopiedNodes;
  if (stayedInSourceTripNode) {
    return tripNodesLessThan2Nodes(reorderedSourceTripNode);
  }


  return tripNodesLessThan2Nodes(reorderedSourceTripNode) || tripNodesLessThan2Nodes(reorderedTargetTripNode);
}

/**
 * Determine whether the trip node has contiguous destinations
 * @param {Object} tripNode the trip node
 * @returns {Boolean} true if the trip node has contiguous destinations, false otherwise
 */
export function tripNodeHasContiguousDestinations(tripNode) {
  let contiguousDestinationFound = false;
  tripNode.tripNodes.forEach((node, index) => {
    if (isNodeDestinationLeaf(node) && index > 0) {
      // only compare destinations if two leaf nodes are next to each other
      const previousNode = tripNode.tripNodes[index - 1];
      if (isNodeDestinationLeaf(previousNode)) {
        if (nodesHaveSameDestinations(node, previousNode)) {
          contiguousDestinationFound = true;
        }
      }
    }
  });
  return contiguousDestinationFound;
}

/**
 * Edit a trip. Send a request to the edit trip backend endpoint with
 * the trip data to edit.
 * @param {number} tripId - The ID of the trip to edit
 * @param {string} tripName - The edited trip name
 * @param {Object[]} tripNodes - The edited trip destinations
 */
export async function editTrip(trip) {
  const userId = localStorage.getItem("userId");
  const authToken = localStorage.getItem("authToken");

  const transformedTripNodes = trip.tripNodes.map(tripNode => {
    if (tripNode.nodeType === "TripComposite") {
      return {
        nodeType: tripNode.nodeType,
        tripNodeId: tripNode.tripNodeId,
      };
    } else {
      return {
        destinationId: tripNode.destination.destinationId,
        arrivalDate: tripNode.arrivalDate ? moment(tripNode.arrivalDate).valueOf() : null,
        arrivalTime: tripNode.arrivalTime ? moment.duration(tripNode.arrivalTime).asMinutes() : null,
        departureDate: tripNode.departureDate ? moment(tripNode.departureDate).valueOf() : null,
        departureTime: tripNode.departureTime ? (tripNode.departureTime === null || tripNode.departureTime === "" ? null : moment.duration(tripNode.departureTime).asMinutes()) : null,
        nodeType: tripNode.nodeType,
      };
    }
  });
  const tripData = {
    name: trip.name,
    tripNodes: transformedTripNodes,
  };

  if (trip.users) {
    tripData.userIds = trip.users.map(user => user.userId);
  }
  await superagent
    .put(endpoint(`/users/${userId}/trips/${trip.tripNodeId}`))
    .set("Authorization", authToken)
    .send(tripData);
}

/**
 * Maps trip nodes (tree structure) to destinations (flat structure) using recursion
 * @param depth the recursion level. Used to indicate destinations which are
 * part of the same sub trip for coloring on the map.
 * @param {} tripNode The current trip node at a specific recursion level
 */
export function mapTripNodesToDestinations(tripNode, depth = 0) {
  if (tripNode.nodeType === "TripDestinationLeaf") {
    const destination = tripNode.destination;
    destination.group = depth;
    return destination;
    //return tripNode.destination;
  }

  let destinations = [];
  for (const currentTripNode of tripNode.tripNodes) {
    destinations = [...destinations, mapTripNodesToDestinations(currentTripNode, depth + 1)];
  }
  return destinations.flatMap(destination => destination);
}

/**
 * Recursively finds a trip node by it's trip node ID
 * @param {number} tripNodeId The ID to find
 * @param {Object} tripNode The current trip node that is being searched
 * @return {Object} The tripNode object that was found
 */
export function getTripNodeById(tripNodeId, tripNode) {
  // base case
  if (tripNode.tripNodeId === tripNodeId) {
    return tripNode;
  }

  let tripNodeToFind = null;

  for (const currentTripNode of tripNode.tripNodes) {
    // recursive case
    tripNodeToFind = getTripNodeById(tripNodeId, currentTripNode);
    if (tripNodeToFind) return tripNodeToFind;
  }

  return tripNodeToFind;
}

/**
 * Recursively finds a trip node parent by it's trip node ID
 * @param {number} tripNodeId The ID to find the parent of
 * @param {Object} currentTripNode The current trip node that is being searched
 * @param {object} parentTripNode The parent of the current trip node being searched. Initially null.
 * @return {Object} The parent trip node.
 */
export function getTripNodeParentById(tripNodeId, currentTripNode, parentTripNode) {
  // base case
  if (currentTripNode.tripNodeId === tripNodeId) {
    return parentTripNode;
  }

  let tripNodeToFind = null;

  for (const node of currentTripNode.tripNodes) {
    // recursive
    tripNodeToFind = getTripNodeParentById(tripNodeId, node, currentTripNode);
    if (tripNodeToFind) {
      break;
    }
  }
  return tripNodeToFind;
}