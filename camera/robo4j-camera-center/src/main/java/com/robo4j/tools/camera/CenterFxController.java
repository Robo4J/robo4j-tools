/*
 * Copyright (C) 2017. Miroslav Kopecky
 * This CenterFxController.java  is part of robo4j.
 * path: /Users/miroslavkopecky/GiTHub_MiroKopecky/robo4j-tools/camera/robo4j-camera-center/src/main/java/com/robo4j/tools/camera/CenterFxController.java
 * module: robo4j-camera-center_main
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

package com.robo4j.tools.camera;

import com.robo4j.core.RoboContext;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.tools.camera.processor.CameraViewProcessor;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class CenterFxController {
    private static final String IMAGE_PROCESSOR = "imageProcessor";
    private static final String NO_SIGNAL_IMAGE = "20161021_NoSignal_640.png";
    private static final int CAMERA_IMAGE_WIDTH = 640;
    private static final int CAMERA_IMAGE_HEIGHT = 480;
    private static final String BUTTON_ACTIVATED = "Activated";
    private static final String IMAGE_FORMAT = "png";
    public static final String DEFAULT_NONAME = "noname";

    private RoboContext roboSystem;
    private boolean cameraActive = false;

    @FXML
    private Button buttonActive;


    @FXML
    private ImageView cameraImageView;

    @FXML
    private TextField imageNameTextField;


    void init(RoboContext roboSystem) {
        this.roboSystem = roboSystem;
    }

    @FXML
    public void initialize(){
        Image image = new Image(Thread.currentThread().getContextClassLoader().getResourceAsStream(NO_SIGNAL_IMAGE));
        cameraImageView.setImage(image);
        cameraImageView.setFitWidth(CAMERA_IMAGE_WIDTH);
        cameraImageView.setFitHeight(CAMERA_IMAGE_HEIGHT);
        cameraImageView.setSmooth(true);
        cameraImageView.setCache(true);
    }

    @FXML
    private void buttonActionClick(ActionEvent event){
        if (cameraActive) {
            SimpleLoggingUtil.print(getClass(), "scheduler active");
        } else {
            buttonActive.setText(BUTTON_ACTIVATED);
            roboSystem.getScheduler().scheduleAtFixedRate(new CameraViewProcessor(roboSystem.getReference(IMAGE_PROCESSOR),
                    cameraImageView), 1, 400, TimeUnit.MILLISECONDS);
        }
    }

    @FXML
    private void saveButtonAction(ActionEvent event){
        String fileName = imageNameTextField.getText().isEmpty() ? DEFAULT_NONAME : imageNameTextField.getText();
        Path path = Paths.get(fileName.concat(".").concat(IMAGE_FORMAT));
        BufferedImage bImage = SwingFXUtils.fromFXImage(cameraImageView.getImage(), null);
        try {
            ImageIO.write(bImage, IMAGE_FORMAT, path.toFile() );
        } catch (IOException e){
            SimpleLoggingUtil.error(getClass(), "image error", e);
        }

    }
}
