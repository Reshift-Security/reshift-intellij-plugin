/*
 * Copyright 2020 SpotBugs plugin contributors
 *
 * This file is part of IntelliJ SpotBugs plugin.
 *
 * IntelliJ SpotBugs plugin is free software: you can redistribute it 
 * and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of 
 * the License, or (at your option) any later version.
 *
 * IntelliJ SpotBugs plugin is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with IntelliJ SpotBugs plugin.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.reshiftsecurity.plugins.intellij.collectors;


import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import com.reshiftsecurity.plugins.intellij.common.util.IdeaUtilImpl;
import com.reshiftsecurity.plugins.intellij.core.FindBugsProject;
import com.reshiftsecurity.plugins.intellij.core.FindBugsState;

import java.io.File;
import java.io.FileFilter;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
public final class RecurseFileCollector implements FileFilter {

	private final Project _project;
	private final ProgressIndicator _indicator;
	private final FindBugsProject _findBugsProject;
	private final int[] _count;


	private RecurseFileCollector(
			@NotNull final Project project,
			@NotNull final ProgressIndicator indicator,
			@NotNull final FindBugsProject findBugsProject,
			@NotNull int[] count
	) {
		_project = project;
		_indicator = indicator;
		_findBugsProject = findBugsProject;
		_count = count;
	}

	@Override
	public boolean accept(final File path) {
		if (path.isDirectory()) {
			if (_indicator.isCanceled() || FindBugsState.get(_project).isAborting()) {
				throw new ProcessCanceledException();
			}
			//noinspection ResultOfMethodCallIgnored
			path.listFiles(this);
		} else {
			// add the classes to the list of files to be analysed
			final FileType type = IdeaUtilImpl.getFileTypeByName(path.getName());
			if (IdeaUtilImpl.isValidFileType(type)) {
				final String filePath = path.getAbsolutePath();
				_indicator.setText2("Files collected: " + ++_count[0]);
				_findBugsProject.addFile(filePath);
			}
		}
		return false;
	}


	public static void addFiles(
			@NotNull final Project project,
			@NotNull final ProgressIndicator indicator,
			@NotNull final FindBugsProject findBugsProject,
			@NotNull final File classesDir,
			@NotNull final int[] count
	) {
		if (classesDir.isDirectory()) {
			//noinspection ResultOfMethodCallIgnored
			classesDir.listFiles(new RecurseFileCollector(project, indicator, findBugsProject, count));
		}
	}
}