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

package com.robo4j.tools.camera.processor;

import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.codec.CameraMessage;
import com.robo4j.tools.camera.analysis.CannyEdgeDetector;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class CannyEdgeDetectorProcessor extends RoboUnit<CameraMessage> {

    private volatile ImageView imageView;

    public CannyEdgeDetectorProcessor(RoboContext context, String id) {
        super(CameraMessage.class, context, id);
    }

    public void setImageView(ImageView imageView){
        this.imageView = imageView;
    }

    @Override
    public void onMessage(CameraMessage message) {
        if(message.getImage() != null){
            final byte[] bytes = Base64.getDecoder().decode(message.getImage());

            try {
                BufferedImage frame = ImageIO.read(new ByteArrayInputStream(bytes));

                CannyEdgeDetector detector  = new CannyEdgeDetector();

                //adjust its parameters as desired
                detector.setLowThreshold(0.5f);
                detector.setHighThreshold(1f);

                //apply it to an image
                detector.setSourceImage(frame);
                detector.process();

                BufferedImage edges = detector.getEdgesImage();

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ImageIO.write(edges,"jpg", bos);

                if(imageView != null){
                    Image image = new Image(new ByteArrayInputStream(bos.toByteArray()));
                    imageView.setImage(image);
                } else {

                    SimpleLoggingUtil.error(getClass(), "no available");
                }

            } catch (IOException e) {
                SimpleLoggingUtil.error(getClass(), e.getMessage());
            }


        } else {
            SimpleLoggingUtil.error(getClass(), "no imageView");
        }
    }
}
