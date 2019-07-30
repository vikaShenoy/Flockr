import cucumber.api.CucumberOptions;
import org.junit.runner.RunWith;
import cucumber.api.junit.Cucumber;
import cucumber.api.SnippetType;

/**
 * This is where the cucumber tests start from.
 * The directory for the tests to be run is specified via the annotations.
 */
@RunWith ( Cucumber.class )
@CucumberOptions (
        features = "classpath:features",
        plugin = {"pretty", "html:target/site/cucumber=pretty", "json:target/cucumber.json"},
        glue = "steps",
        snippets = SnippetType.CAMELCASE
)
public class CucumberTestRunner {

}