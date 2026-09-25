-- Seed data for Skafferiet

-- Recept
INSERT INTO recipes (id, title, description, instructions, servings, prep_time_minutes, cook_time_minutes, created_at, updated_at)
VALUES 
(1, 'Krämig Pasta Carbonara', 'Klassisk italiensk pasta med guanciale, pecorino och ägg.', 
'1. Koka pastan al dente.
2. Stek fläsket krispigt.
3. Vispa äggulor och riven pecorino.
4. Blanda pastan med fläsket och rör ner äggblandningen med lite pastavatten.', 4, 10, 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Klassisk Chili con Carne', 'Fyllig och mustig köttfärsgryta med bönor och chili.', 
'1. Hacka och fräs lök, vitlök och nötfärs.
2. Tillsätt spiskummin, paprikapulver och chili.
3. Häll på krossade tomater och buljong, låt sjuda i 30 min.
4. Rör ner svarta bönor och låt bli varmt.', 4, 15, 35, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Ingredienser
INSERT INTO ingredients (id, name, category) VALUES 
(1, 'Spaghetti', 'PANTRY'),
(2, 'Guanciale', 'MEAT'),
(3, 'Ägg', 'DAIRY'),
(4, 'Pecorino Romano', 'DAIRY'),
(5, 'Nötfärs', 'MEAT'),
(6, 'Krossade tomater', 'PANTRY'),
(7, 'Svarta bönor', 'PANTRY'),
(8, 'Gul lök', 'PRODUCE'),
(9, 'Vitlök', 'PRODUCE'),
(10, 'Spiskummin', 'SPICES');

-- Recept-ingredienser för Pasta Carbonara (Recept 1)
INSERT INTO recipe_ingredients (id, recipe_id, ingredient_id, amount, unit, notes) VALUES 
(1, 1, 1, 400.0, 'g', 'al dente'),
(2, 1, 2, 200.0, 'g', 'tärnad'),
(3, 1, 3, 4.0, 'st', 'äggulor'),
(4, 1, 4, 100.0, 'g', 'finriven');

-- Recept-ingredienser för Chili con Carne (Recept 2)
INSERT INTO recipe_ingredients (id, recipe_id, ingredient_id, amount, unit, notes) VALUES 
(5, 2, 5, 500.0, 'g', NULL),
(6, 2, 6, 800.0, 'g', '2 burkar'),
(7, 2, 7, 400.0, 'g', 'avsköljda'),
(8, 2, 8, 1.0, 'st', 'finhackad'),
(9, 2, 9, 2.0, 'klyftor', 'pressade'),
(10, 2, 10, 1.0, 'msk', NULL);

-- Skafferi (Pantry Items)
INSERT INTO pantry_items (id, ingredient_id, quantity, unit, quantity_level, in_stock, updated_at) VALUES 
(1, 1, 500.0, 'g', 'FULL', true, CURRENT_TIMESTAMP),
(2, 2, 0.0, 'g', 'EMPTY', false, CURRENT_TIMESTAMP),
(3, 3, 6.0, 'st', 'HALF', true, CURRENT_TIMESTAMP),
(4, 4, 0.0, 'g', 'EMPTY', false, CURRENT_TIMESTAMP),
(5, 8, 3.0, 'st', 'FULL', true, CURRENT_TIMESTAMP),
(6, 10, 1.0, 'burk', 'FULL', true, CURRENT_TIMESTAMP);

-- Synchronize sequences for PostgreSQL identity columns
SELECT setval(pg_get_serial_sequence('recipes', 'id'), COALESCE(MAX(id), 1)) FROM recipes;
SELECT setval(pg_get_serial_sequence('ingredients', 'id'), COALESCE(MAX(id), 1)) FROM ingredients;
SELECT setval(pg_get_serial_sequence('recipe_ingredients', 'id'), COALESCE(MAX(id), 1)) FROM recipe_ingredients;
SELECT setval(pg_get_serial_sequence('pantry_items', 'id'), COALESCE(MAX(id), 1)) FROM pantry_items;
