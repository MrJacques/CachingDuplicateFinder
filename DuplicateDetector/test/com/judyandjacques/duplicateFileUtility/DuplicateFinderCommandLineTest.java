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
package com.judyandjacques.duplicateFileUtility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DuplicateFinderCommandLineTest {

	private static final String PARENT_PATH = "test data\\junit\\";
	private static final String DELETABLE = "deletable";
	private static final String TESTS = "test";
	private static final String MASTER = "master";
	private static final String CACHE_FILE = "cache.ser";
	private static final String CACHE_SAVE_FILE = "savecache.ser";

	private static final String[] TEST_ARGS = new String[] { "-extensions",
			"jpg , gif", "-cache", PARENT_PATH + CACHE_FILE,
			"-usecacheformaster", "-mustbeinmaster", "-writecache",
			CACHE_SAVE_FILE, "-verbose", "-pretend", "-master",
			PARENT_PATH + MASTER, "-test", PARENT_PATH + TESTS, "-delete",
			PARENT_PATH + DELETABLE };

	private DuplicateFinderCommandLine dfcl = null;

	// Deletes the created directory
	private static File createTestDataDirectory(String dir) {
		File file = new File(PARENT_PATH, dir);
		if (file.mkdir()) {
			file.deleteOnExit();
		}
		return file;
	}

	// Deletes the created file on jvm exit
	private static File createTestDataFile(String name, String contents)
			throws IOException {

		File file = new File(PARENT_PATH, name);
		try (PrintWriter pOut = new PrintWriter(file);) {
			pOut.println(contents);
		}
		file.deleteOnExit();
		return file;
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		createTestDataDirectory(DELETABLE);
		createTestDataDirectory(TESTS);
		createTestDataDirectory(MASTER);
		createTestDataFile(CACHE_FILE, "cache");
	}

	@Before
	public void setUp() throws Exception {
		dfcl = new DuplicateFinderCommandLine(TEST_ARGS);
	}

	@Test
	public final void testGetCacheSaveFile() {
		File file = dfcl.getCacheSaveFile();
		assertEquals(CACHE_SAVE_FILE, file.getName());
	}

	@Test
	public final void testGetCaches() {
		List<Path> caches = dfcl.getCaches();
		assertEquals(1, caches.size());
		assertEquals(CACHE_FILE, caches.get(0).toFile().getName());
	}

	@Test
	public final void testGetMasters() {
		List<Path> masters = dfcl.getMasters();
		assertEquals(1, masters.size());
		assertEquals(MASTER, masters.get(0).toFile().getName());
	}

	@Test
	public final void testGetTests() {
		List<Path> tests = dfcl.getTests();
		assertEquals(1, tests.size());
		assertEquals(TESTS, tests.get(0).toFile().getName());
	}

	@Test
	public final void testGetDeletables() {
		List<String> deletables = dfcl.getDeletables();
		assertEquals(1, deletables.size());
		File file = new File(deletables.get(0));
		assertEquals(DELETABLE, file.getName());
	}

	@Test
	public final void testGetExtensions() {
		String[] extensions = dfcl.getExtensions();
		assertEquals(2, extensions.length);
		assertEquals("jpg", extensions[0]);
		assertEquals("gif", extensions[01]);
	}

	@Test
	public final void testIsUseCacheForMasters() {
		assertTrue(dfcl.isUseCacheForMasters());
	}

	@Test
	public final void testIsMustBeInMasters() {
		assertTrue(dfcl.isMustBeInMasters());
	}

	@Test
	public final void testIsVerbose() {
		assertTrue(dfcl.isVerbose());
	}

	@Test
	public final void testIsPretend() {
		assertTrue(dfcl.isPretend());
	}
}
