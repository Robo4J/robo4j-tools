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

package com.robo4j.tools.camera.utils;

import com.robo4j.RoboReference;
import com.robo4j.socket.http.codec.VideoConfigMessage;
import com.robo4j.socket.http.enums.VideoMessageType;
import com.robo4j.tools.camera.model.EditableCell;
import com.robo4j.tools.camera.model.SimpleRawElement;
import com.robo4j.units.rpi.camera.RpiCameraProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.util.Arrays;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class VideoCenterUtils {

    @SuppressWarnings("unchecked")
    public static void initVideoConfigTV(TableView<SimpleRawElement> configImageTV) {
        configImageTV.setEditable(true);
        final Callback<TableColumn, TableCell> cellFactory = (p) -> new EditableCell<SimpleRawElement>();

        ObservableList<SimpleRawElement> data = FXCollections
                .observableArrayList(Arrays.asList(
                        new SimpleRawElement(RpiCameraProperty.WIDTH.getName(), "640"),
                        new SimpleRawElement(RpiCameraProperty.HEIGHT.getName(), "480"),
                        new SimpleRawElement(RpiCameraProperty.ROTATION.getName(), "180"),
                        new SimpleRawElement(RpiCameraProperty.TIMEOUT.getName(), "0")));

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

    public static void sendStopMessage(RoboReference<VideoConfigMessage> unit) {
        final VideoConfigMessage stopMessage = new VideoConfigMessage();
        stopMessage.setType(VideoMessageType.STOP);
        unit.sendMessage(stopMessage);
    }
}
