-- =====================================================================
--  Migration EduCore — Compatible MySQL 5.x / phpMyAdmin
--  Exécuter les requêtes UNE PAR UNE dans phpMyAdmin
-- =====================================================================

-- ÉTAPE 1 : Ajouter la colonne visible dans la table cours
-- (Si la colonne existe déjà, MySQL affichera une erreur 1060 — ignorez-la)
ALTER TABLE cours ADD COLUMN visible TINYINT(1) NOT NULL DEFAULT 1;

-- ÉTAPE 2 : Mettre visible = 1 pour tous les cours existants
UPDATE cours SET visible = 1;

-- ÉTAPE 3 : Ajouter la colonne visible dans chapitre si absente
-- (Si elle existe déjà, ignorez l'erreur 1060)
ALTER TABLE chapitre ADD COLUMN visible TINYINT(1) NOT NULL DEFAULT 1;

-- ÉTAPE 4 : Mettre visible = 1 pour tous les chapitres existants
UPDATE chapitre SET visible = 1;

-- ÉTAPE 5 : Vérification finale
DESCRIBE cours;
DESCRIBE chapitre;
