/**
 * Copyright 2014, Emory University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.clearnlp.classification.train;

import com.clearnlp.classification.instance.IntInstance;
import com.clearnlp.classification.model.SparseModel;
import com.clearnlp.classification.model.StringModel;
import com.clearnlp.classification.vector.SparseFeatureVector;
import com.clearnlp.util.MathUtils;

/**
 * AdaGrad algorithm using hinge loss.
 * @since 3.0.0
 * @author Jinho D. Choi ({@code jinho.choi@emory.edu})
 */
public class AdaGradLR extends AbstractAdaGrad
{
	/**
	 * @param alpha the learning rate.
	 * @param rho the smoothing denominator.
	 */
	public AdaGradLR(SparseModel model, boolean average, double alpha, double rho)
	{
		super(model, average, alpha, rho);
	}
	
	/**
	 * @param alpha the learning rate.
	 * @param rho the smoothing denominator.
	 */
	public AdaGradLR(StringModel model, int labelCutoff, int featureCutoff, boolean average, double alpha, double rho)
	{
		super(model, labelCutoff, featureCutoff, average, alpha, rho);
	}
	
	@Override
	protected boolean update(IntInstance instance, int averageCount)
	{
		double[] gradients = getGradients(instance);
		
		if (gradients[instance.getLabel()] > 0.01)
		{
			updateGradients(instance, gradients);
			updateWeights  (instance, gradients, averageCount);
			return true;
		}
		
		return false;
	}
	
	private double[] getGradients(IntInstance instance)
	{
		double[] scores = w_vector.getScores(instance.getFeatureVector(), true);
		int i, size = scores.length;
		
		for (i=0; i<size; i++) scores[i] *= -1;
		scores[instance.getLabel()] += 1;
		
		return scores;
	}
	
	private void updateGradients(IntInstance instance, double[] gradidents)
	{
		SparseFeatureVector x = instance.getFeatureVector();
		int i, j, xi, len = x.size(), lsize = w_vector.getLabelSize();
		double[] g = new double[lsize];
		double vi;
		
		for (j=0; j<lsize; j++)
			g[j] = MathUtils.sq(gradidents[j]);
		
		for (i=0; i<len; i++)
		{
			xi = x.getIndex(i);
			vi = MathUtils.sq(x.getWeight(i));
			
			for (j=0; j<lsize; j++)
				d_gradients[w_vector.getWeightIndex(j, xi)] += vi * g[j];
		}
	}
	
	private void updateWeights(IntInstance instance, double[] gradients, int averageCount)
	{
		SparseFeatureVector x = instance.getFeatureVector();
		int i, j, xi, len = x.size(), lsize = w_vector.getLabelSize();
		double vi;
		
		for (i=0; i<len; i++)
		{
			xi = x.getIndex(i);
			vi = x.getWeight(i);
			
			for (j=0; j<lsize; j++)
				updateWeight(w_vector.getWeightIndex(j, xi), vi*gradients[j], averageCount);
		}
	}
}