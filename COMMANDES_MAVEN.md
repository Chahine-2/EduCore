# 🛠️ COMMANDES MAVEN UTILES - EDUCORE

## 📋 Pré-requis
- Java 17+
- Maven 3.8+
- MySQL Server (en cours d'exécution)

---

## 🚀 Commandes Essentielles

### 1️⃣ Compiler le Projet
```bash
mvn clean compile
```
✅ Nettoie et compile le code source

### 2️⃣ Lancer l'Application
```bash
mvn javafx:run
```
✅ Lance directement l'application JavaFX

### 3️⃣ Construire un JAR
```bash
mvn clean package
```
✅ Crée un fichier JAR dans `target/`

### 4️⃣ Exécuter les Tests
```bash
mvn test
```
✅ Lance tous les tests unitaires

### 5️⃣ Nettoyer le Projet
```bash
mvn clean
```
✅ Supprime les fichiers compilés et générés

---

## 📦 Commandes Avancées

### Compiler et Lancer en une ligne
```bash
mvn clean compile javafx:run
```

### Compiler et Construire le JAR
```bash
mvn clean package
```

### Lancer avec des options Java
```bash
mvn javafx:run -Djavafx.args="--arg1 value1"
```

### Générer la Documentation (Javadoc)
```bash
mvn javadoc:javadoc
```

### Obtenir des informations sur le project
```bash
mvn help:describe
```

---

## 🐛 Dépannage

### Si Maven ne compile pas
```bash
# Nettoyer le cache Maven
mvn clean install -U

# Contourner les tests
mvn clean package -DskipTests
```

### Si l'application ne démarre pas
```bash
# Vérifiez les dépendances
mvn dependency:tree

# Réinstallez les dépendances
mvn clean install
```

### Si port 3306 (MySQL) n'est pas disponible
- Vérifiez que MySQL est en cours d'exécution
- Vérifiez les paramètres de connexion dans `MyDataBase.java`

---

## 📊 Affichage des Dépendances

### Lister toutes les dépendances
```bash
mvn dependency:tree
```

```
[INFO] tree.Application:edu-app:jar:1.0-SNAPSHOT
[INFO] +- org.openjfx:javafx-controls:jar:21.0.1:compile
[INFO] +- org.openjfx:javafx-fxml:jar:21.0.1:compile
[INFO] +- mysql:mysql-connector-java:jar:8.0.33:compile
```

---

## 🔧 Configuration POM.xml

### Dépendances Principales
```xml
<!-- JavaFX -->
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>21.0.1</version>
</dependency>

<!-- MySQL -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>
```

---

## 🎯 Workflow Complet de Développement

### Dev Loop
```bash
# 1. Compiler
mvn clean compile

# 2. Lancer l'app (vérifier visuellement)
mvn javafx:run

# 3. Après modifications
mvn clean compile javafx:run
```

### Avant de Pusher le Code
```bash
# Nettoyer
mvn clean

# Compiler
mvn compile

# Tester
mvn test

# Construire
mvn package
```

---

## 💡 Astuces

### Alias Maven Utiles (Windows PowerShell)
```powershell
# Ajouter à votre profil PowerShell
Set-Alias mvn "mvn.cmd"
```

### Alias Maven Utiles (Linux/Mac)
```bash
# Ajouter au ~/.bashrc ou ~/.zshrc
alias mclean="mvn clean"
alias mcompile="mvn clean compile"
alias mrun="mvn javafx:run"
alias mpackage="mvn clean package"
```

### Commande Rapide One-Liner
```bash
# Compile + Lance
mvn clean compile javafx:run
```

---

## 📝 Notes Importantes

1. **Java Version** - Le projet utilise Java 17 (spécifié dans pom.xml)
2. **JavaFX** - Version 21.0.1 (compatible Java 17+)
3. **MySQL** - Version 8.0.33 minimum recommandée
4. **Compilation** - Peut prendre 10-30 secondes la première fois

---

## 🐛 Erreurs Courantes et Solutions

| Erreur | Cause | Solution |
|--------|-------|----------|
| `mvn: command not found` | Maven non installé | Installez Maven ou ajoutez-le au PATH |
| `JAVA_HOME not set` | Java non configuré | Définissez JAVA_HOME vers votre installation Java |
| `Connection refused` | MySQL non actif | Démarrez MySQL Server |
| `Compilation error` | Dépendances manquantes | `mvn clean install` |
| `Class not found` | Fichiers FXML manquants | Vérifiez `src/main/resources/` |

---

## ✅ Vérificatif de Configuration

### Vérifiez Java
```bash
java -version
```
Devrait afficher: `java version "17"` ou supérieur

### Vérifiez Maven
```bash
mvn -version
```
Devrait afficher: `Apache Maven 3.8+`

### Vérifiez MySQL
```bash
mysql -u root -p -e "SELECT VERSION();"
```
Devrait afficher une version numérique

---

## 🚀 Résumé Rapide

| Objectif | Commande |
|----------|----------|
| Compiler | `mvn clean compile` |
| Lancer l'app | `mvn javafx:run` |
| Tout en un | `mvn clean compile javafx:run` |
| Construire JAR | `mvn clean package` |
| Tests | `mvn test` |
| Dépendances | `mvn dependency:tree` |

---

**Bonne compilation!** ✨

Besoin d'aide? Consultez: `README.md` et `GUIDE_RAPIDE.md`

