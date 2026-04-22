package tn.esprit.projet.services;

import tn.esprit.projet.models.Publication;
import tn.esprit.projet.utils.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PublicationService {

    private Connection cnx() {
        return MyBDConnexion.getInstance().getCnx();
    }

    public boolean create(Publication p) {
        String sql = "INSERT INTO publication (titre, contenu, description, created_at, author_name, author_avatar, is_admin, image, view_count, share_count, visibility, scheduled_at, shared_from_id, user_id) " +
                     "VALUES (?, ?, ?, NOW(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getTitre());
            ps.setString(2, p.getContenu());
            ps.setString(3, p.getDescription());
            ps.setString(4, p.getAuthorName());
            ps.setString(5, p.getAuthorAvatar());
            ps.setBoolean(6, p.isAdmin());
            ps.setString(7, p.getImage());
            ps.setInt(8, p.getViewCount());
            ps.setInt(9, p.getShareCount());
            ps.setString(10, p.getVisibility());
            if (p.getScheduledAt() != null) {
                ps.setTimestamp(11, Timestamp.valueOf(p.getScheduledAt()));
            } else {
                ps.setNull(11, Types.TIMESTAMP);
            }
            if (p.getSharedFromId() != null) {
                ps.setInt(12, p.getSharedFromId());
            } else {
                ps.setNull(12, Types.INTEGER);
            }
            ps.setInt(13, p.getUserId());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                p.setId(rs.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[PublicationService.create] " + e.getMessage());
        }
        return false;
    }

    public boolean update(Publication p) {
        String sql = "UPDATE publication SET titre=?, contenu=?, description=?, author_name=?, author_avatar=?, is_admin=?, image=?, view_count=?, share_count=?, visibility=?, scheduled_at=?, shared_from_id=? WHERE id=?";
        try (PreparedStatement ps = cnx().prepareStatement(sql)) {
            ps.setString(1, p.getTitre());
            ps.setString(2, p.getContenu());
            ps.setString(3, p.getDescription());
            ps.setString(4, p.getAuthorName());
            ps.setString(5, p.getAuthorAvatar());
            ps.setBoolean(6, p.isAdmin());
            ps.setString(7, p.getImage());
            ps.setInt(8, p.getViewCount());
            ps.setInt(9, p.getShareCount());
            ps.setString(10, p.getVisibility());
            if (p.getScheduledAt() != null) {
                ps.setTimestamp(11, Timestamp.valueOf(p.getScheduledAt()));
            } else {
                ps.setNull(11, Types.TIMESTAMP);
            }
            if (p.getSharedFromId() != null) {
                ps.setInt(12, p.getSharedFromId());
            } else {
                ps.setNull(12, Types.INTEGER);
            }
            ps.setInt(13, p.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PublicationService.update] " + e.getMessage());
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM publication WHERE id=?";
        try (PreparedStatement ps = cnx().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PublicationService.delete] " + e.getMessage());
        }
        return false;
    }

    public List<Publication> findAll() {
        List<Publication> list = new ArrayList<>();
        String sql = "SELECT * FROM publication ORDER BY created_at DESC";
        try (PreparedStatement ps = cnx().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToPublication(rs));
            }
        } catch (SQLException e) {
            System.err.println("[PublicationService.findAll] " + e.getMessage());
        }
        return list;
    }

    public Publication getLatestPublication() {
        String sql = "SELECT * FROM publication ORDER BY id DESC LIMIT 1";
        try (PreparedStatement ps = cnx().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return mapResultSetToPublication(rs);
            }
        } catch (SQLException e) {
            System.err.println("[PublicationService.getLatestPublication] " + e.getMessage());
        }
        return null;
    }

    private Publication mapResultSetToPublication(ResultSet rs) throws SQLException {
        Publication p = new Publication();
        p.setId(rs.getInt("id"));
        p.setTitre(rs.getString("titre"));
        p.setContenu(rs.getString("contenu"));
        p.setDescription(rs.getString("description"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) p.setCreatedAt(ts.toLocalDateTime());
        p.setAuthorName(rs.getString("author_name"));
        p.setAuthorAvatar(rs.getString("author_avatar"));
        p.setAdmin(rs.getBoolean("is_admin"));
        p.setImage(rs.getString("image"));
        p.setViewCount(rs.getInt("view_count"));
        p.setShareCount(rs.getInt("share_count"));
        p.setVisibility(rs.getString("visibility"));
        Timestamp sched = rs.getTimestamp("scheduled_at");
        if (sched != null) p.setScheduledAt(sched.toLocalDateTime());
        int sharedId = rs.getInt("shared_from_id");
        if (!rs.wasNull()) p.setSharedFromId(sharedId);
        p.setUserId(rs.getInt("user_id"));
        return p;
    }
}
