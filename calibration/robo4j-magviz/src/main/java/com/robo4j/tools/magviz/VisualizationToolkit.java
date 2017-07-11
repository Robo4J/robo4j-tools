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
package com.robo4j.tools.magviz;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;

/**
 * Various helpers.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class VisualizationToolkit {
	public static final double AXIS_LENGTH = 50;
	public static final double AXIS_THICKNESS = 3;
	public static final double MIN_SPHERE_SIZE = 0.4f;
	public static final double MAX_SPHERE_SIZE = 2.0f;
	public static final Point3D ORIGO = new Point3D(0, 0, 0);
	public static final Material BLACK_MATERIAL = new PhongMaterial(Color.BLACK);
	public static final Material RED_MATERIAL = new PhongMaterial(Color.RED);

	private VisualizationToolkit() {
		throw new UnsupportedOperationException("Toolkit!");
	}
	
	public static Sphere createSphere(float diameter, Point3D position, Material material) {
		Sphere s = new Sphere(diameter / 2);
		VisualizationToolkit.translate(s, position);
		s.setMaterial(material);
		return s;
	}

	public static void translate(Sphere s, Point3D position) {
		s.setTranslateX(position.getX());
		s.setTranslateY(position.getY());
		s.setTranslateZ(position.getZ());
	}

	public static Point3D readPoint(String csvLine) {
		String[] values = csvLine.split(MagVizController.SEPARATOR);
		return new Point3D(Double.parseDouble(values[0]), Double.parseDouble(values[1]), Double.parseDouble(values[2]));
	}

	public static void parsePoint(final List<Point3D> points, String s) {
		if (s.startsWith("#")) {
			return;
		}
		String trimmed = s.trim();
		if (trimmed.isEmpty()) {
			return;
		}
		points.add(readPoint(trimmed));
	}

	public static Group getAxes() {
		Box xAxis = new Box(VisualizationToolkit.AXIS_LENGTH, VisualizationToolkit.AXIS_THICKNESS, VisualizationToolkit.AXIS_THICKNESS);
		xAxis.setMaterial(new PhongMaterial(Color.BLUE));
		Text xLabel = new Text(VisualizationToolkit.AXIS_LENGTH / 2, 5, "X");
	
		Box yAxis = new Box(VisualizationToolkit.AXIS_THICKNESS, VisualizationToolkit.AXIS_THICKNESS, VisualizationToolkit.AXIS_LENGTH);
		yAxis.setMaterial(new PhongMaterial(Color.RED));
		Text yLabel = new Text(0, VisualizationToolkit.AXIS_LENGTH / 2, "Y");
		yLabel.setTranslateY(10);
		yLabel.setTranslateX(-4);
	
		Box zAxis = new Box(VisualizationToolkit.AXIS_THICKNESS, VisualizationToolkit.AXIS_LENGTH, VisualizationToolkit.AXIS_THICKNESS);
		zAxis.setMaterial(new PhongMaterial(Color.GREEN));
		Text zLabel = new Text(0, 0, "Z");
		zLabel.setTranslateZ(VisualizationToolkit.AXIS_LENGTH / 2 + 5);
		zLabel.setRotationAxis(Rotate.Y_AXIS);
		zLabel.setRotate(-90);
		zLabel.setTranslateX(-4);
		zLabel.setTranslateY(5);
	
		return new Group(xAxis, yAxis, zAxis, xLabel, yLabel, zLabel);
	}

	public static List<Node> scale(List<Node> spheres, float scale) {
		spheres.forEach((Node n)->VisualizationToolkit.scaleUniformly(n, scale));
		return spheres;
	}

	public static void scaleUniformly(Node n, float scale) {
		n.setScaleX(scale);
		n.setScaleY(scale);
		n.setScaleZ(scale);
	}

	public static List<Point3D> loadPointsFromFile(File csvFile) {
		final List<Point3D> points = new ArrayList<>();
		try (Stream<String> stream = Files.lines(csvFile.toPath())) {
			stream.forEach((s) -> parsePoint(points, s));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return points;
	}

	public static double getValue(TextField field) {
		return Double.valueOf(field.getText());
	}
}
