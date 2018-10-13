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

package com.robo4j.tools.camera.unit;

import com.robo4j.BlockingTrait;
import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.tools.camera.CameraCenterException;
import com.twilight.h264.decoder.AVFrame;
import com.twilight.h264.player.FrameUtils;
import com.twilight.h264.player.H264StreamCallback;
import com.twilight.h264.player.RGBListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.robo4j.tools.camera.CenterFxController.NO_SIGNAL_IMAGE;

/**
 * uses h264 codec lib:  {@see https://github.com/neocoretechs/h264j}
 * robo4j implementation will be soon available
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
@BlockingTrait
public class VideoSocketServerUnit extends RoboUnit<String> {

    public static final String NAME = "videoSocketServer";
    public static final String PROP_SERVER_PORT = "serverPort";
    public static final String MESSAGE_START = "start";
    public static final String MESSAGE_STOP = "stop";
    public static final String PROP_SOURCE_CONTEXT = "sourceContext";
    public static final String PROP_SOURCE_VIDEO_CONFIG_UNIT = "sourceVideoConfigUnit";
    private volatile AtomicBoolean active = new AtomicBoolean(false);
    private Integer port;
    private ServerSocket server;
    private ImageView imageView;
    private String sourceContext;
    private String sourceVideoConfigUnit;

    public VideoSocketServerUnit(RoboContext context, String id) {
        super(String.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        port = configuration.getInteger(PROP_SERVER_PORT, null);
        if (port == null) {
            throw ConfigurationException.createMissingConfigNameException(PROP_SERVER_PORT);
        }
        sourceContext = configuration.getString(PROP_SOURCE_CONTEXT, null);
        if (sourceContext == null) {
            throw ConfigurationException.createMissingConfigNameException(PROP_SOURCE_CONTEXT);
        }
        sourceVideoConfigUnit = configuration.getString(PROP_SOURCE_VIDEO_CONFIG_UNIT, null);
        if (sourceVideoConfigUnit == null) {
            throw ConfigurationException.createMissingConfigNameException(PROP_SOURCE_VIDEO_CONFIG_UNIT);
        }
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
        Image image = new Image(Thread.currentThread().getContextClassLoader().getResourceAsStream(NO_SIGNAL_IMAGE));
        imageView.setImage(image);
    }

    @Override
    public void onMessage(String message) {
        switch (message) {
            case MESSAGE_START:
                if (!active.get()) {
                    active.set(true);
                    try {
                        server = new ServerSocket(port);
                    } catch (IOException e) {
                        throw new CameraCenterException(e);
                    }

                    getContext().getScheduler().execute(() -> {
                        try {
                            System.out.println("Start New server port: " + port);
                            while (active.get()) {
                                try (Socket socket = server.accept()) {
                                    InputStream is = socket.getInputStream();
                                    playInputStream(is);
                                }
                            }
                        } catch (IOException e) {
                            throw new CameraCenterException(e);
                        }

                    });
                }
                break;
            case MESSAGE_STOP:
                if (server != null) {
                    try {
                        server.close();
                        System.out.println("video server closed: " + server.isClosed() + ", port: " + port);
                        server = null;
                    } catch (IOException e) {
                        throw new CameraCenterException(e);
                    }
                }
                active.set(false);
                break;
            default:
                SimpleLoggingUtil.error(getClass(), "not implemented:" + message);
        }
    }

    private void playInputStream(InputStream is) {
        RGBListener listener = (AVFrame picture) -> {
            BufferedImage bufferedImage = FrameUtils.imageFromFrame(picture);
            Image image = SwingFXUtils.toFXImage(bufferedImage, null);
            imageView.setImage(image);
        };
        H264StreamCallback hsc = new H264StreamCallback(is, listener);
        try {
            hsc.playStream();
        } catch (Exception e) {
            throw new CameraCenterException(e);
        }
    }
}
