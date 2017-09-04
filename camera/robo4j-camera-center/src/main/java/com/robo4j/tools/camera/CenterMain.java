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

import com.robo4j.core.RoboBuilder;
import com.robo4j.core.RoboContext;
import com.robo4j.core.util.SystemUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Camera Utility
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class CenterMain extends Application {

    private static final String ROBO4J_CENTER_FXML = "robo4jCenter.fxml";
    private RoboContext roboSystem;

    public static void main(String[] args) throws Exception {
        Application.launch(args);
    }


    @Override
    public void start(Stage stage) throws Exception {
        URL file = Thread.currentThread().getContextClassLoader().getResource(ROBO4J_CENTER_FXML);
        RoboBuilder builder = new RoboBuilder(Thread.currentThread().getContextClassLoader().getResourceAsStream("robo4jSystem.xml"));
        builder.add(Thread.currentThread().getContextClassLoader().getResourceAsStream("robo4jUnits.xml"));
        roboSystem = builder.build();
        roboSystem.start();

        FXMLLoader fxmlLoader = new FXMLLoader(file);
        BorderPane myPane = fxmlLoader.load();
        CenterFxController controller = fxmlLoader.getController();
        controller.init(roboSystem);

        stage.setScene(new Scene(myPane, 800, 600));
        myPane.setStyle("-fx-border-color:black");
        initializeStage(stage);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        roboSystem.stop();
        System.out.println("State after stop:");
        System.out.println(SystemUtil.printStateReport(roboSystem));
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
