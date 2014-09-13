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
package com.clearnlp.collection.triple;

import java.io.Serializable;

/**
 * @since 3.0.0
 * @author Jinho D. Choi ({@code jinho.choi@emory.edu})
 */
public class BooleanIntIntTriple implements Serializable
{
	private static final long serialVersionUID = -5353827334306132865L;
	
	public boolean b;
	public int i1;
	public int i2;
	
	public BooleanIntIntTriple(boolean b, int i1, int i2)
	{
		set(b, i1, i2);
	}
	
	public void set(boolean b, int i1, int i2)
	{
		this.b  = b;
		this.i1 = i1;
		this.i2 = i2;
	}
}