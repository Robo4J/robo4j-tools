/*
 * Copyright (C) 2017. Miroslav Kopecky
 * This CameraViewProcessor.java  is part of robo4j.
 * path: /Users/miroslavkopecky/GiTHub_MiroKopecky/robo4j-tools/camera/robo4j-camera-center/src/main/java/com/robo4j/tools/camera/processor/CameraViewProcessor.java
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

package com.robo4j.tools.camera.processor;

import com.robo4j.core.DefaultAttributeDescriptor;
import com.robo4j.core.RoboReference;
import com.robo4j.core.logging.SimpleLoggingUtil;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.concurrent.ExecutionException;

/**
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class CameraViewProcessor implements Runnable {
    private static final String ATTRIBUTE_IMAGE = "image";
    private final RoboReference<?> cameraProcessor;
    private volatile ImageView imageView;

    public CameraViewProcessor(RoboReference<?> cameraProcessor, ImageView imageView) {
        this.cameraProcessor = cameraProcessor;
        this.imageView = imageView;
    }

    @Override
    public void run() {
        DefaultAttributeDescriptor<Image> descriptor = DefaultAttributeDescriptor.create(Image.class, ATTRIBUTE_IMAGE);
        try {
            Image image = cameraProcessor.getAttribute(descriptor).get();
            imageView.setImage(image);
        } catch (InterruptedException | ExecutionException e) {
            SimpleLoggingUtil.error(getClass(), "image failure", e);
        }
    }
}
