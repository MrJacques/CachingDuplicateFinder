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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.judyandjacques.hash.ContentHash;
import com.judyandjacques.hash.HashCreator;

/**
 * Pass a list of directories or files and this will list which are duplicates.
 * For the command line switches @See {@link DuplicateFinderCommandLine}
 */
public class DuplicateFinder {

	// Return true if the file is contained in the directory
	private static boolean fileInDirectory(String directory, File file) {
		String filePath = file.getAbsolutePath();

		return filePath.startsWith(directory);
	}

	/**
	 * TODO comment
	 * 
	 * @See {@link DuplicateFinderCommandLine}
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		DuplicateFinderCommandLine cl = new DuplicateFinderCommandLine(args);

		// Where and if to save a cache file
		File cacheSaveFile = cl.getCacheSaveFile();

		// Cache files to load
		List<Path> caches = cl.getCaches();

		// List of master directories
		List<Path> masters = cl.getMasters();

		// List of test directories
		List<Path> tests = cl.getTests();

		// List of directories where files can be deleted
		List<String> deletables = cl.getDeletables();

		// List of extentions to filter for or null for all files
		String[] extensions = cl.getExtensions();

		// if true the use cache to get list of master files
		boolean useCacheForMasters = cl.isUseCacheForMasters();

		// if true the only delete duplicates that are in a master directory
		boolean mustBeInMasters = cl.isMustBeInMasters();

		boolean verbose = cl.isVerbose();
		boolean pretend = cl.isPretend();

		long startMillis = System.currentTimeMillis();

		// HashCreator is resposible for creating collections of ContentHashes
		HashCreator hashCreator = new HashCreator();

		hashCreator.setVerbose(verbose);
		if (pretend && verbose) {
			System.out.println("Running in pretend mode");
		}

		// Add extension filter
		if (extensions == null) {
			hashCreator.matchAllExtensions();
			if (verbose) {
				System.out.println("No filter on extensions");
			}
		} else {
			hashCreator.setExtensionsToMatch(extensions);
			if (verbose) {

				System.out.println("Matching the following extensions: "
						+ Arrays.toString(extensions));
			}
		}

		// Load caches
		for (Path path : caches) {
			File file = path.toFile();
			if (file.exists()) {
				try (FileInputStream fis = new FileInputStream(file);
						ObjectInputStream oInStream = new ObjectInputStream(fis)) {
					hashCreator.loadCache(oInStream);
				}
			}
		}
		if (verbose) {
			System.out.println("Initial cache size "
					+ hashCreator.getCacheSize());
		}

		// Process master directories
		Map<ContentHash, Set<File>> masterHashes;
		if (useCacheForMasters) {
			masterHashes = hashCreator.createFromCache(masters);
			if (verbose) {
				System.out.println("Loaded " + +masterHashes.size()
						+ " files for master directories from cache");
			}
		} else {
			masterHashes = hashCreator.create(masters);
			if (verbose) {
				int count = 0;
				for (Set<File> files : masterHashes.values()) {
					count += files.size();
				}
				System.out.println("Loaded " + count
						+ " files from master directories");
			}
		}

		// Process test directories
		Map<ContentHash, Set<File>> testHashes = hashCreator.create(tests);
		if (verbose) {
			int count = 0;
			for (Set<File> files : testHashes.values()) {
				count += files.size();
			}
			System.out.println("Loaded " + count
					+ " files from test directories");
		}

		// Find and display duplicates
		if (verbose) {
			for (Map.Entry<ContentHash, Set<File>> entry : testHashes
					.entrySet()) {
				ContentHash testHash = entry.getKey();
				Set<File> testFiles = entry.getValue();
				int testCount = testFiles.size();

				Set<File> masterFiles = masterHashes.get(testHash);
				int masterCount = (masterFiles == null ? 0 : masterFiles.size());

				// Must be duplicated and if mustBeInMasters is set then it must
				// be
				// in the masters
				boolean duplicate = ((testCount + masterCount) > 1)
						&& ((!mustBeInMasters) || (masterCount > 0));

				// Assert testCount > 0
				if (duplicate) {
					// The hash is in the test directory
					// The hash file is duplicated between the master and test
					// directories
					System.out.println("Duplicates found("
							+ (testCount + masterCount) + "):");
					if (masterFiles != null) {
						for (File file : masterFiles) {
							System.out.println("   (M) " + file);
						}
					}
					for (File file : testFiles) {
						System.out.println("   (T) " + file);
					}
				}
			}
		}

		// Find and delete deletable duplicates
		if (!deletables.isEmpty()) {
			List<File> toRemove = new ArrayList<>();
			for (Map.Entry<ContentHash, Set<File>> entry : testHashes
					.entrySet()) {
				ContentHash testHash = entry.getKey();
				Set<File> testFiles = entry.getValue();
				int testCount = testFiles.size();

				Set<File> masterFiles = masterHashes.get(testHash);

				// If there is a match in the masters then set the master count
				// to 1
				int masterCount = 0;
				if ((masterFiles != null) && (!masterFiles.isEmpty())) {
					masterCount = 1;
				}

				// Assert testCount > 0
				// Must be duplicated and if mustBeInMasters is set then it must
				// be
				// in the masters
				boolean duplicate = ((testCount + masterCount) > 1)
						&& ((!mustBeInMasters) || (masterCount > 0));

				if (duplicate) {
					// The hash is in the test directory
					// The hash file is duplicated between the master and test
					// directories

					Set<File> toDelete = new HashSet<>();
					File lastAdded = null;
					for (String deletable : deletables) {
						for (File file : testFiles) {
							if (fileInDirectory(deletable, file)) {
								toDelete.add(file);
								lastAdded = file;
							}
						}
					}

					// Assert testCount > 0
					if ((masterCount == 0) && (toDelete.size() == testCount)) {
						// Not in a master directory and all copies are in
						// toDelete. Remove the last added copy
						toDelete.remove(lastAdded);
						if (verbose) {
							System.out
									.println("Note: Not deleting last copy - "
											+ lastAdded);
						}
					}
					toRemove.addAll(toDelete);
				}
			}
			long bytes = 0;
			Collections.sort(toRemove);
			for (File file : toRemove) {
				bytes += file.length();
				if (verbose) {
					System.out.println("Delete \"" + file + "\"");
				}
				if (!pretend) {
					if (!file.delete()) {
						throw new IOException("Could not delete file - " + file);
					}
				}
			}
			if (!pretend) {
				hashCreator.removeFromCache(toRemove);
			}
			if (verbose) {
				System.out.println("Deleted " + toRemove.size() + " files");
				String total = NumberFormat.getNumberInstance().format(bytes);
				System.out.println("Deleted " + total + " bytes");
			}
		}

		if (cacheSaveFile != null) {
			try (FileOutputStream fileOutputStream = new FileOutputStream(
					cacheSaveFile);
					ObjectOutputStream oOutStream = new ObjectOutputStream(
							fileOutputStream)) {

				int count = hashCreator.writeCache(oOutStream);
				if (verbose) {
					System.out.println("Wrote " + count + " file hashes to "
							+ cacheSaveFile.getName());
				}
			}
		}

		if (verbose) {
			System.out.println();

			long endMillis = System.currentTimeMillis();
			System.out.println("Total time: "
					+ ((endMillis - startMillis) / 1000) + " seconds");
			System.out.println("Files Processed: "
					+ hashCreator.getFilesProcessed());
			System.out.println("Cache hits: " + hashCreator.getCacheHits());
		}
	}
}
