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

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class EllipsoidSolverMainTest {

    public static void main(String[] args) {
        System.out.println("ellipsoid fit test");

        // Generates points for plots.
        EllipsoidToSphereSolver solver = new EllipsoidToSphereSolver(new SampleDataGenerator().generatePoints((1000)));
        Tuple3d center = solver.getSphereMatrix();

        System.out.println("CENTER: " + center);
        System.out.println("RADII: " + solver.getRadii());
        System.out.println("eigen0: " + solver.getEigenVector0());
        System.out.println("eigen1: " + solver.getEigenVector1());
        System.out.println("eigen2: " + solver.getEigenVector2());
    }
}
