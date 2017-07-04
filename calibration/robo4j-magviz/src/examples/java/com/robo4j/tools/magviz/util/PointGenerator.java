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

	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println(
					"Usage: PointGenerator <number of points> <radius> <stddev>\nExample: PointGenerator 1000 200 4");
			System.exit(2);
		}

		int noOfPoints = Integer.parseInt(args[0]);
		double r = Double.parseDouble(args[1]);
		double stddev = Double.parseDouble(args[2]);

		for (int i = 0; i < noOfPoints; i++) {
			Point3D point = generatePoint(r, stddev);
			System.out.println(String.format("%f;%f;%f", point.getX(), point.getY(), point.getZ()));
		}
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
