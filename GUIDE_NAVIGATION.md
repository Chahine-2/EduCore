## 🎯 GUIDE - Système de Navigation EduCore

### 📌 Architecture de Navigation

```
ACCUEIL (Accueil.fxml)
    ↓
    ├─→ Sélectionner Catégorie (Informatique/Mécanique/Électrique)
    ├─→ Sélectionner Niveau (Débutant/Intermédiaire/Avancé)
    └─→ Cliquer "Voir mes cours"
         ↓
       ESPACE ÉTUDIANT (Etudiant.fxml)
         ├─→ Voir les cours filtrés
         ├─→ Consulter les chapitres
         └─→ Mode lecture seule
```

---

### 🔄 Flux de Navigation

#### **1️⃣ Écran d'Accueil** (`Accueil.fxml`)
- **Contrôleur:** `AccueilController.java`
- **Fonction:** Point d'entrée de l'application
- **Sélections:**
  - 🏷️ Catégorie: Informatique, Mécanique, Électrique
  - 📊 Niveau: Débutant, Intermédiaire, Avancé
- **Action:** Transmet les choix à `EtudiantController`

#### **2️⃣ Espace Étudiant** (`Etudiant.fxml`)
- **Contrôleur:** `EtudiantController.java`
- **Fonction:** Consultation des cours
- **Reçoit:**
  - `categorieFiltre` (statique)
  - `niveauFiltre` (statique)
- **Affichage:** Cours pré-filtrés selon les sélections d'Accueil
- **Actions Possibles:**
  - 🔍 Rechercher par mot clé
  - 🔄 Filtrer par niveau/catégorie
  - 📖 Voir les détails et chapitres
  - 🔄 Reset les filtres

#### **3️⃣ Interface Administrateur** (`GestionCours.fxml`)
- **Contrôleur:** `GestionCoursController.java`
- **Fonction:** Gestion complète des cours
- **Accès Direct:** En modifiant `MainFx.java`

#### **4️⃣ Détails Cours** (`DetailsCours.fxml`)
- **Contrôleur:** `DetailsCoursController.java`
- **Fonction:** Gérer les chapitres d'un cours
- **Navigation:** Accessible depuis GestionCours ou Espace Étudiant

---

### 🚀 Comment Démarrer l'Application

#### **Mode 1: Via Accueil (RECOMMANDÉ)**
```bash
# Les filtres sont appliqués automatiquement
# L'application démarre sur Accueil.fxml
mvn javafx:run

# Ou depuis votre IDE:
# Run → MainFx
```

#### **Mode 2: Accès Administrateur (Gestion Directe)**
Modifier `MainFx.java`:
```java
// Ligne 20:
FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionCours.fxml"));
```

#### **Mode 3: Accès Direct Étudiant (Sans Filtre)**
Modifier `MainFx.java`:
```java
// Ligne 20:
FXMLLoader loader = new FXMLLoader(getClass().getResource("/Etudiant.fxml"));
```

---

### 📦 Variables Statiques (Communication entre Contrôleurs)

Le système utilise deux variables statiques pour communiquer entre `AccueilController` et `EtudiantController`:

```java
// Dans EtudiantController.java
public static String categorieFiltre = null;
public static String niveauFiltre    = null;
```

**Flux:**
1. L'utilisateur sélectionne sur Accueil
2. `AccueilController` définit les valeurs statiques
3. L'application navigue vers `Etudiant.fxml`
4. `EtudiantController.initialize()` lit les valeurs statiques
5. Les filtres sont appliqués automatiquement

---

### 🎨 Styles des Écrans

| Écran | Couleur Principale | Style |
|-------|-------------------|-------|
| Accueil | Bleu `#1a5276` | Modern avec emojis |
| Étudiant | Bleu `#1a5276` | Split-pane avec recherche |
| Gestion | Gris `#2c3e50` | Split-pane avec formulaire |
| Détails | Dark `#2c3e50` | Tableau des chapitres |

---

### 🔐 Séquence de Filtre

```
initialize() du EtudiantController
    ↓
    ├─ Remplir ComboBox avec options
    ├─ Vérifier si categorieFiltre != null
    │  ├─ OUI → Appliquer à ComboBox catégorie
    │  └─ NON → "Tous"
    ├─ Vérifier si niveauFiltre != null
    │  ├─ OUI → Appliquer à ComboBox niveau
    │  └─ NON → "Tous"
    ├─ Charger tous les cours de la BD
    └─ Si filtres trouvés → appliquer appliquerFiltres()
         ↓
       TableView affiche cours filtrés
```

---

### ✅ Vérification et Tests

**Avant de lancer:**
- ✅ Tous les fichiers .fxml existent: `Accueil.fxml`, `Etudiant.fxml`, `GestionCours.fxml`, `DetailsCours.fxml`
- ✅ Tous les contrôleurs existent: `AccueilController.java`, `EtudiantController.java`, `GestionCoursController.java`, `DetailsCoursController.java`
- ✅ Les variables statiques sont déclarées dans `EtudiantController`
- ✅ `MainFx.java` charge `/Accueil.fxml` par défaut

**Lors du lancement:**
1. L'écran Accueil s'affiche
2. Sélectionnez une catégorie et un niveau
3. Cliquez "Voir mes cours"
4. L'Espace Étudiant s'affiche avec les filtres pré-appliqués

---

### 🛠 Dépannage

**Erreur: Cannot find symbol `AccueilController`**
- Vérifier que `AccueilController.java` existe et a le bon package

**Erreur: Cannot find resource `/Accueil.fxml`**
- Vérifier que `Accueil.fxml` est dans `src/main/resources/`
- Recharger le projet Maven

**Les filtres ne s'appliquent pas**
- Vérifier que `categorieFiltre` et `niveauFiltre` sont static
- Vérifier que `AccueilController` les assigne avant navigation
- Vérifier au démarrage: `System.out.println("Filtre: " + categorieFiltre);`

---

### 📋 Fichiers Modifiés/Créés

| Fichier | Action | Détails |
|---------|--------|---------|
| `Accueil.fxml` | ✅ CRÉÉ | Interface d'accueil |
| `AccueilController.java` | ✅ EXISTANT | Gestion sélections |
| `EtudiantController.java` | ✅ MODIFIÉ | Ajout variables static + filtres |
| `MainFx.java` | ✅ MODIFIÉ | Charge `/Accueil.fxml` |
| `Etudiant.fxml` | ✅ EXISTANT | Interface étudiant |
| `GestionCours.fxml` | ✅ EXISTANT | Interface admin |
| `DetailsCours.fxml` | ✅ EXISTANT | Détails cours |

---

### 🎯 Prochaines Améliorations Possibles

1. **Ajouter un bouton Admin** sur Accueil pour accès directs GestionCours
2. **Bouton Retour** vers Accueil depuis Étudiant
3. **Authentification** (login/password)
4. **Historique** de navigation en pile (stack)
5. **Thème sombre/clair** configurable

---

**Dernière mise à jour:** 2 mai 2026  
**Version:** 1.1.0  
**Status:** ✅ Prêt pour production

