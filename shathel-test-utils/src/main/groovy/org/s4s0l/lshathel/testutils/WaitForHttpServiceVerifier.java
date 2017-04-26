package org.s4s0l.lshathel.testutils;

import org.s4s0l.shathel.commons.scripts.HttpApis;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Marcin Wielgus
 */
public class WaitForHttpServiceVerifier implements Verifier {

    public static final List<Integer> DEFAULT_EXPECTED_STATUS = Arrays.asList(200, 401, 403);

    private final String address;
    private final List<Integer> expectedStatus;

    public WaitForHttpServiceVerifier(String address, List<Integer> expectedStatus) {
        this.address = address;
        this.expectedStatus = expectedStatus;
    }

    private static final Logger LOGGER = getLogger(WaitForHttpServiceVerifier.class);

    @Override
    public boolean verify(VerifierContextContract context) {
        LOGGER.warn("Verifying " + address);
        String fill = context.fill(address);
        new HttpApis().test(fill, getExpectedStatus());
        return true;
    }

    public List<Integer> getExpectedStatus() {
        return expectedStatus == null || expectedStatus.isEmpty()
                ? DEFAULT_EXPECTED_STATUS : expectedStatus;
    }
}
