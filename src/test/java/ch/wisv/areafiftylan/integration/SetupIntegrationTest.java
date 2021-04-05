package ch.wisv.areafiftylan.integration;

import ch.wisv.areafiftylan.utils.TestDataRunner;
import ch.wisv.areafiftylan.utils.setup.SetupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SetupIntegrationTest extends XAuthIntegrationTest {

    @Autowired
    private TestDataRunner runner;

    @Autowired
    private SetupService setupService;

    @Test
    public void testSetupEvent() {
        runner.insertTestData();

        setupService.deleteAllCurrentEventData();
        setupService.checkDeletion();
    }
}
