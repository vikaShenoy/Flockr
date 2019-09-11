package tasks;

import akka.actor.ActorSystem;
import models.*;
import play.Environment;
import repository.UserRepository;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import util.Security;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * Task to create 10,000 example users in the system with valid data. Users have random names but
 * the same date of birth to help locate them within the database.
 */
public class ExampleData {

  private final UserRepository userRepository;
  private final ActorSystem actorSystem;
  private final ExecutionContext executionContext;
  private final Security security;


    /**
     * Constructor
     * @param actorSystem
     * @param executionContext
     * @param security
     * @param userRepository
     */
  @Inject
  public ExampleData(
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
                        Role userRole = new Role(RoleType.TRAVELLER);
                        List<Role> rolesList = new ArrayList<>();
                        rolesList.add(userRole);
                        BufferedReader firstReader =
                            new BufferedReader(new FileReader("SampleData/FirstNames.txt"));
                        String firstName;
                        BufferedReader lastReader =
                            new BufferedReader(new FileReader("SampleData/LastNames.txt"));
                        String lastName;
                        List<Nationality> nationalities =
                            userRepository.getAllNationalities().toCompletableFuture().get();
                        List<TravellerType> travellerTypes =
                            userRepository.getAllTravellerTypes().toCompletableFuture().get();
                        List<Passport> passports =
                            userRepository.getAllPassports().toCompletableFuture().get();
                        while ((firstName = firstReader.readLine()) != null) {
                          lastName = lastReader.readLine();
                          User user =
                              new User(
                                  firstName,
                                  " ",
                                  lastName,
                                  this.security.hashPassword("so-secure"),
                                  "Female",
                                  firstName + lastName + "@example-user.com",
                                  nationalities,
                                  travellerTypes,
                                  new Date(631152000),
                                  passports,
                                  rolesList,
                                  "abcdef78584");
                          user.save();
                        }
                        firstReader.close();
                        lastReader.close();
                      } catch (FileNotFoundException e) {
                        e.printStackTrace();
                      } catch (IOException e) {
                        e.printStackTrace();
                      } catch (InterruptedException e) {
                        e.printStackTrace();
                      } catch (ExecutionException e) {
                        e.printStackTrace();
                      }

                      System.out.println("Ended populating Example profile data");
                      return null;
                    }),
            this.executionContext);
  }
}
