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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ContentHashTest {
	@Rule
	public ExpectedException exception = ExpectedException.none();

	@SuppressWarnings("static-method")
	@Test
	public final void testContentHash() throws IOException, NoSuchAlgorithmException {
		MessageDigest messageDigest = MessageDigest.getInstance("md5");

		File file1 = new File("test data\\Master\\IMG_0794.JPG");
		ContentHash contentHash1 = new ContentHash(file1, messageDigest);

		File file2 = new File("test data\\Master\\IMG_0795.JPG");
		ContentHash contentHash2 = new ContentHash(file2, messageDigest);

		assertEquals(
				contentHash1.toString(),
				"ContentHash [digest=[-61, -101, 126, -66, 94, -67, -41, -128, -18, 69, -2, 10, -85, -31, -43, -21]]");
		assertEquals(
				contentHash2.toString(),
				"ContentHash [digest=[108, -68, -104, -128, 45, 79, -71, -116, 14, 36, 53, -103, 16, 108, -84, -20]]");

		assertNotEquals(contentHash1, contentHash2);
	}

	@SuppressWarnings("unused")
	@Test
	public final void testContentHashNullMD() throws IOException {
		MessageDigest messageDigest = null;
		File file1 = new File("test data\\Master\\IMG_0794.JPG");

		exception.expect(IllegalArgumentException.class);
		new ContentHash(file1, messageDigest);
	}

	@SuppressWarnings("unused")
	@Test
	public final void testContentHashNullFile() throws Exception {
		MessageDigest messageDigest = MessageDigest.getInstance("md5");
		File file1 = null;

		exception.expect(IllegalArgumentException.class);
		new ContentHash(file1, messageDigest);
	}

	@SuppressWarnings("unused")
	@Test
	public final void testContentHashBadFile() throws IOException, NoSuchAlgorithmException {
		MessageDigest messageDigest = MessageDigest.getInstance("md5");
		File file1 = new File("test data\\Master\\Does Not Exist");

		exception.expect(FileNotFoundException.class);
		new ContentHash(file1, messageDigest);
	}
}
