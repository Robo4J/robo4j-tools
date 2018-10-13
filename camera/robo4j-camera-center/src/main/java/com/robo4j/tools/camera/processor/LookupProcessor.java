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

import com.robo4j.ConfigurationException;
import com.robo4j.LifecycleState;
import com.robo4j.RoboBuilder;
import com.robo4j.RoboBuilderException;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationBuilder;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.net.LookupServiceProvider;
import com.robo4j.net.RoboContextDescriptor;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.codec.VideoConfigMessage;
import com.robo4j.socket.http.units.HttpServerUnit;
import com.robo4j.socket.http.util.HttpPathConfigJsonBuilder;
import com.robo4j.socket.http.util.RoboHttpUtils;
import com.robo4j.tools.camera.CenterFxLookupCameraController;
import com.robo4j.tools.camera.CenterFxLookupVideoController;
import com.robo4j.tools.camera.model.DescRawElement;
import com.robo4j.tools.camera.unit.DefaultImagesUnit;
import com.robo4j.tools.camera.unit.VideoSocketServerUnit;
import com.robo4j.tools.camera.utils.VideoCenterUtils;
import com.robo4j.util.SystemUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class LookupProcessor extends RoboUnit<Integer> implements TableViewProcessor<DescRawElement> {

    public static final String NAME = "lookupProcessor";
    public static final String PROPERTY_DELAY = "delay";
    public static final String PROPERTY_INTERVAL = "interval";
    public static final String METADATA_UNIT_HTTP_CONF = "unitConf";
    public static final String METADATA_UNIT_PROCESSOR = "unitProcessor";
    public static final String METADATA_DESC = "desc";
    private static final int PORT_RANGE_START = 12000;
    public static final String METADATA_IP = "ip";
    private final static String BUTTON_TEXT_ACTION = "Action";
    private final static String BUTTON_TEXT_DISABLE = "Disable";

    /* staring port range*/
    private volatile AtomicInteger lastPortInRange = new AtomicInteger(PORT_RANGE_START);
    private TableView<DescRawElement> systemTableView;
    private Map<String, RoboContextDescriptor> discoveredContexts = new ConcurrentHashMap<>();
    private Map<String, RoboContext> singleCameraContexts = new HashMap<>();
    private Map<String, Button> singleContextButton = new HashMap<>();
    private Map<String, Scene> singleCameraFxScene = new HashMap<>();
    private long delay;
    private long interval;

    public LookupProcessor(RoboContext context, String id) {
        super(Integer.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {

        delay = configuration.getLong(PROPERTY_DELAY, 2L);
        interval = configuration.getLong(PROPERTY_INTERVAL, 2L);
        try {
            LookupServiceProvider.getDefaultLookupService().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void start() {

        ObservableList<DescRawElement> data = FXCollections.observableArrayList();
        createHeaderTableView();
        getContext().getScheduler().scheduleAtFixedRate(() -> {
            //@formatter:off

            Map<String, RoboContextDescriptor> map = LookupServiceProvider.getDefaultLookupService().getDiscoveredContexts();
            List<DescRawElement> netData = map
                    .entrySet().stream()
                    .filter(e -> !Boolean.valueOf(e.getValue().getMetadata().get("internal")))
                    .map(e -> {
                        String context = e.getKey();
                        String unitConf = e.getValue().getMetadata().get(METADATA_UNIT_HTTP_CONF);
                        String unitProcessor = e.getValue().getMetadata().get(METADATA_UNIT_PROCESSOR);
                        String desc = e.getValue().getMetadata().get(METADATA_DESC);
                        return new DescRawElement(context, unitConf, unitProcessor, desc);
                    })
                    .collect(Collectors.toList());

            if(systemTableView.getItems().size() != netData.size() || !systemTableView.getItems().containsAll(netData)){
                systemTableView.getItems().clear();
                systemTableView.getItems().addAll(netData);
                map.forEach((key, value) -> {
                    discoveredContexts.putIfAbsent(key, value);
                });
            }
            //@formatter:on

        }, delay, interval, TimeUnit.SECONDS);

        systemTableView.setItems(data);
    }


    @SuppressWarnings("unchecked")
    private void createHeaderTableView() {
        TableColumn nameCol = new TableColumn("Context:");
        nameCol.setMinWidth(120);
        nameCol.setCellValueFactory(
                new PropertyValueFactory<DescRawElement, String>(DescRawElement.KEY_NAME));

        TableColumn valueCol = new TableColumn("UnitConf.:");
        valueCol.setMinWidth(120);
        valueCol.setCellValueFactory(new PropertyValueFactory<DescRawElement, String>(DescRawElement.KEY_VALUE));

        TableColumn value2Col = new TableColumn("Processor:");
        value2Col.setMinWidth(120);
        value2Col.setCellValueFactory(new PropertyValueFactory<DescRawElement, String>(DescRawElement.KEY_VALUE2));

        TableColumn descCol = new TableColumn("Desc.:");
        descCol.setMinWidth(120);
        descCol.setCellValueFactory(new PropertyValueFactory<DescRawElement, String>(DescRawElement.KEY_DESC));
        TableColumn<DescRawElement, Void> buttonCol = new TableColumn("Camera:");
        buttonCol.setMinWidth(50);

        Callback<TableColumn<DescRawElement, Void>, TableCell<DescRawElement, Void>> cellFactory = (param) ->
                new TableCell<DescRawElement, Void>() {

                    private final Button btn = new Button(BUTTON_TEXT_ACTION);
                    private boolean active = false;

                    {
                        btn.setOnAction((ActionEvent event) -> {
                            DescRawElement data = getTableView().getItems().get(getIndex());
                            singleContextButton.putIfAbsent(data.getName(), btn);
                            if (active) {
                                btn.setText(BUTTON_TEXT_ACTION);
                            } else {
                                btn.setText(BUTTON_TEXT_DISABLE);
                                try {
                                    if (data.getDesc().startsWith("video")) {
                                        String[] conf = data.getDesc().split(":");
                                        openSingleVideoView(data.getName(), Integer.valueOf(conf[1]), data.getValue2());
                                    } else {
                                        openSingleCameraView(data.getName());
                                    }
                                } catch (Exception e) {
                                    SimpleLoggingUtil.error(getClass(), e.getMessage());
                                }
                            }
                            active = !active;
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }

                };

        buttonCol.setCellFactory(cellFactory);


        systemTableView.getColumns().addAll(nameCol, valueCol, value2Col, descCol, buttonCol);
    }

    @Override
    public void setTableView(TableView<DescRawElement> tableView) {
        this.systemTableView = tableView;
    }

    private void openSingleVideoView(String contextName, Integer port, String videoConfigUnit) throws Exception {
        if (!singleCameraContexts.containsKey(contextName)) {
            URL fxFile = Thread.currentThread().getContextClassLoader().getResource("robo4jCenterLookupVideo.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(fxFile);
            fxmlLoader.setLocation(fxFile);
            Parent parent = fxmlLoader.load();
            RoboContext system = createVideoChildViewContext(fxmlLoader, contextName, videoConfigUnit, port);
            singleCameraContexts.putIfAbsent(contextName, system);
            Scene scene = new Scene(parent, 800, 600);
            singleCameraFxScene.put(contextName, scene);
        }

        RoboContext system = singleCameraContexts.get(contextName);
        if (system.getState() != LifecycleState.STARTED) {
            system.start();
            System.out.println("video window: " + contextName);
            System.out.println(SystemUtil.printStateReport(system));
        }
        Scene scene = singleCameraFxScene.get(contextName);
        Stage stage = new Stage();
        stage.setTitle(contextName);
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest((WindowEvent event) -> {
            deactivateContext(contextName);
        });
    }

    private void deactivateContext(final String systemName){
        RoboContext system = singleCameraContexts.get(systemName);
        system.stop();
        System.out.println("systemName: " + systemName);
        System.out.println(SystemUtil.printStateReport(system));
        singleContextButton.get(systemName).setText(BUTTON_TEXT_ACTION);
        RoboReference<VideoConfigMessage> videoConfigUnitRef = LookupServiceProvider.getDefaultLookupService()
                .getContext(systemName).getReference("videoConfigUnit");
        if(videoConfigUnitRef != null){
            VideoCenterUtils.sendStopMessage(videoConfigUnitRef);
            system.getReference(VideoSocketServerUnit.NAME).sendMessage(VideoSocketServerUnit.MESSAGE_STOP);
        }
    }

    private void openSingleCameraView(String contextName) throws Exception {

        if (!singleCameraContexts.containsKey(contextName)) {
            URL fxFile = Thread.currentThread().getContextClassLoader().getResource("robo4jCenterLookupCamera.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(fxFile);
            fxmlLoader.setLocation(fxFile);
            Parent parent = fxmlLoader.load();
            int childServerPort = lastPortInRange.getAndIncrement();
            RoboContext system = createCameraChildViewContext(fxmlLoader, contextName, childServerPort);
            system.start();
            System.out.println("camera window: " + contextName);
            System.out.println(SystemUtil.printStateReport(system));

            sendHttpConfigToMediaNode(contextName, childServerPort);

            singleCameraContexts.putIfAbsent(contextName, system);
            Scene scene = new Scene(parent, 800, 600);
            singleCameraFxScene.put(contextName, scene);
        }

        Scene scene = singleCameraFxScene.get(contextName);
        Stage stage = new Stage();
        stage.setTitle(contextName);
        stage.setScene(scene);
        stage.show();
    }

    private void sendHttpConfigToMediaNode(String contextName, int port) {
        String httpClientConfigUnit = discoveredContexts.get(contextName).getMetadata().get(METADATA_UNIT_HTTP_CONF);
        String processorUnit = discoveredContexts.get(contextName).getMetadata().get(METADATA_UNIT_PROCESSOR);

        final Map<String, String> httpConfigMap = new HashMap<>();
        httpConfigMap.put(RoboHttpUtils.PROPERTY_SOCKET_PORT, String.valueOf(port));
        httpConfigMap.put(RoboHttpUtils.PROPERTY_HOST, LookupServiceProvider.getDefaultLookupService()
                .getDescriptor(getContext().getId())
                .getMetadata().get(METADATA_IP));
        LookupServiceProvider.getDefaultLookupService().getContext(contextName).getReference(httpClientConfigUnit)
                .sendMessage(httpConfigMap);

        if (processorUnit != null && !processorUnit.isEmpty()) {
            LookupServiceProvider.getDefaultLookupService().getContext(contextName)
                    .getReference(processorUnit).sendMessage("sequence");

        }


    }

    private RoboContext createVideoChildViewContext(FXMLLoader fxmlLoader, String contextName,
                                                    String videoUnit, int port) {
        CenterFxLookupVideoController controller = fxmlLoader.getController();
        controller.setContextName(contextName);
        controller.setVideoConfigUnit(videoUnit);
        controller.setPort(port);
        RoboBuilder builder = createChildViewSystemBuilder(contextName);
        try {
            controller.init(builder);
            controller.initVideoConfig();
            return controller.getSystem();
        } catch (RoboBuilderException | ConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private RoboContext createCameraChildViewContext(FXMLLoader fxmlLoader, String contextName, int port) {
        CenterFxLookupCameraController controller = fxmlLoader.getController();
        controller.setContextName(contextName);
        RoboBuilder builder = createChildViewSystemBuilder(contextName);
        try {
            createCameraChildViewUnitContext(builder, port);
            controller.init(builder);
            controller.initCameraConfig();
            return controller.getSystem();
        } catch (RoboBuilderException e) {
            throw new RuntimeException(e);
        }
    }


    private void createCameraChildViewUnitContext(final RoboBuilder builder, int port) throws RoboBuilderException {
        Configuration config = new ConfigurationBuilder()
                .addString(DefaultImagesUnit.PROP_TARGET, ImageProcessor.NAME)
                .build();
        builder.add(DefaultImagesUnit.class, config, DefaultImagesUnit.NAME);

        config = new ConfigurationBuilder()
                .addInteger(RoboHttpUtils.PROPERTY_SOCKET_PORT, port)
                .addString(RoboHttpUtils.PROPERTY_CODEC_PACKAGES, "com.robo4j.socket.http.codec")
                .addInteger(RoboHttpUtils.PROPERTY_BUFFER_CAPACITY, 600000)
                .addString(RoboHttpUtils.PROPERTY_UNIT_PATHS_CONFIG,
                        HttpPathConfigJsonBuilder.Builder().addPath(ImageProcessor.NAME, HttpMethod.POST).build())
                .build();
        builder.add(HttpServerUnit.class, config, HttpServerUnit.NAME);
    }

    private RoboBuilder createChildViewSystemBuilder(String systemName) {
        Configuration sysConf = new ConfigurationBuilder()
                .addInteger(RoboBuilder.KEY_SCHEDULER_POOL_SIZE, 6)
                .addInteger(RoboBuilder.KEY_WORKER_POOL_SIZE, 6)
                .addInteger(RoboBuilder.KEY_BLOCKING_POOL_SIZE, 6)
                .build();
        return new RoboBuilder(systemName, sysConf);
    }


}
