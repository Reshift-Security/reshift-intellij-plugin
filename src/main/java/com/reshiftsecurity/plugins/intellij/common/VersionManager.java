/*
 * Copyright 2020 Reshift Security Intellij plugin contributors
 *
 * This file is part of Reshift Security Intellij plugin.
 *
 * Reshift Security Intellij plugin is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Reshift Security Intellij plugin is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Reshift Security Intellij plugin.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.reshiftsecurity.plugins.intellij.common;

import com.reshiftsecurity.plugins.intellij.common.util.IoUtil;

import java.io.*;
import java.nio.charset.*;
import java.util.Properties;


/**
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 */
@SuppressWarnings({"HardCodedStringLiteral", "UseOfSystemOutOrSystemErr", "StringConcatenation", "CallToPrintStackTrace", "CallToPrintStackTrace"})
public class VersionManager {

	private static final long _major;
	private static final long _minor;
	private static final long _build;

	private static final String PROPERTIES_FILE = "version.properties";

	static {
		Properties properties = new Properties();
		try (InputStream stream = VersionManager.class.getResourceAsStream(PROPERTIES_FILE)) {
			properties.load(stream);
		} catch (IOException e) {
			throw new RuntimeException("Unable to read '"+PROPERTIES_FILE+"': build corrupted", e);
		}
		String version = properties.getProperty("version");
		if (version == null) {
			throw new RuntimeException("Unable to read version from '"+PROPERTIES_FILE+"': build corrupted");
		}
		String[] components = version.split("\\.");
		if (components.length != 3) {
			throw new RuntimeException("Invalid version: "+version);
		}
		try {
			_major = Long.parseLong(components[0]);
			_minor = Long.parseLong(components[1]);
			_build = Long.parseLong(components[2]);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Invalid version: "+version);
		}
	}

	private static final String NAME = FindBugsPluginConstants.PLUGIN_NAME;
	
	private static final String WEBSITE = "https://reshiftsecurity.com/";

	private static final String DOWNLOAD_WEBSITE = "https://plugins.jetbrains.com/";

	private static final String ISSUE_TRACKER = WEBSITE + "issues";

	private static final long REVISION;

	private static final String FULL_VERSION;

	private static final String MAJOR_MINOR_BUILD = _major + "." + _minor + '.' + _build;


	static {
		final String revisionString = VersionManager.class.getPackage().getImplementationVersion();
		long parsedRevision = -1;
		if (revisionString != null) {
			try {
				parsedRevision = Long.parseLong(revisionString);
				System.out.println("Revision: " + revisionString);
				System.out.println("parsedRevision: " + parsedRevision);
			} catch (final RuntimeException ignore) {
			}
		}
		REVISION = parsedRevision;
		FULL_VERSION = NAME + ' ' + MAJOR_MINOR_BUILD;
	}


	/**
	 * @return version number, e.g. "1.0.0"
	 */
	public static String getVersion() {
		return MAJOR_MINOR_BUILD;
	}


	public static String getFullVersion() {
		return FULL_VERSION;
	}


	public static long getRevision() {
		return REVISION;
	}


	public static String getName() {
		return NAME;
	}


	public static String getWebsite() {
		return WEBSITE;
	}


	public static String getDownloadWebsite() {
		return DOWNLOAD_WEBSITE;
	}


	public static String getIssueTracker() {
		return ISSUE_TRACKER;
	}


	@SuppressWarnings({"UseOfSystemOutOrSystemErr"})
	public static void main(final String[] args) {
		if (args.length == 1) {
			final File file = new File(args[0]);
			System.out.println("version string file: " + args[0]);
			OutputStreamWriter writer = null;
			try {
				//noinspection IOResourceOpenedButNotSafelyClosed
				writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
				writer.write(getVersion());
				writer.flush();
			} catch (final IOException e) {
				e.printStackTrace();
			} finally {
				IoUtil.safeClose(writer);
			}

		}
		System.out.println(getVersion());
	}
}
