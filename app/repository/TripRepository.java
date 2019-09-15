package repository;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import io.ebean.Ebean;
import io.ebean.SqlUpdate;
import io.ebean.Transaction;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import models.TripComposite;
import models.TripNode;
import models.User;

/**
 * Contains all trip related db interactions
 */
public class TripRepository {

  private final DatabaseExecutionContext executionContext;

  @Inject
  public TripRepository(DatabaseExecutionContext executionContext) {
    this.executionContext = executionContext;
  }

  /**
   * Saves a trip
   *
   * @param trip The trip to save
   * @return The saved trip
   */
  public CompletionStage<TripComposite> saveTrip(TripComposite trip) {
    return persistTripNode(trip);
  }

  /**
   * Deletes the leaf nodes in a list of trip nodes from the database permanently.
   *
   * @param tripNodes the list of trip nodes.
   * @return Void
   */
  public CompletionStage<Void> deleteListOfTrips(List<TripNode> tripNodes) {
    return supplyAsync(
        () -> {
          for (TripNode tripNode : tripNodes) {
            if (tripNode.getNodeType().equals("TripDestinationLeaf")) {
              tripNode.setDeletedExpiry(Timestamp.from(Instant.now()));
              tripNode.delete();
            }
          }
          return null;
        });
  }

  /**
   * Saves a trip in the database including the order of it's trip nodes.
   *
   * @param trip the trip to persist.
   * @return the trip once it is persisted.
   */
  private CompletionStage<TripComposite> persistTripNode(TripComposite trip) {
    return supplyAsync(
        () -> {
          recursivelyAddUsersToSubTrips(trip.getTripNodes(), trip.getUsers());
          trip.save();

          // Persist trip node order.
          List<TripNode> tripNodes = trip.getTripNodes();

          try (Transaction txn = Ebean.beginTransaction()) {
            String sqlUpdateQuery =
                "UPDATE trip_node_parent SET child_index = ? "
                    + "WHERE trip_node_child_id = ? "
                    + "AND trip_node_parent_id = ?";
            SqlUpdate update = Ebean.createSqlUpdate(sqlUpdateQuery);

            for (int i = 0; i < tripNodes.size(); i++) {
              update.setNextParameter(i);
              update.setNextParameter(tripNodes.get(i).getTripNodeId());
              update.setNextParameter(trip.getTripNodeId());
              update.addBatch();
            }
            update.executeBatch();

            txn.commit();
          }

          return trip;
        },
        executionContext);
  }

  /**
   * Recursively adds all users in this trip to all sub trips
   *
   * @param trips the list of trips to add users to.
   * @param users the list of users to add to the sub trips.
   */
  private void recursivelyAddUsersToSubTrips(List<TripNode> trips, List<User> users) {
    for (TripNode tripNode: trips) {
      if (tripNode.getNodeType().equals("TripComposite")) {
        for (User user : users) {
          ((TripComposite) tripNode).addUser(user);
        }
        tripNode.save();
        recursivelyAddUsersToSubTrips(tripNode.getTripNodes(), tripNode.getUsers());
      }
    }
  }

  /**
   * Updates a trip in the database.
   *
   * @param trip The trip to update changes.
   * @return The trip that was updated.
   */
  public CompletionStage<TripComposite> update(TripComposite trip) {
    return persistTripNode(trip);
  }

  /**
   * Delete a trip.
   *
   * @param trip The trip to delete.
   */
  public CompletionStage<TripComposite> deleteTrip(TripComposite trip) {
    return supplyAsync(
        () -> {
          trip.setDeletedExpiry(Timestamp.from(Instant.now().plus(Duration.ofHours(1))));
          trip.delete(); // Soft delete
          return trip;
        },
        executionContext);
  }

  /**
   * Restore a deleted trip
   *
   * @param trip the trip to be restored
   */
  public CompletionStage<TripNode> restoreTrip(TripNode trip) {
    return supplyAsync(
        () -> {
          trip.setDeleted(false);
          trip.setDeletedExpiry(null);
          String statement = "UPDATE trip_node "
              + "SET deleted = ?, deleted_expiry = ? "
              + "WHERE trip_node_id = ?";
          SqlUpdate sqlUpdate = Ebean.createSqlUpdate(statement);
          sqlUpdate.setNextParameter(false);
          sqlUpdate.setNextParameter(null);
          sqlUpdate.setNextParameter(trip.getTripNodeId());
          sqlUpdate.execute();
          return trip;
        });
  }

  /**
   * Get a trip by its tripId and userId.
   *
   * @param tripId The id of the trip to find.
   * @param userId The user id of the trip to find.
   * @return the trip that matches given ids.
   */
  public CompletionStage<Optional<TripComposite>> getTripByIds(int tripId, int userId) {
    return supplyAsync(
        () -> {
          Optional<TripComposite> trip =
              TripComposite.find
                  .query()
                  .fetch("users")
                  .where()
                  .eq("tripNodeId", tripId)
                  .in("users.userId", userId)
                  .findOneOrEmpty();
          if (trip.isPresent()) {
            List<TripNode> tripNodes = recursiveGetTripNodes(tripId);
            trip.get().setTripNodes(tripNodes);
          }
          return trip;
        },
        executionContext);
  }

  /**
   * Recursive function to get all trip nodes in order, this also gets all composite sub trips trip
   * nodes recursively.
   *
   * @param tripId the id of the trip to get trip nodes from.
   * @return the list of trip nodes in order.
   */
  private List<TripNode> recursiveGetTripNodes(int tripId) {
    List<TripNode> tripNodes =
        TripNode.find
            .query()
            .fetch("parents")
            .where()
            .eq("trip_node_parent_id", tripId)
            .ne("tripNodeId", tripId)
            .orderBy("child_index")
            .findList();
    for (TripNode tripNode : tripNodes) {
      if (tripNode.getNodeType().equals("TripComposite")) {
        tripNode.setTripNodes(recursiveGetTripNodes(tripNode.getTripNodeId()));
      }
    }
    return tripNodes;
  }

  /**
   * Recursive function to get all trip nodes in order, this also gets all composite sub trips trip
   * nodes recursively.
   *
   * @param tripId the id of the trip to get trip nodes from.
   * @return the list of trip nodes in order.
   */
  private List<TripNode> recursiveGetTripNodesWithSoftDelete(int tripId) {
    List<TripNode> tripNodes =
        TripNode.find
            .query()
            .setIncludeSoftDeletes()
            .fetch("parents")
            .where()
            .eq("trip_node_parent_id", tripId)
            .ne("tripNodeId", tripId)
            .orderBy("child_index")
            .findList();
    for (TripNode tripNode : tripNodes) {
      if (tripNode.getNodeType().equals("TripComposite")) {
        tripNode.setTripNodes(recursiveGetTripNodesWithSoftDelete(tripNode.getTripNodeId()));
      }
    }
    return tripNodes;
  }

  /**
   * Get a trip, including if it has been soft deleted, by its tripId and userId
   *
   * @param tripId The id of the trip to get
   * @param userId The user id of the owner of the trip
   * @return the trip that matches the given ids
   */
  public CompletionStage<Optional<TripNode>> getTripByIdsIncludingDeleted(int tripId, int userId) {
    return supplyAsync(
        () -> {
          Optional<TripNode> trip =
              TripNode.find
                  .query()
                  .setIncludeSoftDeletes()
                  .fetch("users")
                  .where()
                  .eq("tripNodeId", tripId)
                  .in("users.userId", userId)
                  .findOneOrEmpty();
          if (trip.isPresent()) {
            List<TripNode> tripNodes = recursiveGetTripNodesWithSoftDelete(tripId);
            trip.get().setTripNodes(tripNodes);
          }
          return trip;
        },
        executionContext);
  }

  /**
   * Get trips by the users userId
   *
   * @param travellerId The user id of the trips
   * @return The users trips
   */
  public CompletionStage<List<TripComposite>> getTripsByUserId(int travellerId) {
    return supplyAsync(
        () -> {
          List<TripComposite> trips =
              TripComposite.find
                  .query()
                  .fetchLazy("users","userId")
                  .where()
                  .in("users.userId", travellerId)
                  .findList();
          for (TripComposite tripNode : trips) {
            if (tripNode.getNodeType().equals("TripComposite")) {
              tripNode.setTripNodes(recursiveGetTripNodes(tripNode.getTripNodeId()));
            }
          }
          return trips;
        },
        executionContext);
  }

  /**
   * Get High Level Trips by the users Id
   *
   * @param userId The user id of the trips
   * @return The users trips
   */
  public CompletionStage<List<TripComposite>> getHighLevelTripsByUserId(int userId) {
    return supplyAsync(
        () -> {
          List<TripComposite> trips = TripComposite.find.query().fetch("tripNodes").findList();

          for (TripComposite tripNode : trips) {
            if (tripNode.getNodeType().equals("TripComposite")) {
              tripNode.setTripNodes(recursiveGetTripNodes(tripNode.getTripNodeId()));
            }
          }

          return trips;
        },
        executionContext);
  }

  public Set<TripComposite> getAllTrips() {
    return new HashSet<>(TripComposite.find.all());
  }
}
