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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import com.robo4j.tools.magviz.ellipsoid.EllipsoidToSphereSolver;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
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
	private final static Material RED = new PhongMaterial(Color.RED);

	private final static double MIN_SPHERE_SIZE = 0.4f;
	private final static double MAX_SPHERE_SIZE = 2.0f;

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
	private TextField textMeanRadius;

	@FXML
	private TextField textFilterStddev;

	@FXML
	private CheckBox checkRawData;
	@FXML
	private CheckBox checkCorrectedData;
	@FXML
	private Slider sliderSphereSize;

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
	private List<Node> lastCorrectedSpheres;
	private List<Node> rawSpheres;
	private File lastLoaded;

	public void initialize() {
		sliderSphereSize.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
				updateSphereSizes(old_val, new_val);
			}
		});
	}

	public void loadFile(File csvFile) {
		lastLoaded = csvFile;
		points = loadPointsFromFile(csvFile);
		setupInitialScene(points);
	}

	private void setupInitialScene(List<Point3D> points) {
		solveSphereMapping(points);
		initializeStats(points);
		initializeSubScene(points);
		fade(true, null);
	}

	private void initializeStats(List<Point3D> points) {
		textNoOfPoints.setText(String.valueOf(points.size()));
		Point3D center = getBiasFromFields();
		
		double maxRadius = Double.MIN_VALUE;
		Mean mean = new Mean();
		
		for (Point3D p : points) {
			maxRadius = Math.max(maxRadius, p.distance(center));
			mean.increment(p.distance(center));
		}
		textMaxRadius.setText(String.valueOf(maxRadius));
		textMeanRadius.setText(String.valueOf(mean.getResult()));
	}

	public synchronized void initializeSubScene(List<Point3D> rawPointList) {
		AmbientLight ambient = new AmbientLight(Color.WHITE);
		Group pointsGroup = null;
		Group correctedPointsGroup = null;

		if (points != null && points.size() > 0) {
			rawSpheres = createSpheres(rawPointList, 1.5f, RED);
			pointsGroup = new Group(rawSpheres);
			lastCorrectedSpheres = createCorrectedSpheres(rawPointList, 1.5f, BLACK);
			correctedPointsGroup = new Group(lastCorrectedSpheres);
		} else {
			pointsGroup = new Group();
			correctedPointsGroup = new Group();
		}
		Group axesAndPoints = new Group(getAxes(), pointsGroup, correctedPointsGroup);
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

		SequentialTransition transition = new SequentialTransition(parallelTransition, new PauseTransition(Duration.seconds(1)),
				parallelTransitionBack, new PauseTransition(Duration.seconds(1)));
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
		fade(checkRawData.isSelected(), null);
	}

	@FXML
	private void handleCorrectedData(ActionEvent event) {
		fade(null, checkCorrectedData.isSelected());
	}

	@FXML
	private void updateVisualization(ActionEvent event) {
		initializeSubScene(points);
		fade(checkRawData.isSelected(), checkCorrectedData.isSelected());
	}

	@FXML
	private void filterPoints(ActionEvent event) {
		List<Point3D> pointsFromFile = loadPointsFromFile(lastLoaded);
		solveSphereMapping(pointsFromFile);
		points = filter(pointsFromFile);
		setupInitialScene(points);
	}

	private List<Point3D> filter(List<Point3D> originalPoints) {
		initializeStats(originalPoints);
		Point3D biasCorrectedCenter = getBiasFromFields();
		
		double maxRadius = Double.MIN_VALUE;
		Mean mean = new Mean();
		StandardDeviation stddev = new StandardDeviation();
		for (Point3D p : points) {
			double r = p.distance(biasCorrectedCenter);
			maxRadius = Math.max(maxRadius, r);
			mean.increment(r);
			stddev.increment(r);
		}
		double meanResult = mean.getResult();
		double stddevResult = stddev.getResult();
		double allowedDeviation = stddevResult * getValue(textFilterStddev);
		
		List<Point3D> filteredPoints = new ArrayList<>();
		for (Point3D p : originalPoints) {
			if (Math.abs(p.distance(biasCorrectedCenter) - meanResult) <= allowedDeviation) {
				filteredPoints.add(p);
			}
		}		
		return filteredPoints;
	}

	private void fade(Boolean showRaw, Boolean showCorrected) {
		ArrayList<Animation> animations = new ArrayList<>();
		synchronized (this) {
			if (showRaw != null) {
				animations.addAll(Arrays.asList(createFadeAnimation(3000, rawSpheres, showRaw)));
			}
			if (showCorrected != null) {
				animations.addAll(Arrays.asList(createFadeAnimation(3000, lastCorrectedSpheres, showCorrected)));
			}
		}
		new ParallelTransition(animations.toArray(new Animation[0])).play();
	}

	// Opacity fade does not seem to work well on phong shaded stuff, so
	// shrinking them instead. ;)
	private Animation[] createFadeAnimation(int millis, List<Node> nodes, boolean fadeIn) {
		Animation[] animations = new Animation[nodes.size()];
		for (int i = 0; i < animations.length; i++) {
			ScaleTransition fadeTransition = new ScaleTransition(Duration.millis(2000), nodes.get(i));
			fadeTransition.setFromX(fadeIn ? 1 / 100 : 1);
			fadeTransition.setFromY(fadeIn ? 1 / 100 : 1);
			fadeTransition.setFromZ(fadeIn ? 1 / 100 : 1);
			fadeTransition.setToX(fadeIn ? 1 : 1 / 100);
			fadeTransition.setToY(fadeIn ? 1 : 1 / 100);
			fadeTransition.setToZ(fadeIn ? 1 : 1 / 100);
			animations[i] = fadeTransition;
		}
		return animations;
	}

	private Point3D getBiasFromFields() {
		return new Point3D(Double.parseDouble(textBiasX.getText()), Double.parseDouble(textBiasY.getText()),
				Double.parseDouble(textBiasZ.getText()));
	}

	private RealMatrix getMatrixFromFields() {
		RealMatrix matrix = new Array2DRowRealMatrix(3, 3);
		matrix.setRow(0, new double[] { getValue(m11), getValue(m12), getValue(m13) });
		matrix.setRow(1, new double[] { getValue(m21), getValue(m22), getValue(m23) });
		matrix.setRow(2, new double[] { getValue(m31), getValue(m32), getValue(m33) });
		return matrix;
	}

	public List<Node> createSpheres(List<Point3D> points, float size, Material material) {
		double maxRadius = 0;
		for (Point3D p : points) {
			maxRadius = Math.max(maxRadius, ZERO.distance(p));
		}

		final List<Node> spheres = new ArrayList<>();
		double normalizingFactor = 100.0f / maxRadius;
		for (Point3D p : points) {
			spheres.add(createSphere(1.5f, p.multiply(normalizingFactor), material));
		}
		return spheres;
	}

	public void solveSphereMapping(List<Point3D> points) {
		EllipsoidToSphereSolver solver = new EllipsoidToSphereSolver(points);
		solver.solve();

		RealMatrix matrix = solver.getMatrix();
		m11.setText(String.valueOf(matrix.getEntry(0, 0)));
		m12.setText(String.valueOf(matrix.getEntry(0, 1)));
		m13.setText(String.valueOf(matrix.getEntry(0, 2)));
		m21.setText(String.valueOf(matrix.getEntry(1, 0)));
		m22.setText(String.valueOf(matrix.getEntry(1, 1)));
		m23.setText(String.valueOf(matrix.getEntry(1, 2)));
		m31.setText(String.valueOf(matrix.getEntry(2, 0)));
		m32.setText(String.valueOf(matrix.getEntry(2, 1)));
		m33.setText(String.valueOf(matrix.getEntry(2, 2)));

		Point3D bias = solver.getCenter();
		textBiasX.setText(String.valueOf(bias.getX()));
		textBiasY.setText(String.valueOf(bias.getY()));
		textBiasZ.setText(String.valueOf(bias.getZ()));
	}

	public List<Node> createCorrectedSpheres(List<Point3D> points, float size, Material material) {
		Point3D bias = getBiasFromFields();
		RealMatrix matrix = getMatrixFromFields();

		// Get the eigenvalues and eigenvectors.
		EigenDecomposition solvedEigenVecors = new EigenDecomposition(matrix);
		double[] eigenValues = solvedEigenVecors.getRealEigenvalues();
		RealVector ev0 = solvedEigenVecors.getEigenvector(0);
		RealVector ev1 = solvedEigenVecors.getEigenvector(1);
		RealVector ev2 = solvedEigenVecors.getEigenvector(2);
		Point3D eigenVector0 = new Point3D(ev0.getEntry(0), ev0.getEntry(1), ev0.getEntry(2));
		Point3D eigenVector1 = new Point3D(ev1.getEntry(0), ev1.getEntry(1), ev1.getEntry(2));
		Point3D eigenVector2 = new Point3D(ev2.getEntry(0), ev2.getEntry(1), ev2.getEntry(2));
		RealMatrix rotationMatrix = new Array2DRowRealMatrix(3, 3);
		rotationMatrix.setRow(0, new double[] { eigenVector0.getX(), eigenVector0.getY(), eigenVector0.getZ() });
		rotationMatrix.setRow(1, new double[] { eigenVector1.getX(), eigenVector1.getY(), eigenVector1.getZ() });
		rotationMatrix.setRow(2, new double[] { eigenVector2.getX(), eigenVector2.getY(), eigenVector2.getZ() });

		// Find the radii of the ellipsoid.
		double aII = 1;
		double[] radiiArray = EllipsoidToSphereSolver.findRadii(aII, eigenValues);
		Point3D radii = new Point3D(radiiArray[0], radiiArray[1], radiiArray[2]);

		final List<Node> spheres = points.stream().map(p -> {
			double valX = p.getX() - bias.getX();
			double valY = p.getY() - bias.getY();
			double valZ = p.getZ() - bias.getZ();

			RealMatrix vector = new Array2DRowRealMatrix(1, 3);
			vector.setRow(0, new double[] { valX, valY, valZ });

			RealMatrix resultMatrix = rotationMatrix.multiply(vector.getRowMatrix(0).transpose());

			double[] test1 = resultMatrix.getRow(0);
			double[] test2 = resultMatrix.getRow(1);
			double[] test3 = resultMatrix.getRow(2);

			return new Point3D(test1[0], test2[0], test3[0]);
		}).map(p1 -> {
			Sphere s = new Sphere(size / 2);
			s.setScaleX(1 / 100);
			s.setScaleY(1 / 100);
			s.setScaleZ(1 / 100);
			Point3D tmpP = new Point3D(p1.getX() * 100.0f / radii.getX(), p1.getY() * 100.0f / radii.getY(),
					p1.getZ() * 100.0f / radii.getZ());

			s.setTranslateX(tmpP.getX());
			s.setTranslateY(tmpP.getY());
			s.setTranslateZ(tmpP.getZ());
			s.setMaterial(material);
			return s;
		}).collect(Collectors.toList());

		return spheres;
	}

	private static double getValue(TextField field) {
		return Double.valueOf(field.getText());
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

	private static void parsePoint(final List<Point3D> points, String s) {
		if (s.startsWith("#")) {
			return;
		}
		String trimmed = s.trim();
		if (trimmed.isEmpty()) {
			return;
		}
		points.add(readPoint(trimmed));
	}

	private static Point3D readPoint(String csvLine) {
		String[] values = csvLine.split(SEPARATOR);
		return new Point3D(Double.parseDouble(values[0]), Double.parseDouble(values[1]), Double.parseDouble(values[2]));
	}

	private static Sphere createSphere(float diameter, Point3D position, Material material) {
		Sphere s = new Sphere(diameter / 2);
		translate(s, position);
		s.setMaterial(material);
		return s;
	}

	private static void translate(Sphere s, Point3D position) {
		s.setTranslateX(position.getX());
		s.setTranslateY(position.getY());
		s.setTranslateZ(position.getZ());
	}

	private static Group getAxes() {
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

	private void updateSphereSizes(Number fromValue, Number toVal) {
		double fromSize = calculateSizeFromSliderValue(fromValue);
		double toSize = calculateSizeFromSliderValue(fromValue);
		List<Node> allNodes = null;
		synchronized (this) {
			allNodes = getAllSphereNodes();
		}
		resizeSpheres(allNodes, fromSize, toSize);
	}

	private void resizeSpheres(List<Node> allNodes, double fromSize, double toSize) {
		Animation[] animations = new Animation[allNodes.size()];

		for (int i = 0; i < animations.length; i++) {
			Sphere s = (Sphere) allNodes.get(i);
			Timeline timeline = new Timeline();
			timeline.getKeyFrames().add(new KeyFrame(Duration.millis(20), new KeyValue(s.radiusProperty(), fromSize / 2)));
			timeline.getKeyFrames().add(new KeyFrame(Duration.millis(1000), new KeyValue(s.radiusProperty(), toSize / 2)));
			animations[i] = timeline;
		}
		new ParallelTransition(animations).play();
	}

	private List<Node> getAllSphereNodes() {
		List<Node> allNodes = new ArrayList<>();
		if (rawSpheres != null) {
			allNodes.addAll(rawSpheres);
		}
		if (lastCorrectedSpheres != null) {
			allNodes.addAll(lastCorrectedSpheres);
		}
		return allNodes;
	}

	private double calculateSizeFromSliderValue(Number sliderValue) {
		return sliderValue.doubleValue() * (MAX_SPHERE_SIZE - MIN_SPHERE_SIZE) / 100 + MIN_SPHERE_SIZE;
	}

}
