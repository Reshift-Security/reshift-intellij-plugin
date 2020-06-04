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

package com.reshiftsecurity.plugins.intellij.gui.common;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * Custom FileChooser Descriptor that allows the specification of a file filter.
 */
public class FilterFileChooserDescriptor extends FileChooserDescriptor {
  private final FileFilter _fileFilter;

  /**
   * Construct a file chooser descriptor for the given file filter.
   *
   * @param title       the dialog title.
   * @param description the dialog description.
   * @param filter      the file filter.
   */
  public FilterFileChooserDescriptor(final String title, final String description, final FileFilter filter) {
    // select a single file, not jar contents
    super(true, false, true, true, false, false);
    setTitle(title);
    setDescription(description);
    _fileFilter = filter;
  }

  /**
   * Construct a file chooser descriptor for directories only.
   *
   * @param title the dialog title.
   * @param description the dialog description.
   */
  public FilterFileChooserDescriptor(final String title, final String description) {
    // select a single file, not jar contents
    super(false, true, false, true, false, false);
    setTitle(title);
    setDescription(description);
    _fileFilter = new DirectoryFileFilter();
  }

  @Override
  public boolean isFileSelectable(final VirtualFile file) {
    return _fileFilter.accept(VfsUtilCore.virtualToIoFile(file));
  }

  @Override
  public boolean isFileVisible(final VirtualFile file, final boolean showHiddenFiles) {
    return file.isDirectory() || _fileFilter.accept(VfsUtilCore.virtualToIoFile(file));
  }

  /**
   * File filter that only accepts directories.
   */
  private static class DirectoryFileFilter extends FileFilter {
    public DirectoryFileFilter() {
    }

    @Override
    public boolean accept(final File file) {
      return file.isDirectory();
    }

    @Override
    public String getDescription() {
      return "directories-only";
    }
  }
}
