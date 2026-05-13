-- Presence/Attendance Table Migration for EduCore
-- This script adds support for tracking student attendance by class

USE educore;

-- Create the presence table if it doesn't exist
CREATE TABLE IF NOT EXISTS presence (
    id INT PRIMARY KEY AUTO_INCREMENT,
    etudiant_id INT NOT NULL,
    cours_id INT NOT NULL,
    date_presence DATE NOT NULL,
    est_present BOOLEAN DEFAULT TRUE,
    notes TEXT,
    date_enregistrement DATETIME DEFAULT NOW(),
    UNIQUE KEY unique_presence (etudiant_id, cours_id, date_presence),
    FOREIGN KEY (etudiant_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    FOREIGN KEY (cours_id) REFERENCES cours(id) ON DELETE CASCADE,
    INDEX idx_cours_date (cours_id, date_presence),
    INDEX idx_etudiant_date (etudiant_id, date_presence)
);

-- Optional: Create a view for easy admin queries
CREATE OR REPLACE VIEW v_presence_stats AS
SELECT
    c.id as cours_id,
    c.titre as cours_titre,
    u.id as etudiant_id,
    u.nom,
    u.prenom,
    COUNT(p.id) as total_seances,
    SUM(CASE WHEN p.est_present = TRUE THEN 1 ELSE 0 END) as presences,
    SUM(CASE WHEN p.est_present = FALSE THEN 1 ELSE 0 END) as absences,
    ROUND(SUM(CASE WHEN p.est_present = TRUE THEN 1 ELSE 0 END) / COUNT(p.id) * 100, 2) as taux_presence
FROM cours c
LEFT JOIN presence p ON c.id = p.cours_id
LEFT JOIN utilisateurs u ON p.etudiant_id = u.id
GROUP BY c.id, u.id
ORDER BY c.titre, u.nom;

