/*
 * Copyright (C) 2017. Miroslav Kopecky
 * This Robo4jOS.java  is part of robo4j.
 * path: /Users/miroslavkopecky/GiTHub_MiroKopecky/robo4j-tools/compiler/robo4j-center/src/main/java/com/robo4j/tools/center/enums/Robo4jOS.java
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

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public enum SupportedOS {

    // @formatter:off
	WINDOWS			(0, "win", "\\"),
	UNIX			(1, "nux", "/"),
	MAC				(2, "mac", "/")
	;
	// @formatter:on

    private volatile static Map<String, SupportedOS> labelToOsMapping;
    private int code;
    private String label;
    private String separator;

    SupportedOS(int code, String label, String separator) {
        this.code = code;
        this.label = label;
        this.separator = separator;
    }

    /**
     * preferred is UNIX
     *
     * @param osProperty System property
     * @return found operation system
     */
    public static SupportedOS getOsByProperty(final String osProperty) {
        if (labelToOsMapping == null) {
            labelToOsMapping = mappingInit();
        }
        //@formatter:off
		return labelToOsMapping.entrySet()
				.stream()
				.filter(e -> osProperty.toLowerCase().contains(e.getKey()))
				.map(Map.Entry::getValue)
				.findFirst().orElse(UNIX);
		//@formatter:on
    }


    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public String getSeparator() {
        return separator;
    }

    private static Map<String, SupportedOS> mappingInit() {
        return Arrays.stream(values())
                .collect(Collectors.toMap(SupportedOS::getLabel, e -> e));
    }


    @Override
    public String toString() {
        return "SupportedOS{" + "code=" + code + ", label='" + label + '\'' + '}';
    }
}
