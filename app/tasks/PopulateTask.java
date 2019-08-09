package tasks;

import akka.actor.ActorSystem;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.*;
import org.slf4j.LoggerFactory;
import play.Logger;
import play.libs.Json;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import util.Security;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static java.util.concurrent.CompletableFuture.supplyAsync;


public class PopulateTask {

    private final ActorSystem actorSystem;
    private final ExecutionContext executionContext;
    private final Security security;

    @Inject
    public PopulateTask(ActorSystem actorSystem, ExecutionContext executionContext, Security security) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;
        this.security = security;
        this.initialise();
    }


    private void initialise() {
        this.actorSystem
                .scheduler()
                .scheduleOnce(Duration.create(0, TimeUnit.SECONDS), () -> {
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

        List<Role> adminRoleTypes = new ArrayList<>();
        adminRoleTypes.add(superAdminRole);

        User adminUser = new User("Luis", "something", "Hamilton", this.security.hashPassword("so-secure"), "Male", "luis@gmail.com", adminUserNationalities, adminTravellerTypes, new Date(), adminPassports, adminRoleTypes, "abc");
        adminUser.save();

        // Creating some initial destinations
        DestinationType destinationType = new DestinationType("city");
        destinationType.save();
        Country country = new Country("New Zealand", "NZ", true);
        country.save();
        District district = new District("Canterbury", country);
        district.save();

        Destination christchurch = new Destination("Christchurch", destinationType, district, 0.0, 0.0, country, adminUser.getUserId(), new ArrayList<>(), true);
        christchurch.save();

        Destination westMelton = new Destination("West Melton", destinationType, district,0.0, 0.0, country, adminUser.getUserId(), new ArrayList<>(), true);
        westMelton.save();

        Destination helkett = new Destination("Helkett", destinationType, district,0.0, 0.0, country, adminUser.getUserId(), new ArrayList<>(), true);
        helkett.save();

        // Creating a trip

        TripDestinationLeaf tripChristchurch = new TripDestinationLeaf(christchurch, new Date(1564272000), 43200, new Date(1564358400), 43200);
        TripDestinationLeaf tripWestMelton = new TripDestinationLeaf(westMelton, new Date(1564358400), 50400, new Date(1564358400), 68400);
        TripDestinationLeaf tripHelkett = new TripDestinationLeaf(westMelton, new Date(1564358400), 50400, new Date(1564358400), 68400);
        tripChristchurch.save();
        tripWestMelton.save();
        tripHelkett.save();
        ArrayList<TripNode> tripNodes = new ArrayList<>();
        tripNodes.add(tripChristchurch);
        tripNodes.add(tripWestMelton);
        List<User> users = new ArrayList<>();
        users.add(adminUser);
        TripComposite trip = new TripComposite(tripNodes, users, "Testing Trip 1");
        trip.save();
        tripNodes.remove(tripWestMelton);
        tripNodes.add(tripHelkett);

        TripComposite trip2 = new TripComposite(tripNodes, users, "Find the family graves");
        trip2.save();
        System.out.println("Ended populating data");
    }, this.executionContext);
   }
}
