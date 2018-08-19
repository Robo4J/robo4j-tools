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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package io.robo4j.jmc.visualization.scan;

/**
 * TODO(Marcus/Mar 9, 2017): Should at some point just depend on math.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class PointXY {
	/**
	 * Range in meters
	 */
	private float range;
	
	/**
	 * Angle in radians
	 */
	private float angle;

	/**
	 * x coordinate relative Coff-E
	 */
	private float x;
	
	/**
	 * y coordinate relative Coff-E
	 */
	private float y;
	
	public PointXY(float x, float y) {
		this.range = (float) Math.sqrt(x*x + y*y);
		this.angle = (float) Math.atan(x/y);
		this.x = x;
		this.y = y;
	}
	
	public float getRange() {
		return range;
	}
	
	public float getAngle() {
		return angle;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}
	
	public boolean closer(PointXY p) {
		return this.range <= p.getRange();
	}
	
	public boolean farther(PointXY p) {
		return this.range > p.getRange();
	}
	
	public String toString() {
		return String.format("x:%2.1f, y:%2.1f, range:%2.1f, angle:%2.1f", x, y, range, Math.toDegrees(angle));
	}

	public static PointXY fromPolar(float angle, float range) {
		float x = (float) Math.sin(angle) * range;
		float y = (float) Math.cos(angle) * range;
		return new PointXY(x, y);
	}

	public float distance(PointXY p) {
		float deltaX = p.getX() - x;
		float deltaY = p.getY() - y;
		return (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(angle);
		result = prime * result + Float.floatToIntBits(range);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PointXY))
			return false;
		PointXY other = (PointXY) obj;
		if (Float.floatToIntBits(angle) != Float.floatToIntBits(other.angle))
			return false;
		if (Float.floatToIntBits(range) != Float.floatToIntBits(other.range))
			return false;
		return true;
	}
}
