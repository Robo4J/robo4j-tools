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

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.robo4j.tools.center.builder.CenterBuilder;
import com.robo4j.tools.center.enums.CenterCommand;
import com.robo4j.tools.center.enums.DeviceType;
import com.robo4j.tools.center.enums.SupportedOS;
import com.robo4j.tools.center.model.CenterProperties;
import com.robo4j.tools.center.property.CompilerProperties;
import com.robo4j.tools.center.provider.CompilerProvider;
import com.robo4j.tools.center.provider.UploadProvider;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class CenterMain {

	private static final String COMMAND_SPLITTER = ",";
	private static final int OPTION_FILE = 1;
	public static final String PROJECT_MAVEN_TYPE = "maven";
	private final SupportedOS os;

	private final CenterProperties centerProperties;

	public static void main(String[] args) throws Exception {

		int argsSize = args.length;

		switch (argsSize) {
		case OPTION_FILE:
			Path path = Paths.get(args[0]);
			if (path.toFile().exists()) {
				final InputStream configStream = new FileInputStream(path.toFile());
				CenterBuilder builder = new CenterBuilder().add(configStream);
                CenterMain center = new CenterMain(builder.build());
                center.execute();
			} else {
				printHelpAndThrow();
			}
			break;
		default:
			printHelpAndThrow();
		}
	}

	CenterMain(CenterProperties centerProperties) {
		this.centerProperties = centerProperties;
		this.os = getOpSystem();

	}

	List<String> execute() {
		List<String> result = new ArrayList<>();
		getActions(centerProperties.getCenterActions()).forEach(action -> {
			switch (action) {
			case COMPILE:
				CompilerProperties properties = new CompilerProperties(os, centerProperties);
				CompilerProvider compiler = new CompilerProvider(properties);
				System.out.println("Compile STARTS");
				try {
					boolean compileState = compiler.compile();
					if (compileState) {
						compiler.createJar();
					}
				} catch (Exception e) {
					throw new CenterException("compile error", e);
				}
				result.add(action.getName());
				break;
			case UPLOAD:
				UploadProvider uploadProvider = new UploadProvider();
				DeviceType device = DeviceType.getDeviceByName(centerProperties.getDeviceType());
				uploadProvider.uploadScp(centerProperties.getJarFileName(), centerProperties.getDeviceIP(),
						device.getUser(), centerProperties.getPassword(), device.getPath());
				result.add(action.getName());
				break;
			default:
				throw new CenterException("not supported action: " + action);
			}
		});
		result.add("Done");
		return result;
	}

	private static void printHelpAndThrow() throws IllegalStateException {
		System.out.println(String.format("%s <robo4jCenter.xml>", CenterMain.class.getSimpleName()));
		throw new IllegalStateException("no valid configuration available");
	}

	private static SupportedOS getOpSystem() {
		final String currentOs = System.getProperty("os.name");
		return SupportedOS.getOsByProperty(currentOs);
	}

	private static List<CenterCommand> getActions(String actions) {
		String[] split = actions.split(COMMAND_SPLITTER);
		if (split.length == 0 || split.length > 2) {
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
