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
import com.robo4j.RoboBuilderException;
import com.robo4j.RoboContext;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.util.StringConstants;
import com.robo4j.util.SystemUtil;
import com.robo4j.tools.camera.processor.ConfigurationProcessor;
import com.robo4j.tools.camera.processor.ImageProcessor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class CenterFxController {
    private static final String NO_SIGNAL_IMAGE = "20161021_NoSignal_640.png";
    private static final int CAMERA_IMAGE_WIDTH = 640;
    private static final int CAMERA_IMAGE_HEIGHT = 480;
    private static final String LABEL_READY = "Ready";
    private static final String BUTTON_ACTIVATED = "Activated";
    private static final String IMAGE_FORMAT = "png";
    private static final String IMAGE_PROCESSOR1 = "imageProcessor";
    private static final String CONFIGURATION_PROCESSOR = "configurationProcessor";
    private static final String CAMERA_CLIENT = "http://192.168.178.67:8025/";
    public static final String DEFAULT_NONAME = "noname";

    private RoboContext roboSystem;
    private boolean cameraActive = false;

    @FXML
    private Button buttonActive;


    @FXML
    private ImageView cameraImageView;

    @FXML
    private TextField imageNameTextField;

    @FXML
    private Label stateL;

    @FXML
    private TableView<RawUnit> systemTV;


    void init(RoboBuilder roboBuilder) {
        ImageProcessor imageProcessor = new ImageProcessor(roboBuilder.getContext(), IMAGE_PROCESSOR1);
        imageProcessor.setImageView(cameraImageView);
        ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(roboBuilder.getContext(), CONFIGURATION_PROCESSOR);
        configurationProcessor.setTableView(systemTV);
        try {
            roboBuilder.add(imageProcessor);
            roboBuilder.add(configurationProcessor);
        } catch (RoboBuilderException e){
            SimpleLoggingUtil.error(getClass(), "error" + e);
        }
        this.roboSystem = roboBuilder.build();
    }

    @FXML
    public void initialize() {
        Image image = new Image(Thread.currentThread().getContextClassLoader().getResourceAsStream(NO_SIGNAL_IMAGE));
        cameraImageView.setImage(image);
        cameraImageView.setFitWidth(CAMERA_IMAGE_WIDTH);
        cameraImageView.setFitHeight(CAMERA_IMAGE_HEIGHT);
        cameraImageView.setSmooth(true);
        cameraImageView.setCache(true);
    }

    @FXML
    private void buttonActionClick(ActionEvent event) {
        if (cameraActive) {
            SimpleLoggingUtil.print(getClass(), "scheduler active");
        } else {
            roboSystem.start();
            stateL.setText(LABEL_READY);
            buttonActive.setText(BUTTON_ACTIVATED);

            cameraActive = true;
            roboSystem.getReference("configurationProcessor").sendMessage(CAMERA_CLIENT);
        }
    }

    @FXML
    private void saveButtonAction(ActionEvent event) {
        String fileName = imageNameTextField.getText().isEmpty() ? DEFAULT_NONAME : imageNameTextField.getText();
        Path path = Paths.get(fileName.concat(".").concat(IMAGE_FORMAT));
        BufferedImage bImage = SwingFXUtils.fromFXImage(cameraImageView.getImage(), null);
        try {
            ImageIO.write(bImage, IMAGE_FORMAT, path.toFile());
        } catch (IOException e) {
            SimpleLoggingUtil.error(getClass(), "image error", e);
        }
    }

    public void stop(){
        System.out.println("State after stop:");
        System.out.println(SystemUtil.printStateReport(roboSystem));
        roboSystem.shutdown();
    }
}
