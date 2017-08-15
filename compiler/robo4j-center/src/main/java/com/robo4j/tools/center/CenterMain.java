/*
 * Copyright (C) 2017. Miroslav Kopecky
 * This CenterMain.java  is part of robo4j.
 * path: /Users/miroslavkopecky/GiTHub_MiroKopecky/robo4j-tools/compiler/robo4j-center/src/main/java/com/robo4j/tools/center/CenterMain.java
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

import com.robo4j.tools.center.builder.CenterBuilder;
import com.robo4j.tools.center.enums.CenterCommand;
import com.robo4j.tools.center.enums.DeviceType;
import com.robo4j.tools.center.enums.SupportedConfigElements;
import com.robo4j.tools.center.enums.SupportedOS;
import com.robo4j.tools.center.model.CenterProperties;
import com.robo4j.tools.center.provider.CompilerProvider;
import com.robo4j.tools.center.provider.UploadProvider;
import com.robo4j.tools.center.property.TaskProperties;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class CenterMain {

    private static final String SPLITERATOR_COMMAND = ",";
    private static final int OPTION_FILE = 1;
    private static final int OPTION_COMMAND_LINE = 4;
    private final SupportedOS os;

    private final CenterProperties centerProperties;

	public static void main(String[] args) throws Exception{

        int argsSize = args.length;

        CenterProperties centerProperties = null;
        switch (argsSize){
            case OPTION_FILE:
                Path path = Paths.get(args[0]);
                if(path.toFile().exists()){
                    final InputStream isConfig = new FileInputStream(path.toFile());
                    CenterBuilder builder = new CenterBuilder().add(isConfig);
                    centerProperties = builder.build();
                }
                break;
            case OPTION_COMMAND_LINE:
                Map<SupportedConfigElements, String> configProperties = new HashMap<>();
                configProperties.put(SupportedConfigElements.MAIN_PACKAGE, "com.robo4j.lego.j1kids.example");
                configProperties.put(SupportedConfigElements.MAIN_CLASS, "Number42Main");
                configProperties.put(SupportedConfigElements.ROBO4J_LIB,  "robo4j-units-lego-alpha-0.3.jar");
                configProperties.put(SupportedConfigElements.OUT_DIR,  "out");
                configProperties.put(SupportedConfigElements.ACTIONS,  args[0]);
                configProperties.put(SupportedConfigElements.JAR_FILE_NAME,  args[1]);
                configProperties.put(SupportedConfigElements.DEVICE_IP,  args[2]);
                configProperties.put(SupportedConfigElements.DEVICE_TYPE,  args[3]);
                centerProperties = new CenterProperties(configProperties);
                break;
            default:
                System.out.println("Usage1: CenterMain [actions] result_jarFileName deviceIp deviceType");
                System.out.println("Example1: CenterMain compile,upload number42 <Device_IP> lego");
                System.out.println("Example1: CenterMain compile,upload number42 <Device_IP> rpi\n");
                System.out.println("Usage2: resource contains robo4jCenter.xml");

                System.exit(2);

        }

        CenterMain center = new CenterMain(centerProperties);
		center.execute();

	}

	CenterMain(CenterProperties centerProperties){
	    this.centerProperties = centerProperties;
        this.os = getOpSystem();

    }

    List<String> execute() {
	    List<String> result = new ArrayList<>();
	    getActions(centerProperties.getCenterActions()).forEach(action -> {
                switch (action){
                    case COMPILE:
                        String mainPackage = centerProperties.getMainPackage();
                        String mainClass = centerProperties.getMainClass();
                        String robo4jLibrary = centerProperties.getRobo4jLibrary();
                        String outDirectory = centerProperties.getOutDirectory();
                        String resultFileName = centerProperties.getJarFileName();
                        TaskProperties properties = new TaskProperties(os, mainPackage, mainClass, robo4jLibrary, resultFileName, outDirectory );
                        CompilerProvider compiler = new CompilerProvider(properties);
                        System.out.println("Compile STARTS");
                        try {
                            boolean compileState = compiler.compile();
                            if(compileState){
                                compiler.createJar();
                            }
                        } catch (Exception e){
                            throw new CenterException("compile error", e);
                        }
                        result.add(action.getName());
                        break;
                    case UPLOAD:
                        UploadProvider uploadProvider = new UploadProvider();
                        DeviceType device = DeviceType.getDeviceByName(centerProperties.getDeviceType());
                        uploadProvider.uploadScp(centerProperties.getJarFileName(), centerProperties.getDeviceIP(), device.getUser(),"root", device.getPath());
                        result.add(action.getName());
                        break;
                    default:
                        throw new CenterException("not supported action: " + action);
                }
        });
        result.add("Done");
        return result;
    }

    //private static
    private static SupportedOS getOpSystem() {
        final String currentOs = System.getProperty("os.name");
        return SupportedOS.getOsByProperty(currentOs);
    }


    private static List<CenterCommand> getActions(String actions){
        String[] split = actions.split(SPLITERATOR_COMMAND);
        if(split.length == 0 || split.length > 2){
            throw new CenterException("not supported size of actions");
        }

        //@formatter:off
        return Stream.of(split)
                .map(CenterCommand::getCommandByName)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(CenterCommand::getId))
                .collect(Collectors.toList());
        //@formatter:on
    }

}
