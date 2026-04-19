package tn.esprit.projet.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import tn.esprit.projet.dao.UserDAO;
import tn.esprit.projet.models.User;

import java.util.List;

/**
 * Face ID service — stores/compares 128-float descriptors as JSON.
 * Euclidean distance ≤ 0.6 = match.
 *
 * NOTE: Actual webcam capture requires a native library (e.g., OpenCV).
 * This service handles the descriptor storage and comparison logic.
 * The UI layer is responsible for capturing the descriptor array.
 */
public class FaceIdService {

    private static final double THRESHOLD = 0.6;
    private final UserDAO dao = new UserDAO();
    private final ObjectMapper mapper = new ObjectMapper();

    /** Enroll: save descriptor JSON for a user. */
    public boolean enroll(int userId, double[] descriptor) {
        try {
            String json = mapper.writeValueAsString(descriptor);
            return dao.saveFaceDescriptor(userId, json);
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /** Remove Face ID enrollment. */
    public boolean remove(int userId) {
        return dao.clearFaceDescriptor(userId);
    }

    /**
     * Find a matching user by comparing the live descriptor against all enrolled users.
     * Returns the first user whose stored descriptor is within threshold distance.
     */
    public User findMatch(double[] liveDescriptor) {
        List<User> enrolled = dao.findAllWithFaceId();
        for (User u : enrolled) {
            double[] stored = parseDescriptor(u.getFaceDescriptor());
            if (stored != null && euclidean(liveDescriptor, stored) <= THRESHOLD) {
                return u;
            }
        }
        return null;
    }

    /** Parse a JSON descriptor string into a double array. */
    public double[] parseDescriptor(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            List<Double> list = mapper.readValue(json, new TypeReference<>() {});
            return list.stream().mapToDouble(Double::doubleValue).toArray();
        } catch (Exception e) { return null; }
    }

    /** Euclidean distance between two equal-length vectors. */
    public double euclidean(double[] a, double[] b) {
        if (a.length != b.length) return Double.MAX_VALUE;
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            double d = a[i] - b[i];
            sum += d * d;
        }
        return Math.sqrt(sum);
    }
}
