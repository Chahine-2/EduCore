CREATE DATABASE IF NOT EXISTS educore;
USE educore;

CREATE TABLE IF NOT EXISTS evaluation (
    id INT PRIMARY KEY AUTO_INCREMENT,
    titre VARCHAR(150) NOT NULL,
    description TEXT,
    type ENUM('qcm','examen','devoir','projet','tp') DEFAULT 'qcm',
    duree_minutes INT,
    note_max FLOAT DEFAULT 20,
    note_passage FLOAT DEFAULT 10,
    nb_tentatives INT DEFAULT 1,
    ordre_aleatoire BOOLEAN DEFAULT FALSE,
    afficher_correc BOOLEAN DEFAULT TRUE,
    date_debut DATETIME,
    date_fin DATETIME,
    statut ENUM('brouillon','publie','ferme') DEFAULT 'brouillon',
    date_creation DATETIME DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS question (
    id INT PRIMARY KEY AUTO_INCREMENT,
    texte TEXT NOT NULL,
    type ENUM('qcm','vrai_faux','texte_libre','correspondance') DEFAULT 'qcm',
    points FLOAT DEFAULT 1,
    explication TEXT,
    image_url VARCHAR(255),
    ordre INT DEFAULT 0,
    evaluation_id INT NOT NULL,
    FOREIGN KEY (evaluation_id) REFERENCES evaluation(id)
);

CREATE TABLE IF NOT EXISTS reponse (
    id INT PRIMARY KEY AUTO_INCREMENT,
    texte TEXT NOT NULL,
    est_correct BOOLEAN DEFAULT FALSE,
    explication TEXT,
    ordre INT DEFAULT 0,
    question_id INT NOT NULL,
    FOREIGN KEY (question_id) REFERENCES question(id)
);

CREATE TABLE IF NOT EXISTS resultat (
    id INT PRIMARY KEY AUTO_INCREMENT,
    score FLOAT NOT NULL,
    score_pourcentage FLOAT,
    est_reussi BOOLEAN DEFAULT FALSE,
    temps_passe_min INT,
    tentative_num INT DEFAULT 1,
    date_passage DATETIME DEFAULT NOW(),
    evaluation_id INT NOT NULL,
    FOREIGN KEY (evaluation_id) REFERENCES evaluation(id)
);

CREATE TABLE IF NOT EXISTS reponse_etudiant (
    id INT PRIMARY KEY AUTO_INCREMENT,
    resultat_id INT NOT NULL,
    question_id INT NOT NULL,
    reponse_id INT,
    texte_libre TEXT,
    est_correct BOOLEAN DEFAULT FALSE,
    points_obtenus FLOAT DEFAULT 0,
    FOREIGN KEY (resultat_id) REFERENCES resultat(id),
    FOREIGN KEY (question_id) REFERENCES question(id),
    FOREIGN KEY (reponse_id) REFERENCES reponse(id)
);
