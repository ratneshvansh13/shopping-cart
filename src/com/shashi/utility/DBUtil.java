package com.shashi.utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DBUtil {
	private static Connection conn;

	public DBUtil() {
	}

	public static Connection provideConnection() {

    try {
        if (conn == null || conn.isClosed()) {

            String connectionString = System.getenv("DB_CONNECTION_STRING");
            String driverName = System.getenv("DB_DRIVER");
            String username = System.getenv("DB_USERNAME");
            String password = System.getenv("DB_PASSWORD");

            if (connectionString == null ||
                driverName == null ||
                username == null ||
                password == null) {

                throw new SQLException(
                    "Database environment variables are not configured"
                );
            }

            Class.forName(driverName);

            conn = DriverManager.getConnection(
                connectionString,
                username,
                password
            );
        }

    } catch (ClassNotFoundException e) {
        System.err.println("JDBC driver not found");
        e.printStackTrace();

    } catch (SQLException e) {
        System.err.println("Database connection failed");
        e.printStackTrace();
    }

    return conn;
}

	public static void closeConnection(Connection con) {
		/*
		 * try { if (con != null && !con.isClosed()) {
		 * 
		 * con.close(); } } catch (SQLException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */
	}

	public static void closeConnection(ResultSet rs) {
		try {
			if (rs != null && !rs.isClosed()) {
				try {
					rs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void closeConnection(PreparedStatement ps) {
		try {
			if (ps != null && !ps.isClosed()) {
				try {
					ps.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
