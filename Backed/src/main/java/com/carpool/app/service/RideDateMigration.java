package com.carpool.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class RideDateMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        if (!hasColumn("ride_date_dmy")) {
            throw new IllegalStateException("The ride_date_dmy column was not created by Hibernate.");
        }
        if (!hasColumn("ride_date")) {
            return;
        }

        int migratedRows = jdbcTemplate.update(
                "UPDATE rides SET ride_date_dmy = DATE_FORMAT(ride_date, '%d/%m/%y') "
                        + "WHERE ride_date_dmy IS NULL AND ride_date IS NOT NULL");

        String nullable = jdbcTemplate.queryForObject(
                "SELECT is_nullable FROM information_schema.columns "
                        + "WHERE table_schema = DATABASE() AND table_name = 'rides' AND column_name = 'ride_date'",
                String.class);
        if ("NO".equalsIgnoreCase(nullable)) {
            jdbcTemplate.execute("ALTER TABLE rides MODIFY COLUMN ride_date DATE NULL");
        }
        if (!hasColumn("ride_date_iso_legacy")) {
            jdbcTemplate.execute("ALTER TABLE rides RENAME COLUMN ride_date TO ride_date_iso_legacy");
        }

        if (migratedRows > 0) {
            log.info("Migrated {} ride dates to DD/MM/YY storage", migratedRows);
        }
    }

    private boolean hasColumn(String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns "
                        + "WHERE table_schema = DATABASE() AND table_name = 'rides' AND column_name = ?",
                Integer.class,
                columnName);
        return count != null && count > 0;
    }
}
