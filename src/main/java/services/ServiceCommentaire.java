package services;

import interfaces.IService;
import models.CommentaireCours;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCommentaire implements IService<CommentaireCours> {

    private Connection cnx = MyDataBase.getInstance().getCnx();

    @Override
    public void add(CommentaireCours c) {
        String req = "INSERT INTO commentaire_cours (contenu, note, cours_id) VALUES (?,?,?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, c.getContenu());
            ps.setInt(2, c.getNote());
            ps.setInt(3, c.getCoursId());
            ps.executeUpdate();
            System.out.println("Commentaire ajouté ✅");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(CommentaireCours c) {
        String req = "UPDATE commentaire_cours SET contenu=?, note=?, cours_id=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, c.getContenu());
            ps.setInt(2, c.getNote());
            ps.setInt(3, c.getCoursId());
            ps.setInt(4, c.getId());
            ps.executeUpdate();
            System.out.println("Commentaire modifié ✅");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(CommentaireCours c) {
        String req = "DELETE FROM commentaire_cours WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, c.getId());
            ps.executeUpdate();
            System.out.println("Commentaire supprimé ✅");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<CommentaireCours> getAll() {
        List<CommentaireCours> liste = new ArrayList<>();
        String req = "SELECT * FROM commentaire_cours";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);
            while (rs.next()) {
                CommentaireCours c = new CommentaireCours();
                c.setId(rs.getInt("id"));
                c.setContenu(rs.getString("contenu"));
                c.setNote(rs.getInt("note"));
                c.setCoursId(rs.getInt("cours_id"));
                liste.add(c);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return liste;
    }
}