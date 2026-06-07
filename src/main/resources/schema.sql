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
