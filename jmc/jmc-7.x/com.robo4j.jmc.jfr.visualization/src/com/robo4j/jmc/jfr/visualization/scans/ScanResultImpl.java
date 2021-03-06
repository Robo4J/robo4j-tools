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
package com.robo4j.jmc.jfr.visualization.scans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import com.robo4j.math.geometry.Point2f;
import com.robo4j.math.geometry.ScanResult2D;

/**
 * The implementation of a scan result. This particular implementation will emit
 * JFR events to help with the analysis of the recorded JFR data.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ScanResultImpl implements ScanResult2D {
	private static final PointComparator POINT_COMPARATOR = new PointComparator();
	private final List<Point2f> points = new ArrayList<>();

	private final Predicate<Point2f> filter;
	
	private double maxX;
	private double minX;
	private double maxY;
	private double minY;
	private int scanID;

	private Point2f farthestPoint;
	private Point2f closestPoint;
	private Point2f targetPoint;

	private float angularResolution = Float.NaN;

	private static class PointComparator implements Comparator<Point2f> {
		@Override
		public int compare(Point2f o1, Point2f o2) {
			return Float.compare(o1.getAngle(), o2.getAngle());
		}
	}
	
	public ScanResultImpl(int scanID, Predicate<Point2f> filter) {
		this(scanID, Float.NaN, filter);
	}

	public ScanResultImpl(int scanID, float angularResolution, Predicate<Point2f> filter) {
		this.scanID = scanID;
		this.angularResolution = angularResolution;
		this.filter = filter;
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

	/**
	 * Adds a point to the scan.
	 * 
	 * @param range
	 *            range in meters.
	 * @param angle
	 *            angle in radians.
	 */
	public void addPoint(float range, float angle) {
		addPoint(Point2f.fromPolar(range, angle));
	}

	public void addPoint(Point2f p) {
		if (filter.test(p)) {
			points.add(p);
			updateBoundaries(p);
		}
	}

	private void updateBoundaries(Point2f p) {
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

	public List<Point2f> getPoints() {
		return points;
	}

	public void addResult(ScanResultImpl result) {
		for (Point2f p : result.getPoints()) {
			addPoint(p);
		}
	}

	public Point2f getNearestPoint() {
		return closestPoint;
	}

	public Point2f getFarthestPoint() {
		return farthestPoint;
	}

	public void sort() {
		Collections.sort(points, POINT_COMPARATOR);
	}

	public Point2f getTargetPoint() {
		return targetPoint;
	}

	public void setTargetPoint(Point2f targetPoint) {
		this.targetPoint = targetPoint;
	}

	public String toString() {
		return String.format("Closest: %s, Farthest: %s, # points: %d", String.valueOf(getNearestPoint()),
				String.valueOf(getFarthestPoint()), getPoints().size());
	}

	@Override
	public float getAngularResolution() {
		if (Float.isNaN(angularResolution)) {
			angularResolution = calculateApproximateAngularResolution();
		}
		return angularResolution;
	}

	private float calculateApproximateAngularResolution() {
		return Math.abs((getRightmostPoint().getAngle() - getLeftmostPoint().getAngle()) / points.size());
	}
	
	@Override
	public Point2f getLeftmostPoint() {
		return points.get(0);
	}

	@Override
	public Point2f getRightmostPoint() {
		// NOTE(Marcus/Sep 5, 2017): Should be fine, as the add phase is
		// separate from the read phase.
		return points.get(points.size() - 1);
	}
}
