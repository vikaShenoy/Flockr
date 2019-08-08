package models;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.test.Helpers;
import utils.FakeClient;
import utils.FakePlayClient;
import utils.TestState;

import java.util.HashMap;
import java.util.Map;

public class TripNodeTest {

    private Application application;
    private FakeClient fakeClient;

    @Before
    public void setUp() {
        Map<String, String> testSettings = new HashMap<>();
        testSettings.put("db.default.driver", "org.h2.Driver");
        testSettings.put("db.default.url", "jdbc:h2:mem:testdb;MODE=MySQL;");
        testSettings.put("play.evolutions.db.default.enabled", "true");
        testSettings.put("play.evolutions.db.default.autoApply", "true");
        testSettings.put("play.evolutions.db.default.autoApplyDowns", "true");

        application = Helpers.fakeApplication(testSettings);
        Helpers.start(application);

        // Make some users
        fakeClient = new FakePlayClient(application);
    }

    @After
    public void tearDown() {
        Helpers.stop(application);
        TestState.clear();
    }

    @Test
    public void firstTest() {
        TripNode superTrip = new CompositeTrip();
        superTrip.setName("Composite Trip");
        TripNode subTrip1 = new TripDestinationLeaf();
        subTrip1.setName("sub Trip 1");
        TripNode subTrip2 = new TripDestinationLeaf();
        subTrip2.setName("sub Trip 2");
        TripNode compTrip2 = new CompositeTrip();
        compTrip2.setName("comp Trip 2");

        superTrip.addTripNodes(subTrip1);
        superTrip.addTripNodes(subTrip2);
        superTrip.addTripNodes(compTrip2);

        superTrip.save();

        Assert.assertEquals(3, superTrip.getTripNodes().size());

        CompositeTrip afterSavedTripNode = CompositeTrip.find.byId(superTrip.getId());
        Assert.assertEquals("Composite Trip", afterSavedTripNode.getName());
        Assert.assertEquals(3, afterSavedTripNode.getTripNodes().size());
    }

}