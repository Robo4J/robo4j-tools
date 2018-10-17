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
import com.robo4j.RoboBuilderException;
import com.robo4j.RoboContext;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.net.LookupServiceProvider;
import com.robo4j.socket.http.codec.CameraConfigMessage;
import com.robo4j.tools.camera.model.SimpleRawElement;
import com.robo4j.tools.camera.processor.CannyEdgeDetectorProcessor;
import com.robo4j.tools.camera.processor.ConfigurationProcessor;
import com.robo4j.tools.camera.processor.ImageProcessor;
import com.robo4j.tools.camera.utils.CameraCenterUtils;
import com.robo4j.units.rpi.camera.RpiCameraProperty;
import com.robo4j.util.SystemUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;

import java.util.Map;
import java.util.stream.Collectors;


/**
 * CenterFxLookupCameraController for individual camera
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class CenterFxLookupCameraController implements LookupCenterController {

    @FXML
    private ImageView cameraImageView;

    @FXML
    private TableView<SimpleRawElement> cameraConfTV;

    @FXML
    private Label contextNameL;

    private RoboContext system;

    private String contextName;

    @Override
    public void init(RoboBuilder builder) throws RoboBuilderException {
        ImageProcessor imageProcessor = new ImageProcessor(builder.getContext(), ImageProcessor.NAME);
//        CannyEdgeDetectorProcessor imageProcessor = new CannyEdgeDetectorProcessor(builder.getContext(), ImageProcessor.NAME);
        imageProcessor.setImageView(cameraImageView);
        builder.add(imageProcessor);

        system = builder.build();
    }

    public ImageView getCameraImageView() {
        return cameraImageView;
    }

    public TableView getCameraConfTV() {
        return cameraConfTV;
    }

    public RoboContext getSystem() {
        return system;
    }

    public String getContextName() {
        return contextName;
    }

    public void setContextName(String contextName) {
        this.contextName = contextName;
        contextNameL.setText(contextName);
    }

    public void initCameraConfig() {
        CameraCenterUtils.initCameraConfigTV(cameraConfTV);
    }

    @FXML
    private void onClickCamConfig(ActionEvent event) {
        final Map<RpiCameraProperty, String> camConf = cameraConfTV.getItems()
                .stream()
                .collect(Collectors.toMap(k ->
                                RpiCameraProperty.getByName(k.getName())
                        , SimpleRawElement::getValue));

        final CameraConfigMessage cameraConfigMessage = createCameraConfigMessage(camConf);
        SimpleLoggingUtil.info(getClass(), "SEND: " + camConf);
        LookupServiceProvider.getDefaultLookupService().getContext(contextName).getReference("cameraConfig")
                .sendMessage(cameraConfigMessage);
    }


    private CameraConfigMessage createCameraConfigMessage(Map<RpiCameraProperty, String> map){
        CameraConfigMessage result = new CameraConfigMessage();
        result.setWidth(Integer.valueOf(map.get(RpiCameraProperty.WIDTH)));
        result.setHeight(Integer.valueOf(map.get(RpiCameraProperty.HEIGHT)));
        result.setBrightness(Integer.valueOf(map.get(RpiCameraProperty.BRIGHTNESS)));
        result.setSharpness(Integer.valueOf(map.get(RpiCameraProperty.SHARPNESS)));
        result.setTimelapse(Integer.valueOf(map.get(RpiCameraProperty.TIMELAPSE)));
        return result;
    }
}
