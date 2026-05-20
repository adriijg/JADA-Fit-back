package es.jadafit.jadafit_api.config;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        try {
            jdbcTemplate.execute("ALTER TABLE routine ADD COLUMN IF NOT EXISTS user_id UUID REFERENCES users(id)");
            jdbcTemplate.execute("ALTER TABLE routine ADD COLUMN IF NOT EXISTS is_completed BOOLEAN DEFAULT FALSE");
            jdbcTemplate.execute("ALTER TABLE routine ADD COLUMN IF NOT EXISTS completed_at TIMESTAMP");
        } catch (Exception e) {
            System.err.println("[DB INIT] Could not alter routine table: " + e.getMessage());
        }
    }
}
