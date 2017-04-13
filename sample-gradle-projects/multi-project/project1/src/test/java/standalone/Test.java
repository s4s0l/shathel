package standalone;

import static io.restassured.RestAssured.when;
import static java.lang.System.getProperty;
import static org.hamcrest.Matchers.equalTo;
import org.junit.Assert;
import io.restassured.RestAssured;
import io.restassured.response.Response;

/**
 * @author Marcin Wielgus
 */
public class Test {

    @org.junit.Test
    public void checkIfComposedProjectAStarted() throws Exception {
        Assert.assertNotNull(System.getProperty("shathel.plugin.current"));
        Assert.assertEquals(System.getProperty("shathel.plugin.project1.project1.8080"), "8081");

    }


}
