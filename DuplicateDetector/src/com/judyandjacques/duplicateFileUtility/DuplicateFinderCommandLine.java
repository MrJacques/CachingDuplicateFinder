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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility class to handle the command line for duplicateFinder.
 * 
 * The switches and there descriptions are defined in the CLSwitches enum.
 * 
 */
public class DuplicateFinderCommandLine {

	private File cacheSaveFile = null;
	private List<Path> caches = new LinkedList<>();
	private List<Path> masters = new LinkedList<>();
	private List<Path> tests = new LinkedList<>();
	private List<String> deletables = new LinkedList<>();

	private String[] extensions = null;
	private boolean useCacheForMasters = false;
	private boolean mustBeInMasters = false;
	private boolean verbose = false;
	private boolean pretend = false;

	private enum CLSwitches {
		NO_COMMAND(null),

		EXTENSIONS("\"ext1, ext2\" : Only process the given extensions"),

		CACHE("file.ser : Use the given file for the cache"),

		WRITECACHE("file.ser : file to write cache"),

		MASTER("directory : Defines the master directories"),

		TEST("directory : Defines the test directories"),

		DELETE("directory : delete any duplicates in this directory"),

		USECACHEFORMASTER(
				": Just use the cache for the master directories.  Don't look at the file system"),

		MUSTBEINMASTER(
				": Only mark as duplicate if it is in a master directory too"),

		PRETEND(
				": Don't actually delete anything but do show what would have been deleted"),

		VERBOSE(": Display extra information");

		private String message;

		private CLSwitches(String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}
	}

	/**
	 * Print an error message & the valipd switches.
	 * 
	 * This does not return.
	 */
	private static void usageException(String message) {
		// TODO
		System.out.println("Error: " + message);
		System.out.println();
		System.out.println("Valid parameters:");
		for (CLSwitches clSwitch : CLSwitches.values()) {
			String description = clSwitch.getMessage();
			if (description != null) {
				System.out.println("-" + clSwitch.toString().toLowerCase()
						+ " " + description);
			}
		}
		System.exit(1);
	}

	// Turn arg into CLSwitch
	private static CLSwitches getSwitch(String arg) {
		if ((arg == null) || (arg.length() == 0)) {
			usageException("Empty argument");
		}
		// Assert: Can't be null
		if (arg == null) {
			throw new IllegalArgumentException("Arg shouldn't be null");
		}
		if (!arg.startsWith("-")) {
			usageException("Was expecting a -switch");
		}
		String withoutDash = arg.substring(1);
		CLSwitches clSwitch = null;
		try {
			clSwitch = CLSwitches.valueOf(withoutDash.toUpperCase());
		} catch (IllegalArgumentException e) {
			usageException("Unknown switch " + arg);
		}
		return clSwitch;
	}

	public DuplicateFinderCommandLine(String[] args) {
		if ((args == null) || (args.length == 0)) {
			System.out
					.println("Usage: List of files or directories to search through ");
		}
		// Assert: Can't be null
		if (args == null) {
			throw new IllegalArgumentException("Args shouldn't be null");
		}

		CLSwitches lastSwitch = CLSwitches.NO_COMMAND;
		for (String arg : args) {
			if (lastSwitch == CLSwitches.NO_COMMAND) {
				// process current arg
				CLSwitches current = getSwitch(arg);
				switch (current) {
				case NO_COMMAND: {
					usageException("Unrecognized command - " + arg);
					break;
				}
				case USECACHEFORMASTER: {
					useCacheForMasters = true;
					break;
				}
				case MUSTBEINMASTER: {
					mustBeInMasters = true;
					break;
				}
				case VERBOSE: {
					verbose = true;
					break;
				}
				case PRETEND: {
					pretend = true;
					break;
				}
				default:
					lastSwitch = current;
					break;
				}
			} else {
				// handle last switch
				switch (lastSwitch) {
				case EXTENSIONS: {
					if (extensions != null) {
						usageException("Can only use -extension switch once");
					}
					extensions = arg.split("\\s*,\\s*");
					break;
				}
				case CACHE: {
					Path path = Paths.get(arg);
					File file = path.toFile();
					if (!file.exists()) {
						System.out
								.println("Warning - cache file does not exist - "
										+ path);
					}
					if (file.isDirectory()) {
						usageException("Cache is a directory - " + path);
					}
					caches.add(path);
					break;
				}
				case MASTER: {
					Path path = Paths.get(arg);
					File file = path.toFile();
					if (!file.exists()) {
						usageException("Master does not exist - " + path);
					}
					masters.add(path);
					break;
				}
				case TEST: {
					Path path = Paths.get(arg);
					File file = path.toFile();
					if (!file.exists()) {
						usageException("Test does not exist - " + path);
					}
					tests.add(path);
					break;
				}
				case DELETE: {
					File file = new File(arg);
					if (!file.exists()) {
						usageException("Delete directory does not exist - " + file);
					}
					deletables.add(file.getAbsolutePath());
					break;
				}
				case WRITECACHE: {
					cacheSaveFile = new File(arg);
					if (cacheSaveFile.exists() && (!cacheSaveFile.canWrite())) {
						usageException("Cannot write to cache save location - "
								+ cacheSaveFile);
					}
					break;
				}
				default:
					// Handle twhe impossible NO_COMMAND
					break;
				}
				lastSwitch = CLSwitches.NO_COMMAND;
			}
		}
		if (masters.isEmpty() && tests.isEmpty()) {
			usageException("No master or test directories specified");
		}

		// if useCacheForMasters then there must be a cache
		if (useCacheForMasters) {
			if (caches.isEmpty()) {
				usageException("When using -useCacheForMasters a cache must specified");
			}

			boolean found = false;
			for (Path path : caches) {
				if (path.toFile().exists()) {
					found = true;
					break;
				}
			}
			if (!found) {
				usageException("When using -useCacheForMasters a cache must exist");
			}
		}
	}

	/**
	 * @return the cache save file or null if there is none
	 */
	public File getCacheSaveFile() {
		return cacheSaveFile;
	}

	/**
	 * @return the caches
	 */
	public List<Path> getCaches() {
		return caches;
	}

	/**
	 * @return the master directories
	 */
	public List<Path> getMasters() {
		return masters;
	}

	/**
	 * @return the test directories
	 */
	public List<Path> getTests() {
		return tests;
	}

	/**
	 * @return the directories where files can be deleted
	 */
	public List<String> getDeletables() {
		return deletables;
	}

	/**
	 * @return the extensions that should be matched or null for everything
	 */
	public String[] getExtensions() {
		return extensions;
	}

	/**
	 * @return the useCacheForMasters
	 */
	public boolean isUseCacheForMasters() {
		return useCacheForMasters;
	}

	/**
	 * @return true if mustBeInMasters
	 */
	public boolean isMustBeInMasters() {
		return mustBeInMasters;
	}

	/**
	 * @return true if verbose
	 */
	public boolean isVerbose() {
		return verbose;
	}

	/**
	 * @return true if pretend
	 */
	public boolean isPretend() {
		return pretend;
	}

}
