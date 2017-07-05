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

package com.robo4j.tools.magviz.util;

import java.util.List;

import com.robo4j.tools.magviz.ellipsoid.PointGenerator;

import javafx.geometry.Point3D;

/**
 * Simple generator to test the visualizer.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class PointGeneratorExample {
	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println("Usage: PointGenerator <number of points> <radius> <stddev>\nExample: PointGenerator 1000 200 4");
			System.exit(2);
		}

		int noOfPoints = Integer.parseInt(args[0]);
		double r = Double.parseDouble(args[1]);
		double stddev = Double.parseDouble(args[2]);

		List<Point3D> generatedPoints = PointGenerator.generatePoints(noOfPoints, r, stddev);
		generatedPoints.forEach((point) -> System.out.println(String.format("%f;%f;%f", point.getX(), point.getY(), point.getZ())));
	}
}
