/*
 * Copyright (C) 2017. Miroslav Kopecky
 * This CenterProperties.java  is part of robo4j.
 * path: /Users/miroslavkopecky/GiTHub_MiroKopecky/robo4j-tools/compiler/robo4j-center/src/main/java/com/robo4j/tools/center/model/CenterProperties.java
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

package com.robo4j.tools.center.model;

import java.util.HashMap;
import java.util.Map;

import com.robo4j.tools.center.CenterMain;
import com.robo4j.tools.center.enums.ProjectTypeEnum;
import com.robo4j.tools.center.enums.SupportedConfigElements;

/**
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class CenterProperties {

	private final Map<SupportedConfigElements, String> map;

	public CenterProperties() {
		map = new HashMap<>();
		map.put(SupportedConfigElements.PROJECT_TYPE, CenterMain.PROJECT_MAVEN_TYPE);
		map.put(SupportedConfigElements.MAIN_SRC_PATH, ProjectTypeEnum.MAVEN.getSrcPath());
		map.put(SupportedConfigElements.MAIN_RESOURCE_PATH, ProjectTypeEnum.MAVEN.getResourcesPath());
		map.put(SupportedConfigElements.MAIN_PACKAGE, "com.robo4j.lego.j1kids.example");
		map.put(SupportedConfigElements.MAIN_CLASS, "Number42Main.java");
		map.put(SupportedConfigElements.ROBO4J_LIB, "robo4j-units-lego-alpha-0.3.jar");
		map.put(SupportedConfigElements.OUT_DIR, "out");
		map.put(SupportedConfigElements.JAR_FILE_NAME, "Number42Robot");
		map.put(SupportedConfigElements.ACTIONS, "compile,upload");
		map.put(SupportedConfigElements.DEVICE_IP, "10.0.1.1");
		map.put(SupportedConfigElements.DEVICE_TYPE, "lego");
		map.put(SupportedConfigElements.EXCLUDED_PATHS, "excluded.paths");
	}

	public CenterProperties(Map<SupportedConfigElements, String> map) {
		this.map = map;
	}

	public String getProjectType() {
		return map.get(SupportedConfigElements.PROJECT_TYPE);
	}

	public String getSrcPath() {
		return map.get(SupportedConfigElements.MAIN_SRC_PATH);
	}

	public String getResourcePath() {
		return map.get(SupportedConfigElements.MAIN_RESOURCE_PATH);
	}

	public String getMainPackage() {
		return map.get(SupportedConfigElements.MAIN_PACKAGE);
	}

	public String getMainClass() {
		return map.get(SupportedConfigElements.MAIN_CLASS);
	}

	public String getRobo4jLibrary() {
		return map.get(SupportedConfigElements.ROBO4J_LIB);
	}

	public String getOutDirectory() {
		return map.get(SupportedConfigElements.OUT_DIR);
	}

	public String getCenterActions() {
		return map.get(SupportedConfigElements.ACTIONS);
	}

	public String getDeviceIP() {
		return map.get(SupportedConfigElements.DEVICE_IP);
	}

	public String getDevicePort() {
		return map.get(SupportedConfigElements.DEVICE_PORT);
	}

	public String getDeviceType() {
		return map.get(SupportedConfigElements.DEVICE_TYPE);
	}

	public String getJarFileName() {
		return map.get(SupportedConfigElements.JAR_FILE_NAME);
	}

	public String getPassword() {
		return map.get(SupportedConfigElements.DEVICE_PASS);
	}

	public String getExcludedPaths() {
		return map.get(SupportedConfigElements.EXCLUDED_PATHS);
	}

	@Override
	public String toString() {
		return "CenterProperties{" + "map=" + map + '}';
	}
}
