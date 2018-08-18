/*
 * Copyright (C) 2018. Miroslav Kopecky
 * This ProjectTypeEnum.java  is part of robo4j.
 * path: /Users/mirowengner/GiTHub_MiroKopecky/robo4j-tools/compiler/robo4j-center/src/main/java/com/robo4j/tools/center/enums/ProjectTypeEnum.java
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
 * @author Miroslav Wengner (@miragemiko)
 */
public enum ProjectTypeEnum {

	//@formatter:off
    MAVEN           ("maven", "src.main.java", "src.main.resources"),
    STAND_ALONE     ("standalone", "", "");
    ;
    //@formatter:on

	private static final Map<String, ProjectTypeEnum> map = Stream.of(ProjectTypeEnum.values())
			.collect(Collectors.toMap(e -> e.getType().toLowerCase(), e -> e));

	private final String type;
	private final String srcPath;
	private final String resourcesPath;

	ProjectTypeEnum(String type, String srcPath, String resourcesPath) {
		this.type = type;
		this.srcPath = srcPath;
		this.resourcesPath = resourcesPath;
	}

	public String getType() {
		return type;
	}

	public String getSrcPath() {
		return srcPath;
	}

    public String getResourcesPath() {
        return resourcesPath;
    }

    public static ProjectTypeEnum byName(String name) {
		return map.get(name.toLowerCase());
	}

	@Override
	public String toString() {
		return "ProjectTypeEnum{" + "type='" + type + '\'' + '}';
	}
}
