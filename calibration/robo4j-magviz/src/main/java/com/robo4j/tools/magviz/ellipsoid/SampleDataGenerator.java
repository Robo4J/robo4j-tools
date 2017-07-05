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

import com.robo4j.tools.magviz.math.Tuple3d;


/**
 * Simple points generator
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class SampleDataGenerator {

	// This ellipsoid will be scaled back to the sphere
	private static final double A_CONTROL_ELLIPSE = 22.4;
	private static final double B_CONTROL_ELLIPSE = 3.3;
	private static final double C_CONTROL_ELLIPSE = 9.2;
	private static final double SHIFT_X_CONTROL_ELLIPSE = 2;
	private static final double SHIFT_Y_CONTROL_ELLIPSE = 2;
	private static final double SHIFT_Z_CONTROL_ELLIPSE = 2;
	private static final double NOISE_INTENSITY = 0.01;

	private Random r = new Random();

	public  List<Tuple3d> generatePoints(int number){
		return generatePoints(number, A_CONTROL_ELLIPSE, B_CONTROL_ELLIPSE, C_CONTROL_ELLIPSE,
				SHIFT_X_CONTROL_ELLIPSE, SHIFT_Y_CONTROL_ELLIPSE,
				SHIFT_Z_CONTROL_ELLIPSE, NOISE_INTENSITY);
	}

	public List<Tuple3d> generatePoints(int number, double a, double b, double c, double shiftx, double shifty, double shiftz,
			double noiseIntensity) {
		double[] x = new double[number];
		double[] y = new double[number];
		double[] z = new double[number];

		IntStream.range(0, number).forEach(i -> {
			double s = Math.toRadians(r.nextInt(360));
			double t = Math.toRadians(r.nextInt(360));

			x[i] = a * Math.cos(s) * Math.cos(t);
			y[i] = b * Math.cos(s) * Math.sin(t);
			z[i] = c * Math.sin(s);

		});

		double angle = Math.toRadians((Math.PI / 6));

		IntStream.range(0, number).forEach(i ->{
            x[i] = x[i] * Math.cos(angle) - y[i] * Math.sin(angle) + shiftx + r.nextDouble() * noiseIntensity;
            y[i] = x[i] * Math.sin(angle) + y[i] * Math.cos(angle) + shifty + r.nextDouble() * noiseIntensity;
            z[i] = z[i] + shiftz + r.nextDouble() * noiseIntensity;
        });

		return IntStream.range(0, number).mapToObj(i -> new Tuple3d(x[i], y[i], z[i])).collect(Collectors.toList());
	}
}