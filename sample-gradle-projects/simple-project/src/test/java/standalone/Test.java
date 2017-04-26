package standalone;

import static io.restassured.RestAssured.when;
import static java.lang.System.getProperty;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Assert;

/**
 * @author Marcin Wielgus
 */
public class Test {

    @org.junit.Test
    public void checkIfComposedProjectAStarted() throws Exception {
        Assert.assertNotNull(System.getProperty("shathel.plugin.current"));
        Assert.assertTrue(System.getProperty("shathel.plugin.simple.project.project1a.8080").startsWith("127.0.0.1:"));
        Assert.assertTrue(System.getProperty("shathel.plugin.simple.project.project1b.9090").startsWith("127.0.0.1:"));
    }


}
