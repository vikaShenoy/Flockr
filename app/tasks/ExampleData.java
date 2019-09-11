package tasks;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import akka.actor.ActorSystem;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
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
import repository.UserRepository;
import scala.Array;
import scala.Option;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import util.Security;

public class ExampleData {

    private final UserRepository userRepository;
    private final ActorSystem actorSystem;
    private final ExecutionContext executionContext;
    private final Security security;
    private final Environment environment;

    @Inject
    public ExampleData(
            ActorSystem actorSystem,
            ExecutionContext executionContext,
            Security security,
            Environment environment,
            UserRepository userRepository) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;
        this.security = security;
        this.environment = environment;
        this.userRepository = userRepository;
        this.initialise();



    }

    private void initialise() {

    this.actorSystem
        .scheduler()
        .scheduleOnce(
            Duration.create(0, TimeUnit.SECONDS),
            () ->
                supplyAsync(
                    () -> {
                      int userNo = 0;
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

                        System.out.println("Ended populating Example data");
                      return null;
                    }),
            this.executionContext);
    }
}
