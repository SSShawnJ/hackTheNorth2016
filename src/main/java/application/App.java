package application;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class App extends Application {

	Stage window;
	
	
	public static void main(String[] args) {
		launch(args);
	}
	
	
	
	@Override
	public void start(Stage primaryStage) throws IOException{
		window=primaryStage;
		window.setTitle("YouEye");
		
		
		//initial page
		Pane InitialPageRoot=FXMLLoader.load(getClass().getResource("StartPage.fxml"));
		Scene InitialPageScene=new Scene(InitialPageRoot);
		
		window.setScene(InitialPageScene);
		window.show();
	}
	
}

