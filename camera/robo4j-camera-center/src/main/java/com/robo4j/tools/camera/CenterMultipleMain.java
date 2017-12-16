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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.tools.camera;

import com.robo4j.RoboBuilder;
import com.robo4j.tools.camera.builder.CenterBuilder;
import com.robo4j.tools.camera.model.CameraCenterProperties;
import com.robo4j.tools.camera.utils.CameraCenterUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.InputStream;
import java.net.URL;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class CenterMultipleMain extends Application {
    private static final String ROBO4J_CONFIGURATION = "robo4jMultipleUnits.xml";
    private static final String CAM_CENTER_CONFIGURATION = "robo4jConfigCenterMultiple.xml";
    private static final String ROBO4J_CENTER_FXML = "robo4jCenterCameras.fxml";
    private CenterFxMultipleController controller;

    public static void main(String[] args) throws Exception {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        URL file = Thread.currentThread().getContextClassLoader().getResource(ROBO4J_CENTER_FXML);

        final InputStream robo4jConfig = getClass().getClassLoader().getResourceAsStream(ROBO4J_CONFIGURATION);
        final InputStream camCenterConfig = getClass().getClassLoader().getResourceAsStream(CAM_CENTER_CONFIGURATION);

        final RoboBuilder builder = new RoboBuilder();
        builder.add(robo4jConfig);
        CameraCenterProperties properties = new CenterBuilder().add(camCenterConfig).build();

        final FXMLLoader fxmlLoader = new FXMLLoader(file);
        BorderPane myPane = fxmlLoader.load();
        controller = fxmlLoader.getController();
        controller.init(properties, builder);

        stage.setScene(new Scene(myPane, 1024, 768));
        myPane.setStyle("-fx-border-color:black");
        CameraCenterUtils.initializeStage(stage, properties);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        controller.stop();
    }
}
