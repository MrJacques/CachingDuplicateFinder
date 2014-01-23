/**
 * Copyright 2012=4 Jacques Parker mrjacques@gmail.com
 * 
 * This file is part of the Caching Duplicate Finder project
 * 
 * Caching Duplicate Finder project is free software: you can redistribute it 
 * and/or modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation, either version 3 of the License, 
 * or (at your option) any later version.
 * 
 * Caching Duplicate Finder project is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General 
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * the Single-Script-Photo-Frame. If not, see http://www.gnu.org/licenses/.
 */
package com.judyandjacques.hash;

import java.io.File;

/**
 * Wrapper for exceptions thrown by the HashCreator that includes the file that
 * caused the exception
 */
public class HashException extends Exception {

	private static final long serialVersionUID = -8198762011334822570L;

	// File associated with the erro
	private final File file;

	/**
	 * @param file
	 *            file that was associated with the exception or null if there
	 *            was none.
	 * @see Exception
	 */
	public HashException(File file) {
		this.file = file;
	}

	/**
	 * @param file
	 *            file that was associated with the exception or null if there
	 *            was none.
	 * @see Exception
	 */
	public HashException(File file, String arg0) {
		super(arg0);
		this.file = file;
	}

	/**
	 * @param file
	 *            file that was associated with the exception or null if there
	 *            was none.
	 * @see Exception
	 */
	public HashException(File file, Throwable arg0) {
		super(arg0);
		this.file = file;
	}

	/**
	 * @param file
	 *            file that was associated with the exception or null if there
	 *            was none.
	 * @see Exception
	 */
	public HashException(File file, String arg0, Throwable arg1) {
		super(arg0, arg1);
		this.file = file;
	}

	/**
	 * @param file
	 *            file that was associated with the exception or null if there
	 *            was none.
	 * @see Exception
	 */
	public HashException(File file, String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
		this.file = file;
	}

	/**
	 * @see Exception
	 */
	public HashException(Exception e) {
		super(e);
		file = null;
	}

	/**
	 * @see Exception
	 */
	public HashException(String message) {
		super(message);
		file = null;
	}

	public HashException(String message, Exception e) {
		super(message, e);
		file = null;
	}

	/**
	 * @return The file associated with the exception (if there was one)
	 */
	public File getFile() {
		return file;
	}

	@Override
	public String toString() {
		return "HashException [file=" + file + "Exception=" + super.toString()
				+ "]";
	}
}
