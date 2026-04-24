package tn.esprit.projet.test;

import tn.esprit.projet.utils.MyBDConnexion;
import java.sql.*;

public class TestDB {
    public static void main(String[] args) throws Exception {
        Connection cnx = MyBDConnexion.getInstance().getCnx();

        System.out.println("=== nutrition_objective columns ===");
        ResultSet rs1 = cnx.getMetaData().getColumns(null, null, "nutrition_objective", null);
        while (rs1.next()) System.out.println(rs1.getString("COLUMN_NAME") + " - " + rs1.getString("TYPE_NAME"));

        System.out.println("\n=== daily_log columns ===");
        ResultSet rs2 = cnx.getMetaData().getColumns(null, null, "daily_log", null);
        while (rs2.next()) System.out.println(rs2.getString("COLUMN_NAME") + " - " + rs2.getString("TYPE_NAME"));

        System.out.println("\n=== nutrition_objective rows ===");
        ResultSet rs3 = cnx.createStatement().executeQuery("SELECT * FROM nutrition_objective LIMIT 3");
        ResultSetMetaData meta = rs3.getMetaData();
        while (rs3.next()) {
            for (int i = 1; i <= meta.getColumnCount(); i++)
                System.out.print(meta.getColumnName(i) + "=" + rs3.getString(i) + " | ");
            System.out.println();
        }
    }
}
