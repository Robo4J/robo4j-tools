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

package com.robo4j.tools.camera.processor;

import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.dto.ResponseUnitDTO;
import com.robo4j.socket.http.util.JsonUtil;
import com.robo4j.tools.camera.model.RawElement;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class ConfigurationProcessor extends RoboUnit<String> {

    private TableView<RawElement> tableView;

    public ConfigurationProcessor(RoboContext context, String id) {
        super(String.class, context, id);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onMessage(String message) {

        try {
            if (Bindings.isEmpty(tableView.getItems()).get()) {
                List<ResponseUnitDTO> unitDTOs = JsonUtil.jsonToList(ResponseUnitDTO.class, message);
                ObservableList<RawElement> data = FXCollections.observableArrayList(unitDTOs.stream()
                        .map(e -> new RawElement(e.getId(), e.getState().getLocalizedName())).collect(Collectors.toList()));
                TableColumn roboUnitCol = new TableColumn("RoboUnit");
                roboUnitCol.setMinWidth(200);
                roboUnitCol.setCellValueFactory(
                        new PropertyValueFactory<RawElement, String>("name"));

                TableColumn stateCol = new TableColumn("State");
                stateCol.setMinWidth(100);
                stateCol.setCellValueFactory(
                        new PropertyValueFactory<RawElement, String>("state"));

                tableView.setItems(data);
                tableView.getColumns().addAll(roboUnitCol, stateCol);
            }

        } catch (Exception e) {
            SimpleLoggingUtil.error(getClass(), "error: " + e);
        }

    }

    public void setTableView(TableView<RawElement> tableView) {
        this.tableView = tableView;
    }
}
