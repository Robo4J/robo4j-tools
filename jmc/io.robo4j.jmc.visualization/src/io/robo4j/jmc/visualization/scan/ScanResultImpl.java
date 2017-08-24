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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * TODO(Marcus/Mar 9, 2017): Should at some point just depend on math.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ScanResultImpl implements ScanResult {
	private double maxX;
	private double minX;
	private double maxY;
	private double minY;
	private int scanID;
	
	private PointXY farthestPoint;
	private PointXY closestPoint;
	private PointXY targetPoint;
	private List<PointXY> points = new ArrayList<PointXY>();
	private PointXY goalPoint;

	public ScanResultImpl(int scanID) {
		this.scanID = scanID;
	}
	
	public double getMaxX() {
		return maxX;
	}

	public double getMinX() {
		return minX;
	}

	public double getMaxY() {
		return maxY;
	}

	public double getMinY() {
		return minY;
	}

	public int getScanID() {
		return scanID;
	}
	
	public void addPoint(PointXY p) {
		if (p.getRange() < 0.05) {
			return;
		}
		points.add(p);
		maxX = Math.max(maxX, p.getX());
		maxY = Math.max(maxY, p.getY());
		minX = Math.min(minX, p.getX());
		minY = Math.min(minY, p.getY());
		if (closestPoint == null) {
			closestPoint = p;
		} else {
			if (p.closer(closestPoint)) {
				closestPoint = p;
			}
		}
		if (farthestPoint == null) {
			farthestPoint = p;
		} else {
			if (p.farther(farthestPoint)) {
				farthestPoint = p;
			}
		}
	}

	public List<PointXY> getPoints() {
		return points;
	}

	public void addPoint(float range, float angle) {
		PointXY point = new PointXY(range, angle);
		addPoint(point);
	}

	public void addResult(ScanResultImpl result) {
		for (PointXY p : result.getPoints()) {
			addPoint(p);
		}
	}

	public PointXY getNearestPoint() {
		return closestPoint;
	}

	public PointXY getFarthestPoint() {
		return farthestPoint;
	}

	public PointXY getTargetPoint() {
		return targetPoint;
	}

	public PointXY getGoalPoint() {
		return goalPoint;
	}

	public void addTargetPoint(PointXY targetPoint) {
		this.targetPoint = targetPoint;
	}

	public void addGoalPoint(PointXY goalPoint) {
		this.goalPoint = goalPoint;
	}

	public void sort() {
		Collections.sort(points, new Comparator<PointXY>() {
			@Override
			public int compare(PointXY o1, PointXY o2) {
				return Float.compare(o1.getAngle(), o2.getAngle());
			}
		});
	}
}
