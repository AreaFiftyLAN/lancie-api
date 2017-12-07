package ch.wisv.areafiftylan.utils.db.migration;

import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.core.JdbcTemplate;

public class V20171208163050__add_banner implements SpringJdbcMigration {
    @Override
    public void migrate(JdbcTemplate jdbcTemplate) throws Exception {
        jdbcTemplate.execute("CREATE TABLE banner (" +
                        "id bigint NOT NULL CONSTRAINT banner_pkey PRIMARY KEY, " +
                        "end_date DATE, " +
                        "start_date DATE, " +
                        "text VARCHAR(255))");
    }
}
