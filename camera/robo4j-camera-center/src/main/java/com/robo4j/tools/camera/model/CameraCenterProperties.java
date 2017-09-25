/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.tools.camera.model;

import com.robo4j.tools.camera.enums.SupportedConfigElements;

import java.util.Map;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class CameraCenterProperties {

    private final Map<SupportedConfigElements, String> map;

    public CameraCenterProperties(Map<SupportedConfigElements, String> map) {
        this.map = map;
    }

    public String getDeviceIP() {
        return map.get(SupportedConfigElements.DEVICE_IP);
    }

    public String getDevicePort() {
        return map.get(SupportedConfigElements.DEVICE_PORT);
    }

    @Override
    public String toString() {
        return "CameraCenterProperties{" +
                "map=" + map +
                '}';
    }
}
