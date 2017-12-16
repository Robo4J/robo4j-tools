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
import com.robo4j.socket.http.HttpHeaderFieldNames;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.HttpVersion;
import com.robo4j.socket.http.message.HttpDecoratedRequest;
import com.robo4j.socket.http.util.JsonUtil;
import com.robo4j.socket.http.util.RequestDenominator;
import com.robo4j.socket.http.util.RoboHttpUtils;
import com.robo4j.tools.camera.model.CameraCenterProperties;
import com.robo4j.tools.camera.model.CameraDevice;
import com.robo4j.tools.camera.model.RawElement;
import com.robo4j.tools.camera.processor.ConfigurationProcessor;
import com.robo4j.tools.camera.processor.ImageProcessor;
import com.robo4j.tools.camera.utils.CameraCenterUtils;
import com.robo4j.util.SystemUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Multiple cameras application controller
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class CenterFxMultipleController implements CenterController {

    private static final String NO_SIGNAL_IMAGE = "20161021_NoSignal_640.png";
    private static final int CAMERA_IMAGE_WIDTH = 480;
    private static final int CAMERA_IMAGE_HEIGHT = 320;
    private static final String LABEL_READY = "Ready";
    private static final String BUTTON_ACTIVATED = "Activated";
    private static final String IMAGE_FORMAT = "jpg";
    private static final String IMAGE_PROCESSOR1 = "imageProcessor1";
    private static final String IMAGE_PROCESSOR2 = "imageProcessor2";
    private static final String CONFIGURATION_PROCESSOR1 = "configurationProcessor1";
    private static final String CONFIGURATION_PROCESSOR2 = "configurationProcessor2";
    private static final String DEFAULT_NONAME = "noname";
    private static final String CAMERA_NAME_1 = "camera1";
    private static final String CAMERA_NAME_2 = "camera2";
    private static final String UNIT_NAME_CONFIGURATION_PROCESSOR = "configurationProcessor";
    private static final String UNIT_NAME_HTTP_CLIENT = "httpClient";

    private RoboContext roboSystem;
    private Map<String, CameraDevice> cameraDevices;
    private CameraCenterProperties properties;
    private boolean contextStarted = false;

    @FXML
    private Button activateB1;

    @FXML
    private Button activateB2;

    @FXML
    private ImageView cameraIV1;

    @FXML
    private ImageView cameraIV2;

    @FXML
    private TableView<RawElement> systemTV1;

    @FXML
    private TableView<RawElement> systemTV2;

    @FXML
    private TableView<RawElement> configImageTV1;

    @FXML
    private TableView<RawElement> configImageTV2;

    @FXML
    private void imageProcessButton1(ActionEvent event){
        CameraCenterUtils.buttonImageConfigClick(roboSystem, UNIT_NAME_HTTP_CLIENT, configImageTV1, cameraDevices.get(CAMERA_NAME_1));
    }

    @FXML
    private void imageProcessButton2(ActionEvent event){
        CameraCenterUtils.buttonImageConfigClick(roboSystem, UNIT_NAME_HTTP_CLIENT, configImageTV2, cameraDevices.get(CAMERA_NAME_2));
    }


    private void activateButtonPress(String cameraDeviceName, String configurationProcessorName) {
        startSystem();

        CameraDevice cameraDevice = cameraDevices.get(cameraDeviceName);
        if (cameraDevice.isActive()) {
            SimpleLoggingUtil.print(getClass(), "ACTIVATED: " + cameraDevice);
        } else {
            cameraDevice.setActive(true);

        }
        CameraCenterUtils.sendRequestForClientConfiguration(roboSystem, configurationProcessorName, UNIT_NAME_HTTP_CLIENT, cameraDevice);
    }


    @Override
    public void init(CameraCenterProperties properties, RoboBuilder roboBuilder) {
        ImageProcessor imageProcessor1 = new ImageProcessor(roboBuilder.getContext(), IMAGE_PROCESSOR1);
        imageProcessor1.setImageView(cameraIV1);

        ImageProcessor imageProcessor2 = new ImageProcessor(roboBuilder.getContext(), IMAGE_PROCESSOR2);
        imageProcessor2.setImageView(cameraIV2);

        ConfigurationProcessor configurationProcessor1 = new ConfigurationProcessor(roboBuilder.getContext(), CONFIGURATION_PROCESSOR1);
        configurationProcessor1.setTableView(systemTV1);

        ConfigurationProcessor configurationProcessor2 = new ConfigurationProcessor(roboBuilder.getContext(), CONFIGURATION_PROCESSOR2);
        configurationProcessor2.setTableView(systemTV2);

        Stream.of(imageProcessor1, imageProcessor2, configurationProcessor1, configurationProcessor2)
                .forEach(u -> {
                    try {
                        roboBuilder.add(u);
                    } catch (RoboBuilderException e) {
                        SimpleLoggingUtil.error(getClass(), "error" + e);
                    }
                });

        this.roboSystem = roboBuilder.build();
        this.properties = properties;

        cameraDevices = new HashMap<>();
        cameraDevices.put(CAMERA_NAME_1, new CameraDevice("192.168.0.14", 8035));
        cameraDevices.put(CAMERA_NAME_2, new CameraDevice("192.168.0.2", 8035));

        activateB1.setOnAction((e) -> {
            activateButtonPress(CAMERA_NAME_1, CONFIGURATION_PROCESSOR1);
        });

        activateB2.setOnAction((e) -> activateButtonPress(CAMERA_NAME_2, CONFIGURATION_PROCESSOR2));
        CameraCenterUtils.initCameraConfigTV(configImageTV1);
        CameraCenterUtils.initCameraConfigTV(configImageTV2);
    }

    private void startSystem() {
        if (!contextStarted) {
            contextStarted = true;
            roboSystem.start();
            System.out.println(SystemUtil.printStateReport(roboSystem));
        }
    }

    @Override
    public void start() {
        roboSystem.start();
        System.out.println(SystemUtil.printStateReport(roboSystem));
    }

    @Override
    public void stop() {
        System.out.println("State after stop:");
        roboSystem.shutdown();
        System.out.println(SystemUtil.printStateReport(roboSystem));
    }

}
