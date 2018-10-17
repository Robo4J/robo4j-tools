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

import com.robo4j.ConfigurationException;
import com.robo4j.RoboBuilder;
import com.robo4j.RoboBuilderException;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationBuilder;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.net.LookupServiceProvider;
import com.robo4j.socket.http.codec.VideoConfigMessage;
import com.robo4j.socket.http.enums.VideoMessageType;
import com.robo4j.tools.camera.model.SimpleRawElement;
import com.robo4j.tools.camera.unit.VideoSocketServerUnit;
import com.robo4j.tools.camera.utils.VideoCenterUtils;
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
 * CenterFxLookupVideoController
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class CenterFxLookupVideoController implements LookupCenterController {

    @FXML
    private ImageView mediaIV;

    @FXML
    private TableView<SimpleRawElement> videoConfTV;

    @FXML
    private Label contextNameL;

    private RoboContext system;

    private String contextName;
    private String videoConfigUnit;
    private Integer port;
    private VideoSocketServerUnit server;

    @Override
    public void init(RoboBuilder builder) throws RoboBuilderException, ConfigurationException {

        Configuration conf = new ConfigurationBuilder()
                .addInteger(VideoSocketServerUnit.PROP_SERVER_PORT, port)
                .addString(VideoSocketServerUnit.PROP_SOURCE_CONTEXT, contextName)
                .addString(VideoSocketServerUnit.PROP_SOURCE_VIDEO_CONFIG_UNIT, videoConfigUnit)
                .build();
        server = new VideoSocketServerUnit(builder.getContext(), VideoSocketServerUnit.NAME);
        server.initialize(conf);
        server.setImageView(mediaIV);
        builder.add(server);
        system = builder.build();
    }

    public RoboContext getSystem() {
        return system;
    }

    public void setContextName(String contextName) {
        this.contextName = contextName;
        contextNameL.setText(contextName);
    }

    public void setVideoConfigUnit(String videoConfigUnit) {
        this.videoConfigUnit = videoConfigUnit;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void initVideoConfig() {
        VideoCenterUtils.initVideoConfigTV(videoConfTV);
    }

    @FXML
    private void onClickVideoConfig(ActionEvent event) {
        RoboReference<VideoConfigMessage> configUnit = LookupServiceProvider.getDefaultLookupService().getContext(contextName).getReference("videoConfigUnit");
        server.sendMessage(VideoSocketServerUnit.MESSAGE_STOP);
        VideoCenterUtils.sendStopMessage(configUnit);
        server.sendMessage(VideoSocketServerUnit.MESSAGE_START);

        final Map<RpiCameraProperty, String> videoConf = videoConfTV.getItems()
                .stream()
                .collect(Collectors.toMap(k ->
                                RpiCameraProperty.getByName(k.getName())
                        , SimpleRawElement::getValue));
        final VideoConfigMessage videoConfigMessage = createVideoConfigMessage(videoConf);
        SimpleLoggingUtil.info(getClass(), "SEND: " + videoConf);
        configUnit.sendMessage(videoConfigMessage);

    }


    private VideoConfigMessage createVideoConfigMessage(Map<RpiCameraProperty, String> map) {
        final VideoConfigMessage result = new VideoConfigMessage();
        result.setType(VideoMessageType.CONFIG);
        result.setWidth(Integer.valueOf(map.get(RpiCameraProperty.WIDTH)));
        result.setHeight(Integer.valueOf(map.get(RpiCameraProperty.HEIGHT)));
        result.setRotation(Integer.valueOf(map.get(RpiCameraProperty.ROTATION)));
        result.setTimeout(Integer.valueOf(map.get(RpiCameraProperty.TIMEOUT)));
        return result;
    }

}
