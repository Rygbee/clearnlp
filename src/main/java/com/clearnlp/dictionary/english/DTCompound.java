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
package com.clearnlp.dictionary.english;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.clearnlp.dictionary.DTPath;
import com.clearnlp.util.IOUtils;
import com.clearnlp.util.regex.Splitter;
import com.google.common.collect.Maps;

/**
 * @since 3.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class DTCompound
{
	private Map<String,int[]> m_compound;
	
	public DTCompound()
	{
		init(IOUtils.getInputStreamsFromClasspath(DTPath.EN_COMPOUND));
	}
	
	public DTCompound(InputStream compound)
	{
		init(compound);
	}
	
	public void init(InputStream compound)
	{
		BufferedReader reader = IOUtils.createBufferedReader(compound);
		m_compound = Maps.newHashMap();
		List<String> tokens;
		StringBuilder build;
		String line, token;
		int i, size;
		int[] tmp;
		
		try
		{
			while ((line = reader.readLine()) != null)
			{
				tokens = Splitter.splitSpace(line.trim());
				build  = new StringBuilder();
				size   = tokens.size() - 1;
				tmp    = new int[size];
				
				for (i=0; i<size; i++)
				{
					token  = tokens.get(i);
					tmp[i] = build.length() + token.length();
					build.append(token);
				}
				
				build.append(tokens.get(size));
				m_compound.put(build.toString(), tmp);
			}
		}
		catch (IOException e) {e.printStackTrace();}
	}
	
	public int[] getSplitIndices(String lower)
	{
		return m_compound.get(lower);
	}
}
