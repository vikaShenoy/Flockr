package tasks;

import static java.util.concurrent.CompletableFuture.runAsync;

import akka.actor.ActorSystem;
import com.google.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import models.Country;
import models.Destination;
import models.Role;
import models.TripComposite;
import models.TripDestinationLeaf;
import models.TripNode;
import models.User;
import models.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.ws.WSClient;
import repository.TripRepository;
import repository.UserRepository;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;

/** This class contains the code needed to populate trips. */
public class ExampleTripsDataTask {

  private ActorSystem actorSystem;
  private ExecutionContext executionContext;
  final Logger log = LoggerFactory.getLogger(this.getClass());
  private TripRepository tripRepository;
  private UserRepository userRepository;
  private Date pointOfReference = Date.from(Instant.now().plus(1, ChronoUnit.DAYS));
  private Role role;
  private Role ownerRole;
  private boolean makeBigTrips = true;
  private String[] firstNames = {
      "Andy",
      "Exequiel",
      "Sam",
      "Rafael",
      "Vikas",
      "Isaac",
      "Angelica"
  };
  private String[] lastNames = {
      "Holden",
      "Bahamonde",
      "Annand",
      "Goesmann",
      "Shenoy",
      "Foster",
      "Dela Cruz"
  };

  @Inject
  public ExampleTripsDataTask(
      ActorSystem actorSystem,
      ExecutionContext executionContext,
      WSClient ws,
      TripRepository tripRepository,
      UserRepository userRepository) {
    this.actorSystem = actorSystem;
    this.executionContext = executionContext;
    this.tripRepository = tripRepository;
    this.userRepository = userRepository;
    initialise();
  }

  /**
   * Fetches a number of destinations from a given country.
   *
   * @param country the country containing the destinations.
   * @param numDestinations the maximum number of destinations to fetch.
   * @return a list of destinations from the country.
   */
  private List<Destination> fetchNumDestinationsFromCountry(Country country, int numDestinations) {
    return Destination.find
        .query()
        .where()
        .eq("destination_country_country_id", country.getCountryId())
        .setMaxRows(numDestinations)
        .findList();
  }

  /**
   * Gets a list of all users in the database.
   *
   * @return the users in the database.
   */
  private List<User> fetchAllUsers() {
    return User.find.all();
  }

  /**
   * Checks if a user already has trips.
   *
   * @param user the user to search from.
   * @return true if the user has trips.
   */
  private boolean doesUserHaveTrips(User user) {
    return TripNode.find
        .query()
        .fetchLazy("users", "user_id")
        .where()
        .in("users.userId", user.getUserId())
        .exists();
  }

  /**
   * Makes a list of saved trip node leaves.
   *
   * @param destinations the destinations to add leafs for.
   * @return the list of trip nodes.
   */
  private List<TripNode> makeTripNodesList(Destination... destinations) {
    ArrayList<TripNode> tripNodes = new ArrayList<>();
    for (Destination destination : destinations) {
      Date arrivalDate = Date.from(pointOfReference.toInstant().plus(1, ChronoUnit.DAYS));
      Date departureDate = Date.from(arrivalDate.toInstant().plus(1, ChronoUnit.DAYS));
      pointOfReference = departureDate;

      TripDestinationLeaf tripDestinationLeaf =
          new TripDestinationLeaf(destination, arrivalDate, null, departureDate, null);
      tripDestinationLeaf.save();

      tripNodes.add(tripDestinationLeaf);
    }

    return tripNodes;
  }

  /**
   * Gets a few destinations from the given country and creates a trip with them.
   *
   * @param users the users going on the trip.
   * @param country the country the trip is going to.
   */
  private void makeTourOfCountry(List<User> users, List<UserRole> userRoles, Country country) {
    List<Destination> destinations = fetchNumDestinationsFromCountry(country, 20);
    List<TripNode> tripNodes = new ArrayList<>();

    for (Destination destination : destinations) {
      TripDestinationLeaf leaf = new TripDestinationLeaf(destination, null, null, null, null);
      leaf.save();
      tripNodes.add(leaf);
    }

    TripComposite tripComposite =
        new TripComposite(tripNodes, users, String.format("Tour of %s", country.getCountryName()));
    tripComposite.setUserRoles(userRoles);
    tripRepository.saveTrip(tripComposite);
  }

  /**
   * Define the code to be run, and when it should be run NOTE - internet enabler must be turned on.
   * Runs at start up and then every 24 hours after that.
   */
  private void initialise() {
    this.actorSystem
        .scheduler()
        .scheduleOnce(
            Duration.create(1, TimeUnit.SECONDS), // initial delay
            () ->
                runAsync(
                    () -> {
                      log.info("Loading Example Trips");
                      role = userRepository.getSingleRoleByType("TRIP_MANAGER");
                      ownerRole = userRepository.getSingleRoleByType("TRIP_OWNER");
                      int countryIndex = 0;
                      List<Country> countries = Country.find.all();
                      List<User> users = fetchAllUsers();
                                            for (User user : users) {
                                              if (!doesUserHaveTrips(user)) {
                                                List<User> thisUserList = new ArrayList<>();
                                                List<UserRole> thisUserRoles = new ArrayList<>();
                                                UserRole userRole = new UserRole(user, role);
                                                userRole.save();
                                                thisUserRoles.add(userRole);
                                                thisUserList.add(user);

                                                // Get countries
                                                Country countryOne = countries.get(countryIndex);
                                                String countryOneName =
                       countryOne.getCountryName();
                                                List<Destination> countryOneDestinations = new
                       ArrayList<>();
                                                while (countryOneDestinations.size() < 5) {
                                                  countryOneDestinations =
                       fetchNumDestinationsFromCountry(countryOne, 5);
                                                  countryOneName = countryOne.getCountryName();
                                                  countryIndex = (countryIndex + 1) %
                       countries.size();
                                                  countryOne = countries.get(countryIndex);
                                                }
                                                Country countryTwo = countries.get(countryIndex);
                                                String countryTwoName =
                       countryTwo.getCountryName();
                                                List<Destination> countryTwoDestinations = new
                       ArrayList<>();
                                                while (countryTwoDestinations.size() < 5) {
                                                  countryTwoDestinations =
                       fetchNumDestinationsFromCountry(countryTwo, 5);
                                                  countryTwoName = countryTwo.getCountryName();
                                                  countryIndex = (countryIndex + 1) %
                       countries.size();
                                                  countryTwo = countries.get(countryIndex);
                                                }

                                                List<TripNode> tripNodes1 =
                                                    makeTripNodesList(
                                                        countryOneDestinations.get(0),
                                                        countryOneDestinations.get(1),
                                                        countryTwoDestinations.get(0),
                                                        countryTwoDestinations.get(1));

                                                TripComposite tripOne =
                                                    new TripComposite(
                                                        tripNodes1,
                                                        thisUserList,
                                                        String.format(
                                                            "%s %s's trip from %s to %s",
                                                            user.getFirstName(),
                                                            user.getLastName(),
                                                            countryOneName,
                                                            countryTwoName));
                                                tripOne.setUserRoles(thisUserRoles);

                                                List<TripNode> tripNodes2 =
                                                    makeTripNodesList(
                                                        countryTwoDestinations.get(2),
                                                        countryTwoDestinations.get(3),
                                                        countryOneDestinations.get(2),
                                                        countryOneDestinations.get(3));

                                                List<TripNode> tripNodes3 =
                                                    makeTripNodesList(
                                                        countryOneDestinations.get(4),
                       countryTwoDestinations.get(4));

                                                TripComposite tripThree =
                                                    new TripComposite(
                                                        tripNodes3,
                                                        thisUserList,
                                                        String.format(
                                                            "%s %s's trip from %s to %s",
                                                            user.getFirstName(),
                                                            user.getLastName(),
                                                            countryTwoName,
                                                            countryOneName));
                                                tripThree.setUserRoles(thisUserRoles);

                                                tripNodes2.add(tripThree);

                                                TripComposite tripTwo =
                                                    new TripComposite(
                                                        tripNodes2,
                                                        thisUserList,
                                                        String.format(
                                                            "%s %s's return trip from %s to %s",
                                                            user.getFirstName(),
                                                            user.getLastName(),
                                                            countryTwoName,
                                                            countryOneName));
                                                tripTwo.setUserRoles(thisUserRoles);

                                                tripOne.save();
                                                log.info(String.format("%s has been created",
                       tripOne.getName()));

                                                tripThree.save();
                                                log.info(String.format("%s has been created",
                       tripThree.getName()));

                                                tripTwo.save();
                                                log.info(String.format("%s has been created",
                       tripTwo.getName()));
                                              }
                                            }
                      if (makeBigTrips) {
                        List<User> vipUsers = new ArrayList<>();
                        List<UserRole> userRoles = new ArrayList<>();

                        for (int i = 0; i < firstNames.length; i++) {
                          Optional<User> optionalUser =
                              User.find
                                  .query()
                                  .where()
                                  .eq("first_name", firstNames[i])
                                  .and()
                                  .eq("last_name", lastNames[i])
                                  .findOneOrEmpty();
                          if (optionalUser.isPresent()) {
                            vipUsers.add(optionalUser.get());
                            UserRole userRole;
                            if (i == 0) {
                              userRole = new UserRole(optionalUser.get(), ownerRole);
                            } else {
                              userRole = new UserRole(optionalUser.get(), role);

                            }
                            userRole.save();
                            userRoles.add(userRole);
                          }
                        }

                        makeTourOfCountry(
                            vipUsers,
                            userRoles,
                            Country.find.query().where().eq("country_name", "Australia").findOne());
                        makeTourOfCountry(
                            vipUsers,
                            userRoles,
                            Country.find.query().where().eq("country_name", "Austria").findOne());
                        makeTourOfCountry(
                            vipUsers,
                            userRoles,
                            Country.find.query().where().eq("country_name", "Finland").findOne());
                        makeTourOfCountry(
                            vipUsers,
                            userRoles,
                            Country.find.query().where().eq("country_name", "Colombia").findOne());
                        makeTourOfCountry(
                            vipUsers,
                            userRoles,
                            Country.find.query().where().eq("country_name", "Germany").findOne());
                        makeTourOfCountry(
                            vipUsers,
                            userRoles,
                            Country.find
                                .query()
                                .where()
                                .eq("country_name", "South Africa")
                                .findOne());
                        makeTourOfCountry(
                            vipUsers,
                            userRoles,
                            Country.find.query().where().eq("country_name", "China").findOne());
                        makeTourOfCountry(
                            vipUsers,
                            userRoles,
                            Country.find.query().where().eq("country_name", "Argentina").findOne());
                        makeTourOfCountry(
                            vipUsers,
                            userRoles,
                            Country.find.query().where().eq("country_name", "Brazil").findOne());
                        makeTourOfCountry(
                            vipUsers,
                            userRoles,
                            Country.find.query().where().eq("country_name", "Peru").findOne());
                        makeTourOfCountry(
                            vipUsers,
                            userRoles,
                            Country.find
                                .query()
                                .where()
                                .eq("country_name", "New Zealand")
                                .findOne());
                        makeTourOfCountry(
                            vipUsers,
                            userRoles,
                            Country.find
                                .query()
                                .where()
                                .eq("country_name", "United States of America")
                                .findOne());
                      }
                      log.info("Finished Loading Example Trips");
                    }),
            this.executionContext);
  }
}
