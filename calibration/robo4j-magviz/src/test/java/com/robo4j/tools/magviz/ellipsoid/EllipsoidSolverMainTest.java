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

import com.robo4j.tools.magviz.math.Tuple3d;

import java.util.List;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class EllipsoidSolverMainTest {

    // This ellipsoid will be scaled back to the sphere
    private static double A_CONTROL_ELLIPSE = 1.4;
    private static double B_CONTROL_ELLIPSE = 1.3;
    private static double C_CONTROL_ELLIPSE = 1.2;
    private static double SHIFT_X_CONTROL_ELLIPSE = 2;
    private static double SHIFT_Y_CONTROL_ELLIPSE = 2;
    private static double SHIFT_Z_CONTROL_ELLIPSE = 2;
    private static double NOISE_INTENSITY = 0.01;

    // Generates points for plots.
    static SampleDataGenerator pointGenerator = new SampleDataGenerator();

    private static final List<Tuple3d> CONTROL_ELLIPSOID_POINTS = pointGenerator.generatePoints(
            A_CONTROL_ELLIPSE, B_CONTROL_ELLIPSE, C_CONTROL_ELLIPSE,
            SHIFT_X_CONTROL_ELLIPSE, SHIFT_Y_CONTROL_ELLIPSE,
            SHIFT_Z_CONTROL_ELLIPSE, NOISE_INTENSITY);

    public static void main(String[] args) {
        System.out.println("ellipsoid fit test");

        EllipsoidToSphereSolver solver = new EllipsoidToSphereSolver(CONTROL_ELLIPSOID_POINTS);
        Tuple3d center = solver.getSphereMatrix();

        System.out.println("CENTER: " + center);
        System.out.println("RADII: " + solver.getRadii());
        System.out.println("eigen0: " + solver.getEigenVector0());
        System.out.println("eigen1: " + solver.getEigenVector1());
        System.out.println("eigen2: " + solver.getEigenVector2());
    }
}
