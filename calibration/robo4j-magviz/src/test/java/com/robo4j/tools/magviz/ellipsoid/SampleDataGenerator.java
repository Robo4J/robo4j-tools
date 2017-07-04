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
class SampleDataGenerator {
    private static final int NUMBER_POINTS = 1000;
	private Random r = new Random();

	List<Tuple3d> generatePoints(double a, double b, double c, double shiftx, double shifty, double shiftz,
			double noiseIntensity) {
		double[] x = new double[NUMBER_POINTS];
		double[] y = new double[NUMBER_POINTS];
		double[] z = new double[NUMBER_POINTS];

		IntStream.range(0, NUMBER_POINTS).forEach(i -> {
			double s = Math.toRadians(r.nextInt(360));
			double t = Math.toRadians(r.nextInt(360));

			x[i] = a * Math.cos(s) * Math.cos(t);
			y[i] = b * Math.cos(s) * Math.sin(t);
			z[i] = c * Math.sin(s);

		});

		double angle = Math.toRadians((Math.PI / 6));

		IntStream.range(0, NUMBER_POINTS).forEach(i ->{
            x[i] = x[i] * Math.cos(angle) - y[i] * Math.sin(angle) + shiftx + r.nextDouble() * noiseIntensity;
            y[i] = x[i] * Math.sin(angle) + y[i] * Math.cos(angle) + shifty + r.nextDouble() * noiseIntensity;
            z[i] = z[i] + shiftz + r.nextDouble() * noiseIntensity;
        });

		return IntStream.range(0, NUMBER_POINTS).mapToObj(i -> new Tuple3d(x[i], y[i], z[i])).collect(Collectors.toList());
	}
}