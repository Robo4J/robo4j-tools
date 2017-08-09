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
 * calibration https://goo.gl/v4XuQV
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class EllipsoidToSphereSolver {

	private List<Point3D> dataPoints;

	public EllipsoidToSphereSolver(List<Point3D> dataPoints) {
		this.dataPoints = dataPoints;
	}

	public SolvedEllipsoidResult solve() {
		if (dataPoints == null || dataPoints.isEmpty()) {
			throw new SolverException("no data-points");
		}

		RealVector fittingVector = pointsToEquation(dataPoints);
		RealMatrix algMatrix = formAlgebraicMatrix(fittingVector);
		RealVector solvedCenterOffset = solveCenter(algMatrix);
		Point3D center = new Point3D(solvedCenterOffset.getEntry(0), solvedCenterOffset.getEntry(1), solvedCenterOffset.getEntry(2));

		// Translate the algebraic form of the ellipsoid to the center.
		RealMatrix translatedMatrix4 = translateToCenter(solvedCenterOffset, algMatrix);

		// [rotM ev]=eig(translatedMatrix4(1:3,1:3)/-translatedMatrix4[4x4]); %
		RealMatrix subTranslatedM = translatedMatrix4.getSubMatrix(0, 2, 0, 2);
		double divisor = -translatedMatrix4.getEntry(3, 3);

		EigenDecomposition solvedEigenVecors = new EigenDecomposition(divideMatrixByValue(subTranslatedM, divisor));
		double[] eigenValues = new double[] { solvedEigenVecors.getRealEigenvalues()[2], solvedEigenVecors.getRealEigenvalues()[1],
				solvedEigenVecors.getRealEigenvalues()[0] };
		double number = -1;
		RealVector ev0 = solvedEigenVecors.getEigenvector(2).mapMultiply(number);
		RealVector ev1 = solvedEigenVecors.getEigenvector(1).mapMultiply(number);
		RealVector ev2 = solvedEigenVecors.getEigenvector(0).mapMultiply(number);

		RealMatrix rotationMatrix = new Array2DRowRealMatrix(3, 3);
		rotationMatrix.setRow(0, ev0.toArray());
		rotationMatrix.setRow(1, ev1.toArray());
		rotationMatrix.setRow(2, ev2.toArray());

		double[] gainArray = findGain(eigenValues);
		//@formatter:off
		RealMatrix gainCorrectionMatrix = new Array2DRowRealMatrix(
				new double[][] {
						{ 1 / gainArray[0], 0, 0 },
						{ 0, 1 / gainArray[1], 0 },
						{ 0, 0, 1 / gainArray[2] }
				});
		//@formatter:on

		// adjusting
		RealMatrix gainCorrectedRotationMatrix = rotationMatrix.transpose().multiply(gainCorrectionMatrix).multiply(rotationMatrix);
		return new SolvedEllipsoidResult(center, gainCorrectedRotationMatrix);
	}

	// Private Methods
	private RealMatrix divideMatrixByValue(final RealMatrix matrix, double divisor) {
		final RealMatrix result = new Array2DRowRealMatrix(matrix.getRowDimension(), matrix.getRowDimension());
		for (int i = 0; i < matrix.getRowDimension(); i++) {
			for (int j = 0; j < matrix.getRowDimension(); j++) {
				result.setEntry(i, j, (matrix.getEntry(i, j) / divisor));
			}
		}
		return result;
	}

	/**
	 * Returns the radii of the ellipsoid
	 *
	 * @param eigenValues
	 *            the eigenvalues of the ellipsoid.
	 * @return the radii of the ellipsoid.
	 */
	private static double[] findGain(double[] eigenValues) {
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
	 * @param algM
	 *            the algebraic form of the polynomial.
	 * @return the center translated form of the algebraic ellipsoid.
	 */
	private RealMatrix translateToCenter(RealVector center, RealMatrix algM) {
		// Form the corresponding translation rotationMatrix.
		RealMatrix tmtx = MatrixUtils.createRealIdentityMatrix(4);
		tmtx.setEntry(0, 3, center.getEntry(0));
		tmtx.setEntry(1, 3, center.getEntry(1));
		tmtx.setEntry(2, 3, center.getEntry(2));

		// Translate to the center = T[4x4]*AlgMatrix[4x4]*T'[4x4]
		return tmtx.transpose().multiply(algM.multiply(tmtx));
	}

	/**
	 * calculate ellipsoid center
	 *
	 * @param algMatrix
	 *            alg matrix
	 * @return center (offset)
	 */
	private RealVector solveCenter(RealMatrix algMatrix) {
		RealMatrix subA = algMatrix.getSubMatrix(0, 2, 0, 2).scalarMultiply(-1);
		RealVector vectorGhi = algMatrix.getRowVector(3).getSubVector(0, 3);

		return new SingularValueDecomposition(subA).getSolver().getInverse().operate(vectorGhi);
	}

	/**
	 * Create a rotationMatrix in the algebraic form of the polynomial Ax^2 +
	 * By^2 + Cz^2 + 2Dxy + 2Exz + 2Fyz + 2Gx + 2Hy + 2Iz = 1.
	 *
	 * @param v
	 *            the vector polynomial.
	 * @return the rotationMatrix of the algebraic form of the polynomial.
	 */
	private RealMatrix formAlgebraicMatrix(RealVector v) {
		// a =
		// [ Ax^2 2Dxy 2Exz 2Gx ]
		// [ 2Dxy By^2 2Fyz 2Hy ]
		// [ 2Exz 2Fyz Cz^2 2Iz ]
		// [ 2Gx 2Hy 2Iz -1 ] ]

		//@formatter:off
		double[][] data = {
				{ v.getEntry(0), v.getEntry(3), v.getEntry(4), v.getEntry(6) },
				{ v.getEntry(3), v.getEntry(1), v.getEntry(5), v.getEntry(7) },
				{ v.getEntry(4), v.getEntry(5), v.getEntry(2), v.getEntry(8) },
				{ v.getEntry(6), v.getEntry(7), v.getEntry(8), -1 }
		};
		//@formatter:on

		return new Array2DRowRealMatrix(data);
	}

	/**
	 * Solve the polynomial expression Ax^2 + By^2 + Cz^2 + 2Dxy + 2Exz + 2Fyz +
	 * 2Gx + 2Hy + 2Iz from the provided points.
	 *
	 * @param dataPoints
	 *            the points that will be fit to the polynomial expression.
	 *            dataPoint represent 3D Point dataPoint are fit to all 3 axes
	 *            => result vector is then size of 9
	 * @return the solution vector to the polynomial expression.
	 */
	private RealVector pointsToEquation(List<Point3D> dataPoints) {

		RealMatrix designMatrix = new Array2DRowRealMatrix(dataPoints.size(), 9);
		IntStream.range(0, dataPoints.size()).forEach(i -> {
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

		// v = (( d' * d )^-1) * ( d' * ones(Identity of size 9);
		RealMatrix dtd = designMatrix.transpose().multiply(designMatrix);
		RealVector ones9 = new ArrayRealVector(dataPoints.size(), 1);
		RealVector dtOnes = designMatrix.transpose().operate(ones9);

		// Find Inv(( d' * d )^-1)
		RealMatrix dtdMatrix9 = new SingularValueDecomposition(dtd).getSolver().getInverse();
		return dtdMatrix9.operate(dtOnes);
	}

}
