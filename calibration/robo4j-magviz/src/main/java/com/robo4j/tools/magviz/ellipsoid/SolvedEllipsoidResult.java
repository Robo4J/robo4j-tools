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

import org.apache.commons.math3.linear.RealMatrix;

import javafx.geometry.Point3D;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class SolvedEllipsoidResult {

    private Point3D offset;
    private RealMatrix rotationMatrix;

    public SolvedEllipsoidResult(Point3D offset, RealMatrix rotationMatrix) {
        this.offset = offset;
        this.rotationMatrix = rotationMatrix;
    }

    public Point3D getOffset() {
        return offset;
    }


    public RealMatrix getRotationMatrix() {
        return rotationMatrix;
    }

    @Override
    public String toString() {
        return "{" +
                "offset=" + offset +
                ", rotationMatrix=" + rotationMatrix +
                '}';
    }
}
