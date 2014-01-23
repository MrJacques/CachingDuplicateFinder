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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FileHashTest {
	@Rule
	public ExpectedException exception = ExpectedException.none();

	// Deletes the created file on jvm exit
	private static File createTestDataFile(String name, String contents)
			throws IOException {
		
		File file = new File("test data\\junit", name);
		try (PrintWriter pOut = new PrintWriter(file);) {
			pOut.println(contents);
		}
		
		file.deleteOnExit();
		
		return file;
	}

	@SuppressWarnings("static-method")
	@Test
	public final void testHashCode() throws InterruptedException, IOException,
			NoSuchAlgorithmException {
		File one = createTestDataFile("one", "A");
		Thread.sleep(1000);
		File two = createTestDataFile("two", "A");
		File three = createTestDataFile("three", "B");

		MessageDigest messageDigest = MessageDigest.getInstance("md5");
		ContentHash chOne = new ContentHash(one, messageDigest);
		ContentHash chTwo = new ContentHash(two, messageDigest);
		ContentHash chThree = new ContentHash(three, messageDigest);

		assertEquals(chOne, chTwo);
		assertNotEquals(chOne, chThree);

		FileHash fhOne = new FileHash(one, chOne);
		FileHash fhTwo = new FileHash(two, chTwo);
		FileHash fhThree = new FileHash(three, chThree);

		assertNotEquals(fhOne, fhTwo);
		assertNotEquals(fhOne, fhThree);
		assertNotEquals(fhTwo, fhThree);
		assertTrue(fhOne.isValidForFile(one));
		assertFalse(fhOne.isValidForFile(two));
		assertFalse(fhOne.equals(fhTwo));
		assertTrue(fhOne.equals(fhOne));

		assertTrue(fhOne.isValidForFile(one));
		assertFalse(fhOne.isValidForFile(two));

		assertEquals(one.length(), fhOne.getLength());
		Date date = new Date(one.lastModified());
		assertEquals(date, fhOne.getLastModified());
		assertEquals(chOne, fhOne.getContentHash());
		
		assertEquals(one.getAbsolutePath(), fhOne.getAbsolutePath());
	}
	
	@SuppressWarnings("unused")
	@Test
	public final void testFileHashNullFile() throws Exception  {
		MessageDigest messageDigest = MessageDigest.getInstance("md5");
		File file = createTestDataFile("testFileHashNullFile", "A");
		ContentHash hash = new ContentHash(file, messageDigest);

		exception.expect(IllegalArgumentException.class);
		new FileHash(null, hash);
	}
	
	@SuppressWarnings("unused")
	@Test
	public final void testFileHashNullHash() throws Exception  {
		File file = createTestDataFile("testFileHashNullHash", "A");

		exception.expect(IllegalArgumentException.class);
		new FileHash(file, null);
	}

	@SuppressWarnings("unused")
	@Test
	public final void testFileHashBadFile() throws Exception  {
		MessageDigest messageDigest = MessageDigest.getInstance("md5");
		File file = createTestDataFile("testFileHashBadFile", "A");
		ContentHash hash = new ContentHash(file, messageDigest);
		
		File bad = new File("Does not exist");

		exception.expect(IOException.class);
		new FileHash(bad, hash);
	}
	

	@Test
	public final void testValidhBadFile() throws Exception  {
		MessageDigest messageDigest = MessageDigest.getInstance("md5");
		File file = createTestDataFile("testValidhBadFile", "A");
		ContentHash hash = new ContentHash(file, messageDigest);
		FileHash fileHash = new FileHash(file, hash);
		
		exception.expect(IllegalArgumentException.class);
		fileHash.isValidForFile(null);
	}
}
