package tn.esprit.projet.services;

import tn.esprit.projet.models.ComplaintResponse;
import tn.esprit.projet.utils.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComplaintResponseService implements CRUD<ComplaintResponse> {

    private Connection cnx() {
        return MyBDConnexion.getInstance().getCnx();
    }

    public ComplaintResponseService() {
    }

    @Override
    public void ajouter(ComplaintResponse r) {
        String req = "INSERT INTO complaint_response (complaint_id, response_content, response_date) VALUES (?, ?, ?)";
        try (PreparedStatement ps = cnx().prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getComplaintId());
            ps.setString(2, r.getResponseContent());
            ps.setTimestamp(3, Timestamp.valueOf(r.getResponseDate()));
            ps.executeUpdate();
            
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                r.setId(keys.getInt(1));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.err.println("Error adding complaint response: " + ex.getMessage());
        }
    }

    @Override
    public void modifier(ComplaintResponse r) {
        String req = "UPDATE complaint_response SET response_content=?, response_date=? WHERE id=?";
        try (PreparedStatement ps = cnx().prepareStatement(req)) {
            ps.setString(1, r.getResponseContent());
            ps.setTimestamp(2, Timestamp.valueOf(r.getResponseDate()));
            ps.setInt(3, r.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void supprimer(int id) {
        String req = "DELETE FROM complaint_response WHERE id=?";
        try (PreparedStatement ps = cnx().prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public ComplaintResponse getById(int id) {
        String req = "SELECT * FROM complaint_response WHERE id=?";
        try (PreparedStatement ps = cnx().prepareStatement(req)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ComplaintResponse> getAll() {
        List<ComplaintResponse> list = new ArrayList<>();
        String req = "SELECT * FROM complaint_response ORDER BY id DESC";
        try (Statement st = cnx().createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public ComplaintResponse getByComplaintId(int complaintId) {
        String req = "SELECT * FROM complaint_response WHERE complaint_id=? ORDER BY id DESC LIMIT 1";
        try (PreparedStatement ps = cnx().prepareStatement(req)) {
            ps.setInt(1, complaintId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void supprimerByComplaintId(int complaintId) {
        String req = "DELETE FROM complaint_response WHERE complaint_id=?";
        try (PreparedStatement ps = cnx().prepareStatement(req)) {
            ps.setInt(1, complaintId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private ComplaintResponse mapResultSet(ResultSet rs) throws SQLException {
        ComplaintResponse r = new ComplaintResponse();
        r.setId(rs.getInt("id"));
        r.setComplaintId(rs.getInt("complaint_id"));
        r.setResponseContent(rs.getString("response_content"));
        
        Timestamp ts = rs.getTimestamp("response_date");
        if (ts != null) {
            r.setResponseDate(ts.toLocalDateTime());
        }
        return r;
    }
}
