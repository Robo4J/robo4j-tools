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
package se.hirt.coffee.visualization.jfr.scan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * TODO(Marcus/Mar 9, 2017): Should at some point just depend on math.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class FeatureExtraction {
	/**
	 * The residual variance of the Lidar Lite (25 mm).
	 */
	private static final double RESIDUAL_VARIANCE = 0.025f;

	/**
	 * Auxiliary constant parameter for segmenting
	 */
	private static final double AUXILIARY_CONSTANT = Math.toRadians(10);

	/**
	 * Length variance constant
	 */
	private static final double Uk = 0.02;

	/**
	 * Minimal samples for a line
	 */
	private static final int MIN_LINE_SAMPLES = 4;
	
	/**
	 * Minimal angle deviation for a line
	 */
	private static final float LINE_ANGLE_THRESHOLD = 0.5f;

	/**
	 * Minimal angle deviation to be part of a possible corner
	 */
	private static final float CURVATURE_THRESHOLD = (float) (Math.PI / 6);

	/**
	 * Minimal angle deviation to be a corner
	 */
	private static final float CORNER_THRESHOLD = (float) (Math.PI / 3);

	/**
	 * Calculates the segments in the scan, using the Borg & Aldon adaptive
	 * break point algorithm.
	 * 
	 * @param points
	 *            the point to segment.
	 * @param angularResolution
	 *            of the scan in radians.
	 * 
	 * @return the points broken up into segments.
	 */
	public static List<List<PointXY>> segment(List<PointXY> points, float angularResolution) {
		List<List<PointXY>> segments = new ArrayList<List<PointXY>>(10);

		Iterator<PointXY> iterator = points.iterator();
		List<PointXY> currentSegment = new ArrayList<PointXY>();
		segments.add(currentSegment);

		PointXY lastPoint = iterator.next();
		currentSegment.add(lastPoint);

		while (iterator.hasNext()) {
			PointXY nextPoint = iterator.next();
			float delta = nextPoint.distance(lastPoint);
			double maxRange = segmentMaxRange(lastPoint.getRange(), angularResolution);
			if (delta > maxRange) {
				currentSegment = new ArrayList<PointXY>();
				segments.add(currentSegment);
			}
			currentSegment.add(nextPoint);
			lastPoint = nextPoint;
		}
		return segments;
	}

//	public static float calculateVectorAngle(PointXY b, PointXY center, PointXY f) {
//		if (b.equals(center) || f.equals(center)) {
//			return 0;
//		}
//
//		float bDeltaX = center.getX() - b.getX();
//		float bDeltaY = center.getY() - b.getY();
//
//		float fDeltaX = f.getX() - center.getX();
//		float fDeltaY = f.getY() - center.getY();
//
//		double numerator = bDeltaX * fDeltaX + bDeltaY * fDeltaY;
//		double denominator = Math.sqrt(bDeltaX * bDeltaX + bDeltaY * bDeltaY) * Math.sqrt(fDeltaX * fDeltaX + fDeltaY * fDeltaY);
//		return (float) Math.acos(numerator / denominator);
//	}
	
	
	public static float calculateVectorAngle(PointXY b, PointXY center, PointXY f) {
		if (b.equals(center) || f.equals(center)) {
			return 0;
		}

		float bDeltaX = center.getX() - b.getX();
		float bDeltaY = center.getY() - b.getY();

		float fDeltaX = f.getX() - center.getX();
		float fDeltaY = f.getY() - center.getY();

		return (float) (Math.atan2(fDeltaX, fDeltaY) - Math.atan2(bDeltaX, bDeltaY));		
	}

	private final static double segmentMaxRange(float lastRange, float angularResolution) {
		return lastRange * Math.sin(angularResolution) / Math.sin(AUXILIARY_CONSTANT - angularResolution) + 3 * RESIDUAL_VARIANCE;
	}

	public static float[] calculateSimpleVectorAngles(List<PointXY> points) {
		if (points.size() < 5) {
			return null;
		}

		float[] alphas = new float[points.size()];
		for (int i = 0; i < points.size(); i++) {
			PointXY before = i == 0 ? points.get(0) : points.get(i - 1);
			PointXY center = points.get(i);
			PointXY following = i == points.size() - 1 ? points.get(i) : points.get(i + 1);
			alphas[i] = calculateVectorAngle(before, center, following);
		}
		return alphas;
	}
	
	public static FeatureSet getFeatures(List<PointXY> sample, float angularResolution) {
		List<List<PointXY>> segments = segment(sample, angularResolution);
		List<CurvaturePoint> corners = new ArrayList<>(); 
		List<Line> lines = new ArrayList<>();
		for (List<PointXY> points: segments) {
			if (points.size() < MIN_LINE_SAMPLES) {
				continue;
			}
			float [] deltaAngles = calculateSamplePointDeltaAngles(points);
			if (deltaAngles == null) {
				continue;
			}
			lines.addAll(extractLines(points, deltaAngles));
			corners.addAll(extractCorners(points, deltaAngles));
		}	
		return new FeatureSet(lines, corners);
	}

	@SuppressWarnings("unused")
	private static Collection<? extends CurvaturePoint> extractCornersOld(List<PointXY> points, float[] deltaAngles) {
		List<CurvaturePoint> corners = new ArrayList<>();
		for (int i = 0; i < deltaAngles.length; i++) {
			if (Math.abs(deltaAngles[i]) > CURVATURE_THRESHOLD) {
				int maxIndex = i;
				float maxPhi = deltaAngles[i];
				int j = i + 1;
				float totalPhi = maxPhi;
				while (j < deltaAngles.length - 1) {
					if (Math.abs(deltaAngles[j]) > CURVATURE_THRESHOLD && Math.signum(deltaAngles[i]) == Math.signum(deltaAngles[j])) {
						totalPhi += deltaAngles[j];
						if (deltaAngles[j] > maxPhi) {
							maxPhi = deltaAngles[j];
							maxIndex = j;
						}						
						j++;
					} else {
						i = j;
						break;
					}
				}
				
				if (Math.abs(totalPhi) > CORNER_THRESHOLD) {
					corners.add(new CurvaturePoint(points.get(maxIndex), totalPhi));
				}
			}
		}
		return corners;
	}

	private static Collection<? extends CurvaturePoint> extractCorners(List<PointXY> points, float[] deltaAngles) {
		List<CurvaturePoint> corners = new ArrayList<>();
		for (int i = 0; i < deltaAngles.length; i++) {
			if (Math.abs(deltaAngles[i]) > CURVATURE_THRESHOLD) {
				int maxIndex = i;
				float maxPhi = deltaAngles[i];
				float totalPhi = maxPhi;
				int last = Math.min(i + 4, deltaAngles.length);
				for (int k = i + 1; k < last; k++) {
					totalPhi += deltaAngles[k];
					if (deltaAngles[k] > maxPhi) {
						maxPhi = deltaAngles[k];
						maxIndex = k;
					}
					i = k;
				}

				if (Math.abs(totalPhi) > CORNER_THRESHOLD && Math.signum(totalPhi) == Math.signum(maxPhi) && maxIndex - 3 >= 0 && maxIndex + 4 < deltaAngles.length) {
					PointXY p = points.get(maxIndex);
					PointXY b = points.get(maxIndex - 3);
					PointXY f = points.get(maxIndex + 3);
					float cornerAlpha = calculateVectorAngle(b, p, f); 
					if (cornerAlpha > CORNER_THRESHOLD) {
						corners.add(new CurvaturePoint(p, cornerAlpha));	
					}
				}
			}
		}
		return corners;
	}

	public static float[] calculateSamplePointDeltaAngles(List<PointXY> points) {
		if (points.size() < 5) {
			return null;
		}

		float[] alphas = new float[points.size()];
		for (int i = 0; i < points.size(); i++) {
			if (i == 0 || i == points.size() -1) {
				alphas[i] = 0;
				continue;
			}
			int kb = calculateKB(points, i);
			int kf = calculateKF(points, i);
			PointXY before = points.get(i - kb);
			PointXY center = points.get(i);
			PointXY following = points.get(i + kf);
			alphas[i] = calculateVectorAngle(before, center, following);
		}
		return alphas;
	}
	
	public static int calculateKF(List<PointXY> points, int pointIndex) {
		if (pointIndex >= points.size() - 1) {
			return 0;
		}
		double length = 0;
		double distance = 0;
		PointXY startPoint = points.get(pointIndex);
		int i = pointIndex;
		while (i < points.size() -1) {			
			length += points.get(i + 1).distance(points.get(i));
			distance = points.get(i + 1).distance(startPoint);
			if ( (length - Uk) >= distance) {
				break;
			}
			i++;
		}
		return i - pointIndex;
	}

	public static int calculateKB(List<PointXY> points, int pointIndex) {
		if (pointIndex < 1) {
			return 0;
		}
		float length = 0;
		float distance = 0;
		PointXY startPoint = points.get(pointIndex);
		int i = pointIndex;
		while (i > 0) {
			length += points.get(i - 1).distance(points.get(i));
			distance = points.get(i - 1).distance(startPoint);
			if ((length - Uk) >= distance) {
				break;
			}
			i--;
		}
		return pointIndex - i;
	}
	
	public final static void main(String[] args) {
		PointXY b = new PointXY(18, 18);
		PointXY center = new PointXY(19, 19);
		PointXY f = new PointXY(20, 20);
		float radians = calculateVectorAngle(b, center, f);

		System.out.println("Vec angle: " + Math.toDegrees(radians) + " radians: " + radians);
	}
	
	private static List<Line> extractLines(List<PointXY> points, float[] deltaAngles) {	
		List<Line> lines = new ArrayList<>();
		for (int i = 0; i < deltaAngles.length - MIN_LINE_SAMPLES; ) {
			while (i < deltaAngles.length - 1 && Math.abs(deltaAngles[i]) > LINE_ANGLE_THRESHOLD) {
				i++;
			}	
			int j = i;
			while (j < deltaAngles.length - 2 && (Math.abs(deltaAngles[j]) <= LINE_ANGLE_THRESHOLD)) {
				j++;
			}
			if (j - i - 1 >= MIN_LINE_SAMPLES) {
				lines.add(new Line(points.get(i), points.get(j)));			
			}
			i = j;
		}
		return lines;
	}

	public static float getAngularResolution(List<PointXY> points) {
		return points.get(1).getAngle() - points.get(0).getAngle();
	}

	public static float calculateNoGoRadius(PointXY p, float defaultNoGoRadius) {
		if (p instanceof CurvaturePoint) {
			CurvaturePoint cp = (CurvaturePoint) p;
			if (cp.getCurvature() < 0) {
				return defaultNoGoRadius;
			} else {
				return (float) (Math.sin(Math.PI - cp.getCurvature()) * defaultNoGoRadius * 1.5) + defaultNoGoRadius;
			}
		}
		return defaultNoGoRadius;
	}

}
