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

import org.openjdk.jmc.common.item.IItemFilter;
import org.openjdk.jmc.common.item.ItemFilters;

public class Robo4JFilters {
	public static final IItemFilter SCAN = ItemFilters.type(Robo4JTypeIDs.SCAN);
	public static final IItemFilter SCAN_POINT_2D = ItemFilters.type(Robo4JTypeIDs.SCAN_POINT_2D);
	public static final IItemFilter ALL_ROBO4J = ItemFilters.or(SCAN, SCAN_POINT_2D);
}
