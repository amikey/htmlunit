/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package flash.swf.types;

import java.util.List;

/**
 * If you want to use ArrayList.toArray() so that you can use
 * Arrays.equals, please use this class.
 */
public class ArrayLists
{
	public static boolean equals(List a1, List a2)
	{
		if (a1 == a2)
		{
			return true;
		}

		if (a1 == null || a2 == null)
		{
			return false;
		}

		int length = a1.size();
		if (a2.size() != length)
		{
			return false;
		}

		for (int i = 0; i < length; i++)
		{
			Object o1 = a1.get(i);
			Object o2 = a2.get(i);
			if (!(o1 == null ? o2 == null : o1.equals(o2)))
			{
				return false;
			}
		}

		return true;
	}
}
