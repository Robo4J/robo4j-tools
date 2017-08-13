/*
 * Copyright (C) 2017. Miroslav Kopecky
 * This Robo4jCenterProperties.java  is part of robo4j.
 * path: /Users/miroslavkopecky/GiTHub_MiroKopecky/robo4j-tools/compiler/robo4j-center/src/main/java/com/robo4j/tools/center/util/Robo4jCenterProperties.java
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

package com.robo4j.tools.center.util;

import com.robo4j.tools.center.enums.SupportedOS;

/**
 * Upload/Compile task information holder
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 07.09.2016
 */
public class TaskProperties {

	private final SupportedOS detectedSystem;
	private final String mainPackage;
	private final String mainClass;
	private final String robo4jLibrary;
	private final String compiledFilename;
	private final String outputDirectory;

	public TaskProperties(SupportedOS detectedSystem, String mainPackage, String mainClass, String robo4jLibrary, String compiledFilename,
						  String outputDirectory) {
		this.detectedSystem = detectedSystem;
		this.mainPackage = mainPackage;
		this.mainClass = mainClass;
		this.robo4jLibrary = robo4jLibrary;
		this.compiledFilename = compiledFilename;
		this.outputDirectory = outputDirectory;
	}

	public SupportedOS getDetectedSystem() {
		return detectedSystem;
	}

	public String getMainPackage() {
		return mainPackage;
	}

	public String getMainClass() {
		return mainClass;
	}

	public String getSeparator() {
		return detectedSystem.getSeparator();
	}

	public String getRobo4jLibrary() {
		return robo4jLibrary;
	}

	public String getCompiledFilename() {
		return compiledFilename;
	}

	public String getOutputDirectory() {
		return outputDirectory;
	}

	@Override
	public String toString() {
		return "TaskProperties{" +
				"detectedSystem=" + detectedSystem +
				", mainPackage='" + mainPackage + '\'' +
				", mainClass='" + mainClass + '\'' +
				", robo4jLibrary='" + robo4jLibrary + '\'' +
				", compiledFilename='" + compiledFilename + '\'' +
				", outputDirectory='" + outputDirectory + '\'' +
				'}';
	}
}
