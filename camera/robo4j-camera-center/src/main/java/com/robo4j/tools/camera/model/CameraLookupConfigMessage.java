/*
 * Copyright (c) 2014, 2018, Marcus Hirt, Miroslav Wengner
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

import com.robo4j.socket.http.codec.CameraConfigMessage;

/**
 * CameraLookupConfigMessage
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class CameraLookupConfigMessage {

    private String context;
    private String target;
    private CameraConfigMessage configMessage;

    public CameraLookupConfigMessage(String context, String target, CameraConfigMessage configMessage) {
        this.context = context;
        this.target = target;
        this.configMessage = configMessage;
    }

    public String getContext() {
        return context;
    }

    public String getTarget() {
        return target;
    }

    public CameraConfigMessage getConfigMessage() {
        return configMessage;
    }
}
