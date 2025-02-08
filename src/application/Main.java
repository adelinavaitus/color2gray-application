package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
        // Initialize the Controller class, which manages the application's logic
		Controller controller = new Controller();

        controller.selectTab1(primaryStage);    // Call a method to select tab 1 in the application
        controller.selectTab2(primaryStage);    // Call a method to select tab 2 in the application
        
        controller.configureStart();    // Call the configureStart() method, which performs the initial configuration for the application

        // Set the scene for the primary stage (window) to the one managed by the Controller
        primaryStage.setScene(new Scene(controller));
        primaryStage.setTitle("Gray Scale");     	// Set the title of the primary stage (window)
        primaryStage.show(); 						// Show the primary stage (window) on the screen
	}
	
    public static void main(String[] args) {
        // Launch the JavaFX application
        launch(args);
    }
}

