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

import com.robo4j.tools.center.enums.DeviceType;
import com.robo4j.tools.center.enums.SupportedOS;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class CenterMain {

    private final List<String> actions;
    private final String resultFileName;
    private final String sourcePath;
    private final String targetIp;
    private final DeviceType device;

	public static void main(String[] args) {
		// [compile, upload], Result FileName, sourcePath, deviceType
		if (args.length != 5) {
			System.out.println("Usage: CenterMain [actions] result_fileName sourcePath targetIp deviceType");
			System.out
					.println("Example: CenterMain compile,upload number42.jar /home/root/lego /lejos/samples lego");
			System.exit(2);
		}

        CenterMain center = new CenterMain(getActions(args[0]), args[1], args[2], args[3], getDevice(args[4]));
		center.execute();

	}

	private CenterMain(List<String> actions, String resultFileName, String sourcePath, String targetIp, DeviceType device){
        if(device == null){
            throw new CenterException("no device");
        }

        this.actions = actions;
        this.resultFileName = resultFileName;
        this.sourcePath = sourcePath;
        this.targetIp = targetIp;
        this.device = device;


    }

    public void execute(){

	    UploadProvider uploadProvider = new UploadProvider();

	    uploadProvider.uploadScp(sourcePath, targetIp, device.getUser(),"mirage09", device.getPath());

    }

    //private static
    private static DeviceType getDevice(String type){
	    return DeviceType.getDeviceByName(type);
    }

    private static List<String> getActions(String actions){

        String[] split = actions.split(",");
        if(split.length > 2){
            throw new CenterException("not supported size of actions");
        }

        return Stream.of(split).collect(Collectors.toList());

    }

	private static SupportedOS getOpSystem() {
		final String currentOs = System.getProperty("os.name");
		return SupportedOS.getOsByProperty(currentOs);
	}
}
