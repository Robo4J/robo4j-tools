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
		if (args.length != 3 && args.length != 7) {
			System.out.println(
					"Usage: PointGenerator <number of points> <radius> <stddev> [<bias> <scale> <rotAxis> <rotangle>]\nExample: PointGenerator 1000 200 4 5,2,15 1,1.4,1 45,45,45 45");
			System.exit(2);
		}

		int noOfPoints = Integer.parseInt(args[0]);
		double r = Double.parseDouble(args[1]);
		double stddev = Double.parseDouble(args[2]);
		Point3D bias = null;
		Point3D scale = null;
		Point3D rotAxis = null;
		double angle = 0.0;
		if (args.length == 7) {
			bias = getPoint(args[3]);
			scale = getPoint(args[4]);
			rotAxis =  getPoint(args[5]);
			angle = Double.parseDouble(args[6]);
		}
		List<Point3D> generatedPoints = args.length == 3 ? PointGenerator.generatePoints(noOfPoints, r, stddev)
				: PointGenerator.generatePoints(noOfPoints, r, stddev, bias, scale, rotAxis, angle);
		generatedPoints.forEach((point) -> System.out.println(String.format("%f;%f;%f", point.getX(), point.getY(), point.getZ())));
	}

	private static Point3D getPoint(String string) {
		String[] split = string.split(",");
		if (split.length != 3) {
			throw new IllegalArgumentException("");
		}
		return new Point3D(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));
	}
}
