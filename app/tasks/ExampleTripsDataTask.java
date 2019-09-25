package tasks;

import static java.util.concurrent.CompletableFuture.runAsync;

import akka.actor.ActorSystem;
import com.google.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import models.Country;
import models.Destination;
import models.DestinationType;
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
  private boolean makeBigTrips = false;
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
   * Makes an Island Cruise trip via the following destinations: NZ(Christchurch - Auckland)
   * Fiji(Suva - Nadi) Samoa(Apia - Savai'i) Cook Islands(Avarua) Tonga(Nuku'Alofa) Fiji(Suva)
   * NZ(Auckland - Christchurch)
   *
   * @param users the users going on the trip.
   */
  private void makeIslandCruise(List<User> users, List<UserRole> userRoles) {
    int nzId =
        Objects.requireNonNull(Country.find.query().where().eq("isocode", "NZ").findOne())
            .getCountryId();

    Destination christchurch =
        Destination.find
            .query()
            .where()
            .eq("destination_name", "Christchurch")
            .and()
            .eq("destination_country_country_id", nzId)
            .findOne();
    Destination auckland =
        Destination.find
            .query()
            .where()
            .eq("destination_name", "Auckland")
            .and()
            .eq("destination_country_country_id", nzId)
            .findOne();

    int fijiId =
        Objects.requireNonNull(Country.find.query().where().eq("isocode", "FJ").findOne())
            .getCountryId();

    Destination suva =
        Destination.find
            .query()
            .where()
            .eq("destination_name", "Suva City")
            .and()
            .eq("destination_country_country_id", fijiId)
            .findOne();
    Destination nadi =
        Destination.find
            .query()
            .where()
            .eq("destination_name", "Nadi")
            .and()
            .eq("destination_country_country_id", fijiId)
            .findOne();

    int samoaId =
        Objects.requireNonNull(Country.find.query().where().eq("isocode", "WS").findOne())
            .getCountryId();

    Destination apia =
        Destination.find
            .query()
            .where()
            .eq("destination_name", "Apia")
            .and()
            .eq("destination_country_country_id", samoaId)
            .findOne();
    Destination savaii =
        Destination.find
            .query()
            .where()
            .eq("destination_name", "Savai`i")
            .and()
            .eq("destination_country_country_id", samoaId)
            .findOne();

    int cookIslandsId =
        Objects.requireNonNull(Country.find.query().where().eq("isocode", "CK").findOne())
            .getCountryId();

    Destination avarua =
        Destination.find
            .query()
            .where()
            .eq("destination_name", "Avarua")
            .and()
            .eq("destination_country_country_id", cookIslandsId)
            .findOne();

    int tongaId =
        Objects.requireNonNull(Country.find.query().where().eq("isocode", "TO").findOne())
            .getCountryId();

    Destination nukuAlofa =
        Destination.find
            .query()
            .where()
            .eq("destination_name", "Nuku`alofa")
            .and()
            .eq("destination_country_country_id", tongaId)
            .findOne();

    List<TripNode> tripNodes =
        makeTripNodesList(
            christchurch,
            auckland,
            suva,
            nadi,
            apia,
            savaii,
            avarua,
            nukuAlofa,
            suva,
            auckland,
            christchurch);

    TripComposite tripComposite = new TripComposite(tripNodes, users, "Island Cruise");
    tripComposite.setUserRoles(userRoles);
    tripRepository.saveTrip(tripComposite);
  }

  /**
   * Make a Rugby Championship Tour 2020 trip to the following destinations NZ(Christchurch -
   * Auckland) Argentina(Buenos Aires - Mendoza - Buenos Aires) NZ(Auckland) AU(Brisbane)
   * NZ(Auckland SA(Capetown) NZ(Auckland - Christchurch)
   *
   * @param users the users going on the trip.
   */
  private void makeRugbyChampionshipTour(List<User> users, List<UserRole> userRoles) {
    int nzId =
        Objects.requireNonNull(Country.find.query().where().eq("isocode", "NZ").findOne())
            .getCountryId();

    Destination christchurch =
        Destination.find
            .query()
            .where()
            .eq("destination_name", "Christchurch")
            .and()
            .eq("destination_country_country_id", nzId)
            .findOne();
    Destination auckland =
        Destination.find
            .query()
            .where()
            .eq("destination_name", "Auckland")
            .and()
            .eq("destination_country_country_id", nzId)
            .findOne();

    int argentinaId =
        Objects.requireNonNull(Country.find.query().where().eq("isocode", "AR").findOne())
            .getCountryId();

    Destination buenosAires =
        Destination.find
            .query()
            .where()
            .eq("destination_name", "Buenos Aires")
            .and()
            .eq("destination_country_country_id", argentinaId)
            .findOne();
    Destination mendoza =
        Destination.find
            .query()
            .where()
            .eq("destination_name", "Mendoza")
            .and()
            .eq("destination_country_country_id", argentinaId)
            .findOne();

    int australiaId =
        Objects.requireNonNull(Country.find.query().where().eq("isocode", "AU").findOne())
            .getCountryId();

    Destination brisbane =
        Destination.find
            .query()
            .where()
            .eq("destination_name", "Brisbane")
            .and()
            .eq("destination_country_country_id", australiaId)
            .findOne();

    int southAfricaId =
        Objects.requireNonNull(Country.find.query().where().eq("isocode", "ZA").findOne())
            .getCountryId();

    Destination capeTown =
        Destination.find
            .query()
            .where()
            .eq("destination_name", "Cape Town")
            .and()
            .eq("destination_country_country_id", southAfricaId)
            .findOne();

    List<TripNode> tripNodes =
        makeTripNodesList(
            christchurch,
            auckland,
            buenosAires,
            mendoza,
            buenosAires,
            auckland,
            brisbane,
            auckland,
            capeTown,
            auckland,
            christchurch);

    TripComposite tripComposite =
        new TripComposite(tripNodes, users, "Rugby Championship Tour 2020");
    tripComposite.setUserRoles(userRoles);
    tripRepository.saveTrip(tripComposite);
  }

  /**
   * Makes a El Viaje de Sudamérica (The Tour of South America) trip to the following destinations:
   * NZ(Christchurch - Auckland) -> Chile(Santiago - Punta Arenas - Puerto Natales - Punta Arenas -
   * Santiago) -> Colombia(Bogota - medellin - Bogota) -> Peru(Lima - Machu Pichu - Lima) ->
   * Brazil(Rio de Janeiro - São Paulo) -> NZ(Auckland - Christchurch)
   *
   * @param users the users going on the trip.
   */
  private void makeElViajeDeSudamerica(List<User> users, List<UserRole> userRoles) {
    int nzId =
        Objects.requireNonNull(Country.find.query().where().eq("isocode", "NZ").findOne())
            .getCountryId();

    Destination christchurch =
        Destination.find
            .query()
            .where()
            .eq("destination_name", "Christchurch")
            .and()
            .eq("destination_country_country_id", nzId)
            .findOne();
    Destination auckland =
        Destination.find
            .query()
            .where()
            .eq("destination_name", "Auckland")
            .and()
            .eq("destination_country_country_id", nzId)
            .findOne();

    int chileId =
        Objects.requireNonNull(Country.find.query().where().eq("isocode", "CL").findOne())
            .getCountryId();

    Destination santiago =
        Destination.find
            .query()
            .where()
            .eq("destination_name", "Santiago")
            .and()
            .eq("destination_country_country_id", chileId)
            .findOne();
    Destination puntaArenas =
        Destination.find
            .query()
            .where()
            .eq("destination_name", "Punta Arenas")
            .and()
            .eq("destination_country_country_id", chileId)
            .findOne();
    Destination puertoNatales =
        Destination.find
            .query()
            .where()
            .eq("destination_name", "Puerto Natales")
            .and()
            .eq("destination_country_country_id", chileId)
            .findOne();

    int colombiaId =
        Objects.requireNonNull(Country.find.query().where().eq("isocode", "CO").findOne())
            .getCountryId();

    Destination bogota =
        Destination.find
            .query()
            .where()
            .eq("destination_name", "Bogotá")
            .and()
            .eq("destination_country_country_id", colombiaId)
            .findOne();
    Destination medellin =
        Destination.find
            .query()
            .where()
            .eq("destination_name", "Medellín")
            .and()
            .eq("destination_country_country_id", colombiaId)
            .findOne();

    Country peru =
        Objects.requireNonNull(Country.find.query().where().eq("isocode", "PE").findOne());

    Destination lima =
        Destination.find
            .query()
            .where()
            .eq("destination_name", "Lima")
            .and()
            .eq("destination_country_country_id", peru.getCountryId())
            .findOne();
    Destination machuPicchu =
        Destination.find
            .query()
            .where()
            .eq("destination_name", "Machu Picchu")
            .and()
            .eq("destination_country_country_id", peru.getCountryId())
            .findOne();
    if (machuPicchu == null) {
      DestinationType type =
          DestinationType.find
              .query()
              .where()
              .eq("destination_type_name", "Historic Site")
              .findOne();
      machuPicchu =
          new Destination(
              "Machu Picchu",
              type,
              "Aguas Calientes",
              -13.1631412,
              -72.5471516,
              peru,
              null,
              new ArrayList<>(),
              true);
      machuPicchu.save();
    }

    int brazilId =
        Objects.requireNonNull(Country.find.query().where().eq("isocode", "BR").findOne())
            .getCountryId();

    Destination rioDeJaneiro =
        Destination.find
            .query()
            .where()
            .eq("destination_name", "Rio de Janeiro")
            .and()
            .eq("destination_country_country_id", brazilId)
            .findOne();
    Destination saoPaulo =
        Destination.find
            .query()
            .where()
            .eq("destination_name", "São Paulo")
            .and()
            .eq("destination_country_country_id", brazilId)
            .findOne();

    List<TripNode> tripNodes =
        makeTripNodesList(
            christchurch,
            auckland,
            santiago,
            puntaArenas,
            puertoNatales,
            puntaArenas,
            santiago,
            bogota,
            medellin,
            bogota,
            lima,
            machuPicchu,
            lima,
            rioDeJaneiro,
            saoPaulo,
            auckland,
            christchurch);

    TripComposite tripComposite =
        new TripComposite(tripNodes, users, "El Viaje de Sudamérica (The Tour of South America)");
    tripComposite.setUserRoles(userRoles);
    tripRepository.saveTrip(tripComposite);
  }

  /**
   * Makes a Rugby World Cup 2019 Japan Tour trip to the following destinations: NZ(Christchurch -
   * Auckland) -> Singapore(Singapore) -> Japan(Tokyo - Yokohama - Tokyo - Oita - Tokyo - Nagoya -
   * Tokyo - Yokohama - Tokyo) -> Singapore(Singapore) NZ(Auckland - Christchurch)
   *
   * @param users the users going on the trip.
   */
  private void makeRugbyWorldCupJapanTour(List<User> users, List<UserRole> userRoles) {
    int nzId =
        Objects.requireNonNull(Country.find.query().where().eq("isocode", "NZ").findOne())
            .getCountryId();

    Destination christchurch =
        Destination.find
            .query()
            .where()
            .eq("destination_name", "Christchurch")
            .and()
            .eq("destination_country_country_id", nzId)
            .findOne();
    Destination auckland =
        Destination.find
            .query()
            .where()
            .eq("destination_name", "Auckland")
            .and()
            .eq("destination_country_country_id", nzId)
            .findOne();

    int singaporeId =
        Objects.requireNonNull(Country.find.query().where().eq("isocode", "SG").findOne())
            .getCountryId();

    Destination singapore =
        Destination.find
            .query()
            .where()
            .eq("destination_name", "Singapore")
            .and()
            .eq("destination_country_country_id", singaporeId)
            .findOne();

    int japanId =
        Objects.requireNonNull(Country.find.query().where().eq("isocode", "JP").findOne())
            .getCountryId();

    Destination tokyo =
        Destination.find
            .query()
            .where()
            .eq("destination_name", "Tokyo")
            .and()
            .eq("destination_country_country_id", japanId)
            .findOne();
    Destination yokohama =
        Destination.find
            .query()
            .where()
            .eq("destination_name", "Yokohama")
            .and()
            .eq("destination_country_country_id", japanId)
            .findOne();
    Destination oita =
        Destination.find
            .query()
            .where()
            .eq("destination_name", "Oita-shi")
            .and()
            .eq("destination_country_country_id", japanId)
            .findOne();
    Destination nagoya =
        Destination.find
            .query()
            .where()
            .eq("destination_name", "Nagoya")
            .and()
            .eq("destination_country_country_id", japanId)
            .findOne();

    TripDestinationLeaf leaf1 =
        new TripDestinationLeaf(
            christchurch, null, null, Date.from(Instant.parse("2019-09-18T08:00:00.00Z")), null);
    leaf1.save();
    TripDestinationLeaf leaf2 =
        new TripDestinationLeaf(
            auckland,
            Date.from(Instant.parse("2019-09-18T09:00:00.00Z")),
            null,
            Date.from(Instant.parse("2019-09-18T11:00:00.00Z")),
            null);
    leaf2.save();
    TripDestinationLeaf leaf3 =
        new TripDestinationLeaf(
            singapore,
            Date.from(Instant.parse("2019-09-19T09:00:00.00Z")),
            null,
            Date.from(Instant.parse("2019-09-19T13:00:00.00Z")),
            null);
    leaf3.save();
    TripDestinationLeaf leaf4 =
        new TripDestinationLeaf(
            tokyo,
            Date.from(Instant.parse("2019-09-20T09:00:00.00Z")),
            null,
            Date.from(Instant.parse("2019-09-21T13:00:00.00Z")),
            null);
    leaf4.save();
    TripDestinationLeaf leaf5 =
        new TripDestinationLeaf(
            yokohama,
            Date.from(Instant.parse("2019-09-21T14:00:00.00Z")),
            null,
            Date.from(Instant.parse("2019-09-22T13:00:00.00Z")),
            null);
    leaf5.save();
    TripDestinationLeaf leaf6 =
        new TripDestinationLeaf(
            tokyo,
            Date.from(Instant.parse("2019-09-22T14:00:00.00Z")),
            null,
            Date.from(Instant.parse("2019-10-02T13:00:00.00Z")),
            null);
    leaf6.save();
    TripDestinationLeaf leaf7 =
        new TripDestinationLeaf(
            oita,
            Date.from(Instant.parse("2019-10-02T14:00:00.00Z")),
            null,
            Date.from(Instant.parse("2019-10-06T13:00:00.00Z")),
            null);
    leaf7.save();
    TripDestinationLeaf leaf8 =
        new TripDestinationLeaf(
            tokyo,
            Date.from(Instant.parse("2019-10-06T14:00:00.00Z")),
            null,
            Date.from(Instant.parse("2019-10-07T13:00:00.00Z")),
            null);
    leaf8.save();
    TripDestinationLeaf leaf9 =
        new TripDestinationLeaf(
            nagoya,
            Date.from(Instant.parse("2019-10-07T14:00:00.00Z")),
            null,
            Date.from(Instant.parse("2019-10-08T13:00:00.00Z")),
            null);
    leaf9.save();
    TripDestinationLeaf leaf10 =
        new TripDestinationLeaf(
            tokyo,
            Date.from(Instant.parse("2019-10-08T14:00:00.00Z")),
            null,
            Date.from(Instant.parse("2019-10-11T13:00:00.00Z")),
            null);
    leaf10.save();
    TripDestinationLeaf leaf11 =
        new TripDestinationLeaf(
            yokohama,
            Date.from(Instant.parse("2019-10-11T14:00:00.00Z")),
            null,
            Date.from(Instant.parse("2019-10-16T13:00:00.00Z")),
            null);
    leaf11.save();
    TripDestinationLeaf leaf12 =
        new TripDestinationLeaf(
            tokyo,
            Date.from(Instant.parse("2019-10-16T14:00:00.00Z")),
            null,
            Date.from(Instant.parse("2019-10-17T13:00:00.00Z")),
            null);
    leaf12.save();
    TripDestinationLeaf leaf13 =
        new TripDestinationLeaf(
            singapore,
            Date.from(Instant.parse("2019-10-18T09:00:00.00Z")),
            null,
            Date.from(Instant.parse("2019-10-18T13:00:00.00Z")),
            null);
    leaf13.save();
    TripDestinationLeaf leaf14 =
        new TripDestinationLeaf(
            auckland,
            Date.from(Instant.parse("2019-10-19T09:00:00.00Z")),
            null,
            Date.from(Instant.parse("2019-10-19T13:00:00.00Z")),
            null);
    leaf14.save();
    TripDestinationLeaf leaf15 =
        new TripDestinationLeaf(
            christchurch, Date.from(Instant.parse("2019-10-19T15:00:00.00Z")), null, null, null);
    leaf15.save();

    List<TripNode> tripNodes = new ArrayList<>();
    tripNodes.add(leaf1);
    tripNodes.add(leaf2);
    tripNodes.add(leaf3);
    tripNodes.add(leaf4);
    tripNodes.add(leaf5);
    tripNodes.add(leaf6);
    tripNodes.add(leaf7);
    tripNodes.add(leaf8);
    tripNodes.add(leaf9);
    tripNodes.add(leaf10);
    tripNodes.add(leaf11);
    tripNodes.add(leaf12);
    tripNodes.add(leaf13);
    tripNodes.add(leaf14);
    tripNodes.add(leaf15);

    TripComposite tripComposite =
        new TripComposite(tripNodes, users, "Rugby World Cup 2019 Japan Tour");
    tripComposite.setUserRoles(userRoles);
    tripRepository.saveTrip(tripComposite);
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
                          String countryOneName = countryOne.getCountryName();
                          List<Destination> countryOneDestinations = new ArrayList<>();
                          while (countryOneDestinations.size() < 5) {
                            countryOneDestinations = fetchNumDestinationsFromCountry(countryOne, 5);
                            countryOneName = countryOne.getCountryName();
                            countryIndex = (countryIndex + 1) % countries.size();
                            countryOne = countries.get(countryIndex);
                          }
                          Country countryTwo = countries.get(countryIndex);
                          String countryTwoName = countryTwo.getCountryName();
                          List<Destination> countryTwoDestinations = new ArrayList<>();
                          while (countryTwoDestinations.size() < 5) {
                            countryTwoDestinations = fetchNumDestinationsFromCountry(countryTwo, 5);
                            countryTwoName = countryTwo.getCountryName();
                            countryIndex = (countryIndex + 1) % countries.size();
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
                                  countryOneDestinations.get(4), countryTwoDestinations.get(4));

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
                          log.info(String.format("%s has been created", tripOne.getName()));

                          tripThree.save();
                          log.info(String.format("%s has been created", tripThree.getName()));

                          tripTwo.save();
                          log.info(String.format("%s has been created", tripTwo.getName()));
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
                            UserRole userRole = new UserRole(optionalUser.get(), role);
                            userRole.save();
                            userRoles.add(userRole);
                          }
                        }

                        makeIslandCruise(vipUsers, userRoles);
                        makeRugbyChampionshipTour(vipUsers, userRoles);
                        makeElViajeDeSudamerica(vipUsers, userRoles);
                        makeRugbyWorldCupJapanTour(vipUsers, userRoles);
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
                            Country.find.query().where().eq("country_name", "England").findOne());
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
