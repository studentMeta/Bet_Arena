-- ============================================================================
-- Données de développement BetArena
-- Mot de passe de tous les comptes : Password1!
-- ============================================================================

-------------------------------------------------------------------------------
-- Utilisateurs
-- Les soldes reflètent déjà les paris insérés plus bas (débits + gains).
-------------------------------------------------------------------------------
INSERT INTO users (username, email, password, role, balance) VALUES
('Bookmaker', 'bookmaker@betarena.local', '$2a$12$FhiiXpFt2ywXcpB/0q2RtOb.AHFXLECtbEA41NryBhE1jGl5So8xi', 'BOOKMAKER',    0.00),
('Sacha',     'sacha@betarena.local',     '$2a$12$FhiiXpFt2ywXcpB/0q2RtOb.AHFXLECtbEA41NryBhE1jGl5So8xi', 'BETTOR',     950.00),
('Timothy',  'timothy@betarena.local',   '$2a$12$FhiiXpFt2ywXcpB/0q2RtOb.AHFXLECtbEA41NryBhE1jGl5So8xi', 'BETTOR',     800.00),
('Jeremy',   'jeremy@betarena.local',    '$2a$12$FhiiXpFt2ywXcpB/0q2RtOb.AHFXLECtbEA41NryBhE1jGl5So8xi', 'BETTOR',     850.00),
('Gauthier', 'gauthier@betarena.local',  '$2a$12$FhiiXpFt2ywXcpB/0q2RtOb.AHFXLECtbEA41NryBhE1jGl5So8xi', 'BETTOR',    1000.00),
('Maxime',   'maxime@betarena.local',    '$2a$12$FhiiXpFt2ywXcpB/0q2RtOb.AHFXLECtbEA41NryBhE1jGl5So8xi', 'BETTOR',    9825.00),
('Alice',    'alice@betarena.local',     '$2a$12$FhiiXpFt2ywXcpB/0q2RtOb.AHFXLECtbEA41NryBhE1jGl5So8xi', 'BETTOR',    5240.00),
('Bob',      'bob@betarena.local',       '$2a$12$FhiiXpFt2ywXcpB/0q2RtOb.AHFXLECtbEA41NryBhE1jGl5So8xi', 'BETTOR',    3980.00),
('Charlie',  'charlie@betarena.local',   '$2a$12$FhiiXpFt2ywXcpB/0q2RtOb.AHFXLECtbEA41NryBhE1jGl5So8xi', 'BETTOR',    3210.00);

-------------------------------------------------------------------------------
-- Événement 1 : INTERNE - ouvert
-------------------------------------------------------------------------------
INSERT INTO events (title, description, category, close_at, status, created_by)
VALUES (
    'Le Groupe 4 réussira-t-il son examen d''architecture web ?',
    'Le verdict des évaluations d''architecture logicielle tombe en juin. Mention, passage de justesse ou catastrophe ?',
    'INTERNE',
    NOW() + INTERVAL '20 days',
    'OPEN',
    (SELECT id FROM users WHERE username = 'Bookmaker')
);

INSERT INTO outcomes (event_id, label, odds) VALUES
((SELECT id FROM events WHERE title = 'Le Groupe 4 réussira-t-il son examen d''architecture web ?'), 'Oui, avec mention',     2.10),
((SELECT id FROM events WHERE title = 'Le Groupe 4 réussira-t-il son examen d''architecture web ?'), 'Oui, de justesse',      1.50),
((SELECT id FROM events WHERE title = 'Le Groupe 4 réussira-t-il son examen d''architecture web ?'), 'Non, partie remise',    6.00);

-------------------------------------------------------------------------------
-- Événement 2 : INTERNE - ouvert
-------------------------------------------------------------------------------
INSERT INTO events (title, description, category, close_at, status, created_by)
VALUES (
    'Gauthier terminera-t-il son cursus avant 2030 ?',
    'La grande question existentielle du baccalauréat. Les paris sont ouverts.',
    'INTERNE',
    NOW() + INTERVAL '60 days',
    'OPEN',
    (SELECT id FROM users WHERE username = 'Bookmaker')
);

INSERT INTO outcomes (event_id, label, odds) VALUES
((SELECT id FROM events WHERE title = 'Gauthier terminera-t-il son cursus avant 2030 ?'), 'Oui, sans problème', 1.80),
((SELECT id FROM events WHERE title = 'Gauthier terminera-t-il son cursus avant 2030 ?'), 'Oui, in extremis',   2.20),
((SELECT id FROM events WHERE title = 'Gauthier terminera-t-il son cursus avant 2030 ?'), 'Non',               4.50);

-------------------------------------------------------------------------------
-- Événement 3 : INTERNE - ouvert
-------------------------------------------------------------------------------
INSERT INTO events (title, description, category, close_at, status, created_by)
VALUES (
    'Qui rendra son projet en dernier ?',
    'Tradition oblige : il y a toujours un membre du groupe qui pousse son dernier commit à 23h59.',
    'INTERNE',
    NOW() + INTERVAL '10 days',
    'OPEN',
    (SELECT id FROM users WHERE username = 'Bookmaker')
);

INSERT INTO outcomes (event_id, label, odds) VALUES
((SELECT id FROM events WHERE title = 'Qui rendra son projet en dernier ?'), 'Sacha',    2.50),
((SELECT id FROM events WHERE title = 'Qui rendra son projet en dernier ?'), 'Timothy',  2.80),
((SELECT id FROM events WHERE title = 'Qui rendra son projet en dernier ?'), 'Jérémy',   3.00),
((SELECT id FROM events WHERE title = 'Qui rendra son projet en dernier ?'), 'Gauthier', 1.90);

-------------------------------------------------------------------------------
-- Événement 4 : SPORT - ouvert
-------------------------------------------------------------------------------
INSERT INTO events (title, description, category, close_at, status, created_by)
VALUES (
    'Vainqueur du Championnat de Belgique 2026',
    'Quelle équipe remportera le titre cette saison ?',
    'SPORT',
    NOW() + INTERVAL '30 days',
    'OPEN',
    (SELECT id FROM users WHERE username = 'Bookmaker')
);

INSERT INTO outcomes (event_id, label, odds) VALUES
((SELECT id FROM events WHERE title = 'Vainqueur du Championnat de Belgique 2026'), 'Club Bruges', 1.75),
((SELECT id FROM events WHERE title = 'Vainqueur du Championnat de Belgique 2026'), 'Union SG',    2.40),
((SELECT id FROM events WHERE title = 'Vainqueur du Championnat de Belgique 2026'), 'Anderlecht',  3.10);

-------------------------------------------------------------------------------
-- Événement 5 : SPORT - ouvert
-------------------------------------------------------------------------------
INSERT INTO events (title, description, category, close_at, status, created_by)
VALUES (
    'Meilleur buteur Pro League 2026',
    'Qui terminera meilleur buteur du championnat ?',
    'SPORT',
    NOW() + INTERVAL '45 days',
    'OPEN',
    (SELECT id FROM users WHERE username = 'Bookmaker')
);

INSERT INTO outcomes (event_id, label, odds) VALUES
((SELECT id FROM events WHERE title = 'Meilleur buteur Pro League 2026'), 'Jutgla',  2.10),
((SELECT id FROM events WHERE title = 'Meilleur buteur Pro League 2026'), 'Vanzeir', 3.20),
((SELECT id FROM events WHERE title = 'Meilleur buteur Pro League 2026'), 'Undav',   2.80),
((SELECT id FROM events WHERE title = 'Meilleur buteur Pro League 2026'), 'Autre',   5.00);

-------------------------------------------------------------------------------
-- Événement 6 : CULTURE - ouvert
-------------------------------------------------------------------------------
INSERT INTO events (title, description, category, close_at, status, created_by)
VALUES (
    'Quel artiste belge sera en tête des charts cet été ?',
    'La bataille de l''été s''annonce serrée sur les ondes.',
    'CULTURE',
    NOW() + INTERVAL '50 days',
    'OPEN',
    (SELECT id FROM users WHERE username = 'Bookmaker')
);

INSERT INTO outcomes (event_id, label, odds) VALUES
((SELECT id FROM events WHERE title = 'Quel artiste belge sera en tête des charts cet été ?'), 'Angèle',                2.30),
((SELECT id FROM events WHERE title = 'Quel artiste belge sera en tête des charts cet été ?'), 'Stromae',               2.70),
((SELECT id FROM events WHERE title = 'Quel artiste belge sera en tête des charts cet été ?'), 'Lous and the Yakuza',   4.00),
((SELECT id FROM events WHERE title = 'Quel artiste belge sera en tête des charts cet été ?'), 'Un parfait inconnu',    5.50);

-------------------------------------------------------------------------------
-- Événement 7 : POLITIQUE - ouvert
-------------------------------------------------------------------------------
INSERT INTO events (title, description, category, close_at, status, created_by)
VALUES (
    'Un nouveau gouvernement fédéral sera-t-il formé avant fin 2026 ?',
    'Le sport national belge : la formation d''un gouvernement.',
    'POLITIQUE',
    NOW() + INTERVAL '90 days',
    'OPEN',
    (SELECT id FROM users WHERE username = 'Bookmaker')
);

INSERT INTO outcomes (event_id, label, odds) VALUES
((SELECT id FROM events WHERE title = 'Un nouveau gouvernement fédéral sera-t-il formé avant fin 2026 ?'), 'Oui', 1.65),
((SELECT id FROM events WHERE title = 'Un nouveau gouvernement fédéral sera-t-il formé avant fin 2026 ?'), 'Non', 2.20);

-------------------------------------------------------------------------------
-- Événement 8 : SPORT - fermé (en attente de résolution)
-------------------------------------------------------------------------------
INSERT INTO events (title, description, category, close_at, status, created_by)
VALUES (
    'Vainqueur Coupe de Belgique 2026',
    'Qui soulèvera le trophée ?',
    'SPORT',
    NOW() - INTERVAL '2 days',
    'CLOSED',
    (SELECT id FROM users WHERE username = 'Bookmaker')
);

INSERT INTO outcomes (event_id, label, odds) VALUES
((SELECT id FROM events WHERE title = 'Vainqueur Coupe de Belgique 2026'), 'Gand',        2.50),
((SELECT id FROM events WHERE title = 'Vainqueur Coupe de Belgique 2026'), 'Club Bruges', 1.90),
((SELECT id FROM events WHERE title = 'Vainqueur Coupe de Belgique 2026'), 'Genk',        3.40);

-------------------------------------------------------------------------------
-- Événement 9 : INTERNE - résolu
-------------------------------------------------------------------------------
INSERT INTO events (title, description, category, close_at, status, created_by, resolved_at)
VALUES (
    'Le Groupe 4 a-t-il rendu le projet dans les temps ?',
    'Échéance de remise fixée au 22 février 2026. Mission accomplie !',
    'INTERNE',
    NOW() - INTERVAL '12 days',
    'RESOLVED',
    (SELECT id FROM users WHERE username = 'Bookmaker'),
    NOW() - INTERVAL '10 days'
);

INSERT INTO outcomes (event_id, label, odds, is_winner) VALUES
((SELECT id FROM events WHERE title = 'Le Groupe 4 a-t-il rendu le projet dans les temps ?'), 'Oui, le 22 février', 1.25, TRUE),
((SELECT id FROM events WHERE title = 'Le Groupe 4 a-t-il rendu le projet dans les temps ?'), 'Non, en retard',     3.80, FALSE);

-------------------------------------------------------------------------------
-- Paris
-- potential_gain = amount * odds de l'issue choisie
-------------------------------------------------------------------------------

-- Paris résolus sur l'événement 9
INSERT INTO bets (user_id, outcome_id, amount, potential_gain, status, placed_at) VALUES
(
    (SELECT id FROM users WHERE username = 'Sacha'),
    (SELECT id FROM outcomes WHERE label = 'Oui, le 22 février'
        AND event_id = (SELECT id FROM events WHERE title = 'Le Groupe 4 a-t-il rendu le projet dans les temps ?')),
    200.00, 250.00, 'WON', NOW() - INTERVAL '15 days'
),
(
    (SELECT id FROM users WHERE username = 'Maxime'),
    (SELECT id FROM outcomes WHERE label = 'Oui, le 22 février'
        AND event_id = (SELECT id FROM events WHERE title = 'Le Groupe 4 a-t-il rendu le projet dans les temps ?')),
    500.00, 625.00, 'WON', NOW() - INTERVAL '14 days'
),
(
    (SELECT id FROM users WHERE username = 'Timothy'),
    (SELECT id FROM outcomes WHERE label = 'Non, en retard'
        AND event_id = (SELECT id FROM events WHERE title = 'Le Groupe 4 a-t-il rendu le projet dans les temps ?')),
    80.00, 304.00, 'LOST', NOW() - INTERVAL '14 days'
);

-- Paris en cours sur des événements ouverts
INSERT INTO bets (user_id, outcome_id, amount, potential_gain, status, placed_at) VALUES
(
    (SELECT id FROM users WHERE username = 'Sacha'),
    (SELECT id FROM outcomes WHERE label = 'Oui, avec mention'
        AND event_id = (SELECT id FROM events WHERE title = 'Le Groupe 4 réussira-t-il son examen d''architecture web ?')),
    100.00, 210.00, 'PENDING', NOW() - INTERVAL '3 days'
),
(
    (SELECT id FROM users WHERE username = 'Timothy'),
    (SELECT id FROM outcomes WHERE label = 'Angèle'
        AND event_id = (SELECT id FROM events WHERE title = 'Quel artiste belge sera en tête des charts cet été ?')),
    120.00, 276.00, 'PENDING', NOW() - INTERVAL '2 days'
),
(
    (SELECT id FROM users WHERE username = 'Jeremy'),
    (SELECT id FROM outcomes WHERE label = 'Club Bruges'
        AND event_id = (SELECT id FROM events WHERE title = 'Vainqueur du Championnat de Belgique 2026')),
    150.00, 262.50, 'PENDING', NOW() - INTERVAL '1 day'
),
(
    (SELECT id FROM users WHERE username = 'Maxime'),
    (SELECT id FROM outcomes WHERE label = 'Oui, sans problème'
        AND event_id = (SELECT id FROM events WHERE title = 'Gauthier terminera-t-il son cursus avant 2030 ?')),
    300.00, 540.00, 'PENDING', NOW() - INTERVAL '1 day'
);
