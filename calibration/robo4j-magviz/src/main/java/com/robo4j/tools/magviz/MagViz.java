/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.tools.magviz;

import java.io.File;
import java.net.URL;
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
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class MagViz extends Application {

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		File csvFile = parseFileParam();

		URL file = Thread.currentThread().getContextClassLoader().getResource("magviz.fxml");
		FXMLLoader fxmlLoader = new FXMLLoader(file);
		BorderPane myPane = (BorderPane) fxmlLoader.load();
		MagVizController controller = fxmlLoader.getController();
		controller.loadFile(csvFile);
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
		return new Image(getClass().getClassLoader().getResourceAsStream(iconName));
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
