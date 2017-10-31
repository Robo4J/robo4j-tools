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

import com.robo4j.tools.camera.model.CameraCenterProperties;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class CameraCenterUtil {

    public static void initializeStage(Stage stage, CameraCenterProperties properties) {
        stage.setTitle(properties.getTitle());
        stage.getIcons().add(createIcon("robo4j256.png"));
        stage.getIcons().add(createIcon("robo4j128.png"));
        stage.getIcons().add(createIcon("robo4j64.png"));
        stage.getIcons().add(createIcon("robo4j32.png"));
        stage.getIcons().add(createIcon("robo4j16.png"));
    }

    private static Image createIcon(String iconName) {
        return new Image(Thread.currentThread().getContextClassLoader().getResourceAsStream(iconName));
    }
}
