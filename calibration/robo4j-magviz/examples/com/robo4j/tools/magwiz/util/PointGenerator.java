package com.robo4j.tools.magwiz.util;

import java.util.Random;

import javafx.geometry.Point3D;

/**
 * Simple generator to test the visualizer. 
 */
public class PointGenerator {
	private final static Random RND = new Random();
	
	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println("Usage: PointGenerator <number of points> <radius> <stddev>\nExample: PointGenerator 1000 200 4");
			System.exit(2);
		}
		
		int noOfPoints = Integer.parseInt(args[0]);
		double r = Double.parseDouble(args[1]);
		double stddev = Double.parseDouble(args[2]);
		
		for (int i = 0; i < noOfPoints; i++) {
			Point3D point = generatePoint(r, stddev);
			System.out.println(String.format("%f;%f;%f", point.getX(), point.getY(), point.getZ()));
		}
	}

	private static Point3D generatePoint(double r, double stddev) {
		double radius = RND.nextGaussian() * stddev + r;
		double s = RND.nextDouble() * Math.PI * 2;
		double t = RND.nextDouble() * Math.PI;
		double x = radius * Math.cos(s) * Math.sin(t);
		double y = radius * Math.sin(s) * Math.sin(t);
		double z = radius * Math.cos(t);
		return new Point3D(x, y, z);
	}

}
