// Copyright (C) 2014-2015 Guibing Guo
//
// This file is part of LibRec.
//
// LibRec is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// LibRec is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with LibRec. If not, see <http://www.gnu.org/licenses/>.
//
package librec.baseline;

import librec.data.DenseMatrix;
import librec.data.DenseVector;
import librec.data.SparseVector;
import librec.data.VectorEntry;
import librec.intf.GraphicRecommender;
import librec.util.Randoms;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * It is a graphical model that clusters users into K groups for recommendation, see reference: Barbieri et al.,
 * <strong>Probabilistic Approaches to Recommendations</strong> (Section 2.2), Synthesis Lectures on Data Mining and
 * Knowledge Discovery, 2014.
 * 
 * @author Guo Guibing
 * @author Matthäus Schmedding (clean up and adjusting to recalot.com)
 *
 */

public class UserCluster extends GraphicRecommender {

	private DenseMatrix Pkr; // theta
	private DenseVector Pi; // pi

	private DenseMatrix Gamma;
	private DenseMatrix Nur;
	private DenseVector Nu;

	public UserCluster() {
		super();
	}

	@Override
    public void initModel() throws Exception {

		Pkr = new DenseMatrix(numFactors, numLevels); // k x r
		for (int k = 0; k < numFactors; k++) {
			// random probs 
			double[] probs = Randoms.randProbs(numLevels);
			for (int r = 0; r < numLevels; r++)
				Pkr.set(k, r, probs[r]);
		}

		Pi = new DenseVector(Randoms.randProbs(numFactors));

		Gamma = new DenseMatrix(numUsers, numFactors); // u x k

		// pre-computing Nur, Nu
		Nur = new DenseMatrix(numUsers, numLevels); // Nur
		Nu = new DenseVector(numUsers); // Nu

		for (int u = 0; u < numUsers; u++) {
			SparseVector ru = trainMatrix.row(u);

			for (VectorEntry ve : ru) {
				double rui = ve.get();
				int r = ratingScale.indexOf(rui);

				Nur.add(u, r, 1);
			}

			Nu.set(u, ru.size());
		}
	}

	@Override
    public void buildModel() throws Exception {
		for (int iter = 1; iter <= numIters; iter++) {

			// e-step: compute Gamma_uk = P(Zu_k | ru, theta)
			for (int u = 0; u < numUsers; u++) {
				BigDecimal sum_u = BigDecimal.ZERO;
				SparseVector ru = trainMatrix.row(u);

				BigDecimal[] sum_uk = new BigDecimal[numFactors];
				for (int k = 0; k < numFactors; k++) {
					BigDecimal puk = new BigDecimal(Pi.get(k));

					for (VectorEntry ve : ru) {
						double rui = ve.get();
						int r = ratingScale.indexOf(rui);
						BigDecimal pkr = new BigDecimal(Pkr.get(k, r));

						puk = puk.multiply(pkr);
					}

					sum_uk[k] = puk;
					sum_u = sum_u.add(puk);
				}

				for (int k = 0; k < numFactors; k++) {
					double zuk = sum_uk[k].divide(sum_u, 6, RoundingMode.HALF_UP).doubleValue();
					Gamma.set(u, k, zuk);
				}
			}

			// m-step: update Pkr, Pi
			double sum_uk[] = new double[numFactors];
			double sum = 0;
			for (int k = 0; k < numFactors; k++) {

				for (int r = 0; r < numLevels; r++) {

					double numerator = 0, denorminator = 0;
					for (int u = 0; u < numUsers; u++) {
						double ruk = Gamma.get(u, k);

						numerator += ruk * Nur.get(u, r);
						denorminator += ruk * Nu.get(u);
					}

					Pkr.set(k, r, numerator / denorminator);
				}

				double sum_u = 0;
				for (int u = 0; u < numUsers; u++) {
					double ruk = Gamma.get(u, k);
					sum_u += ruk;
				}

				sum_uk[k] = sum_u;
				sum += sum_u;
			}

			for (int k = 0; k < numFactors; k++) {
				Pi.set(k, sum_uk[k] / sum);
			}

			// compute loss value
			loss = 0;
			for (int u = 0; u < numUsers; u++) {
				for (int k = 0; k < numFactors; k++) {
					double ruk = Gamma.get(u, k);
					double pi_k = Pi.get(k);

					double sum_nl = 0;
					for (int r = 0; r < numLevels; r++) {
						double nur = Nur.get(u, r);
						double pkr = Pkr.get(k, r);

						sum_nl += nur * Math.log(pkr);
					}

					loss += ruk * (Math.log(pi_k) + sum_nl);
				}
			}

			loss = -loss; // turn to minimization problem instead

			if (isConverged(iter))
				break;
		}
	}

	@Override
	protected boolean isConverged(int iter) throws Exception {
		float deltaLoss = (float) (loss - lastLoss);

		if (iter > 1 && (deltaLoss > 0 || Double.isNaN(deltaLoss))) {
	//		Logs.debug("{}{} converges at iter {}", algoName, foldInfo, iter);
			return true;
		}

		lastLoss = loss;
		return false;
	}

	@Override
	public double predict(int u, int j, boolean bound) throws Exception {
		double pred = 0;

		for (int k = 0; k < numFactors; k++) {
			double pu_k = Gamma.get(u, k); // probability that user u belongs to cluster k
			double pred_k = 0;

			for (int r = 0; r < numLevels; r++) {
				double rui = ratingScale.get(r);
				double pkr = Pkr.get(k, r);

				pred_k += rui * pkr;
			}

			pred += pu_k * pred_k;
		}

		return pred;
	}
}
