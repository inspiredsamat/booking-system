INSERT INTO users (id, full_name, email, created_at)
VALUES (1, 'Test Owner', 'owner@test.com', CURRENT_TIMESTAMP);

INSERT INTO units (owner_id, title, description, cost_per_day, type, number_of_rooms, floor, created_at)
VALUES
    (1, 'Sunny Apartment', 'Bright and cozy.', 120.00, 'FLAT', 2, 3, CURRENT_TIMESTAMP),
    (1, 'Cozy Home', 'Quiet residential area.', 150.00, 'HOME', 3, 1, CURRENT_TIMESTAMP),
    (1, 'Luxury Suite', 'Premium city center.', 300.00, 'APARTMENTS', 4, 10, CURRENT_TIMESTAMP),
    (1, 'Budget Flat', 'Affordable option.', 80.00, 'FLAT', 1, 2, CURRENT_TIMESTAMP),
    (1, 'Rooftop Flat', 'Amazing view.', 220.00, 'FLAT', 2, 12, CURRENT_TIMESTAMP),
    (1, 'Modern Loft', 'Industrial design.', 190.00, 'APARTMENTS', 2, 5, CURRENT_TIMESTAMP),
    (1, 'Family Home', 'Large backyard.', 160.00, 'HOME', 4, 1, CURRENT_TIMESTAMP),
    (1, 'Studio Flat', 'Minimalist space.', 100.00, 'FLAT', 1, 2, CURRENT_TIMESTAMP),
    (1, 'Central Flat', 'Downtown location.', 180.00, 'FLAT', 2, 6, CURRENT_TIMESTAMP),
    (1, 'Eco Home', 'Sustainable house.', 140.00, 'HOME', 3, 1, CURRENT_TIMESTAMP);

INSERT INTO events (entity_type, entity_id, event_type, payload, created_at) VALUES
  ('UNIT', 1, 'CREATED', '{"title": "Sunny Apartment", "description": "Bright and cozy."}', CURRENT_TIMESTAMP),
  ('UNIT', 2, 'CREATED', '{"title": "Cozy Home", "description": "Quiet residential area."}', CURRENT_TIMESTAMP),
  ('UNIT', 3, 'CREATED', '{"title": "Luxury Suite", "description": "Premium city center."}', CURRENT_TIMESTAMP),
  ('UNIT', 4, 'CREATED', '{"title": "Budget Flat", "description": "Affordable option."}', CURRENT_TIMESTAMP),
  ('UNIT', 5, 'CREATED', '{"title": "Rooftop Flat", "description": "Amazing view."}', CURRENT_TIMESTAMP),
  ('UNIT', 6, 'CREATED', '{"title": "Modern Loft", "description": "Industrial design."}', CURRENT_TIMESTAMP),
  ('UNIT', 7, 'CREATED', '{"title": "Family Home", "description": "Large backyard."}', CURRENT_TIMESTAMP),
  ('UNIT', 8, 'CREATED', '{"title": "Studio Flat", "description": "Minimalist space."}', CURRENT_TIMESTAMP),
  ('UNIT', 9, 'CREATED', '{"title": "Central Flat", "description": "Downtown location."}', CURRENT_TIMESTAMP),
  ('UNIT', 10, 'CREATED', '{"title": "Eco Home", "description": "Sustainable house."}', CURRENT_TIMESTAMP);