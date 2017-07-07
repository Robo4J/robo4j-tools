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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import com.robo4j.tools.magviz.ellipsoid.EllipsoidToSphereSolver;

import javafx.animation.Animation;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
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
 * Visualizer for magnetometer data.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class MagVizController {
	private static final String SEPARATOR = ";";
	private static final Point3D ZERO = new Point3D(0, 0, 0);
	private static final double AXIS_LENGTH = 50;
	private static final double AXIS_THICKNESS = 3;

	private final static Material BLACK = new PhongMaterial(Color.BLACK);
	private final static Material BLUE = new PhongMaterial(Color.BLUE);

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
	@FXML
	private RadioButton radioRawData;
	@FXML
	private RadioButton radioCorrectedData;

	// FIXME(Marcus/Jul 6, 2017): This thing should be broken up into smaller
	// pieces at some point.
	@FXML
	private TextField textBiasX;
	@FXML
	private TextField textBiasY;
	@FXML
	private TextField textBiasZ;

	@FXML
	private TextField m11;
	@FXML
	private TextField m12;
	@FXML
	private TextField m13;
	@FXML
	private TextField m21;
	@FXML
	private TextField m22;
	@FXML
	private TextField m23;
	@FXML
	private TextField m31;
	@FXML
	private TextField m32;
	@FXML
	private TextField m33;

	private List<Point3D> points;

	public void loadFile(File csvFile) {
		points = loadPointsFromFile(csvFile);
		initializeSubScenes(points);
	}

	public void initializeSubCorrectedScenes(List<Point3D> rawPointList) {

		AmbientLight ambient = new AmbientLight(Color.WHITE);
		Group pointsGroup = null;

		if (points != null && points.size() > 0) {
			pointsGroup = createSphere(rawPointList);
		} else {
			pointsGroup = new Group();
		}
		Group axesAndPoints = new Group(getAxes(), pointsGroup);
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

	public void initializeSubScenes(List<Point3D> rawPointList) {
		AmbientLight ambient = new AmbientLight(Color.WHITE);
		Group pointsGroup = null;

		if (points != null && points.size() > 0) {
			pointsGroup = createEllipsoid(rawPointList, 1.5f);
		} else {
			pointsGroup = new Group();
		}
		Group axesAndPoints = new Group(getAxes(), pointsGroup);
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

	@FXML
	private void handleRawData(ActionEvent event) {
		initializeSubScenes(points);
	}

	@FXML
	private void handleCorrectedData(ActionEvent event) {
		initializeSubCorrectedScenes(points);
	}

	private List<Point3D> transform(List<Point3D> pointsToTransform) {
		List<Point3D> transformedPoints = new ArrayList<>();
		Point3D bias = getBias();

		// Add matrix etc here.
		for (Point3D p : pointsToTransform) {
			transformedPoints.add(p.subtract(bias));
		}
		return transformedPoints;
	}

	private Point3D getBias() {
		return new Point3D(Double.parseDouble(textBiasX.getText()), Double.parseDouble(textBiasY.getText()),
				Double.parseDouble(textBiasZ.getText()));
	}

	public Group createEllipsoid(List<Point3D> points, float size) {
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

	public Group createSphere(List<Point3D> points) {


		EllipsoidToSphereSolver solver = new EllipsoidToSphereSolver(points);

		Point3D offest = solver.getSphereMatrix();

		textNoOfPoints.setText(String.valueOf(points.size()));

		double normalizingFactorX = 100.0f / solver.getRadii().getX();
		double normalizingFactorY = 100.0f / solver.getRadii().getY();
		double normalizingFactorZ = 100.0f / solver.getRadii().getZ();

		RealMatrix rotationMatrix = new Array2DRowRealMatrix(3, 3);
		rotationMatrix.setRow(0, new double[]{solver.eigenVector0.getX(), solver.eigenVector0.getY(), solver.eigenVector0.getZ()});
		rotationMatrix.setRow(1, new double[]{solver.eigenVector1.getX(), solver.eigenVector1.getY(), solver.eigenVector1.getZ()});
		rotationMatrix.setRow(2, new double[]{solver.eigenVector2.getX(), solver.eigenVector2.getY(), solver.eigenVector2.getZ()});


		m11.setText(String.valueOf(solver.eigenVector0.getX()));
		m12.setText(String.valueOf(solver.eigenVector0.getY()));
		m13.setText(String.valueOf(solver.eigenVector0.getZ()));
		m21.setText(String.valueOf(solver.eigenVector1.getX()));
		m22.setText(String.valueOf(solver.eigenVector1.getY()));
		m23.setText(String.valueOf(solver.eigenVector1.getZ()));
		m31.setText(String.valueOf(solver.eigenVector2.getX()));
		m32.setText(String.valueOf(solver.eigenVector2.getY()));
		m33.setText(String.valueOf(solver.eigenVector2.getZ()));

		final Collection<Node> spheres = points.stream().map(p -> {
			double valX = p.getX() - offest.getX();
			double valY = p.getY() - offest.getY();
			double valZ = p.getZ() - offest.getZ();

			RealMatrix vector = new Array2DRowRealMatrix(1, 3);
			vector.setRow(0, new double[]{valX, valY, valZ});

			RealMatrix resultMatrix = rotationMatrix.multiply(vector.getRowMatrix(0).transpose());

			double[] test1 = resultMatrix.getRow(0);
			double[] test2 = resultMatrix.getRow(1);
			double[] test3 = resultMatrix.getRow(2);

			return new Point3D(test1[0], test2[0], test3[0]);
		}).map(p1 -> {
			Sphere s = new Sphere(1.5f / 2);
			Point3D tmpP = new Point3D(p1.getX() * normalizingFactorX , p1.getY() * normalizingFactorY,
					p1.getZ() * normalizingFactorZ);


			s.setTranslateX(tmpP.getX());
			s.setTranslateY(tmpP.getY());
			s.setTranslateZ(tmpP.getZ());
			s.setMaterial(BLUE);
			return s;
		}).collect(Collectors.toList());

		double maxRadius = 0;
		for (Point3D p : points) {
			maxRadius = Math.max(maxRadius, ZERO.distance(p));
		}
		textMaxRadius.setText(String.format("%.2f", maxRadius));

		double normalizingFactor = 100.0f / maxRadius;
		for (Point3D p : points) {
			spheres.add(createSphere(1.5f, p.multiply(normalizingFactor)));
		}

		textBiasX.setText(String.valueOf(offest.getX()));
		textBiasY.setText(String.valueOf(offest.getY()));
		textBiasZ.setText(String.valueOf(offest.getZ()));



		return new Group(spheres);
	}

	public List<Point3D> loadPointsFromFile(File csvFile) {
		final List<Point3D> points = new ArrayList<>();
		try (Stream<String> stream = Files.lines(csvFile.toPath())) {
			stream.forEach((s) -> parsePoint(points, s));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return points;
	}

	private void parsePoint(final List<Point3D> points, String s) {
		if (s.startsWith("#")) {
			return;
		}
		String trimmed = s.trim();
		if (trimmed.isEmpty()) {
			return;
		}
		points.add(readPoint(trimmed));
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
