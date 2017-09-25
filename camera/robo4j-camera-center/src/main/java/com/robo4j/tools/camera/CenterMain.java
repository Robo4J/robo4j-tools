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
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Camera Utility
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class CenterMain extends Application {

    private static final String ROBO4J_CONFIGURATION = "robo4jUnits.xml";
    private static final String CAM_CENTER_CONFIGURATION = "robo4jConfigCenter.xml";
    private static final String ROBO4J_CENTER_FXML = "robo4jCenter.fxml";
    public static final int ROBO4J_CONFIG = 1;
    public static final int ROBO4J_CENTER_CONFIG = 2;
    private CenterFxController controller;
    private static String robo4jConfigFileName;
    private static String cameraCenterConfigFileName;

    public static void main(String[] args) throws Exception {
        switch (args.length) {
            case ROBO4J_CONFIG:
                System.out.println("configuration: robo4j");
                robo4jConfigFileName = args[0];
                break;
            case ROBO4J_CENTER_CONFIG:
                System.out.println("configuration: robo4j, center");
                robo4jConfigFileName = args[0];
                cameraCenterConfigFileName = args[1];
                break;
            default:
                System.out.println("configuration: default");
                break;
        }
        Application.launch(args);
    }


    @Override
    public void start(Stage stage) throws Exception {
        URL file = Thread.currentThread().getContextClassLoader().getResource(ROBO4J_CENTER_FXML);

        final InputStream robo4jConfig = robo4jConfigFileName == null ?
                getClass().getClassLoader().getResourceAsStream(ROBO4J_CONFIGURATION) :
                Files.newInputStream(Paths.get(robo4jConfigFileName));

        final InputStream camCenterConfig = cameraCenterConfigFileName == null ?
                getClass().getClassLoader().getResourceAsStream(CAM_CENTER_CONFIGURATION) :
                Files.newInputStream(Paths.get(cameraCenterConfigFileName));

        final RoboBuilder builder = new RoboBuilder();
        builder.add(robo4jConfig);
        CameraCenterProperties properties = new CenterBuilder().add(camCenterConfig).build();

        final FXMLLoader fxmlLoader = new FXMLLoader(file);
        BorderPane myPane = fxmlLoader.load();
        controller = fxmlLoader.getController();
        controller.init(properties, builder);

        stage.setScene(new Scene(myPane, 800, 600));
        myPane.setStyle("-fx-border-color:black");
        initializeStage(stage);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        controller.stop();
    }

    private void initializeStage(Stage stage) {
        stage.setTitle("Robo4J Center");
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
