package tn.esprit.projet.services;

import tn.esprit.projet.models.PublicationComment;
import tn.esprit.projet.utils.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PublicationCommentService {

    private Connection cnx() {
        return MyBDConnexion.getInstance().getCnx();
    }

    public boolean create(PublicationComment c) {
        String sql = "INSERT INTO publication_comment (contenu, created_at, author_name, author_avatar, is_admin, publication_id, user_id) " +
                     "VALUES (?, NOW(), ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getContenu());
            ps.setString(2, c.getAuthorName());
            ps.setString(3, c.getAuthorAvatar());
            ps.setBoolean(4, c.isAdmin());
            ps.setInt(5, c.getPublicationId());
            ps.setInt(6, c.getUserId());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                c.setId(rs.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[PublicationCommentService.create] " + e.getMessage());
        }
        return false;
    }

    public boolean update(PublicationComment c) {
        String sql = "UPDATE publication_comment SET contenu=? WHERE id=?";
        try (PreparedStatement ps = cnx().prepareStatement(sql)) {
            ps.setString(1, c.getContenu());
            ps.setInt(2, c.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PublicationCommentService.update] " + e.getMessage());
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM publication_comment WHERE id=?";
        try (PreparedStatement ps = cnx().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PublicationCommentService.delete] " + e.getMessage());
        }
        return false;
    }

    public List<PublicationComment> findByPublication(int publicationId) {
        List<PublicationComment> list = new ArrayList<>();
        String sql = "SELECT * FROM publication_comment WHERE publication_id=? ORDER BY created_at ASC";
        try (PreparedStatement ps = cnx().prepareStatement(sql)) {
            ps.setInt(1, publicationId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                PublicationComment c = new PublicationComment();
                c.setId(rs.getInt("id"));
                c.setContenu(rs.getString("contenu"));
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) c.setCreatedAt(ts.toLocalDateTime());
                c.setAuthorName(rs.getString("author_name"));
                c.setAuthorAvatar(rs.getString("author_avatar"));
                c.setAdmin(rs.getBoolean("is_admin"));
                c.setPublicationId(rs.getInt("publication_id"));
                c.setUserId(rs.getInt("user_id"));
                list.add(c);
            }
        } catch (SQLException e) {
            System.err.println("[PublicationCommentService.findByPublication] " + e.getMessage());
        }
        return list;
    }
}
