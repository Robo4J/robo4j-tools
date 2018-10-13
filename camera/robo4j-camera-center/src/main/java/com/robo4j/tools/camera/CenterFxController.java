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
import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationBuilder;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.units.HttpClientUnit;
import com.robo4j.socket.http.util.RoboHttpUtils;
import com.robo4j.tools.camera.enums.SupportedConfigElements;
import com.robo4j.tools.camera.model.CameraCenterProperties;
import com.robo4j.tools.camera.model.CameraDevice;
import com.robo4j.tools.camera.model.SimpleRawElement;
import com.robo4j.tools.camera.processor.ConfigurationProcessor;
import com.robo4j.tools.camera.processor.ImageProcessor;
import com.robo4j.tools.camera.utils.CameraCenterUtils;
import com.robo4j.util.SystemUtil;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Single camera application controller
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class CenterFxController implements CenterController {
    public static final String NO_SIGNAL_IMAGE = "20161021_NoSignal_640.png";
    public static final int CAMERA_IMAGE_WIDTH = 640;
    public static final int CAMERA_IMAGE_HEIGHT = 480;
    public  static final String IMAGE_PROCESSOR1 = "imageProcessor";

    public static void initCameraView(ImageView imageView, Image image) {
        imageView.setImage(image);
        imageView.setFitWidth(CAMERA_IMAGE_WIDTH);
        imageView.setFitHeight(CAMERA_IMAGE_HEIGHT);
        imageView.setSmooth(true);
        imageView.setCache(true);
    }

    private static final String LABEL_READY = "Ready";
    private static final String BUTTON_ACTIVATED = "Activated";
    private static final String IMAGE_FORMAT = "png";
    private static final String HTTP_CLIENT = "httpClient";
    private static final String DEFAULT_NONAME = "noname";

    private String deviceIp;
    private RoboContext roboSystem;
    private boolean cameraActive = false;

    @FXML
    private Button buttonActive;

    @FXML
    private ImageView cameraImageView;

    @FXML
    private TextField imageNameTextField;

    @FXML
    private TextField ipTF;

    @FXML
    private Label stateL;

    @FXML
    private TableView<SimpleRawElement> systemTV;

    @FXML
    private TableView<SimpleRawElement> configImageTV;

    private CameraCenterProperties properties;
    private CameraDevice cameraDevice;
    private RoboBuilder roboBuilder;

    @Override
    public void init(CameraCenterProperties properties, RoboBuilder roboBuilder) {
        this.properties = properties;
        ImageProcessor imageProcessor = new ImageProcessor(roboBuilder.getContext(), ImageProcessor.NAME);
        imageProcessor.setImageView(cameraImageView);
        ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(roboBuilder.getContext(), ConfigurationProcessor.NAME);
        configurationProcessor.setTableView(systemTV);
        try {
            roboBuilder.addAll(imageProcessor, configurationProcessor);
        } catch (RoboBuilderException e) {
            SimpleLoggingUtil.error(getClass(), "error" + e);
        }
        this.roboBuilder = roboBuilder;
    }

    @FXML
    public void initialize() {
        Image image = new Image(Thread.currentThread().getContextClassLoader().getResourceAsStream(NO_SIGNAL_IMAGE));
        initCameraView(cameraImageView, image);
    }

    @FXML
    private void buttonActionClick(ActionEvent event) {
        if (cameraActive) {
            SimpleLoggingUtil.print(getClass(), "scheduler active");
        } else {

            properties = adjustProperties(roboBuilder, properties);
            this.roboSystem = roboBuilder.build();
            cameraDevice = new CameraDevice(properties.getDeviceIP(), Integer.valueOf(properties.getDevicePort()));
            CameraCenterUtils.initCameraConfigTV(configImageTV);

            start();
            stateL.setText(LABEL_READY);
            buttonActive.setText(BUTTON_ACTIVATED);
            cameraActive = true;
        }
        CameraCenterUtils.sendRequestForClientConfiguration(roboSystem, ConfigurationProcessor.NAME, HTTP_CLIENT, cameraDevice);
    }

    @FXML
    private void onConfigImageButtonAction(ActionEvent event) {
        CameraCenterUtils.buttonImageConfigClick(roboSystem, HTTP_CLIENT, configImageTV, cameraDevice);
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

    @Override
    public void stop() {
        if (roboSystem != null) {
            roboSystem.shutdown();
            System.out.println(SystemUtil.printStateReport(roboSystem));
        }
        System.out.println("Bye!");
    }

    @Override
    public void start() {
        roboSystem.start();
        System.out.println(SystemUtil.printStateReport(roboSystem));
    }

    private CameraCenterProperties adjustProperties(RoboBuilder roboBuilder, CameraCenterProperties properties) {
        if (!ipTF.getText().isEmpty()) {
            Map<SupportedConfigElements, String> map = new HashMap<>();
            map.put(SupportedConfigElements.DEVICE_IP, ipTF.getText());
            map.put(SupportedConfigElements.DEVICE_PORT, properties.getDevicePort());
            map.put(SupportedConfigElements.TITLE, properties.getTitle());

            Configuration configuration = new ConfigurationBuilder().addString(RoboHttpUtils.PROPERTY_HOST, ipTF.getText()).
                    addInteger(RoboHttpUtils.PROPERTY_SOCKET_PORT, Integer.valueOf(properties.getDevicePort())).build();
            try {
                roboBuilder.add(HttpClientUnit.class, configuration, HTTP_CLIENT);
            } catch (RoboBuilderException e) {
                e.printStackTrace();
            }

            return new CameraCenterProperties(map);
        } else {
            return properties;
        }
    }
}
