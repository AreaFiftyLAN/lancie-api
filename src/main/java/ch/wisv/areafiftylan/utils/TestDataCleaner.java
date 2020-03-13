package ch.wisv.areafiftylan.utils;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Profile("dev")
public class TestDataCleaner implements FlywayMigrationStrategy {

    @Override
    public void migrate(Flyway flyway) {
        log.debug("Cleaning data");
        flyway.clean();
        log.debug("Initalizing migrations");
        flyway.migrate();
    }

}
