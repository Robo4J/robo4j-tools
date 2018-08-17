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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.robo4j.RoboBuilder;
import com.robo4j.RoboBuilderException;
import com.robo4j.RoboContext;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.dto.ResponseUnitDTO;
import com.robo4j.tools.center.enums.DeviceType;
import com.robo4j.tools.center.enums.SupportedConfigElements;
import com.robo4j.tools.center.model.CenterProperties;

import com.robo4j.tools.center.processor.ConfigurationProcessor;
import com.robo4j.util.SystemUtil;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
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
    private static final String CONFIGURATION_PROCESSOR = "configurationProcessor";

    @FXML
	private TextField deviceIpTextField;

    @FXML
    private TextField devicePortTextField;

	@FXML
	private ComboBox<String> deviceTypeCBox;

	@FXML
    public void initialize(){
	    deviceTypeCBox.getItems().removeAll(deviceTypeCBox.getItems());
        ObservableList<String> observableDeviceValues = FXCollections.observableArrayList(DEFAULT_OPTION);
        observableDeviceValues.addAll(Stream.of(DeviceType.values()).map(DeviceType::getName).collect(Collectors.toList()));

	    deviceTypeCBox.getItems().addAll(observableDeviceValues);
	    deviceTypeCBox.getSelectionModel().select(0);

	    deviceTypeCBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            boolean state = false;
            if(!oldValue.equals(newValue) && newValue.equals(DeviceType.RPI.getName())){
               state = true;
            }
            devicePasswordElements(state);
        });

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
    private TextField statusTF;

	@FXML
    private TextFlow outputProcessTF;

	@FXML
    private CheckBox editCB;

	@FXML
    private TextField passwordTF;

	@FXML
    private Label passwordL;

    @FXML
    private TableView<ResponseUnitDTO> systemTV;

	private List<TextField> mainTextFields;
    private RoboContext roboSystem;
    private boolean systemTabSelected = false;
    private String systemClientUrl;

	public void init(CenterProperties properties, RoboBuilder roboBuilder) throws Exception {

	    mainTextFields = Arrays.asList(mainPackageTF, mainClassTF, roboLibTF, outDirTF);
	    if(properties.isSet()){
            adjustEditableMainFields(false);
            adjustPropertiesToTextFields(properties);
        }

        editCB.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) ->{
            if(newValue){
                adjustEditableMainFields(true);
            } else {
                adjustEditableMainFields(false);
            }
        });

        ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(roboBuilder.getContext(), CONFIGURATION_PROCESSOR);
        configurationProcessor.setTableView(systemTV);
        try {
            roboBuilder.add(configurationProcessor);
        } catch (RoboBuilderException e){
            SimpleLoggingUtil.error(getClass(), "error" + e);
        }
        this.roboSystem = roboBuilder.build();
        roboSystem.start();
	}

    public void stop(){
        System.out.println("State after stop:");
        System.out.println(SystemUtil.printStateReport(roboSystem));
        roboSystem.shutdown();
    }

	@FXML
	private void buttonProcessClick(ActionEvent  event){
        outputProcessTF.getChildren().clear();
        CenterProperties properties = adjustTextFieldToProperties();
	    CenterMain center = new CenterMain(properties);
	    center.execute().forEach(e -> {
            Text text = new Text(e.concat(NEW_LINE));
            text.setFill(Color.DARKGREEN);
            text.setFont(Font.font("Helvetica", FontPosture.REGULAR, 14));
            outputProcessTF.getChildren().add(text);
        });
        statusTF.setText("DONE");
    }

    @FXML
    private void systemChangeTab(Event event){
        Tab selectedTab = (Tab) event.getTarget();
        if(!systemTabSelected && selectedTab.getId().equals("systemTab")){
            systemTabSelected = true;
            roboSystem.getReference(CONFIGURATION_PROCESSOR).sendMessage(systemClientUrl);
        }
    }

    //Private Methods
    private void adjustEditableMainFields(boolean state){
        mainTextFields.forEach(tf -> {
            tf.setEditable(state);
            tf.setDisable(!state);
        });
    }

    private CenterProperties adjustPropertiesToTextFields(CenterProperties properties){
        mainPackageTF.setText(properties.getMainPackage());
        mainClassTF.setText(properties.getMainClass());
        roboLibTF.setText(properties.getRobo4jLibrary());
        outDirTF.setText(properties.getOutDirectory());
        jarNameTF.setText(properties.getJarFileName());
        deviceIpTextField.setText(properties.getDeviceIP());
        devicePortTextField.setText(properties.getDevicePort());
        systemClientUrl = "http://" + properties.getDeviceIP() + ":" + properties.getDevicePort() ;
        DeviceType deviceType = DeviceType.getDeviceByName(properties.getDeviceType());
        switch (deviceType){
            case RPI:
                passwordTF.setText(properties.getPassword());
                break;
            case LEGO:
            default:
        }
        deviceTypeCBox.setValue(deviceType.getName());
        processActionsTF.setText(properties.getCenterActions());
        return properties;
    }

    private CenterProperties adjustTextFieldToProperties(){
        Map<SupportedConfigElements, String> map = new HashMap<>();
        map.put(SupportedConfigElements.MAIN_PACKAGE, mainPackageTF.getText());
        map.put(SupportedConfigElements.MAIN_CLASS, mainClassTF.getText());
        map.put(SupportedConfigElements.ROBO4J_LIB, roboLibTF.getText());
        map.put(SupportedConfigElements.OUT_DIR, outDirTF.getText());
        map.put(SupportedConfigElements.JAR_FILE_NAME, jarNameTF.getText());
        map.put(SupportedConfigElements.DEVICE_IP, deviceIpTextField.getText());


        DeviceType deviceType = DeviceType.getDeviceByName(deviceTypeCBox.getSelectionModel().getSelectedItem());
        map.put(SupportedConfigElements.DEVICE_TYPE, deviceType.getName());
        map.put(SupportedConfigElements.ACTIONS, processActionsTF.getText().trim());
        if(deviceType.equals(DeviceType.RPI)){
            map.put(SupportedConfigElements.DEVICE_PASS, passwordTF.getText().trim());
        } else {
            map.put(SupportedConfigElements.DEVICE_PASS, "");
        }

        return new CenterProperties(map);
    }

    private void devicePasswordElements(boolean status){
        passwordL.setVisible(status);
        passwordTF.setVisible(status);
    }

}
