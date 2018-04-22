package application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class registerYSController {
	@FXML
	private TextField tfUserName;
	@FXML
	private Button btOK;
	@FXML
	private Button btCancel;
	@FXML
	private PasswordField tfPassword;
	@FXML
	private PasswordField tfPasswordConfirm;
	@FXML
	private TextField tfNamePYZS;
	@FXML
	private ComboBox<String> cbKSNum;
	@FXML
	private CheckBox ifSenior;

	private Stage stage;

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public void init() {
		try (Connection connection = ConnectionDB.getConnection()) {
			String sql_KSBHList = "select KSMC from T_KSXX";
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql_KSBHList);
			ArrayList<String> nameList = new ArrayList<>();
			while (resultSet.next()) {
				nameList.add(resultSet.getString("KSMC"));
			}
			if (0 == nameList.size()) {
				JOptionPane.showMessageDialog(null, "科室不为空");
				return;
			}
			cbKSNum.getItems().addAll(nameList);

			stage.getScene().setOnKeyPressed(e -> {
				if (e.getCode() == KeyCode.ENTER) {
					if (btOK.isFocused()) {
						this.registerClicked(new ActionEvent());
					} else if (btCancel.isFocused()) {
						this.cancelClicked(new ActionEvent());
					}
				}
			});

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Event Listener on Button[#btOK].onAction
	@FXML
	public void registerClicked(ActionEvent event) {
		// TODO Autogenerated
		try (Connection connection = ConnectionDB.getConnection()) {
			String userName = tfUserName.getText().trim();
			if (0 == userName.length()) {
				JOptionPane.showMessageDialog(null, "用户名不为空");
				return;
			}
			if (userName.length() > 9) {
				JOptionPane.showMessageDialog(null, "用户名过长");
				return;
			}

			String password = tfPassword.getText().trim();
			if (0 == password.length()) {
				JOptionPane.showMessageDialog(null, "密码不为空");
				return;
			}
			if (password.length() > 7) {
				JOptionPane.showMessageDialog(null, "密码过长");
				return;
			}
			if (!password.equals(tfPasswordConfirm.getText().trim())) {
				JOptionPane.showMessageDialog(null, "密码不一致");
				return;
			}

			String pyzsValue = tfNamePYZS.getText().trim();
			if (0 == pyzsValue.length()) {
				JOptionPane.showMessageDialog(null, "拼音字首不为空");
				return;
			}

			if (pyzsValue.length() > 5) {
				JOptionPane.showMessageDialog(null, "拼音字首过长");
				return;
			}

			String ksName = cbKSNum.getValue();
			String ksNum = null;
			if (null == ksName) {
				JOptionPane.showMessageDialog(null, "请选择科室");
				return;
			}

			String sql_ksNum = "select KSBH from T_KSXX where KSMC = ? ";
			PreparedStatement pStatement = connection.prepareStatement(sql_ksNum);
			pStatement.setString(1, ksName);
			ResultSet resultSet = pStatement.executeQuery();
			if (resultSet.next()) {
				ksNum = resultSet.getString("KSBH");
			} else {
				JOptionPane.showMessageDialog(null, "科室不存在");
				return;
			}

			String sql_alreadyRegist = "select YSBH from T_KSYS where YSMC = ?";
			pStatement = connection.prepareStatement(sql_alreadyRegist);
			pStatement.setString(1, userName);
			resultSet = pStatement.executeQuery();
			if (resultSet.next()) {
				JOptionPane.showMessageDialog(null, "编号 " + resultSet.getString("YSBH"), "信息已注册",
						JOptionPane.ERROR_MESSAGE);
			} else {

				connection.setAutoCommit(false);
				Statement statement = connection.createStatement();
				resultSet = statement.executeQuery("SELECT count(*) total FROM T_KSYS");

				if (resultSet.next()) {
					int idNum = resultSet.getInt("total") + 1;
					PreparedStatement preparedStatement = connection.prepareStatement(
							"insert into T_KSYS(YSBH,KSBH,YSMC,PYZS,DLKL,SFZJ,DLSJ) values (?,?,?,?,?,?,?)");
					preparedStatement.setString(1, String.format("%06d", idNum));
					preparedStatement.setString(2, ksNum);
					preparedStatement.setString(3, userName);
					preparedStatement.setString(4, pyzsValue);
					preparedStatement.setString(5, password);
					preparedStatement.setBoolean(6, ifSenior.isSelected());
					preparedStatement.setTimestamp(7, ConnectionDB.getDBTime());
					preparedStatement.executeUpdate();
					connection.commit();
					JOptionPane.showMessageDialog(null, "注册成功");
				}
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Event Listener on Button[#btCancel].onAction
	@FXML
	public void cancelClicked(ActionEvent event) {
		// TODO Autogenerated
		stage.close();
	}
}
