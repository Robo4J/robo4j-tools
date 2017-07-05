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
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.robo4j.tools.magviz.ellipsoid.SampleDataGenerator;
import javafx.animation.Animation;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.fxml.FXML;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 **
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class MagVizController {
	private static final String SEPARATOR = ";";
	private static final Point3D ZERO = new Point3D(0, 0, 0);
	private static final double AXIS_LENGTH = 50;
	private static final double AXIS_THICKNESS = 3;

	private final static Material BLACK = new PhongMaterial(Color.BLACK);

	@FXML
	private BorderPane animatedBorderPane;
	@FXML
	private SubScene animatedSubScene;
	@FXML
	private HBox animatedSubSceneHBox;
	@FXML
	private Label testLabel;
	@FXML
	private TextField textNoOfPoints;
	@FXML
	private TextField textMaxRadius;

	private Group getGeneratedPoint(){
		SampleDataGenerator generator = new SampleDataGenerator();

		Group result = new Group();


		final Collection<Point3D> points = generator.generatePoints(1000).stream()
				.map(e -> new Point3D(e.x, e.y, e.z))
				.collect(Collectors.toList());

		textNoOfPoints.setText(String.valueOf(points.size()));
		double maxRadius = 0;
		for (Point3D p : points) {
			maxRadius = Math.max(maxRadius, ZERO.distance(p));
		}
		textMaxRadius.setText(String.format("%.2f", maxRadius));

		final Collection<Node> spheres = new LinkedList<>();
		double normalizingFactor = 100.0f / maxRadius;
		for (Point3D p : points) {
			spheres.add(createSphere(1.5f, p.multiply(normalizingFactor)));
		}

		return result;
	}

	public void initializeSubScenes(File csvFile) {

		AmbientLight ambient = new AmbientLight(Color.WHITE);
		Group points = null;

		if (csvFile != null) {
			points = createPoints(csvFile);
		} else {
			points = getGeneratedPoint();
		}

		Group axesAndPoints = new Group(getAxes(), points);
		Group pivotGroup = new Group(axesAndPoints);
		Group root = new Group(ambient, pivotGroup);

		RotateTransition rotation = new RotateTransition(Duration.seconds(4), pivotGroup);
		rotation.setFromAngle(0);
		rotation.setToAngle(360);
		rotation.setAxis(Rotate.X_AXIS);

		RotateTransition rotationBack = new RotateTransition(Duration.seconds(4), pivotGroup);
		rotationBack.setFromAngle(360);
		rotationBack.setToAngle(0);
		rotationBack.setAxis(Rotate.X_AXIS);

		RotateTransition axisRot = new RotateTransition(Duration.seconds(4), axesAndPoints);
		axisRot.setFromAngle(0);
		axisRot.setToAngle(90);
		axisRot.setAxis(Rotate.Y_AXIS);

		RotateTransition axisRotBack = new RotateTransition(Duration.seconds(4), axesAndPoints);
		axisRotBack.setFromAngle(90);
		axisRotBack.setToAngle(0);
		axisRotBack.setAxis(Rotate.Y_AXIS);

		ParallelTransition parallelTransition = new ParallelTransition(rotation, axisRot);
		ParallelTransition parallelTransitionBack = new ParallelTransition(rotationBack, axisRotBack);

		SequentialTransition transition = new SequentialTransition(parallelTransition,
				new PauseTransition(Duration.seconds(1)), parallelTransitionBack,
				new PauseTransition(Duration.seconds(1)));
		transition.setCycleCount(Animation.INDEFINITE);
		transition.setDelay(Duration.seconds(2));
		transition.play();

		PerspectiveCamera camera = new PerspectiveCamera(true);
		camera.setFieldOfView(30);
		camera.setFarClip(50000);
		camera.setTranslateZ(-500);
		animatedSubScene.heightProperty().bind(animatedSubSceneHBox.heightProperty());
		animatedSubScene.widthProperty().bind(animatedSubSceneHBox.widthProperty());
		animatedSubScene.setCamera(camera);
		animatedSubScene.setRoot(root);
	}

	private Group createPoints(File csvFile) {
		final Collection<Point3D> points = new ArrayList<>();
		try (Stream<String> stream = Files.lines(csvFile.toPath())) {
			stream.forEach((s) -> points.add(readPoint(s)));
		} catch (IOException e) {
			e.printStackTrace();
		}

		textNoOfPoints.setText(String.valueOf(points.size()));
		double maxRadius = 0;
		for (Point3D p : points) {
			maxRadius = Math.max(maxRadius, ZERO.distance(p));
		}
		textMaxRadius.setText(String.format("%.2f", maxRadius));

		final Collection<Node> spheres = new LinkedList<>();
		double normalizingFactor = 100.0f / maxRadius;
		for (Point3D p : points) {
			spheres.add(createSphere(1.5f, p.multiply(normalizingFactor)));
		}
		return new Group(spheres);
	}

	private Point3D readPoint(String csvLine) {
		String[] values = csvLine.split(SEPARATOR);
		return new Point3D(Double.parseDouble(values[0]), Double.parseDouble(values[1]), Double.parseDouble(values[2]));
	}

	private Sphere createSphere(float diameter, Point3D position) {
		Sphere s = new Sphere(diameter / 2);
		translate(s, position);
		s.setMaterial(BLACK);
		return s;
	}

	private static void translate(Sphere s, Point3D position) {
		s.setTranslateX(position.getX());
		s.setTranslateY(position.getY());
		s.setTranslateZ(position.getZ());
	}

	private Group getAxes() {
		Box xAxis = new Box(AXIS_LENGTH, AXIS_THICKNESS, AXIS_THICKNESS);
		xAxis.setMaterial(new PhongMaterial(Color.BLUE));
		Text xLabel = new Text(AXIS_LENGTH / 2, 5, "X");

		Box yAxis = new Box(AXIS_THICKNESS, AXIS_THICKNESS, AXIS_LENGTH);
		yAxis.setMaterial(new PhongMaterial(Color.RED));
		Text yLabel = new Text(0, AXIS_LENGTH / 2, "Y");
		yLabel.setTranslateY(10);
		yLabel.setTranslateX(-4);

		Box zAxis = new Box(AXIS_THICKNESS, AXIS_LENGTH, AXIS_THICKNESS);
		zAxis.setMaterial(new PhongMaterial(Color.GREEN));
		Text zLabel = new Text(0, 0, "Z");
		zLabel.setTranslateZ(AXIS_LENGTH / 2 + 5);
		zLabel.setRotationAxis(Rotate.Y_AXIS);
		zLabel.setRotate(-90);
		zLabel.setTranslateX(-4);
		zLabel.setTranslateY(5);

		return new Group(xAxis, yAxis, zAxis, xLabel, yLabel, zLabel);
	}

}
