package standalone;

import static io.restassured.RestAssured.when;
import static java.lang.System.getProperty;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.response.Response;

/**
 * @author Matcin Wielgus
 */
public class Test {

	@org.junit.Test
	public void checkIfComposedProjectStarted() {
		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
		RestAssured.baseURI = "http://" + getProperty("me.host") + ":"
				+ getProperty("me.tcp.8080");
		// @formatter:off
        Response S = when()
                .get("/");
        S.then()
                .statusCode(200)
                .body(equalTo("Hello World!"));
        // @formatter:off
    }
}
