CREATE TABLE todo (
  id SERIAL PRIMARY KEY,
  description TEXT,
  importance TEXT
);

INSERT INTO todo (id, description, importance) VALUES (1, 'test_description_1', 'high');