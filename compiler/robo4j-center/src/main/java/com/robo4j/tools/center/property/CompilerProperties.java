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

package com.robo4j.tools.center.property;

import com.robo4j.tools.center.enums.ProjectTypeEnum;
import com.robo4j.tools.center.enums.SupportedOS;
import com.robo4j.tools.center.model.CenterProperties;

/**
 * Upload/Compile task information holder
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 07.09.2016
 */
public class CompilerProperties {

	private final SupportedOS detectedSystem;
	private final ProjectTypeEnum projectType;
	private final String srcPath;
	private final String resourcePath;
	private final String mainPackage;
	private final String mainClass;
	private final String robo4jLibrary;
	private final String compiledFilename;
	private final String outputDirectory;
	private final String excludedPaths;

	public CompilerProperties(SupportedOS detectedSystem, CenterProperties centerProperties) {
		this.detectedSystem = detectedSystem;
		this.projectType = ProjectTypeEnum.byName(centerProperties.getProjectType());
		this.srcPath = centerProperties.getSrcPath();
		this.resourcePath = centerProperties.getResourcePath();
		this.mainPackage = centerProperties.getMainPackage();
		this.mainClass = centerProperties.getMainClass();
		this.robo4jLibrary = centerProperties.getRobo4jLibrary();
		this.compiledFilename = centerProperties.getJarFileName();
		this.outputDirectory = centerProperties.getOutDirectory();
		this.excludedPaths = centerProperties.getExcludedPaths();
	}

	public SupportedOS getDetectedSystem() {
		return detectedSystem;
	}

	public ProjectTypeEnum getProjectType() {
		return projectType;
	}

	public String getSrcPath(){
		return srcPath;
	}

	public String getResourcePath(){
		return resourcePath;
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

	public String getExcludedPaths() {
		return excludedPaths;
	}

	@Override
	public String toString() {
		return "CompilerProperties{" + "detectedSystem=" + detectedSystem + ", mainPackage='" + mainPackage + '\''
				+ ", mainClass='" + mainClass + '\'' + ", robo4jLibrary='" + robo4jLibrary + '\''
				+ ", compiledFilename='" + compiledFilename + '\'' + ", outputDirectory='" + outputDirectory + '\''
				+ ", excludedPaths='" + excludedPaths + '\'' + '}';
	}
}
