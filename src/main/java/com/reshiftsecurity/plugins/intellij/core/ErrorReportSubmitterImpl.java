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
package com.reshiftsecurity.plugins.intellij.core;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.diagnostic.*;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.progress.*;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Consumer;
import com.reshiftsecurity.plugins.intellij.common.PluginConstants;
import com.reshiftsecurity.plugins.intellij.common.util.ErrorUtil;
import com.reshiftsecurity.plugins.intellij.common.util.FindBugsUtil;
import com.reshiftsecurity.plugins.intellij.common.util.New;
import org.jetbrains.annotations.*;
import com.reshiftsecurity.plugins.intellij.actions.HelpAction;
import com.reshiftsecurity.plugins.intellij.common.VersionManager;
import com.reshiftsecurity.plugins.intellij.resources.ResourcesLoader;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public final class ErrorReportSubmitterImpl extends ErrorReportSubmitter {

	private static final Logger LOGGER = Logger.getInstance(ErrorReportSubmitterImpl.class);

	@Override
	public String getReportActionText() {
		return StringUtil.capitalizeWords(ResourcesLoader.getString("error.submitReport.title"), true);
	}

	@Override
	public boolean submit(@NotNull IdeaLoggingEvent[] events, @Nullable String additionalInfo, @NotNull Component parentComponent, @NotNull Consumer<SubmittedReportInfo> consumer) {

		final AtomicReference<String> latestVersionRef = New.atomicRef(null);
		new Task.Modal(null, ResourcesLoader.getString("error.submitReport.checkVersion"), false) {
			@Override
			public void run(@NotNull final ProgressIndicator indicator) {
				try {
					latestVersionRef.set(PluginVersionChecker.getLatestVersion(indicator));
				} catch (final Exception e) {
					LOGGER.warn(e);
				}
			}
		}.queue();

		String latestVersion = latestVersionRef.get();
		if (!StringUtil.isEmptyOrSpaces(latestVersion)) {
			if (latestVersion.startsWith("v")) {
				latestVersion = latestVersion.substring(1);
				final String current = VersionManager.getVersion();
				if (!latestVersion.equals(current)) {
					final int answer = Messages.showYesNoDialog(
							ResourcesLoader.getString("error.submitReport.old.text", current, latestVersion),
							StringUtil.capitalizeWords(ResourcesLoader.getString("error.submitReport.old.title"), true),
							Messages.getQuestionIcon()
					);
					if (answer == Messages.NO) {
						return false;
					}
				}
			}
		}

		final SubmittedReportInfo reportInfo = submitImpl(events, additionalInfo, parentComponent);
		consumer.consume(reportInfo);
		return true;
	}

	@NotNull
	private SubmittedReportInfo submitImpl(
			@NotNull final IdeaLoggingEvent[] events,
			@Nullable final String additionalInfo,
			@NotNull final Component parentComponent
	) {

		final StringBuilder body = HelpAction.createProductInfo();
		if (!StringUtil.isEmptyOrSpaces(additionalInfo)) {
			body.append("\n\nAdditional Infos\n");
			body.append(additionalInfo);
		}

		boolean isFindBugsError = false;
		final Set<Error> errors = New.set();
		for (final IdeaLoggingEvent event : events) {
			final Error error = createError(event);
			if (error != null) {
				errors.add(error);
				if (!isFindBugsError && error.throwable != null) {
					isFindBugsError = FindBugsUtil.isFindBugsError(error.throwable);
				}
			}
		}
		if (errors.isEmpty()) {
			throw new IllegalStateException("No error to report");
		}
		final List<Error> sorted = new ArrayList<>(errors);
		Collections.sort(sorted);

		for (final Error error : sorted) {
			body.append("\n\n").append(error.fullError);
		}

		final String title = makeTitle(sorted.get(0));

		return openBrowser(
				isFindBugsError,
				title,
				body.toString(),
				parentComponent
		);
	}

	@NotNull
	private SubmittedReportInfo openBrowser(
			final boolean isFindBugsError,
			@NotNull final String title,
			@NotNull final String errorText,
			@SuppressWarnings("UnusedParameters") @NotNull final Component parentComponent
	) {

		final String baseUrl;
		if (isFindBugsError) {
			baseUrl = "https://github.com/spotbugs/spotbugs/issues/";
			final String body = "The error was copied to the clipboard. Press " + (SystemInfo.isMac ? "Command+V" : "Ctrl+V");
			final String newIssueUrl = baseUrl + "/new?title=" + encode(title) + "&body=" + encode(body);

			/*
			 * Note: set errorText as body does not work:
			 *   - can cause HTTP 414 Request URI too long
			 *   - if user is not yet logged in github login page will show an error
			 *     502 - "This page is taking way too long to load." (this will also occure with HTTP POST).
			 */
			CopyPasteManager.getInstance().setContents(new StringSelection(errorText));
			BrowserUtil.browse(newIssueUrl);
			return new SubmittedReportInfo(baseUrl, "issue", SubmittedReportInfo.SubmissionStatus.NEW_ISSUE);
		} else {
			baseUrl = "https://reshiftsecurity.com";
			CopyPasteManager.getInstance().setContents(new StringSelection(errorText));
			Desktop desktop = Desktop.getDesktop();

			if (desktop.isSupported(Desktop.Action.MAIL)) {
				final String emailSubject = "Exception thrown in Reshift Plugin " + VersionManager.getVersion();
				final String emailBody = "Exception thrown with error: " + title;
				String message = String.format("mailto:%s?subject=%s&body=%s", PluginConstants.RESHIFT_DEV_EMAIL, encode(emailSubject), encode(emailBody));
				URI uri = URI.create(message);
				try {
					desktop.mail(uri);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return new SubmittedReportInfo(baseUrl, "issue", SubmittedReportInfo.SubmissionStatus.NEW_ISSUE);
		}
	}

	@NotNull
	private static String encode(@NotNull final String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	@NotNull
	private static String makeTitle(@NotNull final Error error) {
		String ret = error.message;
		if (ret == null && error.throwable != null) {
			@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
			final Throwable cause = ErrorUtil.getCause(error.throwable);
			ret = cause.toString();
			if ("java.lang.IllegalStateException".equals(ret)) {
				ret = "ISE";
			} else if ("java.lang.NullPointerException".equals(ret)) {
				ret = "NPE";
			}
			final StackTraceElement[] stack = cause.getStackTrace();
			if (stack.length > 0) {
				String location = stack[0].toString();
				if (location.startsWith("org.jetbrains.plugins.spotbugs.")) {
					location = location.substring("org.jetbrains.plugins.spotbugs.".length());
				}
				ret += " at " + location;
			}
		}
		if (StringUtil.isEmptyOrSpaces(ret)) {
			ret = "Analysis Error";
		}
		return ret;
	}

	@Nullable
	private static Error createError(@NotNull final IdeaLoggingEvent event) {
		String message = event.getMessage();
		if (StringUtil.isEmptyOrSpaces(message) || "null".equals(message)) {
			message = null;
		}
		final Throwable throwable = event.getThrowable();
		if (message == null && throwable == null) {
			return null;
		}
		final String fullError;
		if (!StringUtil.equals(message, throwable.getMessage())) {
			fullError = message + "\n" + StringUtil.getThrowableText(throwable);
		} else {
			fullError = StringUtil.getThrowableText(throwable);
		}
		return new Error(message, throwable, fullError);
	}

	private static class Error implements Comparable<Error> {
		@Nullable
		private final String message;

		@Nullable
		private final Throwable throwable;

		@NotNull
		private final String fullError;

		public Error(
				@Nullable final String message,
				@Nullable final Throwable throwable,
				@NotNull final String fullError
		) {
			this.message = message;
			this.throwable = throwable;
			this.fullError = fullError;
		}

		@Override
		public int compareTo(@NotNull final Error o) {
			return fullError.compareTo(o.fullError);
		}

		@Override
		public boolean equals(@Nullable final Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Error error = (Error) o;
			return fullError.equals(error.fullError);

		}

		@Override
		public int hashCode() {
			return fullError.hashCode();
		}
	}
}