package tn.esprit.projet.test;

import tn.esprit.projet.utils.MyBDConnexion;
import tn.esprit.projet.utils.PasswordUtil;
import java.sql.PreparedStatement;

public class FixAdminPassword {
    public static void main(String[] args) throws Exception {
        String hash = PasswordUtil.hashPassword("Admin@1234");
        var cnx = MyBDConnexion.getInstance().getCnx();
        PreparedStatement ps = cnx.prepareStatement(
            "UPDATE user SET password=?, first_name='Admin', last_name='NutriLife', is_active=1 WHERE email='admin@nutrilife.com'");
        ps.setString(1, hash);
        int rows = ps.executeUpdate();
        System.out.println("Updated " + rows + " row(s). Admin password set to: Admin@1234");
    }
}
