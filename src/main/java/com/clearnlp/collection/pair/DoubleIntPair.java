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
package com.clearnlp.collection.pair;

import java.io.Serializable;

/**
 * @since 3.0.0
 * @author Jinho D. Choi ({@code jinho.choi@emory.edu})
 */
public class DoubleIntPair implements Serializable
{
	private static final long serialVersionUID = -2439322004395455224L;

	public double d;
	public int i;
	
	public DoubleIntPair(double d, int i)
	{
		set(d, i);
	}
	
	public void set(double d, int i)
	{
		this.d = d;
		this.i = i;
	}
}