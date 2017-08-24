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
package io.robo4j.jmc.visualization.scan;

import java.util.List;
/**
 * TODO(Marcus/Mar 9, 2017): Should at some point just depend on math.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class FeatureSet {
	private final List<Line> lines;
	private final List<CurvaturePoint> corners;
	
	public FeatureSet(List<Line> lines, List<CurvaturePoint> corners) {
		this.lines = lines;
		this.corners = corners;
	}

	public List<Line> getLines() {
		return lines;
	}
	
	public final List<CurvaturePoint> getCorners() {
		return corners;
	}
}
