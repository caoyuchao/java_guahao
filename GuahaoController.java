package application;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Popup;
import javafx.stage.Stage;

public class GuahaoController {
	@FXML
	private TextField tfKSName;
	@FXML
	private TextField tfYSName;
	@FXML
	private ComboBox<String> cbHZLBbox;
	@FXML
	private TextField tfHZName;
	@FXML
	private TextField tfJKNum;
	@FXML
	private TextField tfYJNum;
	@FXML
	private TextField tfZLNum;
	@FXML
	private TextField tfGHNum;
	@FXML
	private Button btOK;
	@FXML
	private Button btClear;
	@FXML
	private Button btCancel;

	private final Popup popup = new Popup();
	private ListView<String> listView;
	private TextField currentTextField;
	private Stage stage;
	private String idNum;
	private double YCJE = 0;
	private double currentGHFY = 0;

	public void init() {
		tfKSName.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
			if (isNowFocused) {
				processKSMCChanged(tfKSName.getText().trim());
			} else {
				tryHidePopupWindow();
			}
		});

		tfKSName.textProperty().addListener((observable, oldValue, newValue) -> {
			processKSMCChanged(newValue);
		});

		tfYSName.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
			if (isNowFocused) {
				processYSMCChanged(tfYSName.getText().trim());
			} else {
				tryHidePopupWindow();
			}
		});

		tfYSName.textProperty().addListener((observable, oldValue, newValue) -> {
			processYSMCChanged(newValue);
		});

		tfHZName.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
			if (isNowFocused) {
				processHZMCChanged(tfHZName.getText().trim());
			} else {
				tryHidePopupWindow();
				processHZLBChanged(tfHZName.getText().trim());
			}
		});

		tfHZName.textProperty().addListener((observable, oldValue, newValue) -> {
			cbHZLBbox.getItems().clear();
			tfYJNum.clear();
			tfJKNum.clear();
			processHZMCChanged(newValue);
		});

		cbHZLBbox.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
			if (!isNowFocused) {
				calculateFYNum(tfHZName.getText().trim(), cbHZLBbox.getValue());
			}
		});
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public void setIdNum(String idNum) {
		this.idNum = idNum;
	}

	private void calculateFYNum(String HZMC, String HZLB) {
		if (0 == HZMC.length() || null == HZLB) {
			return;
		}
		try (Connection connection = ConnectionDB.getConnection()) {
			boolean ifSenior = HZLB.equals("专家号");
			String sql_query = "select GHFY from T_HZXX where HZMC = ? and SFZJ = ?";
			PreparedStatement pStatement = connection.prepareStatement(sql_query);
			pStatement.setString(1, HZMC);
			pStatement.setBoolean(2, ifSenior);
			ResultSet resultSet = pStatement.executeQuery();
			double GHFY = 0;
			if (resultSet.next()) {
				GHFY = Double.parseDouble(resultSet.getString("GHFY"));

			} else {
				JOptionPane.showMessageDialog(null, "找不到该号种");
				return;
			}
			sql_query = "select YCJE from T_BRXX where BRBH = ?";
			pStatement = connection.prepareStatement(sql_query);
			pStatement.setString(1, idNum);
			resultSet = pStatement.executeQuery();
			if (resultSet.next()) {
				YCJE = Double.parseDouble(resultSet.getString("YCJE"));
			} else {
				JOptionPane.showMessageDialog(null, "数据库出错");
				return;
			}
			if (YCJE < GHFY) {
				currentGHFY = YCJE;
				tfYJNum.setText((GHFY - YCJE) + "");
				tfJKNum.setEditable(true);
			} else {
				currentGHFY = GHFY;
				tfYJNum.setText("0.0");
				tfJKNum.clear();
				tfJKNum.setEditable(false);
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void showList(ResultSet resultSet, TextField textField) {
		currentTextField = textField;
		try {
			ArrayList<String> arrayList = new ArrayList<>();
			while (resultSet.next()) {
				String lineInfo = resultSet.getString(1) + " " + resultSet.getString(2) + " " + resultSet.getString(3);
				arrayList.add(lineInfo);
			}
			if (0 == arrayList.size()) {
				tryHidePopupWindow();
				return;
			}
			if (1 == arrayList.size()) {
				String lineInfo = arrayList.get(0);
				textField.setText(lineInfo.substring(lineInfo.indexOf(' ') + 1, lineInfo.lastIndexOf(' ')));
				tryHidePopupWindow();
				return;
			}
			double screenX = stage.getX() + textField.getScene().getX() + textField.localToScene(0, 0).getX();
			double screenY = stage.getY() + textField.getScene().getY() + textField.localToScene(0, 0).getY()
					+ textField.getHeight();
			initListView();
			listView.getItems().addAll(arrayList);
			listView.setPrefWidth(textField.getWidth() - 3);
			listView.setPrefHeight(80);
			popup.setAutoFix(true);
			popup.getContent().clear();
			popup.getContent().add(listView);
			popup.show(textField, screenX, screenY);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void processKSMCChanged(String newValue) {
		// TODO Autogenerated
		try (Connection connection = ConnectionDB.getConnection()) {
			String like = "%" + newValue + "%";
			String sql_query = "select KSBH,KSMC,PYZS from T_KSXX where KSBH like ? or PYZS like ?";
			PreparedStatement pStatement = connection.prepareStatement(sql_query);
			pStatement.setString(1, like);
			pStatement.setString(2, like);
			ResultSet resultSet = pStatement.executeQuery();
			showList(resultSet, tfKSName);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void processYSMCChanged(String newValue) {
		// TODO AutogeneratedString newValue
		try (Connection connection = ConnectionDB.getConnection()) {
			String like = "%" + newValue + "%";
			String sql_query = "select YSBH,YSMC,PYZS from T_KSYS " + "where YSBH like ? or PYZS like ?";
			PreparedStatement pStatement = connection.prepareStatement(sql_query);
			pStatement.setString(1, like);
			pStatement.setString(2, like);
			ResultSet resultSet = pStatement.executeQuery();
			showList(resultSet, tfYSName);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void processHZLBChanged(String newValue) {
		// TODO Autogenerated
		try (Connection connection = ConnectionDB.getConnection()) {
			ArrayList<String> arrayList = new ArrayList<>();

			String sql_query = "select HZBH from T_HZXX where HZMC = ? and SFZJ = ?";
			PreparedStatement pStatement = connection.prepareStatement(sql_query);
			pStatement.setString(1, newValue);
			pStatement.setBoolean(2, true);
			ResultSet resultSet = pStatement.executeQuery();
			if (resultSet.next()) {
				arrayList.add("专家号");
			} else {
				arrayList.add("普通号");
			}
			cbHZLBbox.getItems().clear();
			cbHZLBbox.getItems().addAll(arrayList);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void processHZMCChanged(String newValue) {
		// TODO Autogenerated
		try (Connection connection = ConnectionDB.getConnection()) {
			String like = "%" + newValue + "%";
			String sql_query = "select HZBH,HZMC,PYZS from T_HZXX " + "where HZBH like ? or PYZS like ?";
			PreparedStatement pStatement = connection.prepareStatement(sql_query);
			pStatement.setString(1, like);
			pStatement.setString(2, like);
			ResultSet resultSet = pStatement.executeQuery();
			showList(resultSet, tfHZName);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void tryHidePopupWindow() {
		if (popup.isShowing()) {
			popup.hide();
		}
	}

	private void initListView() {
		listView = new ListView<>();
		listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				currentTextField.setText(newValue.substring(newValue.indexOf(' ') + 1, newValue.lastIndexOf(' ')));
				tryHidePopupWindow();
			}
		});
	}

	// Event Listener on Button[#btOk].onAction
	@FXML
	public void btCommit(ActionEvent event) {
		System.out.println("OK");
		try (Connection connection = ConnectionDB.getConnection()) {

			String KSMC = tfKSName.getText().trim();
			if (null == KSMC) {
				JOptionPane.showMessageDialog(null, "请输入科室信息");
				return;
			}
			String YSMC = tfYSName.getText().trim();
			if (null == YSMC) {
				JOptionPane.showMessageDialog(null, "请输入医生信息");
				return;
			}
			String HZMC = tfHZName.getText().trim();
			if (null == HZMC) {
				JOptionPane.showMessageDialog(null, "请输入号种信息");
				return;
			}
			String HZLB = cbHZLBbox.getValue();
			if (null == HZLB) {
				JOptionPane.showMessageDialog(null, "请选择号种类别");
				return;
			}

			String JKJE = tfJKNum.getText().trim();
			if (tfJKNum.isEditable() && null == JKJE) {
				JOptionPane.showMessageDialog(null, "余额不足请缴费");
				return;
			}

			String sql_query = "select KSBH from T_KSXX where KSMC = ?";
			PreparedStatement pStatement = connection.prepareStatement(sql_query);
			pStatement.setString(1, KSMC);
			System.out.println(KSMC);
			ResultSet resultSet = pStatement.executeQuery();
			if (resultSet.next()) {
				String KSBH = resultSet.getString("KSBH");
				System.out.println(KSBH);
				sql_query = "select YSBH,KSBH,SFZJ from T_KSYS where YSMC = ?";
				pStatement = connection.prepareStatement(sql_query);
				pStatement.setString(1, YSMC);
				resultSet = pStatement.executeQuery();
				if (resultSet.next()) {
					String YSBH = resultSet.getString("YSBH");
					String YSKSBH = resultSet.getString("KSBH");
					if (!KSBH.equals(YSKSBH)) {
						JOptionPane.showMessageDialog(null, "该医生不在该科室");
						return;
					}
					boolean ifSeniorInTable = resultSet.getBoolean("SFZJ");
					boolean ifSenior = HZLB.equals("专家号");
					if (!ifSeniorInTable && ifSenior) {
						JOptionPane.showMessageDialog(null, "该医生无法挂专家号");
						return;
					}
					sql_query = "select KSBH,HZBH from T_HZXX where HZMC = ?";
					pStatement = connection.prepareStatement(sql_query);
					pStatement.setString(1, HZMC);
					resultSet = pStatement.executeQuery();
					if (resultSet.next()) {
						String HZKSBH = resultSet.getString("KSBH");
						if (!HZKSBH.equals(KSBH)) {
							JOptionPane.showMessageDialog(null, "该号种不在该科室");
							return;
						}
						String HZBH = resultSet.getString("HZBH");
						if (checkHZRC(HZBH)) {
							JOptionPane.showMessageDialog(null, "该号种今日已挂满");
							return;
						}
						double JKNum = 0;
						double YJNum = Double.parseDouble(tfYJNum.getText().trim());
						if (tfJKNum.isEditable()) {
							JKNum = Double.parseDouble(JKJE);
							if (JKNum < YJNum) {
								JOptionPane.showMessageDialog(null, "缴费不足");
								return;
							} else {
								tfZLNum.setText((JKNum - YJNum) + "");
							}
						}

						connection.setAutoCommit(false);
						int GHBH = 0;
						resultSet = connection.createStatement().executeQuery("select count(*) total from T_GHXX");
						if (resultSet.next()) {
							GHBH = resultSet.getInt("total") + 1;
						}

						int GHRC = 1;
						String sql_query_GHRC = "select GHRC from T_GHXX where HZBH = ?";
						pStatement = connection.prepareStatement(sql_query_GHRC);
						pStatement.setString(1, HZBH);
						resultSet = pStatement.executeQuery();
						if (resultSet.next()) {
							GHRC = resultSet.getInt("GHRC") + 1;
							System.out.println(GHRC);
							String sql_update_GHRC = "update T_GHXX set GHRC = ? where HZBH = ?";
							pStatement = connection.prepareStatement(sql_update_GHRC);
							pStatement.setInt(1, GHRC);
							pStatement.setString(2, HZBH);
							pStatement.executeUpdate();
						}

						YCJE -= currentGHFY;
						if (YCJE < 0) {
							System.out.println(YCJE);
						}
						String sql_update = "update T_BRXX set YCJE = ? where BRBH = ?";
						pStatement = connection.prepareStatement(sql_update);
						pStatement.setBigDecimal(1, new BigDecimal(YCJE));
						pStatement.setString(2, idNum);
						pStatement.executeUpdate();
						// double GH = currentGHFY + YJJE;
						String sql_insert = "insert into T_GHXX(GHBH,HZBH,YSBH,BRBH,GHRC,THBZ,GHFY,RQSJ) values (?,?,?,?,?,?,?,?)";
						pStatement = connection.prepareStatement(sql_insert);
						pStatement.setString(1, String.format("%06d", GHBH));
						pStatement.setString(2, HZBH);
						pStatement.setString(3, YSBH);
						pStatement.setString(4, idNum);
						pStatement.setInt(5, GHRC);
						pStatement.setBoolean(6, false);
						pStatement.setBigDecimal(7, new BigDecimal(currentGHFY + YJNum));
						pStatement.setTimestamp(8, ConnectionDB.getDBTime());
						pStatement.executeUpdate();
						connection.commit();
						tfGHNum.setText(String.format("%06d", GHBH));
						JOptionPane.showMessageDialog(null, "挂号成功");
					} else {
						JOptionPane.showMessageDialog(null, "未找到号种");
						return;
					}
				} else {
					JOptionPane.showMessageDialog(null, "未找到该医生");
					return;
				}
			} else {
				JOptionPane.showMessageDialog(null, "未找到该科室");
				return;
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean checkHZRC(String HZBH) {
		return false;
	}

	// Event Listener on Button[#btClear].onAction
	@FXML
	public void clearScreen(ActionEvent event) {
		tfKSName.clear();
		tfYSName.clear();
		tfHZName.clear();
		tryHidePopupWindow();
		cbHZLBbox.getItems().clear();
		tfJKNum.clear();
		tfYJNum.clear();
		tfZLNum.clear();
		tfGHNum.clear();
	}

	// Event Listener on Button[#btCancel].onAction
	@FXML
	public void cancelClicked(ActionEvent event) {
		// TODO Autogenerated
		stage.close();
	}
}
