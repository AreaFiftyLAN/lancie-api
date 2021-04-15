package ch.wisv.areafiftylan.integration;

import ch.wisv.areafiftylan.TestRunner;
import ch.wisv.areafiftylan.utils.TestDataRunner;
import ch.wisv.areafiftylan.utils.setup.SetupService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SetupIntegrationTest extends XAuthIntegrationTest {

    @Autowired
    private TestDataRunner runner;

    @Autowired
    private TestRunner testRunner;

    @Autowired
    private SetupService setupService;

    @AfterEach
    public void afterEach() throws Exception {
        testRunner.run();
    }

    @Test
    public void testSetupEvent() {
        runner.insertTestData();

        setupService.deleteAllCurrentEventData();
        setupService.checkDeletion();
    }
}
