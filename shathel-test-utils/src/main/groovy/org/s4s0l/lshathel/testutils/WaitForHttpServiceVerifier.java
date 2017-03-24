package org.s4s0l.lshathel.testutils;

import org.s4s0l.shathel.commons.scripts.HttpApis;

import java.util.Arrays;
import java.util.List;

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

    @Override
    public boolean verify(VerifierContextContract context) {
        String fill = context.fill(address);
        new HttpApis().test(fill, getExpectedStatus());
        return true;
    }

    public List<Integer> getExpectedStatus() {
        return expectedStatus == null || expectedStatus.isEmpty()
                ? DEFAULT_EXPECTED_STATUS : expectedStatus;
    }
}
