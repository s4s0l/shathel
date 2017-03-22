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
        Assert.assertNotNull(System.getProperty("shathel.plugin.ip"));
        Assert.assertEquals(System.getProperty("shathel.plugin.simple-project.project1a.8080"), "8080");
        Assert.assertEquals(System.getProperty("shathel.plugin.simple-project.project1b.9090"), "9091");
    }


    void tryAddress(String address, String expectedString) throws Exception {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.baseURI = address;
        tryNTimes(60, () -> {
            // @formatter:off
            Response S = when()
                    .get("/");
            S.then()
                    .statusCode(200)
                    .body(equalTo(expectedString));
            // @formatter:on
        });

    }

    void tryNTimes(int n, Runnable r) throws Exception {
        for (int i = 1; i <= n; i++) {
            try {
                r.run();
            } catch (Exception e) {
                if (i == n) {
                    throw e;
                } else {
                    System.out.println("Will try again...");
                    Thread.sleep(1000);
                }
            }
        }
    }
}
