package standalone;

import static io.restassured.RestAssured.when;
import static java.lang.System.getProperty;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Assert;
import org.junit.ClassRule;
import org.s4s0l.lshathel.testutils.TestShathelRule;
import org.s4s0l.lshathel.testutils.TestShathelRuleContract;

import java.util.Arrays;

/**
 * @author Marcin Wielgus
 */
public class Test {

    @ClassRule
    public static TestShathelRuleContract shathel = TestShathelRule.builder()
            .addVerifier(TestShathelRuleContract.waitForHttp("http://${IP}:${MYSERVICE_8080}",
                    Arrays.asList(200)))
            .build();

    @org.junit.Test
    public void checkIfComposedProjectAStarted() throws Exception {
        tryAddress(shathel.fill("http://${IP}:${MYSERVICE_8080}"), "Hello World!");
    }


    void tryAddress(String address, String expectedString) throws Exception {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.baseURI = address;
//        tryNTimes(60, () -> {
        // @formatter:off
            Response S = when()
                    .get("/");
            S.then()
                    .statusCode(200)
                    .body(equalTo(expectedString));
            // @formatter:on
//        });

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
