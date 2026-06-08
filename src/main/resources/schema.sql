-- ============================================
-- Migration: JPA entity fixes
-- Solo lo que ddl-auto=update NO puede hacer:
--   • renombrar columnas
--   • dropear columnas
--   • migrar datos
--   • FK constraints en columnas existentes
-- ============================================

-- 1. fitness_profiles: age → date_of_birth
ALTER TABLE fitness_profiles ADD COLUMN IF NOT EXISTS date_of_birth DATE;
UPDATE fitness_profiles
SET date_of_birth = CURRENT_DATE - (age || ' years')::INTERVAL
WHERE age IS NOT NULL AND date_of_birth IS NULL;
ALTER TABLE fitness_profiles DROP COLUMN IF EXISTS age;

-- 2. users: session_id ya está en user_sessions
ALTER TABLE users DROP COLUMN IF EXISTS session_id;

-- 3. FK en user_sessions.user_id (antes era UUID suelto)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_user_sessions_user'
          AND table_name = 'user_sessions'
    ) THEN
        ALTER TABLE user_sessions
        ADD CONSTRAINT fk_user_sessions_user
        FOREIGN KEY (user_id) REFERENCES users(id);
    END IF;
END $$;

-- 4. Normalizar food_source a mayúsculas (FoodSource enum)
UPDATE nutrition_meal_logs
SET food_source = UPPER(TRIM(food_source))
WHERE food_source IS NOT NULL
  AND food_source != UPPER(TRIM(food_source));

-- 5. challenges: backfill columnas nuevas en bases con retos antiguos
ALTER TABLE challenges ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP;
UPDATE challenges
SET expires_at = COALESCE(created_at, CURRENT_TIMESTAMP) + INTERVAL '7 days'
WHERE expires_at IS NULL;
ALTER TABLE challenges ALTER COLUMN expires_at SET NOT NULL;

-- 6. Hibernate @Version no puede incrementar valores NULL en filas antiguas
UPDATE users SET version = 0 WHERE version IS NULL;
UPDATE user_sessions SET version = 0 WHERE version IS NULL;
UPDATE user_exercise_records SET version = 0 WHERE version IS NULL;
UPDATE challenges SET version = 0 WHERE version IS NULL;
UPDATE challenge_progress_entries SET version = 0 WHERE version IS NULL;
UPDATE catalog_exercise SET version = 0 WHERE version IS NULL;
UPDATE catalog_food SET version = 0 WHERE version IS NULL;
UPDATE fitness_profiles SET version = 0 WHERE version IS NULL;
UPDATE fitness_progress_logs SET version = 0 WHERE version IS NULL;
UPDATE nutrition_goals SET version = 0 WHERE version IS NULL;
UPDATE nutrition_meal_logs SET version = 0 WHERE version IS NULL;
UPDATE post_comments SET version = 0 WHERE version IS NULL;
UPDATE post_likes SET version = 0 WHERE version IS NULL;
UPDATE posts SET version = 0 WHERE version IS NULL;
UPDATE recipe_ingredients SET version = 0 WHERE version IS NULL;
UPDATE recipes SET version = 0 WHERE version IS NULL;
UPDATE routine SET version = 0 WHERE version IS NULL;
UPDATE stories SET version = 0 WHERE version IS NULL;
UPDATE user_follows SET version = 0 WHERE version IS NULL;
UPDATE water_logs SET version = 0 WHERE version IS NULL;
