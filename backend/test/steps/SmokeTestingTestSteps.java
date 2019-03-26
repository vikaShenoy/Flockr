package steps;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Module;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import play.Application;
import play.ApplicationLoader;
import play.Environment;
import play.inject.guice.GuiceApplicationBuilder;
import play.inject.guice.GuiceApplicationLoader;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

import javax.inject.Inject;
import static play.test.Helpers.GET;
import static play.test.Helpers.route;

public class SmokeTestingTestSteps {
    @Inject
    private Application application;

    private Result result; // the result returned by the backend


    /**
     * Set up the backend server
     */
    @Before
    public void setUp() {
        Module testModule = new AbstractModule() {
            @Override
            public void configure() {
            }
        };
        GuiceApplicationBuilder builder = new GuiceApplicationLoader()
            .builder(new ApplicationLoader.Context(Environment.simple()))
            .overrides(testModule);
        Guice.createInjector(builder.applicationModule()).injectMembers(this);
        Helpers.start(application);
    }


    /**
     * Stop the backend server
     */
    @After
    public void tearDown() {
        Helpers.stop(application);
    }


    @When("I do a {string} request on {string}")
    public void iDoAGETRequestOn(String requestMethod, String endpoint) {
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(requestMethod)
            .uri(endpoint);
        this.result = route(application, request);
    }

    @Then("the response should have a {int} status code")
    public void theResponseShouldHaveAStatusCode(Integer expectedStatusCode) {
        Assert.assertEquals(expectedStatusCode, (Integer) this.result.status());
    }
}
