# 🎯 GUIDE RAPIDE - EDUCORE

## ⚡ 5 Minutes pour Maîtriser EDUCORE

---

## 🎬 Démarrage

1. Ouvrez votre IDE (IntelliJ, Eclipse, etc.)
2. Ouvrez le projet EDUCORE
3. Lancez `MainFx.java`
4. L'écran principal apparaît

---

## 📘 Écran 1 : Gestion des Cours

### 🖼️ Layout
```
┌─────────────────────────────────────────────────────┐
│              📘 Gestion des Cours EDUCORE            │
├──────────────────────────────────────────────────────┤
│  TABLEAU (Gauche)      │      FORMULAIRE (Droite)    │
│ ┌���─────────────────┐   │  ┌──────────────────────┐   │
│ │ ID │ Titre │...  │   │  │ Titre: ______         │   │
│ ├──────────────────┤   │  │ Description: ______   │   │
│ │ 1  │ Java  │...  │   │  │ Objectifs: _______    │   │
│ │ 2  │ Python│...  │   │  │                       │   │
│ │ 3  │ Web   │...  │   │  │ Niveau: [▼]           │   │
│ │    │       │     │   │  │ Catégorie: [▼]        │   │
│ └──────────────────┘   │  │ Durée: [1-200]h       │   │
│                        │  │ Dates: [▼] - [▼]      │   │
│                        │  │ ☐ Certifiant          │   │
│                        │  │                       │   │
│                        │  │ [Ajouter] [Modifier]  │   │
│                        │  │ [Supprimer] [Détails] │   │
│                        │  └──────────────────────┘   │
└─────────────────────────────────────────────────────┘
│ ✅ Prêt  │ 👆 Sélectionnez un cours...               │
└─────────────────────────────────────────────────────┘
```

---

## 🎬 4 Opérations Principales

### 1️⃣ AJOUTER UN COURS
```
1. Remplissez le formulaire à droite
   • Titre (obligatoire) ⭐
   • Description
   • Objectifs
   • Durée en heures
   • Niveau (obligatoire) ⭐ - Choisir: debutant | intermediaire | avance
   • Catégorie (obligatoire) ⭐ - Choisir: informatique | mecanique | electrique
   • Dates (automatiques si vides)
   • Certifiant (cocher si oui)

2. Cliquez sur [Ajouter]

3. ✅ Succès! → Vous êtes redirigé vers l'écran des DÉTAILS
```

### 2️⃣ MODIFIER UN COURS
```
1. SÉLECTIONNEZ un cours dans le tableau (clic gauche)
   → Le formulaire se remplit automatiquement ✨

2. Modifiez les champs que vous voulez

3. Cliquez sur [Modifier]

4. ✅ Le cours est mis à jour!
```

### 3️⃣ SUPPRIMER UN COURS
```
1. SÉLECTIONNEZ un cours dans le tableau

2. Cliquez sur [Supprimer]

3. Une popup demande confirmation:
   "Êtes-vous sûr de vouloir supprimer le cours '...' ?"

4. Cliquez [OK] pour confirmer

5. ✅ Cours supprimé!
```

### 4️⃣ VOIR LES DÉTAILS (Gérer les chapitres)
```
1. SÉLECTIONNEZ un cours dans le tableau

2. Cliquez sur [Détails]

3. 📖 Nouvel écran: Détails du cours + Chapitres
```

---

## 📖 Écran 2 : Détails & Chapitres

### 🖼️ Layout
```
┌─────────────────────────────────────┐
│ 📘 Java Basics          [← Retour]  │
│ Niveau: debutant       Catégorie:... │
│ Durée: 40 heures       Certifiant... │
│ Description: ...                    │
│ Objectifs: ...                      │
├─────────────────────────────────────┤
│ Total chapitres: 5                  │
│ ┌─────────────────────────────────┐ │
│ │ Ordre │ Titre    │ Type   │Durée│ │
│ ├─────────────────────────────────┤ │
│ │ 1     │ Intro    │ video  │ 15  │ │
│ │ 2     │ Variables│ texte  │ 20  │ │
│ │ 3     │ Boucles  │ quiz   │ 10  │ │
│ └─────────────────────────────────┘ │
├─────────────────────────────────────┤
│ [✚ Ajouter] [✎ Modifier] [✕ Suppr] │
└─────────────────────────────────────┘
```

---

## 📚 3 Opérations sur les Chapitres

### ➕ AJOUTER UN CHAPITRE
```
1. Cliquez sur [✚ Ajouter Chapitre]

2. Une fenêtre s'ouvre:
   • Titre: _______________
   • Description: _________
   • Ordre: [1-100]
   • Durée (min): [1-1000]
   • Type: [▼] - video | texte | pdf | quiz
   • URL: ________________

3. Remplissez les champs

4. Cliquez [OK]

5. ✅ Chapitre ajouté! (visible dans le tableau)
```

### ✎ MODIFIER UN CHAPITRE
```
1. SÉLECTIONNEZ un chapitre dans le tableau

2. Cliquez sur [✎ Modifier Chapitre]

3. La fenêtre s'ouvre avec les données

4. Modifiez ce que vous voulez

5. Cliquez [OK]

6. ✅ Chapitre modifié!
```

### ✕ SUPPRIMER UN CHAPITRE
```
1. SÉLECTIONNEZ un chapitre

2. Cliquez sur [✕ Supprimer Chapitre]

3. Confirmez

4. ✅ Chapitre supprimé!
```

### 🔙 RETOUR AUX COURS
```
Cliquez sur [← Retour] en haut à droite
→ Vous retournez à l'écran de gestion des cours
```

---

## 🎨 Codes Couleur des Boutons

| Couleur | Signification |
|---------|---------------|
| 🟢 Vert | Ajouter / Créer |
| 🔵 Bleu | Modifier |
| 🔴 Rouge | Supprimer |
| 🟠 Orange | Détails / Info |
| ⚫ Gris | Retour / Annuler |

---

## ⚠️ Validation et Alertes

### ✅ Succès
- Message: "✅ Cours ajouté avec succès!"
- L'interface se met à jour automatiquement

### ⚠️ Erreur
Vous verrez une alerte si :
- Vous cliquez "Modifier" sans sélectionner un cours
- Vous cliquez "Supprimer" sans sélectionner un cours
- Vous cliquez "Ajouter" avec des champs obligatoires vides

---

## 🌟 Astuces Productivité

### 💡 Double-Clic = Auto-Remplissage
```
Double-cliquez sur une ligne du tableau
→ Le formulaire se remplit automatiquement ✨
```

### 💡 Champs Obligatoires
```
Ajouter un cours:
  ⭐ Titre (OBLIGATOIRE)
  ⭐ Niveau (OBLIGATOIRE)
  ⭐ Catégorie (OBLIGATOIRE)
  
Tout le reste est optionnel ✓
```

### 💡 Dates Automatiques
```
Si vous ne spécifiez pas les dates:
  • Date début = Aujourd'hui
  • Date fin = Aujourd'hui + 6 mois
```

---

## 🔧 Dépannage Rapide

| Problème | Solution |
|----------|----------|
| "Erreur de connexion BD" | Vérifiez MySQL est en cours d'exécution |
| Tableau vide | Les cours n'existent pas encore, créez-en! |
| Les boutons ne réagissent pas | Assurez-vous d'avoir d'abord cliqué sur "Ajouter" pour créer un cours |
| Erreur "fx:id not found" | Redémarrez l'application |

---

## 📊 Exemple Workflow Complet

```
1. [Ajouter Cours]
   → Créer "Java Basics" (Débutant, Informatique, 40h)
   → Redirection vers Détails ✅

2. [Ajouter Chapitre 1]
   → Titre: "Introduction", video, 15 min ✅

3. [Ajouter Chapitre 2]
   → Titre: "Variables", texte, 20 min ✅

4. [Ajouter Chapitre 3]
   → Titre: "Quiz", quiz, 10 min ✅

5. [Retour]
   → Retour à Gestion des Cours

6. [Sélectionner "Java Basics"]
   → Cliquez [Modifier]
   → Changez durée à 50h
   → Cliquez [Modifier] ✅

7. [Sélectionner "Java Basics"]
   → Cliquez [Détails]
   → Visualisez les 3 chapitres ✅
```

---

## 🎓 Qu'avez-vous appris?

✅ Créer des cours complets  
✅ Organiser les chapitres  
✅ Gérer les niveaux et catégories  
✅ Naviguer dans l'interface  
✅ Modifier et supprimer des données

---

## 📞 Besoin d'aide?

- 📖 Consultez le **README.md** pour plus de détails techniques
- 📋 Regardez le **CHANGELOG.md** pour les modifications
- 💻 Vérifiez la console pour les erreurs détaillées

---

### 🚀 Vous êtes Prêt!

**Lancez l'application et commencez à gérer vos cours!**

Bonne utilisation! 🎉

