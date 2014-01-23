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
import java.io.Serializable;
import java.util.Date;

/**
 * This matches a contentHash to a file on the file system. As long as the file is
 * not modified the hash will remain valid.
 * 
 * The class is immutable.
 */
public class FileHash implements Serializable {
	private static final long serialVersionUID = -8377784058324574998L;
	
	private final String absolutePath;
	private final ContentHash contentHash;
	private final long length;
	private final long lastModified;

	/**
	 * Create a FileHash for the file and contentHash.
	 * 
	 * @param file
	 *            that the FileHash refers to.
	 * @param contentHash
	 *            for the file
	 * @throws FileNotFoundException
	 *             if the file does not exist.
	 */
	public FileHash(File file, ContentHash contentHash)
			throws FileNotFoundException {
		if (file == null) {
			throw new IllegalArgumentException("file cannot be null");
		}
		if (contentHash == null) {
			throw new IllegalArgumentException("hash cannot be null");
		}
		if (!file.exists()) {
			throw new FileNotFoundException(
					"Could not get information on file - " + file);
		}

		this.contentHash = contentHash;
		this.absolutePath = file.getAbsolutePath();
		this.length = file.length();
		this.lastModified = file.lastModified();
	}

	/**
	 * Determines if the FileHash is still valid for the file on the file
	 * system. i.e. That the names match and the file hasn't been modified.
	 * 
	 * Returns false if the file does not exist.
	 * 
	 * @param file
	 *            to check
	 * @return true if the FileHash is still valid
	 */
	public boolean isValidForFile(File file) {
		if (file == null) {
			throw new IllegalArgumentException("file cannot be null");
		}
		return (file.exists() && file.getAbsolutePath().equalsIgnoreCase(absolutePath)
				&& (file.length() == length) && (file.lastModified() == lastModified));
	}

	/**
	 * @return the file's absolutePath
	 */
	public String getAbsolutePath() {
		return absolutePath;
	}

	/**
	 * @return the contentHash
	 */
	public ContentHash getContentHash() {
		return contentHash;
	}

	/**
	 * @return the file's length
	 */
	public long getLength() {
		return length;
	}

	/**
	 * @return the file's lastModified
	 */
	public Date getLastModified() {
		return new Date(lastModified);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((absolutePath == null) ? 0 : absolutePath.hashCode());
		result = prime * result
				+ ((contentHash == null) ? 0 : contentHash.hashCode());
		result = prime * result + (int) (lastModified ^ (lastModified >>> 32));
		result = prime * result + (int) (length ^ (length >>> 32));
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
		FileHash other = (FileHash) obj;
		if (absolutePath == null) {
			if (other.absolutePath != null) {
				return false;
			}
		} else if (!absolutePath.equals(other.absolutePath)) {
			return false;
		}
		if (contentHash == null) {
			if (other.contentHash != null) {
				return false;
			}
		} else if (!contentHash.equals(other.contentHash)) {
			return false;
		}
		if (lastModified != other.lastModified) {
			return false;
		}
		if (length != other.length) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "FileHash [absolutePath=" + absolutePath + ", contentHash="
				+ contentHash + ", length=" + length + ", lastModified="
				+ lastModified + "]";
	}
}
