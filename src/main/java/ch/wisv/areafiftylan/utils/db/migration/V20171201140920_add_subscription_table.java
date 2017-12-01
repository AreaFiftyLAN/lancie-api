package ch.wisv.areafiftylan.utils.db.migration;

import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.core.JdbcTemplate;

public class V20171201140920_add_subscription_table implements SpringJdbcMigration {
    @Override
    public void migrate(JdbcTemplate jdbcTemplate) throws Exception {
        jdbcTemplate.execute("CREATE TABLE \"subscription\" (\"id\" BIGINT, \"email\" VARCHAR(255) UNIQUE, PRIMARY KEY (id));");
    }
}
