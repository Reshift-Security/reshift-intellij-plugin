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

package com.reshiftsecurity.plugins.intellij.common.util;

import com.intellij.openapi.vfs.VirtualFile;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class SourceCodeUtil {
    public static String getLine(Path fileAbsolutePath, int line) {
        if (line <= 0) {
            return "";
        }

        try (Stream<String> lines = Files.lines(fileAbsolutePath)) {
            return lines.skip(line - 1).findFirst().get();
        } catch (Exception e) {
            return "";
        }
    }

    public static String getLine(VirtualFile[] sourcePathFiles, String filePath, int line) {
        for (VirtualFile file : sourcePathFiles) {
            Path fileAbsolutePath = Paths.get(file.getPath(), filePath);
            if (sourceFileExists(fileAbsolutePath)) {
                return getLine(fileAbsolutePath, line);
            }
        }
        return "";
    }

    public static String getLine(VirtualFile[] sourcePathFiles, SourceLineAnnotation lineAnnotation) {
        return getLine(sourcePathFiles, getPathFromSourceLineAnnotation(lineAnnotation).toString(), lineAnnotation.getStartLine());
    }

    public static String getTrimmedSourceLine(VirtualFile[] sourcePathFiles, SourceLineAnnotation lineAnnotation) {
        return StringUtils.deleteWhitespace(getLine(sourcePathFiles, lineAnnotation));
    }

    public static boolean sourceFileExists(String filePath) {
        return sourceFileExists(Path.of(filePath));
    }

    public static boolean sourceFileExists(Path filePath) {
        return Files.exists(filePath);
    }

    public static Path getPathFromSourceLineAnnotation(SourceLineAnnotation annotation) {
        String packageName = annotation.getPackageName();
        String separator = File.separator;
        if(separator.equals("\\")){ //Edge case needed here due to how backslashes operate and the fact that Windows uses them
            separator = "\\\\";
        }
        String packagePath = packageName.trim().replaceAll("[.]", separator);
        String fileName = annotation.getSourceFile();
        return Paths.get(packagePath, fileName);
    }
}
