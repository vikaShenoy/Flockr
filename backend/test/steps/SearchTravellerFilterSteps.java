package steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Module;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import org.junit.Assert;
import play.Application;
import play.ApplicationLoader;
import play.Environment;
import play.inject.guice.GuiceApplicationBuilder;
import play.inject.guice.GuiceApplicationLoader;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import static play.test.Helpers.route;

public class SearchTravellerFilterSteps {
    @Inject
    private Application application;
    private ArrayList<JsonNode> searchResults;
    private Result result;

    @Given("the back-end server is operating")
    public void theBackEndServerIsOperating() {
        ;
    }
}
