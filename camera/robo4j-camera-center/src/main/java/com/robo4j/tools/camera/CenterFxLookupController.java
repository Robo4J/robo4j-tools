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
import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationBuilder;
import com.robo4j.net.LookupService;
import com.robo4j.net.LookupServiceProvider;
import com.robo4j.tools.camera.model.DescRawElement;
import com.robo4j.tools.camera.processor.ImageProcessor;
import com.robo4j.tools.camera.processor.LookupProcessor;
import com.robo4j.util.SystemUtil;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;

import static com.robo4j.tools.camera.CenterFxController.NO_SIGNAL_IMAGE;

/**
 * CenterFxLookupController see {@link CenterLookupMain}
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class CenterFxLookupController implements LookupCenterController {

    @FXML
    private TableView<DescRawElement> systemsTV;

    @FXML
    private ImageView cameraImageView;

    private RoboContext system;

    @FXML
    public void initialize() {
        Image image = new Image(Thread.currentThread().getContextClassLoader().getResourceAsStream(NO_SIGNAL_IMAGE));
        CenterFxController.initCameraView(cameraImageView, image);
    }

    @Override
    public void init(RoboBuilder builder) throws RoboBuilderException, ConfigurationException {

        ImageProcessor imageProcessor = new ImageProcessor(builder.getContext(), ImageProcessor.NAME);
        imageProcessor.setImageView(cameraImageView);
        builder.add(imageProcessor);

        Configuration config = new ConfigurationBuilder()
                .addLong(LookupProcessor.PROPERTY_DELAY, 1L)
                .build();
        LookupProcessor lookupProcessor = new LookupProcessor(builder.getContext(), LookupProcessor.NAME);
        lookupProcessor.initialize(config);
        lookupProcessor.setTableView(systemsTV);
        builder.add(lookupProcessor);

        system = builder.build();
        system.start();

        LookupService service = LookupServiceProvider.getDefaultLookupService();
        try {
            service.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(SystemUtil.printStateReport(system));

    }

    public void stop() {
        if (system != null) {
            system.shutdown();
            System.out.println(SystemUtil.printStateReport(system));
        }
        System.out.println("Bye! ");
    }
}
