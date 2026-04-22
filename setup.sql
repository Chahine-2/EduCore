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

