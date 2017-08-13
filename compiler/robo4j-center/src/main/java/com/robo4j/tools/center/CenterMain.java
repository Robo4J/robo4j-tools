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

import com.robo4j.tools.center.enums.CenterCommand;
import com.robo4j.tools.center.enums.DeviceType;
import com.robo4j.tools.center.enums.SupportedOS;
import com.robo4j.tools.center.util.TaskProperties;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class CenterMain {

    private final List<CenterCommand> actions;
    private final String resultFileName;
    private final String targetIp;
    private final DeviceType device;
    private final SupportedOS os;

	public static void main(String[] args) {
		// [compile, upload], Result FileName, sourcePath, deviceType
		if (args.length != 4) {
			System.out.println("Usage: CenterMain [actions] result_fileName targetIp deviceType");
			System.out
					.println("Example: CenterMain compile,upload number42.jar <Device_IP> lego");
            System.out.println("Example: CenterMain compile,upload number42.jar <Device_IP> rpi");
			System.exit(2);
		}


        CenterMain center = new CenterMain(getActions(args[0]), args[1], args[2], getDevice(args[3]));
		center.execute();

	}

	private CenterMain(List<CenterCommand> actions, String resultFileName, String targetIp, DeviceType device){
        if(device == null){
            throw new CenterException("no device");
        }

        this.actions = actions;
        this.resultFileName = resultFileName;
        this.targetIp = targetIp;
        this.device = device;
        this.os = getOpSystem();


    }

    public void execute() {

	    actions.forEach(action -> {
                switch (action){
                    case COMPILE:
                        String mainPackage = "com.robo4j.lego.j1kids.example";
                        String mainClass = "Number42Main.java";
                        String robo4jLibrary = "robo4j-units-lego-alpha-0.3.jar";
                        String outDirectory = "out";
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

                        break;
                    case UPLOAD:
                        UploadProvider uploadProvider = new UploadProvider();
                        uploadProvider.uploadScp(resultFileName, targetIp, device.getUser(),"root", device.getPath());
                        break;
                    default:
                        throw new CenterException("not supported action: " + action);
                }
        });





    }

    //private static
    public static SupportedOS getOpSystem() {
        final String currentOs = System.getProperty("os.name");
        return SupportedOS.getOsByProperty(currentOs);
    }

    private static DeviceType getDevice(String type){
        return DeviceType.getDeviceByName(type);
    }

    private static List<CenterCommand> getActions(String actions){

        String[] split = actions.split(",");
        if(split.length > 2){
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
