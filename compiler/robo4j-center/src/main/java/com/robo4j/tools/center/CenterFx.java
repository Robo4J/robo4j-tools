/*
 * Copyright (C) 2017. Miroslav Kopecky
 * This CenterFx.java  is part of robo4j.
 * path: /Users/miroslavkopecky/GiTHub_MiroKopecky/robo4j-tools/compiler/robo4j-center/src/main/java/com/robo4j/tools/center/CenterFx.java
 * module: robo4j-center_main
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.tools.center;

import java.io.InputStream;
import java.net.URL;

import com.robo4j.tools.center.builder.CenterBuilder;
import com.robo4j.tools.center.model.CenterProperties;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * JavaFX Robo4J Compiler
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class CenterFx extends Application {

	private static final String ROBO4J_CENTER_FXML = "robo4jCenter.fxml";

	public static void main(String[] args) throws Exception {
		Application.launch(args);
	}


    @Override
	public void start(Stage stage) throws Exception {
		URL file = Thread.currentThread().getContextClassLoader().getResource(ROBO4J_CENTER_FXML);
        InputStream isConfig = Thread.currentThread().getContextClassLoader().getResourceAsStream("robo4jCenter.xml");
        CenterBuilder builder = new CenterBuilder().add(isConfig);

		FXMLLoader fxmlLoader = new FXMLLoader(file);
		BorderPane myPane = fxmlLoader.load();
        CenterFxController controller = fxmlLoader.getController();
		controller.init(builder.build());
		stage.setScene(new Scene(myPane, 600, 400));
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
}
