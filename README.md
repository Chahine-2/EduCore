# 📚 EDUCORE - Plateforme de Gestion de Cours

Une application **JavaFX** moderne et fonctionnelle pour gérer des cours et chapitres avec une base de données **MySQL**.

## 🎯 Fonctionnalités Principales

### 1. **Gestion des Cours** 📘
- ✅ **Ajouter** de nouveaux cours
- ✅ **Modifier** les cours existants
- ✅ **Supprimer** les cours (avec confirmation)
- ✅ **Visualiser** les détails complets des cours
- ✅ **Tableau interactif** affichant tous les cours

**Informations d'un cours :**
- Titre et Description
- Objectifs pédagogiques
- Niveau (Débutant, Intermédiaire, Avancé)
- Catégorie (Informatique, Mécanique, Électrique)
- Durée en heures
- Statut certifiant
- Dates de début et fin

### 2. **Gestion des Chapitres** 📖
- ✅ **Ajouter** des chapitres à un cours
- ✅ **Modifier** les chapitres existants
- ✅ **Supprimer** les chapitres
- ✅ **Visualiser** tous les chapitres d'un cours

**Informations d'un chapitre :**
- Titre et Description
- Ordre (numéro du chapitre)
- Durée en minutes
- Type de contenu (Vidéo, Texte, PDF, Quiz)
- URL du contenu
- Date de création

## 🏗️ Architecture du Projet

```
src/main/java/
├── controllers/
│   ├── GestionCoursController.java      # 🎛️ Contrôleur principal
│   └── DetailsCoursController.java      # 🎛️ Contrôleur des détails
├── models/                              # 📦 Modèles de données
│   ├── Cours.java
│   └── Chapitre.java
├── services/                            # 🔌 Logique métier (BD)
│   ├── ServiceCours.java
│   └── ServiceChapitre.java
├── interfaces/
│   └── IService.java                    # 🔧 Interface générique CRUD
└── utils/
    └── MyDataBase.java                  # 🗄️ Connexion MySQL

src/main/resources/
├── GestionCours.fxml                    # 🎨 Interface de gestion
└── DetailsCours.fxml                    # 🎨 Interface des détails

src/test/java/
└── MainFx.java                          # 🚀 Point d'entrée de l'app
```

## 🚀 Guide d'Utilisation

### **Interface Principale (GestionCours)**

1. **Ajouter un Cours**
   - Remplissez tous les champs du formulaire à droite
   - Cliquez sur le bouton "Ajouter"
   - Vous serez automatiquement redirigé vers l'écran de détails

2. **Modifier un Cours**
   - Sélectionnez un cours dans le tableau (à gauche)
   - Le formulaire se remplit automatiquement
   - Modifiez les informations souhaitées
   - Cliquez sur "Modifier"

3. **Supprimer un Cours**
   - Sélectionnez un cours dans le tableau
   - Cliquez sur "Supprimer"
   - Confirmez la suppression

4. **Voir les Détails**
   - Sélectionnez un cours dans le tableau
   - Cliquez sur "Details"
   - Vous accédez à l'écran de gestion des chapitres

### **Écran des Détails (DetailsCours)**

1. **Ajouter un Chapitre**
   - Cliquez sur "✚ Ajouter Chapitre"
   - Une fenêtre de dialogue s'ouvre
   - Remplissez les informations du chapitre
   - Cliquez OK

2. **Modifier un Chapitre**
   - Sélectionnez un chapitre dans le tableau
   - Cliquez sur "✎ Modifier Chapitre"
   - Modifiez les informations
   - Cliquez OK

3. **Supprimer un Chapitre**
   - Sélectionnez un chapitre
   - Cliquez sur "✕ Supprimer Chapitre"
   - Confirmez

4. **Retour**
   - Cliquez sur le bouton "← Retour" en haut à droite
   - Vous revenez à la gestion des cours

## 🛠️ Configuration Requise

### **Logiciels Nécessaires**
- ☕ **Java 17** ou supérieur
- 🗄️ **MySQL Server** (en local ou distant)
- 🏗️ **Maven** 3.8+
- 🎨 **JavaFX SDK 21**

### **Dépendances Maven**
```xml
<dependencies>
    <!-- JavaFX -->
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>21.0.1</version>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>21.0.1</version>
    </dependency>
    
    <!-- MySQL Connector -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.33</version>
    </dependency>
</dependencies>
```

## 📊 Schéma de Base de Données

### **Table `cours`**
```sql
CREATE TABLE cours (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(500) NOT NULL,
    description TEXT,
    objectifs TEXT,
    duree_heures INT DEFAULT 1,
    niveau VARCHAR(50),
    categorie VARCHAR(50),
    est_certifiant BOOLEAN DEFAULT FALSE,
    date_debut DATE,
    date_fin DATE
);
```

### **Table `chapitre`**
```sql
CREATE TABLE chapitre (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(500) NOT NULL,
    description TEXT,
    ordre INT NOT NULL,
    duree_minutes INT,
    type_contenu VARCHAR(50),
    url_contenu VARCHAR(500),
    date_creation DATE,
    cours_id INT NOT NULL,
    FOREIGN KEY (cours_id) REFERENCES cours(id) ON DELETE CASCADE
);
```

## 🔧 Installation & Démarrage

1. **Cloner ou télécharger le projet**
   ```bash
   git clone <url-du-repo>
   cd "EDUCORE - Copie"
   ```

2. **Configurer la base de données MySQL**
   - Créez une base de données `educore_db`
   - Exécutez les scripts SQL ci-dessus
   - Modifiez les paramètres de connexion dans `MyDataBase.java` si nécessaire

3. **Compiler le projet**
   ```bash
   mvn clean compile
   ```

4. **Lancer l'application**
   ```bash
   mvn javafx:run
   ```
   Ou exécutez `MainFx.java` directement depuis votre IDE

## 📝 Classes Principales

### **GestionCoursController**
Contrôle la gestion complète des cours :
- `ajouterCours()` - Crée un nouveau cours
- `modifierCours()` - Met à jour un cours sélectionné
- `supprimerCours()` - Supprime un cours avec confirmation
- `afficherDetails()` - Affiche les détails d'un cours
- `selectCoursInTable()` - Preremplit le formulaire au clic
- `clearForm()` - Réinitialise le formulaire

### **DetailsCoursController**
Gère l'affichage des détails et les chapitres :
- `ajouterChapitre()` - Ajoute un chapitre via dialog
- `modifierChapitre()` - Met à jour un chapitre sélectionné
- `supprimerChapitre()` - Supprime un chapitre
- `chargerChapitres()` - Charge tous les chapitres d'un cours
- `retour()` - Retour à l'écran de gestion

### **Modèles (Models)**
- `Cours.java` - Classe représentant un cours
- `Chapitre.java` - Classe représentant un chapitre

### **Services**
- `ServiceCours.java` - Opérations CRUD sur les cours
- `ServiceChapitre.java` - Opérations CRUD sur les chapitres

## 🎨 Interface Utilisateur

L'interface utilise un design moderne avec :
- 🎨 Couleurs professionnelles (#2c3e50, #27ae60, #e74c3c, etc.)
- 📱 Layout responsive avec SplitPane
- ⚡ Feedback utilisateur avec alertes
- 🖱️ Interaction intuitive


## 🐛 Débogage

En cas de problème :
1. Vérifiez la connexion MySQL dans `MyDataBase.java`
2. Consultez la console pour les messages d'erreur
3. Assurez que les fichiers FXML existent dans `src/main/resources/`
4. Vérifiez les fx:id dans les FXML correspondent aux @FXML du contrôleur

## 📞 Support

Pour toute question ou problème, consultez les fichiers d'erreur dans la sortie console.

---

✅ **Application complète et prête à l'emploi!** 🚀

