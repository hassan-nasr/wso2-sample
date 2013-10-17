/*
 * Copyright 2013, WSO2, Inc. http://wso2.org
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
 * limitations under the License
 */
package org.wso2.carbon.adc.mgt.cli.utils;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class CommandLineUtils {
	
	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("Resources");

	public static <T> void printTable(T[] data, RowMapper<T> mapper, String... headers) {
		if (data == null) {
			return;
		}
		// The maximum number of columns
		// All data String[] length must be equal to this
		int columns = headers.length;
		int rows = data.length + 1;

		String[][] table = new String[rows][columns];
		table[0] = headers;

		for (int i = 0; i < data.length; i++) {
			T t = data[i];
			table[i + 1] = mapper.getData(t);
		}

		// Find the maximum length of a string in each column
		int[] lengths = new int[columns];
		for (int i = 0; i < table.length; i++) {
			for (int j = 0; j < table[i].length; j++) {
				lengths[j] = Math.max(table[i][j].length(), lengths[j]);
			}
		}

		// The border rows
		String borders[] = new String[lengths.length];
		// Generate a format string for each column
		String[] formats = new String[lengths.length];
		for (int i = 0; i < lengths.length; i++) {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("+");
			for (int j = 0; j < lengths[i] + 2; j++) {
				stringBuilder.append("-");
			}
			boolean finalColumn = (i + 1 == lengths.length);
			if (finalColumn) {
				stringBuilder.append("+\n");
			}
			borders[i] = stringBuilder.toString();
			formats[i] = "| %1$-" + lengths[i] + "s " + (finalColumn ? "|\n" : "");
		}

		// Print the table
		for (int i = 0; i < table.length; i++) {
			for (int j = 0; j < table[i].length; j++) {
				System.out.print(borders[j]);
			}
			for (int j = 0; j < table[i].length; j++) {
				System.out.format(formats[j], table[i][j]);
			}
			if (i + 1 == table.length) {
				for (int j = 0; j < table[i].length; j++) {
					System.out.print(borders[j]);
				}
			}
		}
	}
	
	public static String getMessage(String key, Object... args) {
		String message = BUNDLE.getString(key);
		if (args != null && args.length > 0) {
			message = MessageFormat.format(message, args);
		}
		return message;
	}
}
