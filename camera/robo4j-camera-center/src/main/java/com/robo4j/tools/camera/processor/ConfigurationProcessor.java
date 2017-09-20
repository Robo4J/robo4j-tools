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

import com.robo4j.BlockingTrait;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.util.JsonUtil;
import com.robo4j.tools.camera.RawUnit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
@BlockingTrait
public class ConfigurationProcessor extends RoboUnit<String> {

    private TableView<RawUnit> tableView;

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

            ObservableList<RawUnit> data = FXCollections.observableArrayList(JsonUtil.getMapNyJson(sb.toString()).entrySet().stream()
                    .map(e -> new RawUnit(e.getKey(), e.getValue().toString())).collect(Collectors.toList()));


            TableColumn roboUnitCol = new TableColumn("RoboUnit");
            roboUnitCol.setMinWidth(200);
            roboUnitCol.setCellValueFactory(
                    new PropertyValueFactory<RawUnit, String>("name"));

            TableColumn stateCol = new TableColumn("Status");
            stateCol.setMinWidth(100);
            stateCol.setCellValueFactory(
                    new PropertyValueFactory<RawUnit, String>("state"));

            tableView.setItems(data);
            tableView.getColumns().addAll(roboUnitCol, stateCol);
        } catch (Exception e){
            SimpleLoggingUtil.error(getClass(), "error: " + e);
        }

    }

    public void setTableView(TableView<RawUnit> tableView){
        this.tableView = tableView;
    }
}
