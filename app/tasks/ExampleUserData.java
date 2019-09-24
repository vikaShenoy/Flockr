package tasks;

import akka.actor.ActorSystem;
import models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.UserRepository;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import util.Security;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * Task to create 10,000 example users in the system with valid data. Users have random names but
 * the same date of birth to help locate them within the database.
 */
public class ExampleUserData {

  private final UserRepository userRepository;
  private final ActorSystem actorSystem;
  private final ExecutionContext executionContext;
  private final Security security;
  private int userCount = 0;
  final Logger log = LoggerFactory.getLogger(this.getClass());

  /**
   * Constructor
   *
   * @param actorSystem
   * @param executionContext
   * @param security
   * @param userRepository
   */
  @Inject
  public ExampleUserData(
      ActorSystem actorSystem,
      ExecutionContext executionContext,
      Security security,
      UserRepository userRepository) {
    this.actorSystem = actorSystem;
    this.executionContext = executionContext;
    this.security = security;
    this.userRepository = userRepository;
    this.initialise();
  }

  /** Initialise the scheduled task */
  private void initialise() {

    this.actorSystem
        .scheduler()
        .scheduleOnce(
            Duration.create(0, TimeUnit.SECONDS),
            () ->
                supplyAsync(
                    () -> {
                      try {
                        Role userRole =
                            Role.find.query().where().eq("role_type", "TRAVELLER").findOne();
                        List<Role> rolesList = new ArrayList<>();
                        rolesList.add(userRole);
                        BufferedReader firstReader =
                            new BufferedReader(
                                new FileReader("./app/tasks/SampleData/FirstNames.txt"));
                        BufferedReader lastReader =
                            new BufferedReader(
                                new FileReader("./app/tasks/SampleData/LastNames.txt"));
                        String firstName;
                        String lastName;
                        List<Nationality> nationalities =
                            userRepository.getAllNationalities().toCompletableFuture().get();
                        List<TravellerType> travellerTypes =
                            userRepository.getAllTravellerTypes().toCompletableFuture().get();
                        List<Passport> passports =
                            userRepository.getAllPassports().toCompletableFuture().get();
                        int natIndex = 0;
                        int passIndex = 0;
                        int travellerTypeIndex = 0;
                        int natSize = nationalities.size();
                        int passSize = passports.size();
                        int travSize = travellerTypes.size();
                        List<String> firstNameList = new ArrayList<>();
                        List<String> lastNameList = new ArrayList<>();

                        while ((firstName = firstReader.readLine()) != null) {
                          firstNameList.add(firstName);
                        }

                        while ((lastName = lastReader.readLine()) != null) {
                          lastNameList.add(lastName);
                        }
                        firstReader.close();
                        lastReader.close();
                        log.info(
                            String.format(
                                "Number of users to add:%d",
                                firstNameList.size() * lastNameList.size()));

                        for (String first : firstNameList) {
                          for (String last : lastNameList) {
                            List<Nationality> nationalityToAdd = new ArrayList<>();
                            nationalityToAdd.add(nationalities.get((natIndex % natSize)));
                            natIndex++;

                            List<Passport> passportToAdd = new ArrayList<>();
                            passportToAdd.add(passports.get((passIndex % passSize)));
                            passIndex++;

                            List<TravellerType> travellerTypeToAdd = new ArrayList<>();
                            travellerTypeToAdd.add(
                                travellerTypes.get((travellerTypeIndex % travSize)));
                            travellerTypeIndex++;
                            User user =
                                new User(
                                    first,
                                    " ",
                                    last,
                                    this.security.hashPassword("so-secure"),
                                    "Female",
                                    first.toLowerCase()
                                        + "."
                                        + last.toLowerCase()
                                        + "@example-user.com",
                                    nationalityToAdd,
                                    travellerTypeToAdd,
                                    new Date(631152000),
                                    passportToAdd,
                                    rolesList,
                                    "abcdef");
                            user.save();
                            userCount++;
                          }
                        }
                      } catch (Exception e) {
                        log.info(e.getMessage());
                      }

                      log.info(
                          String.format(
                              "Ended populating Example profile data. Total profiles added: %d",
                              userCount));
                      return null;
                    }),
            this.executionContext);
  }
}
