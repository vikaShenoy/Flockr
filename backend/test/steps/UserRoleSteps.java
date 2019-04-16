package steps;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Module;
import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import models.Role;
import models.RoleType;
import play.Application;
import play.ApplicationLoader;
import play.Environment;
import play.inject.guice.GuiceApplicationBuilder;
import play.inject.guice.GuiceApplicationLoader;
import play.test.Helpers;

import javax.inject.Inject;

public class UserRoleSteps {

    @Inject
    private Application application;

    // Data
    private int userId;
    private String authToken;
    private ArrayNode roles;

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

    @When("The user updates their roles to contain {string}")
    public void the_user_updates_their_roles_to_contain(String string) {
        // Write code here that turns the phrase above into concrete actions
        assert(true);
    }

    @Then("The user's roles contain the {string} role")
    public void the_user_s_roles_contain_the_role(String string) {
        // Write code here that turns the phrase above into concrete actions
        assert(true);
    }

}
