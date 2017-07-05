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

package com.robo4j.tools.magviz.ellipsoid;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javafx.geometry.Point3D;

/**
 * Simple generator to test the visualizer.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class PointGenerator {
	private final static Random RND = new Random();

	public static List<Point3D> generatePoints(int noOfPoints, double startRadius, double gaussianNoise) {
		List<Point3D> points = new ArrayList<>();
		for (int i = 0; i < noOfPoints; i++) {
			Point3D point = generatePoint(startRadius, gaussianNoise);
			points.add(point);
		}
		return points;
	}

	private static Point3D generatePoint(double r, double stddev) {
		double radius = RND.nextGaussian() * stddev + r;
		double s = RND.nextDouble() * Math.PI * 2;
		double t = RND.nextDouble() * Math.PI;
		double x = radius * Math.cos(s) * Math.sin(t);
		double y = radius * Math.sin(s) * Math.sin(t);
		double z = radius * Math.cos(t);
		return new Point3D(x, y, z);
	}
}
