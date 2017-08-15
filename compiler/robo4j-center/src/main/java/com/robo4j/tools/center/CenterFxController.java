/*
 * Copyright (C) 2017. Miroslav Kopecky
 * This CenterFxController.java  is part of robo4j.
 * path: /Users/miroslavkopecky/GiTHub_MiroKopecky/robo4j-tools/compiler/robo4j-center/src/main/java/com/robo4j/tools/center/CenterFxController.java
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

package com.robo4j.tools.center;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.robo4j.tools.center.builder.CenterBuilder;
import com.robo4j.tools.center.enums.DeviceType;
import com.robo4j.tools.center.model.CenterProperties;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class CenterFxController {

    private static final String DEFAULT_OPTION = "Select";
    private static final String NEW_LINE = "\n";
    @FXML
	private TextField deviceIpTextField;

	@FXML
	private ComboBox<String> deviceTypeCBox;

	@FXML
    public void initialize(){
	    deviceTypeCBox.getItems().removeAll(deviceTypeCBox.getItems());
        List<String> options = new ArrayList<>();
        options.add(DEFAULT_OPTION);
        options.addAll(Stream.of(DeviceType.values()).map(DeviceType::getName).collect(Collectors.toList()));
	    deviceTypeCBox.getItems().addAll(options);
	    deviceTypeCBox.getSelectionModel().select(0);
    }

	@FXML
	private TextField mainPackageTF;

	@FXML
	private TextField mainClassTF;

	@FXML
	private TextField roboLibTF;

	@FXML
	private TextField outDirTF;

	@FXML
	private TextField jarNameTF;

	@FXML
	private TextField processActionsTF;


	@FXML
    private TextFlow outputProcessTF;

	private CenterProperties properties;

	public void init() throws Exception {
        final InputStream isConfig = getClass().getClassLoader().getResourceAsStream("robo4jCenter.xml");
		CenterBuilder builder = new CenterBuilder().add(isConfig);
		properties = builder.build();

		deviceIpTextField.setText(properties.getDeviceIP());
		mainPackageTF.setText(properties.getMainPackage());
		mainClassTF.setText(properties.getMainClass());
		roboLibTF.setText(properties.getRobo4jLibrary());
		outDirTF.setText(properties.getOutDirectory());
		jarNameTF.setText(properties.getJarFileName());
		processActionsTF.setText(properties.getCenterActions());


	}

	@FXML
	private void buttonProcessClick(ActionEvent  event){

	    outputProcessTF.getChildren().clear();
	    CenterMain center = new CenterMain(properties);
	    center.execute().forEach(e -> {
            Text text = new Text(e.concat(NEW_LINE));
            text.setFill(Color.DARKGREEN);
            text.setFont(Font.font("Helvetica", FontPosture.REGULAR, 14));
            outputProcessTF.getChildren().add(text);
            System.out.println("ADDED text:" + text.getText());
        });
        System.out.println("DONE");
    }
}
