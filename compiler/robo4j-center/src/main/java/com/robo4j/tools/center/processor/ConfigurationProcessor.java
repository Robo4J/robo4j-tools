/*
 * Copyright (C) 2017. Miroslav Kopecky
 * This ConfigurationProcessor.java  is part of robo4j.
 * path: /Users/miroslavkopecky/GiTHub_MiroKopecky/robo4j-tools/compiler/robo4j-center/src/main/java/com/robo4j/tools/center/processor/ConfigurationProcessor.java
 * module: robo4j-center_main
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

package com.robo4j.tools.center.processor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import com.robo4j.BlockingTrait;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.dto.ResponseUnitDTO;
import com.robo4j.socket.http.util.JsonUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
@BlockingTrait
public class ConfigurationProcessor extends RoboUnit<String> {

    private TableView<ResponseUnitDTO> tableView;

    public ConfigurationProcessor(RoboContext context, String id) {
        super(String.class, context, id);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onMessage(String message) {

        try {
            final URL apiEndpoint = new URL(message);
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


            List<ResponseUnitDTO> unitDTOs = JsonUtil.getListByUnitJsonArray(sb.toString());

            ObservableList<ResponseUnitDTO> data = FXCollections.observableArrayList(unitDTOs.stream()
                    .map(e -> new ResponseUnitDTO(e.getId(), e.getState())).collect(Collectors.toList()));


            TableColumn roboUnitCol = new TableColumn("RoboUnit");
            roboUnitCol.setMinWidth(200);
            roboUnitCol.setCellValueFactory(
                    new PropertyValueFactory<ResponseUnitDTO, String>("id"));

            TableColumn stateCol = new TableColumn("Status");
            stateCol.setMinWidth(100);
            stateCol.setCellValueFactory(
                    new PropertyValueFactory<ResponseUnitDTO, String>("state"));

            tableView.setItems(data);
            tableView.getColumns().addAll(roboUnitCol, stateCol);
        } catch (Exception e){
            SimpleLoggingUtil.error(getClass(), "error: " + e);
        }

    }

    public void setTableView(TableView<ResponseUnitDTO> tableView){
        this.tableView = tableView;
    }
}
