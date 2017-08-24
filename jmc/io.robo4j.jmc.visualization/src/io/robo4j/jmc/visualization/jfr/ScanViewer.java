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
package io.robo4j.jmc.visualization.jfr;

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

import io.robo4j.jmc.visualization.scan.CurvaturePoint;
import io.robo4j.jmc.visualization.scan.FeatureExtraction;
import io.robo4j.jmc.visualization.scan.FeatureSet;
import io.robo4j.jmc.visualization.scan.Line;
import io.robo4j.jmc.visualization.scan.PointXY;
import io.robo4j.jmc.visualization.scan.ScanResult;
import io.robo4j.jmc.visualization.scan.ScanResultImpl;

/**
 * Canvas for drawing a scan.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ScanViewer extends Canvas {
	private static final PointXY ORIGO = new PointXY(0, 0);
	private boolean renderRaycast = false;
	private boolean renderSegments = false;
	private boolean renderAngles = false;
	private boolean renderFeatures = false;

	private enum ColorIndex {
		white, black, grey, red, green, blue, cyan, lightgrey, orange, magenta
	}

	private final Color[] colors;
	private volatile ScanResult result = new ScanResultImpl(-1);

	public ScanViewer(Composite parent, int style) {
		super(parent, style);
		colors = new Color[] { new Color(null, 255, 255, 255), new Color(null, 0, 0, 0), new Color(null, 110, 110, 110),
				new Color(null, 255, 0, 0), new Color(null, 0, 255, 0), new Color(null, 0, 0, 255), new Color(null, 0, 255, 255),
				new Color(null, 190, 190, 190), new Color(null, 255, 165, 0), new Color(null, 255, 0, 255) };

		setBackground(colors[0]);
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
		for (Color c : colors) {
			c.dispose();
		}
	}

	protected void paint(PaintEvent pe) {
		ScanResult lresult = result;
		GC gc = pe.gc;
		int centerY = pe.height / 2;
		gc.setForeground(getColor(ColorIndex.red));
		if (lresult.getPoints().isEmpty()) {
			gc.drawText("Please select a scan to show", 0, centerY);
		} else {
			drawScan(pe, gc, lresult);
		}
	}

	private Color getColor(ColorIndex index) {
		return colors[index.ordinal()];
	}

	private void drawScan(PaintEvent pe, GC gc, ScanResult scan) {
		int width = pe.width;
		int height = pe.height;
		double maxX = Math.max(scan.getMaxX(), Math.abs(scan.getMinX()));
		double maxY = scan.getMaxY();
		double scale = Math.min(width / maxX, (height - 10) / maxY);
		int centerX = width / 2;
		gc.setForeground(getColor(ColorIndex.black));
		gc.drawLine(centerX, height, centerX, 0);
		PointXY goalPoint;

		if (renderRaycast) {
			goalPoint = raycastMostPromisingPoint(gc, centerX, height, scale, scan.getPoints(), 0.32f, (float) Math.toRadians(0.4f));
		} else {
			goalPoint = scan.getGoalPoint();
		}

		gc.setForeground(getColor(ColorIndex.black));
		for (PointXY point : scan.getPoints()) {
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

		PointXY p = scan.getTargetPoint();
		if (p != null) {
			int x = toPaintCoordinateX(centerX, scale, p);
			int y = toPaintCoordinateY(height, scale, p);
			paintCircle(gc, x, y, 2, ColorIndex.blue);
			gc.setBackground(getColor(ColorIndex.grey));
			gc.drawLine(x, y, toPaintCoordinateX(centerX, scale, ORIGO), toPaintCoordinateY(height, scale, ORIGO));
			gc.setBackground(getColor(ColorIndex.white));
		}
		p = scan.getNearestPoint();
		if (p != null) {
			paintCircle(gc, toPaintCoordinateX(centerX, scale, p), toPaintCoordinateY(height, scale, p), 5, ColorIndex.red);
		}

		p = scan.getFarthestPoint();
		if (p != null) {
			paintCircle(gc, toPaintCoordinateX(centerX, scale, p), toPaintCoordinateY(height, scale, p), 2, ColorIndex.orange);
		}

		if (renderFeatures) {
			float angularDelta = scan.getPoints().get(1).getAngle() - scan.getPoints().get(0).getAngle();
			FeatureSet features = FeatureExtraction.getFeatures(scan.getPoints(), angularDelta);
			gc.setBackground(getColor(ColorIndex.white));

			for (Line line : features.getLines()) {
				drawLine(gc, line, ColorIndex.magenta, centerX, height, scale);
			}

			for (PointXY corner : features.getCorners()) {
				drawCross(gc, corner, ColorIndex.red, 4, centerX, height, scale);
			}
		}

		if (renderSegments) {
			float angularDelta = scan.getPoints().get(1).getAngle() - scan.getPoints().get(0).getAngle();
			List<List<PointXY>> segments = FeatureExtraction.segment(scan.getPoints(), angularDelta);
			for (List<PointXY> segment : segments) {
				connect(gc, segment, ColorIndex.lightgrey, centerX, height, scale);
				if (renderAngles) {
					drawAngles(gc, segment, ColorIndex.lightgrey, centerX, height, scale);
				}
				paintCircle(gc, segment.get(0), 3, ColorIndex.green, centerX, height, scale);
				paintCircle(gc, segment.get(segment.size() - 1), 3, ColorIndex.red, centerX, height, scale);
			}
		}
	}

	private void drawCross(GC gc, PointXY corner, ColorIndex index, int size, int centerX, int height, double scale) {
		PointXY center = toDeviceCoordinates(corner, centerX, height, scale);
		gc.setForeground(getColor(index));
		gc.setLineWidth(2);
		gc.drawLine((int) center.getX() - size, (int) center.getY() - size, (int) center.getX() + size, (int) center.getY() + size);
		gc.drawLine((int) center.getX() + size, (int) center.getY() - size, (int) center.getX() - size, (int) center.getY() + size);
		gc.setLineWidth(1);
	}

	private PointXY toDeviceCoordinates(PointXY p, int centerX, int height, double scale) {
		return new PointXY(toPaintCoordinateX(centerX, scale, p), toPaintCoordinateY(height, scale, p));
	}

	private void drawLine(GC gc, Line line, ColorIndex color, int centerX, int height, double scale) {
		int x1 = toPaintCoordinateX(centerX, scale, line.getP1());
		int y1 = toPaintCoordinateY(height, scale, line.getP1());
		int x2 = toPaintCoordinateX(centerX, scale, line.getP2());
		int y2 = toPaintCoordinateY(height, scale, line.getP2());
		gc.setForeground(getColor(color));
		int lineWidth = gc.getLineWidth();
		gc.setLineWidth(2);
		gc.drawLine(x1, y1, x2, y2);
		gc.setLineWidth(lineWidth);
	}

	private void drawAngles(GC gc, List<PointXY> segment, ColorIndex lightgrey, int centerX, int height, double scale) {
		if (segment.size() >= 5) {
			float[] angles = FeatureExtraction.calculateSimpleVectorAngles(segment);
			int midPointIndex = angles.length / 2;

			PointXY midPoint = segment.get(midPointIndex);
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

	private void paintCircle(GC gc, PointXY pointXY, int radius, ColorIndex color, int centerX, int height, double scale) {
		gc.setBackground(getColor(color));
		int diameter = radius * 2;
		int x = toPaintCoordinateX(centerX, scale, pointXY);
		int y = toPaintCoordinateY(height, scale, pointXY);
		gc.fillOval(x - radius, y - radius, diameter, diameter);
	}

	private void connect(GC gc, List<PointXY> segment, ColorIndex color, int centerX, double height, double scale) {
		Iterator<PointXY> pointIterator = segment.iterator();
		PointXY lastPoint = pointIterator.next();

		gc.setForeground(getColor(color));
		while (pointIterator.hasNext()) {
			PointXY p = pointIterator.next();
			int x1 = toPaintCoordinateX(centerX, scale, lastPoint);
			int y1 = toPaintCoordinateY(height, scale, lastPoint);
			int x2 = toPaintCoordinateX(centerX, scale, p);
			int y2 = toPaintCoordinateY(height, scale, p);
			gc.drawLine(x1, y1, x2, y2);
			lastPoint = p;
		}
	}

	private void paintPoint(GC gc, PointXY point, int size, int x, int y) {
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

	public void setModel(ScanResult result) {
		this.result = result;
	}

	private static int toPaintCoordinateY(double height, double scale, PointXY point) {
		return (int) Math.round(height - point.getY() * scale);
	}

	private static int toPaintCoordinateX(int centerX, double scale, PointXY point) {
		return (int) Math.round(point.getX() * scale + centerX);
	}

	public PointXY raycastMostPromisingPoint(GC gc, int centerX, int height, double scale, List<PointXY> points, float noGoRadius,
			float raycastStepAngle) {
		float startAlpha = points.get(0).getAngle();
		float endAlpha = points.get(points.size() - 1).getAngle();

		float resultRange = Float.MIN_VALUE;
		float resultAlpha = 0;

		FeatureSet features = FeatureExtraction.getFeatures(points, FeatureExtraction.getAngularResolution(points));

		for (float alpha = startAlpha; alpha <= endAlpha; alpha += raycastStepAngle) {
			float range = raycast(points, features.getCorners(), alpha, noGoRadius);
			if (range == Float.MAX_VALUE) {
				continue;
			}
			if (range > resultRange) {
				resultRange = range;
				resultAlpha = alpha;
			}
			paintLine(gc, PointXY.fromPolar(alpha, range), centerX, height, scale);
		}
		return PointXY.fromPolar(resultAlpha, resultRange);
	}

	public static float raycast(List<PointXY> points, List<CurvaturePoint> corners, float rayAlpha, float defaultNoGoRadius) {
		float minIntersectionRange = Float.MAX_VALUE;
		for (PointXY p : points) {
			int cornerIndex = corners.indexOf(p);
			float noGoRadius = defaultNoGoRadius;
			if (cornerIndex >= 0) {
				p = corners.get(cornerIndex);
				noGoRadius = FeatureExtraction.calculateNoGoRadius(p, defaultNoGoRadius);
			}
			float tangentDistance = calculateTangentDistance(rayAlpha, p);
			// Fast rejection
			if (Math.abs(tangentDistance) >= noGoRadius) {
				continue;
			}
			float intersectionRange = calculateIntersectionRange(rayAlpha, noGoRadius, tangentDistance, p);
			if (intersectionRange != Float.NaN) {
				minIntersectionRange = Math.min(minIntersectionRange, intersectionRange);
			} else {
				System.out.println(p + " " + tangentDistance + " " + rayAlpha);
			}
		}
		return minIntersectionRange;
	}

	private static float calculateIntersectionRange(float rayAlpha, float noGoRadius, float tangentDistance, PointXY p) {
		float delta = (float) Math.sqrt(noGoRadius * noGoRadius - tangentDistance * tangentDistance);
		if (p.getRange() <= delta) {
			return 0;
		}
		return p.getRange() - delta;
	}

	public static float calculateTangentDistance(float rayAlpha, PointXY p) {
		float deltaAlpha = Math.abs(p.getAngle() - rayAlpha);
		if (deltaAlpha >= 90.0) {
			return Float.MAX_VALUE;
		}
		return (float) (p.getRange() * Math.atan(deltaAlpha));
	}

	private void paintLine(GC gc, PointXY p, int centerX, float height, double scale) {
		int x = toPaintCoordinateX(centerX, scale, p);
		int y = toPaintCoordinateY(height, scale, p);
		gc.setForeground(colors[ColorIndex.grey.ordinal()]);
		gc.drawLine(x, y, toPaintCoordinateX(centerX, scale, ORIGO), toPaintCoordinateY(height, scale, ORIGO));
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
}
