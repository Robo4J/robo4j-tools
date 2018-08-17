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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.jmc.jfr.visualization;

import static org.openjdk.jmc.common.item.Attribute.attr;
import static org.openjdk.jmc.common.unit.UnitLookup.FLAG;
import static org.openjdk.jmc.common.unit.UnitLookup.NUMBER;
import static org.openjdk.jmc.common.unit.UnitLookup.PLAIN_TEXT;

import org.openjdk.jmc.common.item.IAttribute;
import org.openjdk.jmc.common.unit.IQuantity;

public class Robo4JAttributes {
	public static final IAttribute<IQuantity> SCAN_ID = attr("scanID", //$NON-NLS-1$
			"Scan ID", null, NUMBER);

	public static final IAttribute<String> SCAN_INFO = attr("scanInfo", //$NON-NLS-1$
			"Scan Info", "Textual information for a scan", PLAIN_TEXT);

	public static final IAttribute<Boolean> SCAN_LEFT_RIGHT = attr("scanLeftRight", //$NON-NLS-1$
			"Scan Left/Right", null, FLAG);

	public static final IAttribute<IQuantity> SCAN_POINT_2D_X = attr("x", //$NON-NLS-1$
			null, null, NUMBER);

	public static final IAttribute<IQuantity> SCAN_POINT_2D_Y = attr("y", //$NON-NLS-1$
			null, null, NUMBER);

}
