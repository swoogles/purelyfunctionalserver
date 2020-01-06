-- ALTER TABLE daily_quantized_exercises ADD COLUMN user_id text not null

CREATE TABLE daily_exercises (
                                 id SERIAL PRIMARY KEY,
                                 name TEXT,
                                 day DATE,
                                 count INT,
                                 user_id TEXT NOT NULL
);


