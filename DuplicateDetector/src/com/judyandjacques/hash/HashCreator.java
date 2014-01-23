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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Create content hashes and manage the caches
 */
public class HashCreator {

	// if false then throw an exception if a file does not exist
	private boolean ignoreFileNotFound = false;

	// Cached hashes
	private final Map<String, FileHash> cache = new HashMap<>();

	private String extensionsToMatch = null;

	// Cache has been modified
	private boolean cacheModified = false;
	
	// Print out directories being processed
	private boolean verbose = false;

	// Default to MD5, can override
	private String hashAlgorithm = "MD5";

	// Count of files processed
	private int filesProcessed = 0;

	// Count how many files were found in the cache
	private int cacheHits = 0;

	/**
	 * Filter files to only accept those that have the correct extension
	 */
	private final DirectoryStream.Filter<Path> extensionFilter = new DirectoryStream.Filter<Path>() {
		// Accept the file if there are no extensionsToMatch or if the files
		// extension is in the extensionsToMatch

		@SuppressWarnings("synthetic-access")
		@Override
		public boolean accept(Path path) throws IOException {
			boolean accept = true;
			if (extensionsToMatch != null) {
				File file = path.toFile();

				// Always accept directories
				if (!file.isDirectory()) {
					String name = path.toFile().getName();
					if (name != null) {
						int position = name.lastIndexOf(".");
						String extension = (position == -1 ? null : name
								.substring(position + 1));

						accept = ((extension != null) && extensionsToMatch
								.contains(extension.toUpperCase()));
					}
				}
			}
			return accept;
		}
	};

	/**
	 * Add the contents of the directory to the toProcess list. Return the
	 * number of items added.
	 */
	private void addDirectoryToProcess(LinkedList<Path> toProcess, Path path)
			throws HashException {

		// Add the directory's contents to toProcess
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path,
				extensionFilter)) {

			for (Path item : stream) {
				if (item.toFile().isDirectory()) {
					// Put directories at the end
					toProcess.addLast(item);
				} else {
					// put files at the beginning
					toProcess.addFirst(item);
				}
			}
		} catch (IOException | DirectoryIteratorException e) {
			throw new HashException(path.toFile(), e);
		}
	}

	/**
	 * Process the toSearch paths and return content hashes and matching files.
	 * 
	 * @param toSearch
	 *            collection of paths to search
	 * @return map of content hashes and the associated file or files
	 * @throws HashException
	 *             if a file is not found or there is an error iterating over a
	 *             directory
	 */
	public Map<ContentHash, Set<File>> create(Collection<Path> toSearch)
			throws HashException {

		if (toSearch == null) {
			throw new IllegalArgumentException("toSearch cannot be null");
		}

		// Process each toSearch directory
		Map<File, ContentHash> fileHashes = new HashMap<>();
		for (Path path : toSearch) {
			Map<File, ContentHash> pathHashes = create(path);
			fileHashes.putAll(pathHashes);
		}

		// Put results into Map with content hash as the key
		Map<ContentHash, Set<File>> hashes = new HashMap<>();
		for (Map.Entry<File, ContentHash> entry : fileHashes.entrySet()) {
			File file = entry.getKey();
			ContentHash contentHash = entry.getValue();

			// Add to hashes
			Set<File> files = hashes.get(contentHash);
			if (files == null) {
				files = new HashSet<>();
				hashes.put(contentHash, files);
			}
			files.add(file);
		}
		return hashes;
	}

	/**
	 * Process the toSearch path and return content hashes for matching files.
	 * 
	 * @param toSearch
	 *            collection of paths to search
	 * @param verbose print out names of directories processed
	 * @return map of content hashes and the associated file or files
	 * @throws HashException
	 *             if a file is not found or there is an error iterating over a
	 *             directory
	 */
	public Map<File, ContentHash> create(Path toSearch) throws HashException {

		if (toSearch == null) {
			throw new IllegalArgumentException("toSearch cannot be null");
		}

		MessageDigest md;
		try {
			md = MessageDigest.getInstance(hashAlgorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new HashException(e);
		}

		// LinkedList of paths (both directories and files) to be processed.
		LinkedList<Path> toProcess = new LinkedList<>();
		toProcess.add(toSearch);

		Map<File, ContentHash> hashes = new HashMap<>();

		// The file system may contain many levels of directories so it not wise
		// to use recursion
		while (!toProcess.isEmpty()) {
			Path path = toProcess.removeFirst();

			File file = path.toFile();

			if (!file.exists()) {
				if (!ignoreFileNotFound) {
					throw new HashException(file, "File does not exist - "
							+ file);
				}
			} else if (file.isDirectory()) {
				if (verbose) {
					System.out.println("Processing directory - "
							+ filesProcessed + "/" + toProcess.size() + " - "
							+ file);
				}
				addDirectoryToProcess(toProcess, path);
			} else {
				// System.out.println("Processing file - " + filesProcessed +
				// "/" + toProcess.size() + " - " + file);

				String absolutePath = file.getAbsolutePath();
				FileHash cachedHash = cache.get(absolutePath);
				ContentHash hash;

				// Note: Assumes the file still exists at this point (which
				// should be a pretty safe bet). It was checked above.

				// If there is a valid cached hash then use it
				if ((cachedHash != null) && cachedHash.isValidForFile(file)) {
					hash = cachedHash.getContentHash();
					cacheHits++;
				} else {
					try {
						hash = new ContentHash(file, md);

						// Add (or replace) cache entry
						FileHash fileHash = new FileHash(file, hash);
						cache.put(file.getAbsolutePath(), fileHash);
						cacheModified = true;
					} catch (IOException e) {
						throw new HashException(file,
								"Could not create content hash for file - "
										+ file);
					}
				}
				hashes.put(file, hash);
				filesProcessed++;
			}
		}
		return hashes;
	}

	/**
	 * Process the master directories using the current cache and return a map
	 * of content hashes.
	 * 
	 * @param masters
	 *            directories to process
	 * @return map of content hashes and the associated files
	 */
	public Map<ContentHash, Set<File>> createFromCache(Collection<Path> masters) {
		Map<ContentHash, Set<File>> hashes = new HashMap<>();

		if (masters == null) {
			throw new IllegalArgumentException("Masters cannot be null");
		}

		// To the Collection<Path> into a Collection<String>
		// using the absolute path
		Collection<String> masterPaths = new LinkedList<>();
		for (Path path : masters) {
			String absolutePath = path.toFile().getAbsolutePath();

			// Add the trailing / so it doesn't match things it shouldn't
			masterPaths.add(absolutePath + File.separator);
		}

		for (Map.Entry<String, FileHash> entry : cache.entrySet()) {
			String absolutePath = entry.getKey();
			FileHash fileHash = entry.getValue();

			for (String path : masterPaths) {
				// Is this item from the cache in the path?
				if (absolutePath.startsWith(path)) {
					ContentHash contentHash = fileHash.getContentHash();
					Set<File> files = hashes.get(contentHash);
					if (files == null) {
						files = new HashSet<>();
						hashes.put(contentHash, files);
					}
					files.add(new File(absolutePath));

					// No need to check any more
					break;
				}
			}
		}
		return hashes;
	}

	/**
	 * Load the cache from the objectInputStream. The cache should have been
	 * written with the corresponding writeCache method.
	 * 
	 * @param objectInputStream
	 *            to read from
	 */
	public void loadCache(ObjectInputStream objectInputStream)
			throws HashException, IOException {
		String savedHashAlgorithm = objectInputStream.readUTF();

		if (savedHashAlgorithm == null) {
			throw new HashException(
					"The hash algorithm is null.  Stream is malformed");
		}

		int cacheSize = objectInputStream.readInt();
		if (cacheSize < 0) {
			throw new HashException("Caches in stream = " + cacheSize
					+ ".  Stream is malformed");
		}

		if (cacheSize > 0) {
			for (int i = 0; i < cacheSize; i++) {
				FileHash hash;
				try {
					hash = (FileHash) objectInputStream.readObject();
				} catch (ClassNotFoundException e) {
					throw new HashException("Stream is malformed", e);
				}
				cache.put(hash.getAbsolutePath(), hash);
			}
		}
	}

	/**
	 * Write the caches so they can be read back using the loadCache
	 * 
	 * @param objectOutputStream
	 *            to write to
	 * @return number of items in the cache
	 */
	public int writeCache(ObjectOutputStream objectOutputStream)
			throws IOException {

		objectOutputStream.writeUTF(hashAlgorithm);
		objectOutputStream.writeInt(cache.size());
		for (FileHash fileHash : cache.values()) {
			objectOutputStream.writeObject(fileHash);
		}
		return cache.size();
	}

	/**
	 * Set the extensions to match
	 * 
	 * @param extensions
	 *            to match, can be null to match all
	 */
	public void setExtensionsToMatch(String[] extensions) {
		if ((extensions == null) || (extensions.length == 0)) {
			extensionsToMatch = null;
		} else {
			boolean first = true;
			StringBuffer newExtensions = new StringBuffer();

			for (String ext : extensions) {
				if (ext == null) {
					throw new IllegalArgumentException(
							"Individual extensions cannot be null");
				}
				if (ext.length() == 0) {
					throw new IllegalArgumentException(
							"Individual extensions cannot be empty (Not supported)");
				}
				if (ext.contains(".")) {
					throw new IllegalArgumentException(
							"Individual extensions cannot contain periods (.)");
				}
				if (!first) {
					newExtensions.append(".");
				}
				first = false;
				newExtensions.append(ext.trim().toUpperCase());
			}
			extensionsToMatch = newExtensions.toString();
		}
	}

	/**
	 * @return array of the extension to match
	 */
	public String[] getExtensionsToMatch() {
		String[] array;
		if (extensionsToMatch == null) {
			array = new String[0];
		} else {
			array = extensionsToMatch.split("\\.");
		}
		return array;
	}

	/**
	 * Match all extensions
	 */
	public void matchAllExtensions() {
		extensionsToMatch = null;
	}

	/**
	 * @return the hashAlgorithm
	 */
	public String getHashAlgorithm() {
		return hashAlgorithm;
	}

	/**
	 * @param hashAlgorithm
	 *            the hashAlgorithm to set
	 */
	public void setHashAlgorithm(String hashAlgorithm) {
		if (hashAlgorithm == null) {
			throw new IllegalArgumentException("hashAlgorithm cannot be null");
		}
		this.hashAlgorithm = hashAlgorithm;
	}

	/**
	 * @return the filesProcessed
	 */
	public int getFilesProcessed() {
		return filesProcessed;
	}

	/**
	 * @return the cacheHits
	 */
	public int getCacheHits() {
		return cacheHits;
	}

	/**
	 * @return the cache size
	 */
	public int getCacheSize() {
		return cache.size();
	}

	/**
	 * @return the cacheModified
	 */
	public boolean isCacheModified() {
		return cacheModified;
	}

	/**
	 * @return the verbose
	 */
	public boolean isVerbose() {
		return verbose;
	}

	/**
	 * @param verbose the verbose to set
	 */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	// TODO
	public int removeFromCache(List<File> toRemove) {
		if (toRemove == null) {
			throw new IllegalArgumentException("toRemove cannot be null");
		}
		int removed = 0;
		for (File file : toRemove) {
			String absolutePath = file.getAbsolutePath();
		    FileHash removedHash = cache.remove(absolutePath);
		    if (removedHash != null) {
		    	removed++;
		    }
		}
		return removed;
	}
}
