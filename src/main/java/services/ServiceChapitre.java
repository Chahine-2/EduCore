package services;

import interfaces.IService;
import interfaces.IServiceCours;
import models.Chapitre;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class ServiceChapitre implements IServiceCours<Chapitre> {
    private Connection getConnectionOrThrow() throws SQLException {
        Connection cnx = MyDataBase.getInstance().getCnx();
        if (cnx == null) {
            throw new SQLException("Connexion à la base indisponible. Vérifiez que MySQL/WAMP/XAMPP est démarré.");
        }
        return cnx;
    }


    /**
     * Ajoute un chapitre et retourne si l'opération a réussi
     */
    public boolean addChapitre(Chapitre c) {
        String req = "INSERT INTO chapitre (titre, description, ordre, duree_minutes, type_contenu, url_contenu, date_creation, cours_id, visible) VALUES (?,?,?,?,?,?,?,?,?)";
        try {
            // Vérification des données critiques
            if (c.getTitre() == null || c.getTitre().isEmpty()) {
                System.out.println("❌ ERREUR : Le titre du chapitre est vide");
                return false;
            }
            if (c.getTypeContenu() == null || c.getTypeContenu().isEmpty()) {
                System.out.println("❌ ERREUR : Le type de contenu est vide");
                return false;
            }
            if (c.getCoursId() <= 0) {
                System.out.println("❌ ERREUR : cours_id invalide (=" + c.getCoursId() + "). Vérifiez que le cours existe.");
                return false;
            }
            if (c.getDateCreation() == null) {
                System.out.println("❌ ERREUR : La date de création est null");
                return false;
            }

            System.out.println("📝 Données du chapitre:");
            System.out.println("   Titre: " + c.getTitre());
            System.out.println("   Cours ID: " + c.getCoursId());
            System.out.println("   Type: " + c.getTypeContenu());
            System.out.println("   Date: " + c.getDateCreation());

            Connection cnx = getConnectionOrThrow();
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, c.getTitre());
            ps.setString(2, c.getDescription());
            ps.setInt(3, c.getOrdre());
            ps.setInt(4, c.getDureeMinutes());
            ps.setString(5, c.getTypeContenu());
            ps.setString(6, c.getUrlContenu());
            ps.setDate(7, Date.valueOf(c.getDateCreation()));
            ps.setInt(8, c.getCoursId());
            ps.setBoolean(9, c.isVisible());
            int rows = ps.executeUpdate();
            System.out.println("✅ Chapitre ajouté : " + c.getTitre() + " (cours_id=" + c.getCoursId() + ")");
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("❌ ERREUR SQL lors de l'ajout du chapitre :");
            System.out.println("   Message: " + e.getMessage());
            System.out.println("   État SQL: " + e.getSQLState());
            System.out.println("   Code erreur: " + e.getErrorCode());
            System.out.println("\n📋 PROBABLE CAUSE:");
            if (e.getMessage().contains("foreign key")) {
                System.out.println("   → La clé étrangère cours_id ne correspond à aucun cours");
            } else if (e.getMessage().contains("NOT NULL")) {
                System.out.println("   → Un champ obligatoire est null");
            } else {
                System.out.println("   → Erreur de contrainte ou de syntaxe SQL");
            }
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void add(Chapitre c) {
        addChapitre(c);
    }

    /**
     * Modifie un chapitre et retourne si l'opération a réussi
     */
    public boolean updateChapitre(Chapitre c) {
        String req = "UPDATE chapitre SET titre=?, description=?, ordre=?, duree_minutes=?, type_contenu=?, url_contenu=?, date_creation=?, cours_id=?, visible=? WHERE id=?";
        try {
            Connection cnx = getConnectionOrThrow();
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, c.getTitre());
            ps.setString(2, c.getDescription());
            ps.setInt(3, c.getOrdre());
            ps.setInt(4, c.getDureeMinutes());
            ps.setString(5, c.getTypeContenu());
            ps.setString(6, c.getUrlContenu());
            ps.setDate(7, Date.valueOf(c.getDateCreation()));
            ps.setInt(8, c.getCoursId());
            ps.setBoolean(9, c.isVisible());
            ps.setInt(10, c.getId());
            int rows = ps.executeUpdate();
            System.out.println("✅ Chapitre modifié : " + c.getTitre() + " (id=" + c.getId() + ")");
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("❌ ERREUR lors de la modification du chapitre :");
            System.out.println("   Message SQL: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void update(Chapitre c) {
        updateChapitre(c);
    }

    @Override
    public void delete(Chapitre c) {
        String req = "DELETE FROM chapitre WHERE id=?";
        try {
            Connection cnx = getConnectionOrThrow();
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, c.getId());
            ps.executeUpdate();
            System.out.println("Chapitre supprimé ✅");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Chapitre> getAll() {
        List<Chapitre> liste = new ArrayList<>();
        String req = "SELECT * FROM chapitre ORDER BY cours_id, ordre";
        try {
            Connection cnx = getConnectionOrThrow();
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);
            while (rs.next()) liste.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return liste;
    }

    public List<Chapitre> getByCours(int coursId) {
        List<Chapitre> liste = new ArrayList<>();
        String req = "SELECT * FROM chapitre WHERE cours_id = ? ORDER BY ordre";
        try {
            Connection cnx = getConnectionOrThrow();
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, coursId);
            ResultSet rs = ps.executeQuery();
            System.out.println("🔍 Recherche de chapitres pour cours_id = " + coursId);
            while (rs.next()) {
                Chapitre chap = mapResultSet(rs);
                System.out.println("   ✅ Chapitre trouvé: " + chap.getTitre() + " (id=" + chap.getId() + ")");
                liste.add(chap);
            }
            if (liste.isEmpty()) {
                System.out.println("   ⚠️ Aucun chapitre trouvé pour cours_id = " + coursId);
            }
        } catch (SQLException e) {
            System.out.println("❌ ERREUR SQL: " + e.getMessage());
            e.printStackTrace();
        }
        return liste;
    }

    public List<Chapitre> getByType(String type) {
        List<Chapitre> liste = new ArrayList<>();
        String req = "SELECT * FROM chapitre WHERE type_contenu = ?";
        try {
            Connection cnx = getConnectionOrThrow();
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, type);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return liste;
    }

    private Chapitre mapResultSet(ResultSet rs) throws SQLException {
        Chapitre c = new Chapitre();
        c.setId(rs.getInt("id"));
        c.setTitre(rs.getString("titre"));
        c.setDescription(rs.getString("description"));
        c.setOrdre(rs.getInt("ordre"));
        c.setDureeMinutes(rs.getInt("duree_minutes"));
        c.setTypeContenu(rs.getString("type_contenu"));
        c.setUrlContenu(rs.getString("url_contenu"));
        c.setVisible(rs.getBoolean("visible"));           // ← ajouté
        if (rs.getDate("date_creation") != null)
            c.setDateCreation(rs.getDate("date_creation").toLocalDate());
        c.setCoursId(rs.getInt("cours_id"));
        return c;
    }
}