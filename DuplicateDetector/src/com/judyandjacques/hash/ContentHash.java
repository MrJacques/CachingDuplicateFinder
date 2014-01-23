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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * Holds a hash that describes a file
 */
public class ContentHash implements Serializable {

	private static final long serialVersionUID = -8071207538401027533L;
	private final byte[] digest;
	
	/**
	 * Instantiate a ContentHash from the file and message digest.
	 * 
	 * @param file that the ContentHash will match
	 * @param messageDigest to use create the hash.
	 */
	public ContentHash(File file, MessageDigest messageDigest)
			throws IOException {

		if (messageDigest == null) {
			throw new IllegalArgumentException("messasgeDigest cannot be null");
		}
		if (file == null) {
			throw new IllegalArgumentException("file cannot be null");
		}
		if (!file.exists()) {
			throw new FileNotFoundException("File does not exist - " + file);
		}

		// Make sure the message digest is ready
		messageDigest.reset();
		try (InputStream inputStream = Files.newInputStream(file.toPath());
				DigestInputStream digestStream = new DigestInputStream(
						inputStream, messageDigest)) {
			byte[] buffer = new byte[8192];
			while (digestStream.read(buffer) != -1) {
				// No body
			}
		}
		digest = messageDigest.digest();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(digest);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ContentHash other = (ContentHash) obj;
		if (!Arrays.equals(digest, other.digest)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ContentHash [digest=" + Arrays.toString(digest) + "]";
	}
}
