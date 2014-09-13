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
package com.clearnlp.classification.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.clearnlp.classification.instance.AbstractInstance;
import com.clearnlp.classification.instance.AbstractInstanceCollector;
import com.clearnlp.classification.instance.IntInstance;
import com.clearnlp.classification.map.LabelMap;
import com.clearnlp.classification.prediction.StringPrediction;
import com.clearnlp.classification.vector.AbstractFeatureVector;
import com.clearnlp.classification.vector.AbstractWeightVector;
import com.clearnlp.classification.vector.BinaryWeightVector;
import com.clearnlp.classification.vector.MultiWeightVector;
import com.clearnlp.collection.list.SortedArrayList;
import com.clearnlp.collection.pair.DoubleIntPair;
import com.clearnlp.collection.pair.Pair;
import com.clearnlp.util.DSUtils;
import com.google.common.collect.Lists;

/**
 * @since 3.0.0
 * @author Jinho D. Choi ({@code jinho.choi@emory.edu})
 */
abstract public class AbstractModel<I extends AbstractInstance<F>, F extends AbstractFeatureVector>
{
	protected AbstractInstanceCollector<I,F> i_collector;
	protected AbstractWeightVector w_vector;
	protected LabelMap m_labels;

	/** Initializes this model for training. */
	public AbstractModel(boolean binary)
	{
		w_vector = binary ? new BinaryWeightVector() : new MultiWeightVector();
		m_labels = new LabelMap();
	}
	
	public AbstractModel(ObjectInputStream in)
	{
		try
		{
			load(in);
		}
		catch (ClassNotFoundException | IOException e) {e.printStackTrace();}
	}
	
// =============================== Serialization ===============================
	
	abstract public void load(ObjectInputStream  in ) throws IOException, ClassNotFoundException;
	abstract public void save(ObjectOutputStream out) throws IOException;
	
// =============================== Training ===============================

	abstract public void addInstance(I instance);
	
	public void addInstances(Collection<I> instances)
	{
		for (I instance : instances)
			addInstance(instance);
	}
	
// =============================== Labels/Features/Weights ===============================
	
	public int getLabelSize()
	{
		return w_vector.getLabelSize();
	}
	
	public int getFeatureSize()
	{
		return w_vector.getFeatureSize();
	}
	
	public List<String> getLabelList()
	{
		return m_labels.getLabelList();
	}

	public AbstractWeightVector getWeightVector()
	{
		return w_vector;
	}
	
	public boolean isBinaryLabel()
	{
		return w_vector.isBinaryLabel();
	}
	
// =============================== Conversion ===============================
	
	abstract public IntInstance toIntInstance(I instance);
	
	public List<IntInstance> toIntInstanceList(List<I> sInstances)
	{
		ArrayList<IntInstance> iInstances = Lists.newArrayList();
		IntInstance iInstance;
		
		while (!sInstances.isEmpty())
		{
			iInstance = toIntInstance(sInstances.remove(0));
			if (iInstance != null) iInstances.add(iInstance);
		}
		
		iInstances.trimToSize();
		return iInstances;
	}
	
// =============================== Predictions ===============================
	
	abstract public double[] getScores(F x);
	
	public StringPrediction getPrediction(int labelIndex, double score)
	{
		return new StringPrediction(m_labels.getLabel(labelIndex), score);
	}
	
	/** @return the best prediction given the specific feature vector. */
	public StringPrediction predictBest(F x)
	{
		return isBinaryLabel() ? predictBestBinary(x) : predictBestMulti(x);
	}
	
	private StringPrediction predictBestBinary(F x)
	{
		double[] scores = getScores(x);
		return (scores[0] > 0) ? getPrediction(0, scores[0]) : getPrediction(1, scores[1]);
	}
	
	private StringPrediction predictBestMulti(F x)
	{
		double[] scores = getScores(x);
		int i, size = scores.length, maxIndex = 0;
		double maxValue = scores[0];
		
		for (i=1; i<size; i++)
		{
			if (maxValue < scores[i])
			{
				maxIndex = i;
				maxValue = scores[i];
			}
		}
		
		return getPrediction(maxIndex, maxValue);
	}
	
	/** @return the top 2 predictions given the specific feature vector. */
	public Pair<StringPrediction,StringPrediction> predictTop2(F x)
	{
		return isBinaryLabel() ? predictTop2Binary(x) : predictTop2Multi(x);
	}
	
	private Pair<StringPrediction,StringPrediction> predictTop2Binary(F x)
	{
		double[] scores = getScores(x);
		StringPrediction fst = getPrediction(0, scores[0]);
		StringPrediction snd = getPrediction(1, scores[1]);
		return (scores[0] > 0) ? new Pair<StringPrediction,StringPrediction>(fst,snd) : new Pair<StringPrediction,StringPrediction>(snd,fst);
	}
	
	private Pair<StringPrediction,StringPrediction> predictTop2Multi(F x)
	{
		double[] scores = getScores(x);
		Pair<DoubleIntPair,DoubleIntPair> top2 = DSUtils.top2(scores);
		DoubleIntPair p1 = top2.o1;
		DoubleIntPair p2 = top2.o2;
		return new Pair<StringPrediction,StringPrediction>(getPrediction(p1.i,p1.d), getPrediction(p2.i,p2.d));
	}
	
	/** @return the list of predictions given the specific feature vector sorted in descending order. */
	public List<StringPrediction> predictAll(F x)
	{
		return isBinaryLabel() ? predictAllBinary(x) : predictAllMulti(x);
	}
	
	private List<StringPrediction> predictAllBinary(F x)
	{
		Pair<StringPrediction,StringPrediction> p = predictTop2Binary(x);
		List<StringPrediction> ps = new ArrayList<StringPrediction>(2);
		ps.add(p.o1); ps.add(p.o2);
		return ps;
	}

	private List<StringPrediction> predictAllMulti(F x)
	{
		double[] scores = getScores(x);
		int i, lsize = getLabelSize();
		List<StringPrediction> list = new SortedArrayList<StringPrediction>(lsize, false);
		
		for (i=0; i<lsize; i++)
			list.add(getPrediction(i, scores[i]));
		
		return list;
	}
}