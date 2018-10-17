/*
 * Copyright (c) 2014, 2018, Marcus Hirt, Miroslav Wengner
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
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.InputStream;
import java.net.URL;

/**
 * CenterLookupMain provides UI used lookup feature
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class CenterLookupMain extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }

    private CenterFxLookupController controller;

    @Override
    public void start(Stage stage) throws Exception {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL fxFile = classLoader.getResource("robo4jCenterLookup.fxml");
        InputStream systemIS = classLoader.getResourceAsStream("robo4jSystemLookup.xml");
        InputStream contextIS = classLoader.getResourceAsStream("robo4jUnitsLookup.xml");

        final FXMLLoader fxmlLoader = new FXMLLoader(fxFile);
        BorderPane myPane = fxmlLoader.load();
        stage.setScene(new Scene(myPane, 800, 600));
        myPane.setStyle("-fx-border-color:black");

        final RoboBuilder builder = new RoboBuilder(systemIS);
        builder.add(contextIS);

        controller = fxmlLoader.getController();
        controller.init(builder);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Bye Lookup Center!");
        super.stop();
        controller.stop();
    }
}
