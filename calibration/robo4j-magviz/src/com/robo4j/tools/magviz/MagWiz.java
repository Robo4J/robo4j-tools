package com.robo4j.tools.magviz;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import javafx.animation.Animation;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Visualizer for 3D data.
 * 
 * @author marcus
 */
public class MagWiz extends Application {
	private static final String SEPARATOR = ";";
	private static final Point3D ZERO = new Point3D(0, 0, 0);
	private static final int WIDTH = 800;
	private static final int HEIGHT = 600;
	private static final double AXIS_LENGTH = 50;
	private static final double AXIS_THICKNESS = 3;

	private final static Material BLACK = new PhongMaterial(Color.BLACK);

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		File csvFile = parseFileParam();

		stage.getScene();
		AmbientLight ambient = new AmbientLight(Color.WHITE);

		PerspectiveCamera camera = new PerspectiveCamera(false);
		camera.setTranslateX(-WIDTH / 2);
		camera.setTranslateY(-HEIGHT / 2);
		camera.setTranslateZ(-200);

		Group points = null;

		if (csvFile != null) {
			points = createPoints(csvFile);
		} else {
			points = new Group();
		}

		Group axesAndPoints = new Group(getAxes(), points);
		Group root = new Group(ambient, axesAndPoints);

		RotateTransition rotation = new RotateTransition(Duration.seconds(4), root);
		rotation.setFromAngle(0);
		rotation.setToAngle(360);
		rotation.setAxis(Rotate.X_AXIS);

		RotateTransition rotationBack = new RotateTransition(Duration.seconds(4), root);
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

		Scene scene = new Scene(root, WIDTH, HEIGHT, true);
		scene.setCamera(camera);
		stage.setScene(scene);
		stage.setTitle("Magnetometer Calibration Utility");
		stage.show();
	}

	private Group createPoints(File csvFile) {
		final Collection<Point3D> points = new ArrayList<>();
		try (Stream<String> stream = Files.lines(csvFile.toPath())) {
			stream.forEach((s) -> points.add(readPoint(s)));
		} catch (IOException e) {
			e.printStackTrace();
		}

		double maxRadius = 0;
		for (Point3D p : points) {
			maxRadius = Math.max(maxRadius, ZERO.distance(p));
		}
		
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

	private File parseFileParam() {
		List<String> params = getParameters().getRaw();
		if (params.isEmpty()) {
			System.out.println("Warning - no csv file specified!");
			return null;
		}
		File f = new File(params.get(0));
		if (!f.exists()) {
			System.out.println("Warning - the csv file specified (" + f.getName() + ") does not exist!");
			return null;
		}
		return f;
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
