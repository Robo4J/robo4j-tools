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

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.geometry.Point3D;
import javafx.scene.transform.Rotate;

/**
 * Simple generator to test the visualizer.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class PointGenerator {
	private final static Random RND = new Random();

	public static List<Point3D> generatePoints(int noOfPoints, double startRadius, double gaussianNoise) {
		return IntStream.range(0, noOfPoints)
				.mapToObj(i -> generatePoint(startRadius, gaussianNoise))
				.collect(Collectors.toList());
	}
	
	public static List<Point3D> generatePoints(int noOfPoints, double startRadius, double gaussianNoise, Point3D bias, Point3D scale, Point3D rotAxis, double rotAngle) {
		final List<Point3D> generatedPoints = generatePoints(noOfPoints, startRadius, gaussianNoise);
		return generatedPoints.stream()
				.map(p -> {
					Point3D newPoint = new Point3D(p.getX() * scale.getX(), p.getY() * scale.getY(), p.getZ() * scale.getZ());
					Rotate rotate = new Rotate(rotAngle, rotAxis);
					Point3D rotPoint = rotate.transform(newPoint);
					return rotPoint.subtract(bias);})
				.collect(Collectors.toList());
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
