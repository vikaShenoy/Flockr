package tasks;

import static java.util.concurrent.CompletableFuture.runAsync;

import akka.actor.ActorSystem;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import models.Country;
import models.Destination;
import models.DestinationType;
import models.Nationality;
import models.Passport;
import models.Role;
import models.RoleType;
import models.TravellerType;
import models.TripComposite;
import models.TripDestinationLeaf;
import models.TripNode;
import models.User;
import play.Environment;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import util.Security;

public class PopulateTask {

  private final ActorSystem actorSystem;
  private final ExecutionContext executionContext;
  private final Security security;
  private final Environment environment;

  @Inject
  public PopulateTask(
      ActorSystem actorSystem,
      ExecutionContext executionContext,
      Security security,
      Environment environment) {
    this.actorSystem = actorSystem;
    this.executionContext = executionContext;
    this.security = security;
    this.environment = environment;
    this.initialise();
  }

  private void initialise() {

    if (!environment.isDev()) {
      return;
    }

    this.actorSystem
        .scheduler()
        .scheduleOnce(
            Duration.create(0, TimeUnit.SECONDS),
            () ->
                runAsync(
                    () -> {
                      Country newZealand = new Country("New Zealand", "NZL", true);
                      Nationality newZealandNationality = new Nationality("New Zealand");
                      newZealandNationality.setNationalityCountry(newZealand);
                      List<Nationality> adminUserNationalities = new ArrayList<>();
                      adminUserNationalities.add(newZealandNationality);

                      TravellerType frequentWeekender = new TravellerType("Frequent Weekender");
                      frequentWeekender.save();

                      List<TravellerType> adminTravellerTypes = new ArrayList<>();
                      adminTravellerTypes.add(frequentWeekender);
                      List<Passport> adminPassports = new ArrayList<>();
                      Passport newZealandPassport = new Passport("New Zealand");
                      newZealandPassport.save();
                      newZealandPassport.setCountry(newZealand);
                      adminPassports.add(newZealandPassport);

                      Role superAdminRole = new Role(RoleType.SUPER_ADMIN);
                      superAdminRole.save();
                      Role adminRole = new Role(RoleType.ADMIN);
                      adminRole.save();

                      Role userRole = new Role(RoleType.TRAVELLER);
                      userRole.save();
                      List<Role> userRoleTypes = new ArrayList<>();
                      userRoleTypes.add(userRole);

                      List<Role> adminRoleTypes = new ArrayList<>();
                      adminRoleTypes.add(superAdminRole);

                      List<Nationality> janeUserNationalities = new ArrayList<>();
                      List<TravellerType> janeTravellerTypes = new ArrayList<>();
                      List<Passport> janePassports = new ArrayList<>();
                      janePassports.add(newZealandPassport);

                      User adminUser =
                          new User(
                              "Luis",
                              "something",
                              "Hamilton",
                              this.security.hashPassword("so-secure"),
                              "Male",
                              "luis@gmail.com",
                              adminUserNationalities,
                              adminTravellerTypes,
                              new Date(),
                              janePassports,
                              adminRoleTypes,
                              "abc");
                      adminUser.save();

                      User user =
                          new User(
                              "Jane",
                              "",
                              "Doe",
                              this.security.hashPassword("so-secure"),
                              "Female",
                              "jane@gmail.com",
                              janeUserNationalities,
                              janeTravellerTypes,
                              new Date(),
                              janePassports,
                              userRoleTypes,
                              "abcdef");
                      user.save();

                      // Creating some initial destinations
                      DestinationType destinationType = new DestinationType("city");
                      destinationType.save();
                      Country country = new Country("New Zealand", "NZ", true);
                      country.save();
                      String district ="Canterbury";

                      Destination destination1 =
                          new Destination(
                              "destination3",
                              destinationType,
                              district,
                              0.0,
                              40.0,
                              country,
                              adminUser.getUserId(),
                              new ArrayList<>(),
                              true);
                      destination1.save();

                      Destination destination2 =
                          new Destination(
                              "destination5",
                              destinationType,
                              district,
                              11.0,
                              40.0,
                              country,
                              adminUser.getUserId(),
                              new ArrayList<>(),
                              true);
                      destination2.save();

                      Destination destination3 =
                          new Destination(
                              "destination6",
                              destinationType,
                              district,
                              12.0,
                              41.0,
                              country,
                              adminUser.getUserId(),
                              new ArrayList<>(),
                              true);
                      destination3.save();

                      Destination destination4 =
                          new Destination(
                              "destination7",
                              destinationType,
                              district,
                              13.0,
                              42.0,
                              country,
                              adminUser.getUserId(),
                              new ArrayList<>(),
                              true);
                      destination4.save();

                      Destination destination8 =
                          new Destination(
                              "destination8",
                              destinationType,
                              district,
                              14.0,
                              43.0,
                              country,
                              adminUser.getUserId(),
                              new ArrayList<>(),
                              true);
                      destination8.save();

                      // Creating a trip

                      TripDestinationLeaf tripChristchurch =
                          new TripDestinationLeaf(
                              destination1,
                              new Date(1564272000),
                              43200,
                              new Date(1564358400),
                              43200);
                      TripDestinationLeaf tripWestMelton =
                          new TripDestinationLeaf(
                              destination2,
                              new Date(1564358400),
                              50400,
                              new Date(1564358400),
                              68400);
                      TripDestinationLeaf tripHelkett =
                          new TripDestinationLeaf(
                              destination3,
                              new Date(1564358400),
                              50400,
                              new Date(1564358400),
                              68400);

                      tripChristchurch.save();
                      tripWestMelton.save();
                      tripHelkett.save();
                      ArrayList<TripNode> tripNodes = new ArrayList<>();
                      tripNodes.add(tripChristchurch);
                      tripNodes.add(tripWestMelton);
                      tripNodes.add(tripHelkett);
                      List<User> users = new ArrayList<>();
                      users.add(adminUser);

                      Destination morocco =
                          new Destination(
                              "Morocco",
                              destinationType,
                              district,
                              12.0,
                              45.0,
                              country,
                              adminUser.getUserId(),
                              new ArrayList<>(),
                              true);
                      morocco.save();

                      TripDestinationLeaf tripdestination4 =
                          new TripDestinationLeaf(
                              morocco, new Date(1564273000), 43200, new Date(1564359000), 43200);
                      tripdestination4.save();

                      TripComposite trip5 = new TripComposite(tripNodes, users, "Trip 5");
                      trip5.save();

                      ArrayList<TripNode> trip6Nodes = new ArrayList<>();
                      trip6Nodes.add(trip5);
                      trip6Nodes.add(tripdestination4);
                      TripComposite trip6 =
                          new TripComposite(trip6Nodes, users, "Find the family graves");
                      // trip2.setParents(tripNodes);
                      trip6.save();

                      System.out.println("Ended populating data");
                    }),
            this.executionContext);
  }
}
