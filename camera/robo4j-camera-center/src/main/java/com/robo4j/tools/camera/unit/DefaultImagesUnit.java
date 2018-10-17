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

import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.codec.CameraMessage;
import com.robo4j.socket.http.util.JsonUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class DefaultImagesUnit extends RoboUnit<String> {
    public static final String NAME = "defaultImagesUnit";
    public static final String PROP_TARGET = "target";
    public static final String PROP_DELAY = "delay";
    public static final String PROP_INTERVAL = "interval";
    public static final String PROP_TIME_UNIT = "timeUnit";
    private static final List<String> DEFAULT_SEQUENCE = Arrays.asList("20161021_NoSignal_640.png", "20181002_NoSignal_640.png", "20181002_Nighthacking_640.png");
    public static final String COMMAND_SEQUENCE = "sequence";
    public static final String COMMAND_SHUTDOWN = "shutdown";
    private String target;
    private long delay;
    private long interval;
    private TimeUnit timeUnit;
    private List<CameraMessage> defaultMessages;
    private volatile AtomicBoolean process = new AtomicBoolean(false);

    public DefaultImagesUnit(RoboContext context, String id) {
        super(String.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        String t = configuration.getString(PROP_TARGET, null);
        if (t == null) {
            throw ConfigurationException.createMissingConfigNameException(PROP_TARGET);
        }
        target = t;
        delay = configuration.getLong(PROP_DELAY, 1L);
        interval = configuration.getLong(PROP_INTERVAL, 1L);
        timeUnit = TimeUnit.valueOf(configuration.getString(PROP_TIME_UNIT, TimeUnit.SECONDS.name()));
        defaultMessages = DEFAULT_SEQUENCE.stream()
                .map(i -> {
                    CameraMessage cm = new CameraMessage();
                    cm.setType("png");
                    cm.setValue("default");
                    try {
                        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(i);
                        byte[] array = new byte[is.available()];
                        is.read(array);
                        String imageBase64 = JsonUtil.toBase64String(array);
                        cm.setImage(imageBase64);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return cm;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void onMessage(String message) {
        switch (message) {
            case COMMAND_SEQUENCE:
                process.compareAndSet(true, false);
                getContext().getScheduler().schedule(() -> {
                    process.compareAndSet(false, true);
                    while (process.get()) {
                        Random random = new Random();
                        int imageNumber = random.nextInt(defaultMessages.size());
                        getContext().getReference(target).sendMessage(defaultMessages.get(imageNumber));
                        SimpleLoggingUtil.info(getClass(), String.format("command: %s, image %d", message, imageNumber));

                        try {
                            timeUnit.sleep(interval);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }, delay, timeUnit);
                break;
            case COMMAND_SHUTDOWN:
                process.compareAndSet(true, false);
                break;
            default:
                SimpleLoggingUtil.info(getClass(), String.format("not implemented: %s", message));
        }
    }
}
