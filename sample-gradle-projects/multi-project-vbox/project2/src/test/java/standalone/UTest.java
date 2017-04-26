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


public class UTest {

    @ClassRule
    public static TestShathelRuleContract shathel = TestShathelRule.builder()
            .verifierAttempts(25)
            .addVerifier(TestShathelRuleContract.waitForHttp("http://${PROJECT12_8080}", Arrays.asList(200)))
            .addVerifier(TestShathelRuleContract.waitForHttp("http://${PROJECT2_8080}", Arrays.asList(200)))
            .build();

    @org.junit.Test
    public void checkIfPortsAreMappedInVboxRemoteEnv() throws Exception {
        tryAddress(shathel.fill("http://${PROJECT12_8080}"), "Hello World1!");
        tryAddress(shathel.fill("http://${PROJECT2_8080}"), "Hello World2!");
    }

    void tryAddress(String address, String expectedString) throws Exception {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.baseURI = address;
        when().get("/")
                .then().statusCode(200)
                .body(equalTo(expectedString));
    }
}

