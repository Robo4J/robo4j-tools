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

import com.robo4j.tools.magviz.math.Tuple3d;

/**
 * Fit points to the polynomial expression Ax^2 + By^2 + Cz^2 + 2Dxy + 2Exz +
 * 2Fyz + 2Gx + 2Hy + 2Iz = 1 determine the ellipsoidCenter and radii of the fit
 * ellipsoid.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class EllipsoidToSphereSolver {

	public Tuple3d ellipsoidCenter;
	public Tuple3d radii;
	public double[] eigenValues;
	public Tuple3d eigenVector0;
	public Tuple3d eigenVector1;
	public Tuple3d eigenVector2;

	private List<Tuple3d> dataPoints;

	public EllipsoidToSphereSolver(List<Tuple3d> dataPoints) {
		this.dataPoints = dataPoints;
	}

	public Tuple3d getSphereMatrix() {
		if (dataPoints == null || dataPoints.isEmpty()) {
			throw new SolverException("no data-points");
		}

		// Fit the points to Ax^2 + By^2 + Cz^2 + 2Dxy + 2Exz
		// + 2Fyz + 2Gx + 2Hy + 2Iz = 1 and solve the system.
		// v = (( d' * d )^-1) * ( d' * ones.mapAddToSelf(1));
		RealVector fitVector9 = pointsToEquation(dataPoints);

		// Form the algebraic form of the ellipsoid dimension of 4
		RealMatrix algebralicMatrix4 = formAlgebraicMatrix(fitVector9);

		// solve ellipsoid ellipsoidCenter
		RealVector solvedCenter = solveCenter(algebralicMatrix4);
		ellipsoidCenter = new Tuple3d(solvedCenter.getEntry(0), solvedCenter.getEntry(1), solvedCenter.getEntry(2));

		// Translate the algebraic form of the ellipsoid to the center.
		RealMatrix translatedMatrix4 = translateToCenter(solvedCenter, algebralicMatrix4);

		// Generate a submatrix of translatedMatrix4.
		RealMatrix subr = translatedMatrix4.getSubMatrix(0, 2, 0, 2);
		double divisor = -translatedMatrix4.getEntry(3, 3);
        divideMatrixByValue(subr, divisor);
		// Get the eigenvalues and eigenvectors.
		EigenDecomposition solvedEigenVecors = new EigenDecomposition(subr);
		eigenValues = solvedEigenVecors.getRealEigenvalues();
		RealVector ev0 = solvedEigenVecors.getEigenvector(0);
		RealVector ev1 = solvedEigenVecors.getEigenvector(1);
		RealVector ev2 = solvedEigenVecors.getEigenvector(2);
		eigenVector0 = new Tuple3d(ev0.getEntry(0), ev0.getEntry(1), ev0.getEntry(2));
		eigenVector1 = new Tuple3d(ev1.getEntry(0), ev1.getEntry(1), ev1.getEntry(2));
		eigenVector2 = new Tuple3d(ev2.getEntry(0), ev2.getEntry(1), ev2.getEntry(2));
		// Find the radii of the ellipsoid.
		radii = findRadii(eigenValues);

		return ellipsoidCenter;
	}

	public Tuple3d getRadii() {
		return radii;
	}

	public double[] getEigenValues() {
		return eigenValues;
	}

	public Tuple3d getEigenVector0() {
		return eigenVector0;
	}

	public Tuple3d getEigenVector1() {
		return eigenVector1;
	}

	public Tuple3d getEigenVector2() {
		return eigenVector2;
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
	 * Find the radii of the ellipsoid in ascending order.
	 * 
	 * @param evals
	 *            the eigenvalues of the ellipsoid.
	 * @return the radii of the ellipsoid.
	 */
	public Tuple3d findRadii(double[] eigenValues) {
		Tuple3d result = new Tuple3d();
		// radii[i] = sqrt(1/eval[i]);
		for (int i = 0; i < eigenValues.length; i++) {
			result.setEntry(i, Math.sqrt(1 / eigenValues[i]));
		}
		return result;
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

		// Translate to the center.
		RealMatrix tmp1 = t.multiply(aglMatrix);
		RealMatrix transposeT = t.transpose();
		return tmp1.multiply(transposeT);
	}

	private RealVector solveCenter(RealMatrix algMatrix) {

		// sub matrix 3x3
		RealMatrix subA = algMatrix.getSubMatrix(0, 2, 0, 2);

		for (int q = 0; q < subA.getRowDimension(); q++) {
			for (int s = 0; s < subA.getColumnDimension(); s++) {
				subA.multiplyEntry(q, s, -1.0);
			}
		}

		RealVector subV = algMatrix.getRowVector(3).getSubVector(0, 3);
		// inv
		return new SingularValueDecomposition(subA).getSolver().getInverse().operate(subV);
	}

	/**
	 * Create a matrix in the algebraic form of the polynomial Ax^2 + By^2 +
	 * Cz^2 + 2Dxy + 2Exz + 2Fyz + 2Gx + 2Hy + 2Iz = 1.
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
	 * Solve the polynomial expression Ax^2 + By^2 + Cz^2 + 2Dxy + 2Exz + 2Fyz +
	 * 2Gx + 2Hy + 2Iz from the provided points.
	 *
	 *
	 * @param dataPoints
	 *            the points that will be fit to the polynomial expression.
	 *            dataPoint represent 3D Point dataPoint are fit to all 3 axes
	 *            => result vector is then size of 9
	 * @return the solution vector to the polynomial expression.
	 */
	private RealVector pointsToEquation(List<Tuple3d> dataPoints) {

		RealMatrix designMatrix = new Array2DRowRealMatrix(dataPoints.size(), 9);
		IntStream.range(0, dataPoints.size() - 1).forEach(i -> {
			double xx = Math.pow(dataPoints.get(i).x, 2);
			double yy = Math.pow(dataPoints.get(i).y, 2);
			double zz = Math.pow(dataPoints.get(i).z, 2);
			double xy = 2 * (dataPoints.get(i).x * dataPoints.get(i).y);
			double xz = 2 * (dataPoints.get(i).x * dataPoints.get(i).z);
			double yz = 2 * (dataPoints.get(i).y * dataPoints.get(i).z);
			double x = 2 * dataPoints.get(i).x;
			double y = 2 * dataPoints.get(i).y;
			double z = 2 * dataPoints.get(i).z;

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

}
