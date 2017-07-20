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

package com.robo4j.tools.magviz.ellipsoid;

import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import javafx.geometry.Point3D;

/**
 * Fit points to the polynomial expression Ax^2 + By^2 + Cz^2 + 2Dxy + 2Exz +
 * 2Fyz + 2Gx + 2Hy + 2Iz = 1 determine the ellipsoidCenter and radii of the fit
 * ellipsoid.
 *
 * sources: inspired by article: Ellipsoid or sphere fitting for sensor
 * calibration : https://goo.gl/v4XuQV inspired by Mathlab EllipsoidFit script:
 * http://de.mathworks.com/matlabcentral/fileexchange/24693-ellipsoid-fit
 * inspired by EllipsoidFit Java Implementation :
 * https://github.com/KalebKE/EllipsoidFit/tree/master/EllipsoidFit/src/ellipsoidFit
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class EllipsoidToSphereSolver {

	private Point3D radii;
	private double[] eigenValues;
	private Point3D eigenVector0;
	private Point3D eigenVector1;
	private Point3D eigenVector2;
	private RealMatrix eigenMatrix;
	private RealMatrix matrix;
	private Point3D center;
	private RealVector fitVector9;

	private List<Point3D> dataPoints;

	public EllipsoidToSphereSolver(List<Point3D> dataPoints) {
		this.dataPoints = dataPoints;
	}

	public void solve() {
		if (dataPoints == null || dataPoints.isEmpty()) {
			throw new SolverException("no data-points");
		}

		// Fit the points to Ax^2 + By^2 + Cz^2 + 2Dxy + 2Exz
		// + 2Fyz + 2Gx + 2Hy + 2Iz = 1 and solve the system.
		// v = (( d' * d )^-1) * ( d' * ones.mapAddToSelf(1));
		fitVector9 = pointsToEquation(dataPoints);

		// algebralicMatrix4[4x4] Form the algebraic form of the ellipsoid dimension of 4
		RealMatrix algebralicMatrix4 = formAlgebraicMatrix(fitVector9);

		// Solve ellipsoid ellipsoidCenter. Will be used as the bias vector, (offset - o
		// ) = -inv(SubA)[3x3]*vectorGhi[3x1]
		RealVector solvedCenterOffset = solveCenter(algebralicMatrix4);

		center = new Point3D(solvedCenterOffset.getEntry(0), solvedCenterOffset.getEntry(1),
				solvedCenterOffset.getEntry(2));

		// Translate the algebraic form of the ellipsoid to the center.
		RealMatrix translatedMatrix4 = translateToCenter(solvedCenterOffset, algebralicMatrix4);

		// Generate a submatrix of translatedMatrix4 subTranslatedM[3x3] = represents
		// Gain and Cross
		RealMatrix subTranslatedM = translatedMatrix4.getSubMatrix(0, 2, 0, 2);
		double divisor = -translatedMatrix4.getEntry(3, 3);
		// subTranslatedM computed from eigenValues and vectors subTranslatedM[3x3]/
		// (-translatedMatrix4(m33)[4x4])
		divideMatrixByValue(subTranslatedM, divisor);

		// This is the matrix to use in Robo4J
		matrix = subTranslatedM;

		// Get the eigenvalues and eigenvectors.
		EigenDecomposition solvedEigenVecors = new EigenDecomposition(subTranslatedM);
		eigenValues = solvedEigenVecors.getRealEigenvalues();
		eigenMatrix = solvedEigenVecors.getD();
		RealVector ev0 = solvedEigenVecors.getEigenvector(0);
		RealVector ev1 = solvedEigenVecors.getEigenvector(1);
		RealVector ev2 = solvedEigenVecors.getEigenvector(2);
		eigenVector0 = new Point3D(ev0.getEntry(0), ev0.getEntry(1), ev0.getEntry(2));
		eigenVector1 = new Point3D(ev1.getEntry(0), ev1.getEntry(1), ev1.getEntry(2));
		eigenVector2 = new Point3D(ev2.getEntry(0), ev2.getEntry(1), ev2.getEntry(2));
		// Find the radii of the ellipsoid, radii values are the square root fo the
		// inverse of 3 eigen values
		// a'' = a + b + c
		double[] radiiArray = findRadii(eigenValues);
		radii = new Point3D(radiiArray[0], radiiArray[1], radiiArray[2]);
	}

	public Point3D getRadii() {
		return radii;
	}

	public double[] getEigenValues() {
		return eigenValues;
	}

	public RealMatrix getEigenMatrix() {
		return eigenMatrix;
	}

	public Point3D getEigenVector0() {
		return eigenVector0;
	}

	public Point3D getEigenVector1() {
		return eigenVector1;
	}

	public Point3D getEigenVector2() {
		return eigenVector2;
	}

	public RealVector getFitVector9() {
		return fitVector9;
	}

	// Private Methods
	private void divideMatrixByValue(RealMatrix matrix, double divisor) {
		for (int i = 0; i < matrix.getRowDimension(); i++) {
			for (int j = 0; j < matrix.getRowDimension(); j++) {
				matrix.setEntry(i, j, matrix.getEntry(i, j) / divisor);
			}
		}
	}

	/**
	 * Find the radii of the ellipsoid in ascending order. Gains can be calculated
	 * as follow G = [sqrt(a''/gx), sqrt(a''/gy), sqrt(a''/gz)]
	 * 
	 * @param eigenValues
	 *            the eigenvalues of the ellipsoid.
	 * @return the radii of the ellipsoid.
	 */
	public static double[] findRadii(double[] eigenValues) {
		double[] radii = new double[eigenValues.length];
		for (int i = 0; i < eigenValues.length; i++) {
			radii[i] = Math.sqrt(1 / eigenValues[i]);
		}
		return radii;
	}

	/**
	 * Translate the algebraic form of the ellipsoid to the center.
	 *
	 * @param center
	 *            vector containing the center of the ellipsoid.
	 * @param aglMatrix
	 *            the algebraic form of the polynomial.
	 * @return the center translated form of the algebraic ellipsoid.
	 */
	private RealMatrix translateToCenter(RealVector center, RealMatrix aglMatrix) {
		// Form the corresponding translation matrix.
		RealMatrix t = MatrixUtils.createRealIdentityMatrix(4);

		RealMatrix centerMatrix = new Array2DRowRealMatrix(1, 3);

		centerMatrix.setRowVector(0, center);

		t.setSubMatrix(centerMatrix.getData(), 3, 0);

		// Translate to the center = T[4x4]*AlgMatrix[4x4]*T'[4x4]
		RealMatrix tmpM = t.multiply(aglMatrix);
		RealMatrix transposeT = t.transpose();
		return tmpM.multiply(transposeT);
	}

	private RealVector solveCenter(RealMatrix algMatrix) {

		// sub matrix 3x3
		RealMatrix subA = algMatrix.getSubMatrix(0, 2, 0, 2);

		for (int q = 0; q < subA.getRowDimension(); q++) {
			for (int s = 0; s < subA.getColumnDimension(); s++) {
				subA.multiplyEntry(q, s, -1.0);
			}
		}

		// Vghi = Vector[3x1]~(subA.row(3))
		RealVector vectorGhi = algMatrix.getRowVector(3).getSubVector(0, 3);
		// result (offset - o ) = -inv(SubA)[3x3]*vectorGhi[3x1]
		return new SingularValueDecomposition(subA).getSolver().getInverse().operate(vectorGhi);
	}

	/**
	 * Create a matrix in the algebraic form of the polynomial Ax^2 + By^2 + Cz^2 +
	 * 2Dxy + 2Exz + 2Fyz + 2Gx + 2Hy + 2Iz = 1.
	 *
	 * @param v
	 *            the vector polynomial.
	 * @return the matrix of the algebraic form of the polynomial.
	 */
	private RealMatrix formAlgebraicMatrix(RealVector v) {
		// a =
		// [ Ax^2 2Dxy 2Exz 2Gx ]
		// [ 2Dxy By^2 2Fyz 2Hy ]
		// [ 2Exz 2Fyz Cz^2 2Iz ]
		// [ 2Gx 2Hy 2Iz -1 ] ]
		double[][] data = { { v.getEntry(0), v.getEntry(3), v.getEntry(4), v.getEntry(6) },
				{ v.getEntry(3), v.getEntry(1), v.getEntry(5), v.getEntry(7) },
				{ v.getEntry(4), v.getEntry(5), v.getEntry(2), v.getEntry(8) },
				{ v.getEntry(6), v.getEntry(7), v.getEntry(8), -1 } };
		return new Array2DRowRealMatrix(data);
	}

	/**
	 * Solve the polynomial expression Ax^2 + By^2 + Cz^2 + 2Dxy + 2Exz + 2Fyz + 2Gx
	 * + 2Hy + 2Iz from the provided points.
	 *
	 *
	 * @param dataPoints
	 *            the points that will be fit to the polynomial expression.
	 *            dataPoint represent 3D Point dataPoint are fit to all 3 axes =>
	 *            result vector is then size of 9
	 * @return the solution vector to the polynomial expression.
	 */
	private RealVector pointsToEquation(List<Point3D> dataPoints) {

		RealMatrix designMatrix = new Array2DRowRealMatrix(dataPoints.size(), 9);
		IntStream.range(0, dataPoints.size() - 1).forEach(i -> {
			double xx = Math.pow(dataPoints.get(i).getX(), 2);
			double yy = Math.pow(dataPoints.get(i).getY(), 2);
			double zz = Math.pow(dataPoints.get(i).getZ(), 2);
			double xy = 2 * (dataPoints.get(i).getX() * dataPoints.get(i).getY());
			double xz = 2 * (dataPoints.get(i).getX() * dataPoints.get(i).getZ());
			double yz = 2 * (dataPoints.get(i).getY() * dataPoints.get(i).getZ());
			double x = 2 * dataPoints.get(i).getX();
			double y = 2 * dataPoints.get(i).getY();
			double z = 2 * dataPoints.get(i).getZ();

			designMatrix.setEntry(i, 0, xx);
			designMatrix.setEntry(i, 1, yy);
			designMatrix.setEntry(i, 2, zz);
			designMatrix.setEntry(i, 3, xy);
			designMatrix.setEntry(i, 4, xz);
			designMatrix.setEntry(i, 5, yz);
			designMatrix.setEntry(i, 6, x);
			designMatrix.setEntry(i, 7, y);
			designMatrix.setEntry(i, 8, z);
		});

		// solve the normal system of equations
		// v = (( d' * d )^-1) * ( d' * ones(Identity of size 9);
		// Multiply: d' * d
		RealMatrix dtd = designMatrix.transpose().multiply(designMatrix);
		RealVector ones9 = new ArrayRealVector(dataPoints.size(), 1);
		RealVector dtOnes = designMatrix.transpose().operate(ones9);

		// Find Inv(( d' * d )^-1)
		RealMatrix dtdMatrix9 = new SingularValueDecomposition(dtd).getSolver().getInverse();

		// result = (( d' * d )^-1) * ( d' * ones(Identity of size 9));
		return dtdMatrix9.operate(dtOnes);
	}

	public Point3D getCenter() {
		return center;
	}

	public RealMatrix getMatrix() {
		return matrix;
	}
}
