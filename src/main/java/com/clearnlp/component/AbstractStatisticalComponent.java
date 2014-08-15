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
package com.clearnlp.component;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import com.clearnlp.classification.instance.StringInstance;
import com.clearnlp.classification.model.StringModel;
import com.clearnlp.classification.vector.StringFeatureVector;
import com.clearnlp.component.state.AbstractState;
import com.clearnlp.feature.AbstractFeatureExtractor;



/**
 * @since 3.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
abstract public class AbstractStatisticalComponent<LabelType, StateType extends AbstractState<LabelType>, FeatureType extends AbstractFeatureExtractor<?,?,?>> extends AbstractComponent
{
	protected FeatureType[] f_extractors;
	protected StringModel[] s_models;
	private CFlag c_flag;
	
	/** Constructs a statistical component for collecting. */
	public AbstractStatisticalComponent(FeatureType[] extractors)
	{
		setFeatureExtractors(extractors);
		c_flag = CFlag.COLLECT;
	}
	
	/** Constructs a statistical component for training. */
	public AbstractStatisticalComponent(FeatureType[] extractors, Object[] lexicons, boolean binary, int modelSize)
	{
		setFeatureExtractors(extractors);
		setLexicons(lexicons);
		setModels(createModels(binary, modelSize));
		c_flag = CFlag.TRAIN;
	}
	
	/** Constructs a statistical component for bootstrapping or evaluation. */
	public AbstractStatisticalComponent(FeatureType[] extractors, Object[] lexicons, StringModel[] models, boolean bootstrap)
	{
		setFeatureExtractors(extractors);
		setLexicons(lexicons);
		setModels(models);
		c_flag = bootstrap ? CFlag.BOOTSTRAP : CFlag.EVALUATE;
	}
	
	/** Constructs a statistical component for decoding. */
	public AbstractStatisticalComponent(ObjectInputStream in)
	{
		try
		{
			load(in);
		}
		catch (Exception e) {e.printStackTrace();}
		
		c_flag = CFlag.DECODE;
	}
	
	private StringModel[] createModels(boolean binary, int modelSize)
	{
		StringModel[] models = new StringModel[modelSize];
		int i;
		
		for (i=0; i<modelSize; i++)
			models[i] = new StringModel(binary);
		
		return models;
	}
	
//	====================================== LOAD/SAVE ======================================

	/**
	 * Loads all models and objects of this component.
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void load(ObjectInputStream in) throws Exception
	{
		setFeatureExtractors((FeatureType[])in.readObject());
		setLexicons((Object[])in.readObject());
		setModels((StringModel[])in.readObject());
	}
	
	/**
	 * Saves all models and objects of this component. 
	 * @throws Exception
	 */
	public void save(ObjectOutputStream out) throws Exception
	{
		out.writeObject(f_extractors);
		out.writeObject(getLexicons());
		out.writeObject(s_models);
	}
	
//	====================================== LEXICONS ======================================

	/** @return all objects containing lexicons. */
	abstract public Object[] getLexicons();
	
	/** Sets lexicons used for this component. */
	abstract public void setLexicons(Object[] lexicons);

//	====================================== FEATURES ======================================
	
	public FeatureType[] getFeatureExtractors()
	{
		return f_extractors;
	}
	
	public void setFeatureExtractors(FeatureType[] features)
	{
		f_extractors = features;
	}
	
//	====================================== MODELS ======================================

	public StringModel[] getModels()
	{
		return s_models;
	}
	
	public void setModels(StringModel[] models)
	{
		s_models = models;
	}
	
//	====================================== PROCESS ======================================
	
	protected void process(StateType state, List<StringInstance> instances)
	{
		switch (c_flag)
		{
		case TRAIN    : train(state, instances);
		case BOOTSTRAP: bootstrap(state, instances);
		default       : decode(state);
		}
	}
	
	protected void train(StateType state, List<StringInstance> instances)
	{
		StringFeatureVector vector = createStringFeatureVector(state);
		LabelType label = state.getGoldLabel();
		instances.add(new StringInstance(label.toString(), vector));
		state.setAutoLabel(label);
	}
	
	protected void bootstrap(StateType state, List<StringInstance> instances)
	{
		StringFeatureVector vector = createStringFeatureVector(state);
		LabelType label = state.getGoldLabel();
		instances.add(new StringInstance(label.toString(), vector));
		state.setAutoLabel(getAutoLabel(vector));
	}
	
	protected void decode(StateType state)
	{
		StringFeatureVector vector = createStringFeatureVector(state);
		state.setAutoLabel(getAutoLabel(vector));
	}
	
	abstract protected StringFeatureVector createStringFeatureVector(StateType state);
	abstract protected LabelType getAutoLabel(StringFeatureVector vector);
	
//	====================================== FLAG ======================================

	public boolean isCollect()
	{
		return c_flag == CFlag.COLLECT;
	}
	
	public boolean isTrain()
	{
		return c_flag == CFlag.TRAIN;
	}
	
	public boolean isBootstrap()
	{
		return c_flag == CFlag.BOOTSTRAP;
	}
	
	public boolean isDecode()
	{
		return c_flag == CFlag.DECODE;
	}
	
	public boolean isTrainOrBootstrap()
	{
		return isTrain() || isBootstrap();
	}
	
	
}