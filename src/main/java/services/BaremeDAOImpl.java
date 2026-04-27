package services;

import interfaces.IService;
import models.Bareme;
import models.BaremeMention;
import utils.MyDataBase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BaremeDAOImpl implements IService<Bareme> {

    @Override
    public void add(Bareme bareme) {
        String req = "INSERT INTO bareme (evaluation_id, mention, note_min, note_max) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(req)) {
            ps.setInt(1, bareme.getEvaluationId());
            ps.setString(2, bareme.getMention().getDbValue());
            ps.setFloat(3, bareme.getNoteMin());
            ps.setFloat(4, bareme.getNoteMax());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(Bareme bareme) {
        String req = "UPDATE bareme SET evaluation_id = ?, mention = ?, note_min = ?, note_max = ? WHERE id = ?";
        try (PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(req)) {
            ps.setInt(1, bareme.getEvaluationId());
            ps.setString(2, bareme.getMention().getDbValue());
            ps.setFloat(3, bareme.getNoteMin());
            ps.setFloat(4, bareme.getNoteMax());
            ps.setInt(5, bareme.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String req = "DELETE FROM bareme WHERE id = ?";
        try (PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public Bareme getById(int id) {
        String req = "SELECT * FROM bareme WHERE id = ?";
        try (PreparedStatement ps = MyDataBase.getInstance().getConnection().prepareStatement(req)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Bareme bareme = new Bareme();
                    bareme.setId(rs.getInt("id"));
                    bareme.setEvaluationId(rs.getInt("evaluation_id"));
                    bareme.setMention(BaremeMention.fromDbValue(rs.getString("mention")));
                    bareme.setNoteMin(rs.getFloat("note_min"));
                    bareme.setNoteMax(rs.getFloat("note_max"));
                    return bareme;
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    @Override
    public List<Bareme> getAll() {
        List<Bareme> baremes = new ArrayList<>();
        String req = "SELECT * FROM bareme";

        try (Statement stm = MyDataBase.getInstance().getConnection().createStatement();
             ResultSet rs = stm.executeQuery(req)) {

            while (rs.next()) {
                Bareme bareme = new Bareme();
                bareme.setId(rs.getInt("id"));
                bareme.setEvaluationId(rs.getInt("evaluation_id"));
                bareme.setMention(BaremeMention.fromDbValue(rs.getString("mention")));
                bareme.setNoteMin(rs.getFloat("note_min"));
                bareme.setNoteMax(rs.getFloat("note_max"));
                baremes.add(bareme);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return baremes;
    }
}
