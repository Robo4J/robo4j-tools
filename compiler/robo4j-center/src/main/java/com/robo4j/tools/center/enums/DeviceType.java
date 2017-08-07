/*
 * Copyright (C) 2017. Miroslav Kopecky
 * This SupportedDevice.java  is part of robo4j.
 * path: /Users/miroslavkopecky/GiTHub_MiroKopecky/robo4j-tools/compiler/robo4j-center/src/main/java/com/robo4j/tools/center/enums/SupportedDevice.java
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

package com.robo4j.tools.center.enums;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public enum DeviceType {

	//@formatter:off
    RPI     ("rpi", "/home/pi", "pi"),
    LEGO    ("lego", "/home/lejos/samples", "root")
    ;
    //@formatter:on

	private final String name;
	private final String path;
	private final String user;

	private static volatile Map<String, DeviceType> nameToDevice = initMap();

	DeviceType(String name, String path, String user) {
		this.name = name;
		this.path = path;
		this.user = user;
	}

	public static DeviceType getDeviceByName(String name) {
		if (nameToDevice == null) {
			nameToDevice = initMap();
		}
		return nameToDevice.get(name);
	}

	public String getName() {
		return name;
	}

    public String getPath() {
        return path;
    }

    public String getUser() {
        return user;
    }

    private static Map<String, DeviceType> initMap() {
		return Stream.of(values()).collect(Collectors.toMap(DeviceType::getName, e -> e));
	}

}
