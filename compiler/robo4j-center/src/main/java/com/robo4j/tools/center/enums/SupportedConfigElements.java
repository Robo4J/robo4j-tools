/*
 * Copyright (C) 2017. Miroslav Kopecky
 * This SupportedConfigElements.java  is part of robo4j.
 * path: /Users/miroslavkopecky/GiTHub_MiroKopecky/robo4j-tools/compiler/robo4j-center/src/main/java/com/robo4j/tools/center/enums/SupportedConfigElements.java
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
 * Supported XML configuration Elements
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public enum  SupportedConfigElements {

    MAIN_PACKAGE    ("mainPackage"),
    MAIN_CLASS      ("mainClass"),
    ROBO4J_LIB      ("robo4jLibrary"),
    OUT_DIR         ("outDirectory"),
    ACTIONS         ("centerActions"),
    JAR_FILE_NAME   ("jarFileName"),
    DEVICE_IP       ("deviceIp"),
    DEVICE_TYPE     ("deviceType")
    ;

    String name;

    private static final Map<String, SupportedConfigElements> map = Stream.of(SupportedConfigElements.values())
            .collect(Collectors.toMap(e -> e.getName().toLowerCase(), e -> e));

    SupportedConfigElements(String name) {
        this.name = name;
    }

    public static SupportedConfigElements byName(String name){
        return map.get(name.toLowerCase());
    }

    public String getName() {
        return name;
    }


    @Override
    public String toString() {
        return "SupportedConfigElements{" +
                "name='" + name + '\'' +
                '}';
    }
}
