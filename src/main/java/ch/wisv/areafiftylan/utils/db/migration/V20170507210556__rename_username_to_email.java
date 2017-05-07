package ch.wisv.areafiftylan.utils.db.migration;

import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.core.JdbcTemplate;

public class V20170507210556__rename_username_to_email implements SpringJdbcMigration {
    @Override
    public void migrate(JdbcTemplate jdbcTemplate) throws Exception {
        jdbcTemplate.execute("ALTER TABLE \"user\" RENAME username TO email;");
    }
}
