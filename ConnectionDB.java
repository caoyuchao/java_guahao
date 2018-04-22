package application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

public class ConnectionDB {
	public static Connection getConnection() throws SQLException, ClassNotFoundException {
		String url = "jdbc:mysql://localhost:3306/newdatabase?useSSL=false&useUnicode=true&characterEncoding=utf8";// 连接数据库的地址
		String user = "root";
		String password = "cyc131421bhb.";
		Class.forName("com.mysql.jdbc.Driver");
		Connection connection = DriverManager.getConnection(url, user, password);
		return connection;
	}

	public static Timestamp getDBTime() throws SQLException, ClassNotFoundException {
		try (Connection connection = ConnectionDB.getConnection()) {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("select CURRENT_TIMESTAMP() AS time");
			if (resultSet.next()) {
				Timestamp timestamp = resultSet.getTimestamp("time");
				return timestamp;
			} else {
				throw new SQLException();
			}
		}
	}

	public static void updateBRLoginTime(String idNum) {
		try (Connection connection = ConnectionDB.getConnection()) {
			String sql_updateTime = "update T_BRXX set DLRQ = ? where BRBH = ?";
			PreparedStatement pStatement = connection.prepareStatement(sql_updateTime);
			pStatement.setTimestamp(1, ConnectionDB.getDBTime());
			pStatement.setString(2, idNum);
			pStatement.executeUpdate();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void updateYSLoginTime(String idNum) {
		try (Connection connection = ConnectionDB.getConnection()) {
			String sql_updateTime = "update T_KSYS set DLSJ = ? where YSBH = ?";
			PreparedStatement pStatement = connection.prepareStatement(sql_updateTime);
			pStatement.setTimestamp(1, ConnectionDB.getDBTime());
			pStatement.setString(2, idNum);
			pStatement.executeUpdate();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
