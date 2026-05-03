# 🚀 Guide d'Améliorations Futures - EduCore

## Base de Données - Migration Requise

Pour que la visibilité soit persistante, vous devez exécuter ces migrations SQL:

### Ajouter colonne à la table `cours`:
```sql
ALTER TABLE cours ADD COLUMN visible BOOLEAN DEFAULT TRUE;
```

### Ajouter colonne à la table `chapitre`:
```sql
ALTER TABLE chapitre ADD COLUMN visible BOOLEAN DEFAULT TRUE;
```

## Services - Mise à Jour des Requêtes

### ServiceCours.java - Mettre à jour insert/update:
```java
public void add(Cours c) {
    String req = "INSERT INTO cours (titre, description, objectifs, duree_heures, niveau, categorie, est_certifiant, date_debut, date_fin, visible) VALUES (?,?,?,?,?,?,?,?,?,?)";
    // ... ajouter ps.setBoolean(10, c.isVisible());
}

public void update(Cours c) {
    String req = "UPDATE cours SET ..., visible=? WHERE id=?";
    // ... ajouter ps.setBoolean(9, c.isVisible());
}
```

### ServiceChapitre.java - Mettre à jour insert/update:
```java
public void add(Chapitre c) {
    String req = "INSERT INTO chapitre (..., visible) VALUES (...,?)";
    // ... ajouter ps.setBoolean(9, c.isVisible());
}

public void update(Chapitre c) {
    String req = "UPDATE chapitre SET ..., visible=? WHERE id=?";
    // ... ajouter ps.setBoolean(9, c.isVisible());
}
```

## Améliorations UI Optionnelles

### 1. Bulk Actions
- Ajouter des boutons pour rendre plusieurs cours/chapitres visibles/masqués à la fois
- Ajouter une sélection multiple dans les tableaux

### 2. Planning de Visibilité
- Ajouter une date de début/fin de visibilité pour les cours
- Masquer automatiquement les cours après une date limite

### 3. Notification des Étudiants
- Ajouter des notifications quand un cours devient visible
- Email notification quand un nouveau chapitre est ajouté

### 4. Analytics
- Voir quels étudiants ont accédé à quels chapitres
- Statistiques de temps passé par chapitre

### 5. Recherche Avancée
- Afficher aussi les cours "masqués" pour les enseignants
- Ajouter un filtre "Afficher les cours masqués" en gestion

## Checkpoints de Validation

- [ ] Compilé sans erreurs
- [ ] Tests avec des données réelles
- [ ] Vérifier que les cours masqués n'apparaissent pas côté étudiant
- [ ] Vérifier que des les chapitres masqués n'apparaissent pas dans LectureChapitre
- [ ] Tester la désélection des cours
- [ ] Tester la modification de visibilité
- [ ] Vérifier l'affichage du badge "Masqué" dans le sommaire
- [ ] Vérifier que le design sans cadre blanc est correct
- [ ] Vérifier que la TextArea dynamique fonctionne correctement

## Questions pour le Développeur

1. Voulez-vous permettre aux étudiants de voir s'il y a des chapitres masqués? (afficher le badge)
2. Faut-il une confirmation quand on rend un cours avec des étudiants masqué?
3. Devrait-on archiver les données au lieu de les supprimer?
4. Export de la liste des cours vis/mas pour un rapport?

