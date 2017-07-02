package com.robo4j.tools.magviz;

import java.io.File;
import java.util.List;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Visualizer for 3D data.
 * 
 * @author marcus
 */
public class MagViz extends Application {
	
	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		File csvFile = parseFileParam();
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource
    		    ("magviz.fxml"));
	    BorderPane myPane = (BorderPane)fxmlLoader.load();
	    MagVizController controller = fxmlLoader.getController();
	    controller.initializeSubScenes(csvFile);
	    stage.setScene(new Scene(myPane, 800, 600));
	    myPane.setStyle("-fx-border-color:black");
	    initializeStage(stage); 
		stage.show();
	}

	private void initializeStage(Stage stage) {
		stage.setTitle("Magnetometer Calibration Utility");
	    stage.getIcons().add(createIcon("robo4j256.png"));
	    stage.getIcons().add(createIcon("robo4j128.png"));
	    stage.getIcons().add(createIcon("robo4j64.png"));
	    stage.getIcons().add(createIcon("robo4j32.png"));
	    stage.getIcons().add(createIcon("robo4j16.png"));
	}

	private Image createIcon(String iconName) {
		return new Image(MagViz.class.getClassLoader().getResourceAsStream(iconName));
	}

	private File parseFileParam() {
		List<String> params = getParameters().getRaw();
		if (params.isEmpty()) {
			System.out.println("Warning - no csv file specified!");
			return null;
		}
		File f = new File(params.get(0));
		if (!f.exists()) {
			System.out.println("Warning - the csv file specified (" + f.getName() + ") does not exist!");
			return null;
		}
		return f;
	}

}
