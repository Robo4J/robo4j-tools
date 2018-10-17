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

package com.robo4j.tools.camera.utils;

import com.robo4j.RoboContext;
import com.robo4j.socket.http.HttpHeaderFieldNames;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.HttpVersion;
import com.robo4j.socket.http.message.HttpDecoratedRequest;
import com.robo4j.socket.http.message.HttpRequestDenominator;
import com.robo4j.socket.http.util.JsonUtil;
import com.robo4j.socket.http.util.RoboHttpUtils;
import com.robo4j.tools.camera.model.CameraCenterProperties;
import com.robo4j.tools.camera.model.CameraDevice;
import com.robo4j.tools.camera.model.EditableCell;
import com.robo4j.tools.camera.model.SimpleRawElement;
import com.robo4j.units.rpi.camera.RpiCameraProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class CameraCenterUtils {

    public static void initializeStage(Stage stage, CameraCenterProperties properties) {
        stage.setTitle(properties.getTitle());
        stage.getIcons().add(createIcon("robo4j256.png"));
        stage.getIcons().add(createIcon("robo4j128.png"));
        stage.getIcons().add(createIcon("robo4j64.png"));
        stage.getIcons().add(createIcon("robo4j32.png"));
        stage.getIcons().add(createIcon("robo4j16.png"));
    }

    public static void sendRequestForClientConfiguration(RoboContext system, String callBackUnitName, String httpClientName, CameraDevice cameraDevice) {
        final HttpRequestDenominator denominator = new HttpRequestDenominator(HttpMethod.GET, HttpVersion.HTTP_1_1);
        final HttpDecoratedRequest request = new HttpDecoratedRequest(denominator);
        request.setHost(cameraDevice.getAddress());
        request.setPort(cameraDevice.getPort());
        request.addHostHeader();

        request.addCallback(callBackUnitName);
        system.getReference(httpClientName).sendMessage(request);
    }

    // TODO: 1/26/18 (miro) separate unit and use codec
    public static void buttonImageConfigClick(RoboContext system, String httpClientUnitName, TableView<SimpleRawElement> configImageTV, CameraDevice cameraDevice) {
        final String path = "/units/cameraConfig";
        final Map<String, String> entities = configImageTV.getItems()
                .stream()
                .collect(Collectors.toMap(SimpleRawElement::getName, SimpleRawElement::getValue));

        final String message = JsonUtil.toJsonMap(entities);

        final HttpRequestDenominator denominator = new HttpRequestDenominator(HttpMethod.POST, path, HttpVersion.HTTP_1_1);
        final HttpDecoratedRequest request = new HttpDecoratedRequest(denominator);
        request.addHeaderElement(HttpHeaderFieldNames.HOST, RoboHttpUtils.createHost(cameraDevice.getAddress(), cameraDevice.getPort()));
        request.addHeaderElement(HttpHeaderFieldNames.CONTENT_LENGTH, String.valueOf(message.length()));
        request.addMessage(message);
        system.getReference(httpClientUnitName).sendMessage(request);
    }


    @SuppressWarnings("unchecked")
    public static void initCameraConfigTV(TableView<SimpleRawElement> configImageTV) {
        configImageTV.setEditable(true);
        final Callback<TableColumn, TableCell> cellFactory = (p) -> new EditableCell<SimpleRawElement>();

        ObservableList<SimpleRawElement> data = FXCollections
                .observableArrayList(Arrays.asList(
                        new SimpleRawElement(RpiCameraProperty.WIDTH.getName(), "640"),
                        new SimpleRawElement(RpiCameraProperty.HEIGHT.getName(), "480"),
                        new SimpleRawElement(RpiCameraProperty.BRIGHTNESS.getName(), "50"),
                        new SimpleRawElement(RpiCameraProperty.SHARPNESS.getName(), "50"),
                        new SimpleRawElement(RpiCameraProperty.TIMEOUT.getName(), "1"),
                        new SimpleRawElement(RpiCameraProperty.TIMELAPSE.getName(), "100")));

        TableColumn nameCol = new TableColumn("Name");
        nameCol.setMinWidth(100);
        nameCol.setCellValueFactory(
                new PropertyValueFactory<SimpleRawElement, String>(SimpleRawElement.KEY_NAME));

        TableColumn stateCol = new TableColumn("State");
        stateCol.setMinWidth(100);
        stateCol.setCellValueFactory(
                new PropertyValueFactory<SimpleRawElement, String>(SimpleRawElement.KEY_VALUE));
        stateCol.setCellFactory(cellFactory);
        stateCol.setEditable(true);
        stateCol.setOnEditCommit((eh) -> {
            TableColumn.CellEditEvent<SimpleRawElement, String> ev = (TableColumn.CellEditEvent<SimpleRawElement, String>) eh;
            ev.getTableView().getItems().get(ev.getTablePosition().getRow()).setValue(((TableColumn.CellEditEvent<SimpleRawElement, String>) eh).getNewValue());
        });
        configImageTV.setItems(data);
        configImageTV.getColumns().addAll(nameCol, stateCol);
    }

    private static Image createIcon(String iconName) {
        return new Image(Thread.currentThread().getContextClassLoader().getResourceAsStream(iconName));
    }

}
