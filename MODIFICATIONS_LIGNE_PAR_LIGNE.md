# 📌 LISTE DÉTAILLÉE LIGNE PAR LIGNE DES MODIFICATIONS

## 1️⃣ Modèles (Models)

### `src/main/java/models/Cours.java`
```diff
+ private boolean visible = true;  // Nouvelle ligne (ligne 16)
+ public boolean isVisible() { return visible; }  // Nouveau getter
+ public void setVisible(boolean visible) { this.visible = visible; }  // Nouveau setter
```

### `src/main/java/models/Chapitre.java`
```diff
+ private boolean visible = true;  // Nouvelle ligne (ligne 15)
+ public boolean isVisible() { return visible; }  // Nouveau getter
+ public void setVisible(boolean visible) { this.visible = visible; }  // Nouveau setter
```

---

## 2️⃣ Contrôleurs (Controllers)

### `src/main/java/controllers/GestionCoursController.java`

#### Ligne 25: Ajout CheckBox cbVisible
```diff
+ @FXML private CheckBox cbVisible;
```

#### Ligne 37: Ajout variable lastSelectedIndex
```diff
+ private int lastSelectedIndex = -1;  // Pour permettre la désélection
```

#### Ligne 76-90: Modification méthode selectCoursInTable
```diff
- private void selectCoursInTable(MouseEvent event) {
-     Cours selected = tableViewCours.getSelectionModel().getSelectedItem();
-     if (selected != null) {
-         coursEnEdition = selected;
-         remplirFormulaire(selected);
-     }
- }

+ private void selectCoursInTable(MouseEvent event) {
+     int selectedIndex = tableViewCours.getSelectionModel().getSelectedIndex();
+     Cours selected = tableViewCours.getSelectionModel().getSelectedItem();
+     
+     // Permettre la désélection en cliquant à nouveau sur le même cours
+     if (selectedIndex == lastSelectedIndex && selected != null) {
+         tableViewCours.getSelectionModel().clearSelection();
+         clearForm();
+         lastSelectedIndex = -1;
+     } else if (selected != null) {
+         coursEnEdition = selected;
+         remplirFormulaire(selected);
+         lastSelectedIndex = selectedIndex;
+     }
+ }
```

#### Ligne 101: Ajout cbVisible dans remplirFormulaire
```diff
+ cbVisible.setSelected(c.isVisible());
```

#### Ligne 115: Ajout cbVisible dans clearForm
```diff
+ cbVisible.setSelected(true);
```

#### Ligne 126: Ajout setVisible dans ajouterCours
```diff
+ c.setVisible(cbVisible.isSelected());
```

#### Ligne 166: Ajout setVisible dans modifierCours
```diff
+ coursEnEdition.setVisible(cbVisible.isSelected());
```

---

### `src/main/java/controllers/DetailsCoursController.java`

#### Ligne 44: Ajout colVisible
```diff
+ @FXML private TableColumn<Chapitre, Boolean> colVisible;
```

#### Ligne 62-73: Ajout configuration colVisible dans initialize
```diff
+ colVisible.setCellValueFactory(
+         new javafx.scene.control.cell.PropertyValueFactory<>("visible"));
+ 
+ // Afficher les booléens comme "Visible" / "Masqué"
+ colVisible.setCellFactory(col -> new javafx.scene.control.TableCell<Chapitre, Boolean>() {
+     @Override
+     protected void updateItem(Boolean item, boolean empty) {
+         super.updateItem(item, empty);
+         if (empty || item == null) {
+             setText(null);
+         } else {
+             setText(item ? "👁 Visible" : "👁‍🗨 Masqué");
+             setStyle(item ? "-fx-text-fill: #10b981; -fx-font-weight: bold;" : "-fx-text-fill: #ef4444; -fx-font-weight: bold;");
+         }
+     }
+ });
```

#### Ligne 174-180: Ajout CheckBox visible dans ajouterChapitre
```diff
+ grid.add(new Label("Visible pour les étudiants:"), 0, 6);
+ CheckBox cbVisible = new CheckBox("Oui");
+ cbVisible.setSelected(true);
+ grid.add(cbVisible, 1, 6);
```

#### Ligne 182-186: Ajout setVisible dans ajouterChapitre (ResultConverter)
```diff
+ ch.setVisible(cbVisible.isSelected());
```

#### Ligne 254-260: Ajout CheckBox visible dans modifierChapitre
```diff
+ grid.add(new Label("Visible pour les étudiants:"), 0, 6);
+ CheckBox cbVisible = new CheckBox("Oui");
+ cbVisible.setSelected(chapitreEnEdition.isVisible());
+ grid.add(cbVisible, 1, 6);
```

#### Ligne 275: Ajout setVisible dans modifierChapitre (ResultConverter)
```diff
+ chapitreEnEdition.setVisible(cbVisible.isSelected());
```

---

### `src/main/java/controllers/EtudiantController.java`

#### Ligne 146-175: Modification ouvrirCours pour filtrer chapitres visibles
```diff
+ // Filtrer seulement les chapitres visibles pour les étudiants
+ if (chapitres != null) {
+     chapitres = chapitres.stream()
+             .filter(Chapitre::isVisible)
+             .collect(Collectors.toList());
+ }
```

#### Ligne 201: Ajout filtre visible dans appliquerFiltres
```diff
+ .filter(c -> c.isVisible())  // Filtrer les cours visibles
```

---

### `src/main/java/controllers/LectureChapitreController.java`

#### Ligne 82-84: Modification affichage sommaire pour badge masqué
```diff
- String icone = getIconeType(ch.getTypeContenu());
- setText(icone + "  " + ch.getOrdre() + ". " + ch.getTitre());

+ String icone = getIconeType(ch.getTypeContenu());
+ // Ajouter un badge "Masqué" si le chapitre n'est pas visible
+ String masque = !ch.isVisible() ? " 👁‍🗨" : "";
+ setText(icone + "  " + ch.getOrdre() + ". " + ch.getTitre() + masque);
```

---

## 3️⃣ Interfaces (FXML)

### `src/main/resources/GestionCours.fxml`

#### Ligne 90: Ajout CheckBox cbVisible
```xml
<!-- AVANT -->
<HBox spacing="15">
    <CheckBox fx:id="cbCertifiant" text="Cours certifiant"/>
</HBox>

<!-- APRÈS -->
<HBox spacing="15">
    <CheckBox fx:id="cbCertifiant" text="Cours certifiant"/>
    <CheckBox fx:id="cbVisible" text="Visible pour les étudiants"/>
</HBox>
```

---

### `src/main/resources/DetailsCours.fxml`

#### Ligne 44-50: Ajout colonne Visible
```xml
<!-- AVANT -->
<TableView fx:id="tableViewChapitres" VBox.vgrow="ALWAYS">
    <columns>
        <TableColumn fx:id="colOrdre" prefWidth="60" text="Ordre"/>
        <TableColumn fx:id="colTitreChap" prefWidth="250" text="Titre"/>
        <TableColumn fx:id="colType" prefWidth="100" text="Type"/>
        <TableColumn fx:id="colDureeMin" prefWidth="100" text="Durée (min)"/>
    </columns>
</TableView>

<!-- APRÈS -->
<TableView fx:id="tableViewChapitres" VBox.vgrow="ALWAYS">
    <columns>
        <TableColumn fx:id="colOrdre" prefWidth="60" text="Ordre"/>
        <TableColumn fx:id="colTitreChap" prefWidth="250" text="Titre"/>
        <TableColumn fx:id="colType" prefWidth="100" text="Type"/>
        <TableColumn fx:id="colDureeMin" prefWidth="100" text="Durée (min)"/>
        <TableColumn fx:id="colVisible" prefWidth="80" text="Visible"/>
    </columns>
</TableView>
```

---

### `src/main/resources/LectureChapitre.fxml`

#### Ligne 174-181: Suppression cadre blanc et style transparent

```xml
<!-- AVANT (Cadre blanc) -->
<VBox spacing="0"
      style="-fx-background-color: #ffffff;
             -fx-border-color: #e2e8f0;
             -fx-border-radius: 16;
             -fx-background-radius: 16;
             -fx-padding: 40;
             -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 20, 0, 0, 4);">

<!-- APRÈS (Transparent) -->
<VBox spacing="0"
      style="-fx-background-color: transparent;
             -fx-border-color: transparent;
             -fx-border-radius: 0;
             -fx-background-radius: 0;
             -fx-padding: 0;
             -fx-effect: none;">
```

#### Ligne 190-201: TextArea dynamique et transparent

```xml
<!-- AVANT (Fixe) -->
<TextArea fx:id="taDescription"
          wrapText="true"
          editable="false"
          prefRowCount="4"
          style="-fx-background-color: #f8fafc;
                 -fx-border-color: #e2e8f0;
                 -fx-border-radius: 10;
                 -fx-padding: 12;"/>

<!-- APRÈS (Dynamique) -->
<TextArea fx:id="taDescription"
          wrapText="true"
          editable="false"
          prefRowCount="1"
          minHeight="USE_PREF_SIZE"
          style="-fx-background-color: transparent;
                 -fx-border-color: transparent;
                 -fx-border-radius: 0;
                 -fx-padding: 0;
                 -fx-line-spacing: 6;
                 -fx-control-inner-background: transparent;"/>
```

#### Ligne 215-246: Support de cours transparent

```xml
<!-- AVANT -->
<HBox spacing="12" alignment="CENTER_LEFT"
      style="-fx-background-color: #f8fafc;
             -fx-border-color: #e2e8f0;
             -fx-border-radius: 10;
             -fx-background-radius: 10;
             -fx-padding: 15;">

<!-- APRÈS -->
<HBox spacing="12" alignment="CENTER_LEFT"
      style="-fx-background-color: transparent;
             -fx-border-color: transparent;
             -fx-border-radius: 0;
             -fx-background-radius: 0;
             -fx-padding: 0;">
```

#### Ligne 137: Réduction du padding

```xml
<!-- AVANT -->
style="-fx-background-color: #f8f9fa; -fx-padding: 40 80 80 80;">

<!-- APRÈS -->
style="-fx-background-color: #f8f9fa; -fx-padding: 40 40 80 40;">
```

---

## 4️⃣ Documentation Créée

- ✏️ `CHANGEMENTS_APPORTES.md` - Détails complets
- ✏️ `GUIDE_AMELIORATIONS.md` - Améliorations futures
- ✏️ `RESUME_MODIFICATIONS.md` - Vue d'ensemble
- ✏️ `MODIFICATIONS_RAPIDE.md` - Quick reference
- ✏️ `MODIFICATIONS_LIGNE_PAR_LIGNE.md` - Ce fichier

---

## 📊 STATISTIQUES

| Type | Count |
|------|-------|
| Fichiers Java modifiés | 6 |
| Fichiers FXML modifiés | 3 |
| Fichiers créés | 5 |
| Lignes ajoutées | ~150 |
| Lignes modifiées | ~20 |
| Fonctionnalités ajoutées | 4 |

---

**Fin de la documentation détaillée**

