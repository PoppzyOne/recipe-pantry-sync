-- Recept
INSERT INTO recipes (id, title, description, instructions, servings, prep_time_minutes, cook_time_minutes, created_at, updated_at)
VALUES (1, 'Krämig Pasta Carbonara', 'Klassisk italiensk pasta med guanciale, pecorino och ägg.', '1. Koka pastan al dente.
2. Stek fläsket krispigt.
3. Vispa äggulor och riven pecorino.
4. Blanda pastan med fläsket och rör ner äggblandningen med lite pastavatten.', 4, 10, 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO recipes (id, title, description, instructions, servings, prep_time_minutes, cook_time_minutes, created_at, updated_at)
VALUES (2, 'Klassisk Chili con Carne', 'Fyllig och mustig köttfärsgryta med bönor och chili.', '1. Hacka och fräs lök, vitlök och nötfärs.
2. Tillsätt spiskummin, paprikapulver och chili.
3. Häll på krossade tomater och buljong, låt sjuda i 30 min.
4. Rör ner svarta bönor och låt bli varmt.', 4, 15, 35, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Ingredienser
INSERT INTO ingredients (id, name, category) VALUES (1, 'Spaghetti', 'PANTRY');
INSERT INTO ingredients (id, name, category) VALUES (2, 'Guanciale', 'MEAT');
INSERT INTO ingredients (id, name, category) VALUES (3, 'Ägg', 'DAIRY');
INSERT INTO ingredients (id, name, category) VALUES (4, 'Pecorino Romano', 'DAIRY');
INSERT INTO ingredients (id, name, category) VALUES (5, 'Nötfärs', 'MEAT');
INSERT INTO ingredients (id, name, category) VALUES (6, 'Krossade tomater', 'PANTRY');
INSERT INTO ingredients (id, name, category) VALUES (7, 'Svarta bönor', 'PANTRY');
INSERT INTO ingredients (id, name, category) VALUES (8, 'Gul lök', 'PRODUCE');
INSERT INTO ingredients (id, name, category) VALUES (9, 'Vitlök', 'PRODUCE');
INSERT INTO ingredients (id, name, category) VALUES (10, 'Spiskummin', 'SPICES');

-- Recept-ingredienser för Pasta Carbonara (Recept 1)
INSERT INTO recipe_ingredients (id, recipe_id, ingredient_id, amount, unit, notes) VALUES (1, 1, 1, 400.0, 'g', 'al dente');
INSERT INTO recipe_ingredients (id, recipe_id, ingredient_id, amount, unit, notes) VALUES (2, 1, 2, 200.0, 'g', 'tärnad');
INSERT INTO recipe_ingredients (id, recipe_id, ingredient_id, amount, unit, notes) VALUES (3, 1, 3, 4.0, 'st', 'äggulor');
INSERT INTO recipe_ingredients (id, recipe_id, ingredient_id, amount, unit, notes) VALUES (4, 1, 4, 100.0, 'g', 'finriven');

-- Recept-ingredienser för Chili con Carne (Recept 2)
INSERT INTO recipe_ingredients (id, recipe_id, ingredient_id, amount, unit, notes) VALUES (5, 2, 5, 500.0, 'g', null);
INSERT INTO recipe_ingredients (id, recipe_id, ingredient_id, amount, unit, notes) VALUES (6, 2, 6, 800.0, 'g', '2 burkar');
INSERT INTO recipe_ingredients (id, recipe_id, ingredient_id, amount, unit, notes) VALUES (7, 2, 7, 400.0, 'g', 'avsköljda');
INSERT INTO recipe_ingredients (id, recipe_id, ingredient_id, amount, unit, notes) VALUES (8, 2, 8, 1.0, 'st', 'finhackad');
INSERT INTO recipe_ingredients (id, recipe_id, ingredient_id, amount, unit, notes) VALUES (9, 2, 9, 2.0, 'klyftor', 'pressade');
INSERT INTO recipe_ingredients (id, recipe_id, ingredient_id, amount, unit, notes) VALUES (10, 2, 10, 1.0, 'msk', null);

-- Skafferi (Pantry Items)
INSERT INTO pantry_items (id, ingredient_id, quantity, unit, in_stock, updated_at) VALUES (1, 1, 500.0, 'g', true, CURRENT_TIMESTAMP);
INSERT INTO pantry_items (id, ingredient_id, quantity, unit, in_stock, updated_at) VALUES (2, 2, 0.0, 'g', false, CURRENT_TIMESTAMP);
INSERT INTO pantry_items (id, ingredient_id, quantity, unit, in_stock, updated_at) VALUES (3, 3, 6.0, 'st', true, CURRENT_TIMESTAMP);
INSERT INTO pantry_items (id, ingredient_id, quantity, unit, in_stock, updated_at) VALUES (4, 4, 0.0, 'g', false, CURRENT_TIMESTAMP);
INSERT INTO pantry_items (id, ingredient_id, quantity, unit, in_stock, updated_at) VALUES (5, 8, 3.0, 'st', true, CURRENT_TIMESTAMP);
INSERT INTO pantry_items (id, ingredient_id, quantity, unit, in_stock, updated_at) VALUES (6, 10, 1.0, 'burk', true, CURRENT_TIMESTAMP);

ALTER TABLE recipes ALTER COLUMN id RESTART WITH 100;
ALTER TABLE ingredients ALTER COLUMN id RESTART WITH 100;
ALTER TABLE recipe_ingredients ALTER COLUMN id RESTART WITH 100;
ALTER TABLE pantry_items ALTER COLUMN id RESTART WITH 100;