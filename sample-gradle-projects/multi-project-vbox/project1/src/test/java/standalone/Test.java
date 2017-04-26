package standalone;

import static io.restassured.RestAssured.when;
import static java.lang.System.getProperty;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Assert;
import io.restassured.RestAssured;
import io.restassured.response.Response;

/**
 * @author Marcin      Wielgus
 */
public class Test {

    @org.junit.Test
    public void checkIfComposedProjectAStarted() throws Exception {
        Assert.assertNotNull(System.getProperty("shathel.plugin.current"));
        Assert.assertNotNull(System.getProperty("shathel.plugin.project1.project1.8080"));
        Assert.assertNotNull(System.getProperty("shathel.plugin.project1.8080"));
        Assert.assertTrue(System.getProperty("shathel.plugin.project1.8080").startsWith("127.0.0.1:"));
        Assert.assertTrue(System.getProperty("shathel.plugin.project1.project1.8080").startsWith("127.0.0.1:"));
    }


}
