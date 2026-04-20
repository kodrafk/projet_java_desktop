package tn.esprit.projet.services;

import tn.esprit.projet.models.Complaint;
import tn.esprit.projet.utils.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComplaintService implements CRUD<Complaint> {

    private final Connection cnx;

    public ComplaintService() {
        cnx = MyBDConnexion.getInstance().getCnx();
    }

    @Override
    public void ajouter(Complaint c) {
        String req = "INSERT INTO complaint (user_id, title, description, phone_number, rate, date_of_complaint, status, image_path) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, c.getUserId());
            ps.setString(2, c.getTitle());
            ps.setString(3, c.getDescription());
            ps.setString(4, c.getPhoneNumber());
            ps.setInt(5, c.getRate());
            ps.setTimestamp(6, Timestamp.valueOf(c.getDateOfComplaint()));
            ps.setString(7, c.getStatus());
            ps.setString(8, c.getImagePath());
            ps.executeUpdate();
            
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                c.setId(keys.getInt(1));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.err.println("Error adding complaint: " + ex.getMessage());
        }
    }

    @Override
    public void modifier(Complaint c) {
        String req = "UPDATE complaint SET title=?, description=?, phone_number=?, rate=?, status=?, image_path=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, c.getTitle());
            ps.setString(2, c.getDescription());
            ps.setString(3, c.getPhoneNumber());
            ps.setInt(4, c.getRate());
            ps.setString(5, c.getStatus());
            ps.setString(6, c.getImagePath());
            ps.setInt(7, c.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void supprimer(int id) {
        String req = "DELETE FROM complaint WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Complaint getById(int id) {
        String req = "SELECT c.*, u.first_name, u.last_name, cr.id as response_id, cr.response_content, cr.response_date " +
                     "FROM complaint c " +
                     "JOIN user u ON c.user_id = u.id " +
                     "LEFT JOIN complaint_response cr ON c.id = cr.complaint_id " +
                     "WHERE c.id=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
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
    public List<Complaint> getAll() {
        List<Complaint> list = new ArrayList<>();
        String req = "SELECT c.*, u.first_name, u.last_name, cr.id as response_id, cr.response_content, cr.response_date " +
                     "FROM complaint c " +
                     "JOIN user u ON c.user_id = u.id " +
                     "LEFT JOIN complaint_response cr ON c.id = cr.complaint_id " +
                     "ORDER BY c.id DESC";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public List<Complaint> getByUserId(int userId) {
        List<Complaint> list = new ArrayList<>();
        String req = "SELECT c.*, u.first_name, u.last_name, cr.id as response_id, cr.response_content, cr.response_date " +
                     "FROM complaint c " +
                     "JOIN user u ON c.user_id = u.id " +
                     "LEFT JOIN complaint_response cr ON c.id = cr.complaint_id " +
                     "WHERE c.user_id=? ORDER BY c.id DESC";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    private Complaint mapResultSet(ResultSet rs) throws SQLException {
        Complaint c = new Complaint();
        c.setId(rs.getInt("id"));
        c.setUserId(rs.getInt("user_id"));
        c.setTitle(rs.getString("title"));
        c.setDescription(rs.getString("description"));
        c.setPhoneNumber(rs.getString("phone_number"));
        c.setRate(rs.getInt("rate"));
        
        Timestamp ts = rs.getTimestamp("date_of_complaint");
        if (ts != null) {
            c.setDateOfComplaint(ts.toLocalDateTime());
        }
        
        c.setStatus(rs.getString("status"));
        
        // Handle ComplaintResponse mapping
        try {
            int responseId = rs.getInt("response_id");
            if (!rs.wasNull()) {
                tn.esprit.projet.models.ComplaintResponse cr = new tn.esprit.projet.models.ComplaintResponse();
                cr.setId(responseId);
                cr.setComplaintId(c.getId());
                cr.setResponseContent(rs.getString("response_content"));
                
                Timestamp tsResponse = rs.getTimestamp("response_date");
                if (tsResponse != null) {
                    cr.setResponseDate(tsResponse.toLocalDateTime());
                }
                c.setResponseObj(cr);
            }
        } catch (SQLException ignored) {
            // response columns might not exist if someone writes a query without the JOIN
        }
        
        try {
            c.setImagePath(rs.getString("image_path"));
        } catch (SQLException ignored) {
        }
        
        try {
            String firstName = rs.getString("first_name");
            String lastName = rs.getString("last_name");
            c.setUserName(firstName + " " + lastName);
        } catch (SQLException ignored) {
            // column might not exist if someone writes a custom query, safe fallback
        }
        
        return c;
    }
}
