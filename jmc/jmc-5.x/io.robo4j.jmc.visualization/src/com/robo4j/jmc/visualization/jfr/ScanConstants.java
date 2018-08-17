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
package com.robo4j.jmc.visualization.jfr;

/**
 * Constants for the scan events.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class ScanConstants {
	public static final String FIELD_X = "x";
	public static final String FIELD_Y = "y";
	public static final String SCAN_ID = "scanID";

	public static final String TYPE_SCAN = "scan";
	public static final String TYPE_SCAN_POINT = "scan point 2d";
	public static final String TYPE_TARGET = "target point";
	public static final String TYPE_GOAL = "goal point";
}
