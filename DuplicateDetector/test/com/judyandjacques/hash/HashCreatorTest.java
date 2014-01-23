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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class HashCreatorTest {
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

	// Deletes the created directory
	private static File createTestDataDirectory(String dir) {
		File file = new File("test data\\junit", dir);
		if (file.mkdir()) {
			file.deleteOnExit();
		}
		return file;
	}

	private static List<String> hashesToFileNames(
			Map<ContentHash, Set<File>> hashes) {
		List<String> files = new ArrayList<>();
		for (Set<File> returnedFiles : hashes.values()) {
			for (File returnedFile : returnedFiles) {
				files.add(returnedFile.getName());
			}
		}
		Collections.sort(files);
		return files;
	}

	@SuppressWarnings("static-method")
	@Test
	public final void testCreatePath() throws IOException, HashException {
		File dir = createTestDataDirectory("creator");
		createTestDataDirectory("creator\\sub");
		createTestDataFile("creator\\one.1", "X");
		createTestDataFile("creator\\two.2", "X");
		createTestDataFile("creator\\three.1", "Y");
		createTestDataFile("creator\\sub\\four.4", "Y");

		HashCreator creator = new HashCreator();
		creator.setVerbose(false);
		creator.setExtensionsToMatch(new String[] { "1", "2" });
		creator.setHashAlgorithm("MD5");

		Collection<Path> toSearch = new LinkedList<>();
		toSearch.add(dir.toPath());
		Map<ContentHash, Set<File>> hashes = creator.create(toSearch);

		List<String> files = hashesToFileNames(hashes);

		String expected = "[one.1, three.1, two.2]";
		assertEquals(files.toString(), expected);

		assertEquals(0, creator.getCacheHits());
		assertEquals(3, creator.getCacheSize());
		assertEquals("[1, 2]", Arrays.toString(creator.getExtensionsToMatch()));
		assertEquals("MD5", creator.getHashAlgorithm());
		assertEquals(3, creator.getFilesProcessed());
		assertTrue(creator.isCacheModified());
		assertFalse(creator.isVerbose());

		// Do it again
		hashes = creator.create(toSearch);

		files = hashesToFileNames(hashes);
		expected = "[one.1, three.1, two.2]";
		assertEquals(files.toString(), expected);

		assertEquals(3, creator.getCacheHits());
		assertEquals(3, creator.getCacheSize());
		assertEquals("[1, 2]", Arrays.toString(creator.getExtensionsToMatch()));
		assertEquals("MD5", creator.getHashAlgorithm());
		assertEquals(6, creator.getFilesProcessed());
		assertTrue(creator.isCacheModified());
		assertFalse(creator.isVerbose());

		try (ByteArrayOutputStream bOut = new ByteArrayOutputStream();
				ObjectOutputStream oStream = new ObjectOutputStream(bOut)) {

			creator.writeCache(oStream);
			HashCreator creatorCache = new HashCreator();

			try (ByteArrayInputStream bInput = new ByteArrayInputStream(
					bOut.toByteArray());
					ObjectInputStream iStream = new ObjectInputStream(bInput)) {

				creatorCache.loadCache(iStream);
			}
			Map<ContentHash, Set<File>> hashesFromCache = creatorCache
					.createFromCache(toSearch);
			assertEquals(hashesFromCache.keySet(), hashes.keySet());
		}

		creator.matchAllExtensions();
		Map<ContentHash, Set<File>> hashesAll = creator.create(toSearch);

		files = hashesToFileNames(hashesAll);
		expected = "[four.4, one.1, three.1, two.2]";
		assertEquals(files.toString(), expected);

		assertEquals(0, creator.getExtensionsToMatch().length);

	}

}
