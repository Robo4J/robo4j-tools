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

import com.robo4j.core.RoboBuilder;
import com.robo4j.core.RoboBuilderException;
import com.robo4j.core.RoboContext;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.core.util.SystemUtil;
import com.robo4j.socket.http.util.JsonUtil;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
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
    private static final String BUTTON_ACTIVATED = "Activated";
    private static final String IMAGE_FORMAT = "png";
    private static final String IMAGE_PROCESSOR1 = "imageProcessor";
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

    private String clientAddress;


    void init(RoboBuilder roboBuilder, String clientAddress) {
        ImageProcessor imageProcessor = new ImageProcessor(roboBuilder.getContext(), IMAGE_PROCESSOR1);
        imageProcessor.setImageView(cameraImageView);
        try {
            roboBuilder.add(imageProcessor);
        } catch (RoboBuilderException e){
            SimpleLoggingUtil.error(getClass(), "error" + e);
        }
        this.roboSystem = roboBuilder.build();
        this.clientAddress = clientAddress;
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
            stateL.setText(BUTTON_ACTIVATED);
            buttonActive.setText(BUTTON_ACTIVATED);
            Map<String, Object> configurationMap = getSystemConfigurationMap(clientAddress);
            createRoboSystemTableView(configurationMap);
            cameraActive = true;
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

    //Private Methods
    @SuppressWarnings("unchecked")
    private void createRoboSystemTableView(Map<String, Object> configurationMap) {
        ObservableList<RawUnit> data = FXCollections.observableArrayList(configurationMap.entrySet().stream()
                .map(e -> new RawUnit(e.getKey(), e.getValue().toString())).collect(Collectors.toList()));


        TableColumn roboUnitCol = new TableColumn("RoboUnit");
        roboUnitCol.setMinWidth(200);
        roboUnitCol.setCellValueFactory(
                new PropertyValueFactory<RawUnit, String>("name"));

        TableColumn stateCol = new TableColumn("Status");
        stateCol.setMinWidth(100);
        stateCol.setCellValueFactory(
                new PropertyValueFactory<RawUnit, String>("state"));

        systemTV.setItems(data);
        systemTV.getColumns().addAll(roboUnitCol, stateCol);
    }

    private Map<String, Object> getSystemConfigurationMap(String address) {
        try {
            final URL apiEndpoint = new URL(address);
            final HttpURLConnection connection = (HttpURLConnection) apiEndpoint.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            connection.disconnect();
            return JsonUtil.getMapNyJson(sb.toString());
        } catch (IOException e) {
            SimpleLoggingUtil.error(getClass(), "error: " + e);
        }
        return Collections.EMPTY_MAP;
    }
}
