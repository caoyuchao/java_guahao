package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			// Read file fxml and draw interface.
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml"));
			Parent root = fxmlLoader.load();
			LoginController controller = fxmlLoader.getController();
			primaryStage.setResizable(false);
			controller.setStage(primaryStage);
			primaryStage.setTitle("My Application");
			primaryStage.setScene(new Scene(root));
			controller.init();
			primaryStage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}