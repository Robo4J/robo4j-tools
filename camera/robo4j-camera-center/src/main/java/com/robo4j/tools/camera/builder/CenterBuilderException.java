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

package com.robo4j.tools.camera.builder;

/**
 * Exception throws from the CenterBuilder
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class CenterBuilderException extends Exception  {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param message
     *            the exception message.
     */
    public CenterBuilderException(String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param message
     *            the exception message.
     * @param cause
     *            the exception cause.
     */
    public CenterBuilderException(String message, Throwable cause) {
        super(message, cause);
    }

}
