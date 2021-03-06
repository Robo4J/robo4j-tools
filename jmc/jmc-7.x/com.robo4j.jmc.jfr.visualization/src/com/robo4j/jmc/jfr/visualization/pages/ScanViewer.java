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
package com.robo4j.jmc.jfr.visualization.pages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import com.robo4j.math.features.FeatureExtraction;
import com.robo4j.math.features.FeatureSet;
import com.robo4j.math.features.Raycast;
import com.robo4j.math.geometry.CurvaturePoint2f;
import com.robo4j.math.geometry.Line2f;
import com.robo4j.math.geometry.Point2f;
import com.robo4j.math.geometry.ScanResult2D;

/**
 * Canvas for drawing a scan.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ScanViewer extends Canvas {
	private static final Point2f ORIGO = Point2f.fromCartesian(0, 0);
	private boolean renderRaycast = false;
	private boolean renderSegments = false;
	private boolean renderAngles = false;
	private boolean renderFeatures = false;

	private enum ColorIndex {
		white, black, grey, red, green, blue, cyan, lightgrey, orange, magenta
	}

	private final static Color[] PALETTE = new Color[] { new Color(null, 255, 255, 255), new Color(null, 0, 0, 0),
			new Color(null, 110, 110, 110), new Color(null, 255, 0, 0), new Color(null, 0, 255, 0), new Color(null, 0, 0, 255),
			new Color(null, 0, 255, 255), new Color(null, 190, 190, 190), new Color(null, 255, 165, 0), new Color(null, 255, 0, 255) };

	private volatile List<ScanResult2D> results = new ArrayList<>();
	private volatile Point2f goalPoint;

	public ScanViewer(Composite parent, int style) {
		super(parent, style);

		setBackground(PALETTE[0]);
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent de) {
				ScanViewer.this.widgetDisposed(de);
			}
		});

		addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent pe) {
				ScanViewer.this.paint(pe);
			}
		});
	}

	protected void widgetDisposed(DisposeEvent de) {
		disposeColors();
	}

	private void disposeColors() {
		for (Color c : PALETTE) {
			c.dispose();
		}
	}

	protected void paint(PaintEvent pe) {
		for (ScanResult2D result : results) {
			GC gc = pe.gc;
			int centerY = pe.height / 2;
			gc.setForeground(getColor(ColorIndex.red));
			if (result.getPoints().isEmpty()) {
				gc.drawText("Please select a scan to show", 0, centerY);
			} else {
				drawScan(pe, gc, result);
			}

		}
	}

	private static Color getColor(ColorIndex index) {
		return PALETTE[index.ordinal()];
	}

	private void drawScan(PaintEvent pe, GC gc, ScanResult2D scan) {
		int width = pe.width;
		int height = pe.height;
		double maxX = Math.max(scan.getMaxX(), Math.abs(scan.getMinX()));
		double maxY = scan.getMaxY();
		double scale = Math.min(width / maxX, (height - 10) / maxY);
		int centerX = width / 2;
		gc.setForeground(getColor(ColorIndex.black));
		gc.drawLine(centerX, height, centerX, 0);
		goalPoint = null;

		// FIXME(Marcus/17 aug. 2018): This must be fixed - we cannot assume
		// that we do not have skipped points
		// - angular delta must be recorded!
		float angularDelta = scan.getAngularResolution();
		FeatureSet features = FeatureExtraction.getFeatures(scan.getPoints(), angularDelta);

		if (renderRaycast) {
			float stepAngle = scan.getAngularResolution() / 2;
			goalPoint = Raycast.raycastFarthestPoint(scan.getPoints(), 0.32f, stepAngle, features);
			List<Point2f> raycastFull = Raycast.raycastFull(scan.getPoints(), 0.32f, stepAngle, features);
			paintPointsAsLineFromOrigo(raycastFull, gc, 1, ColorIndex.grey, centerX, height, scale);
		}

		gc.setForeground(getColor(ColorIndex.black));
		for (Point2f point : scan.getPoints()) {
			int x = toPaintCoordinateX(centerX, scale, point);
			int y = toPaintCoordinateY(height, scale, point);
			paintPoint(gc, point, 1, x, y);
		}

		if (goalPoint != null) {
			int x = toPaintCoordinateX(centerX, scale, goalPoint);
			int y = toPaintCoordinateY(height, scale, goalPoint);
			gc.setForeground(getColor(ColorIndex.black));
			gc.drawLine(x, y, toPaintCoordinateX(centerX, scale, ORIGO), toPaintCoordinateY(height, scale, ORIGO));
			paintCircle(gc, x, y, 2, ColorIndex.green);
		}

		if (renderFeatures) {
			gc.setBackground(getColor(ColorIndex.white));

			for (Line2f line : features.getLines()) {
				drawLine(gc, line, 1, ColorIndex.magenta, centerX, height, scale);
			}

			for (Point2f corner : features.getCorners()) {
				drawCross(gc, corner, ColorIndex.red, 4, centerX, height, scale);
			}
		}

		if (renderSegments) {
			List<List<Point2f>> segments = FeatureExtraction.segment(scan.getPoints(), angularDelta);
			for (List<Point2f> segment : segments) {
				connect(gc, segment, ColorIndex.lightgrey, centerX, height, scale);
				if (renderAngles) {
					drawAngles(gc, segment, ColorIndex.lightgrey, centerX, height, scale);
				}
				paintCircle(gc, segment.get(0), 3, ColorIndex.green, centerX, height, scale);
				paintCircle(gc, segment.get(segment.size() - 1), 3, ColorIndex.red, centerX, height, scale);
			}
		}
	}

	private static void paintPointsAsLineFromOrigo(List<Point2f> raycastFull, GC gc, int weight, ColorIndex c, int centerX, int height,
			double scale) {
		for (Point2f rayPoint : raycastFull) {
			Line2f ray = new Line2f(ORIGO, rayPoint);
			drawLine(gc, ray, weight, c, centerX, height, scale);
		}
	}

	private static void drawCross(GC gc, Point2f corner, ColorIndex index, int size, int centerX, int height, double scale) {
		Point2f center = toDeviceCoordinates(corner, centerX, height, scale);
		gc.setForeground(getColor(index));
		gc.setLineWidth(2);
		gc.drawLine((int) center.getX() - size, (int) center.getY() - size, (int) center.getX() + size, (int) center.getY() + size);
		gc.drawLine((int) center.getX() + size, (int) center.getY() - size, (int) center.getX() - size, (int) center.getY() + size);
		gc.setLineWidth(1);
	}

	private static Point2f toDeviceCoordinates(Point2f p, int centerX, int height, double scale) {
		return Point2f.fromCartesian(toPaintCoordinateX(centerX, scale, p), toPaintCoordinateY(height, scale, p));
	}

	private static void drawLine(GC gc, Line2f line, int weight, ColorIndex color, int centerX, int height, double scale) {
		int x1 = toPaintCoordinateX(centerX, scale, line.getP1());
		int y1 = toPaintCoordinateY(height, scale, line.getP1());
		int x2 = toPaintCoordinateX(centerX, scale, line.getP2());
		int y2 = toPaintCoordinateY(height, scale, line.getP2());
		gc.setForeground(getColor(color));
		int lineWidth = gc.getLineWidth();
		gc.setLineWidth(weight);
		gc.drawLine(x1, y1, x2, y2);
		gc.setLineWidth(lineWidth);
	}

	private void drawAngles(GC gc, List<Point2f> segment, ColorIndex lightgrey, int centerX, int height, double scale) {
		if (segment.size() >= 5) {
			float[] angles = FeatureExtraction.calculateSimpleVectorAngles(segment);
			int midPointIndex = angles.length / 2;

			Point2f midPoint = segment.get(midPointIndex);
			int x = toPaintCoordinateX(centerX, scale, midPoint);
			int y = toPaintCoordinateY(height, scale, midPoint);
			gc.setForeground(getColor(ColorIndex.cyan));
			gc.drawLine(x, y, x + 20, y);

			int kf = FeatureExtraction.calculateKF(segment, midPointIndex);
			int kb = FeatureExtraction.calculateKB(segment, midPointIndex);
			paintCircle(gc, segment.get(midPointIndex - kb), 5, ColorIndex.cyan, centerX, height, scale);
			paintCircle(gc, segment.get(midPointIndex + kf), 5, ColorIndex.cyan, centerX, height, scale);
			gc.setBackground(getColor(ColorIndex.white));
			// gc.drawText(String.format("%.1f, Kb:%d, Kf: %d",
			// Math.toDegrees(angles[midPointIndex]), kf, kb), x + 25, y - 8);
		}
	}

	private void paintCircle(GC gc, Point2f Point2f, int radius, ColorIndex color, int centerX, int height, double scale) {
		gc.setBackground(getColor(color));
		int diameter = radius * 2;
		int x = toPaintCoordinateX(centerX, scale, Point2f);
		int y = toPaintCoordinateY(height, scale, Point2f);
		gc.fillOval(x - radius, y - radius, diameter, diameter);
	}

	private void connect(GC gc, List<Point2f> segment, ColorIndex color, int centerX, double height, double scale) {
		Iterator<Point2f> pointIterator = segment.iterator();
		Point2f lastPoint = pointIterator.next();

		gc.setForeground(getColor(color));
		while (pointIterator.hasNext()) {
			Point2f p = pointIterator.next();
			int x1 = toPaintCoordinateX(centerX, scale, lastPoint);
			int y1 = toPaintCoordinateY(height, scale, lastPoint);
			int x2 = toPaintCoordinateX(centerX, scale, p);
			int y2 = toPaintCoordinateY(height, scale, p);
			gc.drawLine(x1, y1, x2, y2);
			lastPoint = p;
		}
	}

	private void paintPoint(GC gc, Point2f point, int size, int x, int y) {
		if (Math.abs(point.getX()) < .3) {
			gc.setForeground(getColor(ColorIndex.red));
		} else {
			gc.setForeground(getColor(ColorIndex.blue));
		}
		gc.drawOval(x - size, y - size, size * 2, size * 2);
	}

	private void paintCircle(GC gc, int x, int y, int radius, ColorIndex color) {
		gc.setBackground(getColor(color));
		int diameter = radius * 2;
		gc.fillOval(x - radius, y - radius, diameter, diameter);
	}

	public void setModel(List<ScanResult2D> scanResults) {
		this.results = scanResults;
	}

	private static int toPaintCoordinateY(double height, double scale, Point2f point) {
		return (int) Math.round(height - point.getY() * scale);
	}

	private static int toPaintCoordinateX(int centerX, double scale, Point2f point) {
		return (int) Math.round(point.getX() * scale + centerX);
	}

	public static float calculateTangentDistance(float rayAlpha, Point2f p) {
		float deltaAlpha = Math.abs(p.getAngle() - rayAlpha);
		if (deltaAlpha >= 90.0) {
			return Float.MAX_VALUE;
		}
		return (float) (p.getRange() * Math.atan(deltaAlpha));
	}

	public void setRaycastEnabled(boolean selection) {
		renderRaycast = selection;
	}

	public void setSegmentsEnabled(boolean selection) {
		renderSegments = selection;
	}

	public void setAnglesEnabled(boolean selection) {
		renderAngles = selection;
	}

	public void setFeaturesEnabled(boolean selection) {
		renderFeatures = selection;
	}

	public static float calculateNoGoRadius(Point2f p, float defaultNoGoRadius) {
		if (p instanceof CurvaturePoint2f) {
			CurvaturePoint2f cp = (CurvaturePoint2f) p;
			if (cp.getCurvature() < 0) {
				return defaultNoGoRadius;
			} else {
				return (float) (Math.sin(Math.PI - cp.getCurvature()) * defaultNoGoRadius * 1.5) + defaultNoGoRadius;
			}
		}
		return defaultNoGoRadius;
	}

	public Point2f getGoalPoint() {
		return goalPoint;
	}
}
