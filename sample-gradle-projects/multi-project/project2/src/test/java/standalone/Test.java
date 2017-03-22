package standalone;

import static io.restassured.RestAssured.when;
import static java.lang.System.getProperty;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.response.Response;

/**
 * @author Marcin Wielgus
 */
public class Test {

    @org.junit.Test
    public void checkIfComposedProjectAStarted() throws Exception {
        tryAddress("http://localhost:8080", "Hello World1!");
        tryAddress("http://localhost:7070", "Hello World1!");
        tryAddress("http://localhost:9090", "Hello World2!");
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
