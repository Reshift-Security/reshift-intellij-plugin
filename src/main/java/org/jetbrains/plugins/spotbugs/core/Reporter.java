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
package org.jetbrains.plugins.spotbugs.core;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.util.Processor;
import com.intellij.util.containers.TransferToEDTQueue;
import edu.umd.cs.findbugs.*;
import edu.umd.cs.findbugs.classfile.ClassDescriptor;
import edu.umd.cs.findbugs.config.ProjectFilterSettings;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.spotbugs.common.EventDispatchThreadHelper;
import org.jetbrains.plugins.spotbugs.common.util.New;
import org.jetbrains.plugins.spotbugs.messages.MessageBusManager;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

final class Reporter extends AbstractBugReporter implements FindBugsProgress {

	private static final Logger LOGGER = Logger.getInstance(Reporter.class.getName());
	private static final String ANALYZING_CLASSES_i18N = "Analyzing classes: ";

	@NotNull
	private final Project _project;

	@NotNull
	private final Module module;

	@NotNull
	private final SortedBugCollection _bugCollection;

	@NotNull
	private final ProjectFilterSettings projectFilterSettings;

	private final ProgressIndicator _indicator;
	private final AtomicBoolean _cancellingByUser;
	private final TransferToEDTQueue<Runnable> _transferToEDTQueue;

	private int pass = -1;
	private int bugCount;
	private int stepCount;
	private int goal;
	@NonNls
	private String _currentStageName;
	private boolean _canceled;
	private int analyzedClassCountOffset;

	final static ArrayList<String> acceptedList = new ArrayList<>(
			Arrays.asList(
					"AWS_QUERY_INJECTION",
					"BLOWFISH_KEY_SIZE",
					"BUFFER_OVERFLOWS",
					"CIPHER_INTEGRITY",
					"CODE_EXECUTION",
					"COMMAND_INJECTION",
					"CSRF",
					"CUSTOM_INJECTION",
					"DDOS",
					"DES_USAGE",
					"ECB_MODE",
					"EL_INJECTION",
					"FILE_UPLOAD_FILENAME",
					"HARD_CODE_KEY",
					"HAZELCAST_SYMMETRIC_ENCRYPTION",
					"HEADER_INJECTION",
					"INFORMATION_LEAKAGE",
					"INSECURE_CRYPTO",
					"INSECURE_DATA_STORAGE",
					"INSECURE_DESERIALIZATION",
					"INSECURE_FILE_UPLOAD",
					"INSECURE_LOGGING",
					"INSECURE_OBJECT",
					"INSECURE_REQUIRE",
					"INSECURE_TRANSPORT",
					"JACKSON_UNSAFE_DESERIALIZATION",
					"JSP_INCLUDE",
					"JSP_JSTL_OUT",
					"JSP_SPRING_EVAL",
					"JSP_XSLT",
					"LDAP_INJECTION",
					"MALICIOUS_XSLT",
					"OBJECT_DESERIALIZATION",
					"OGNL_INJECTION",
					"PADDING_ORACLE",
					"PARAMETER_TAMPERING",
					"PATH_TRAVERSAL_IN",
					"PATH_TRAVERSAL_OUT",
					"PLAY_UNVALIDATED_REDIRECT",
					"PT_ABSOLUTE_PATH_TRAVERSAL",
					"PT_RELATIVE_PATH_TRAVERSAL",
					"REDOS",
					"RESTRICT_ANON_ACCESS",
					"RSA_KEY_SIZE",
					"RSA_NO_PADDING",
					"SCALA_COMMAND_INJECTION",
					"SCALA_PATH_TRAVERSAL_IN",
					"SCALA_PLAY_SSRF",
					"SCALA_SQL_INJECTION_ANORM",
					"SCALA_SQL_INJECTION_SLICK",
					"SENSITIVE_DATA_EXPOSURE",
					"SMTP_HEADER_INJECTION",
					"SPRING_ENTITY_LEAK",
					"SPRING_UNVALIDATED_REDIRECT",
					"SQL_INJECTION",
					"SQL_INJECTION_ANDROID",
					"SQL_INJECTION_HIBERNATE",
					"SQL_INJECTION_JDBC",
					"SQL_INJECTION_JDO",
					"SQL_INJECTION_JPA",
					"SQL_INJECTION_SPRING_JDBC",
					"SQL_INJECTION_TURBINE",
					"SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE",
					"SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING",
					"SSRF",
					"STATIC_IV",
					"TAV_TAMPERING",
					"TDES_USAGE",
					"TIME_ATTACK",
					"UNAUTHORIZED_ACCESS_FILE_SYSTEM",
					"UNTRUSTED_USER_INPUT",
					"UNVALIDATED_REDIRECT",
					"URLCONNECTION_SSRF_FD",
					"WEAK_HASHING",
					"WEAK_MESSAGE_DIGEST_MD5",
					"WEAK_MESSAGE_DIGEST_SHA1",
					"WICKET_XSS1",
					"XPATH_INJECTION",
					"XSS",
					"XSS_REQUEST_PARAMETER_TO_JSP_WRITER",
					"XSS_REQUEST_PARAMETER_TO_SEND_ERROR",
					"XSS_REQUEST_PARAMETER_TO_SERVLET_WRITER",
					"XSS_REQUEST_WRAPPER",
					"XXE",
					"XXE_DOCUMENT",
					"XXE_DTD_TRANSFORM_FACTORY",
					"XXE_SAXPARSER",
					"XXE_XMLREADER",
					"XXE_XMLSTREAMREADER",
					"XXE_XPATH",
					"XXE_XSLT_TRANSFORM_FACTORY"
			)
	);

	public enum Severity {
		CRITICAL(1), HIGH(2), MODERATE(3), LOW(4);
		private final int sev;
		private Severity(int sev) {this.sev = sev;}
		public int getSeverity(){ return this.sev; }
	};

	Map<String, Reporter.Severity> severityTransform = new HashMap<String, Reporter.Severity>() {{
		put("AWS_QUERY_INJECTION", Reporter.Severity.CRITICAL);
		put("COMMAND_INJECTION", Reporter.Severity.CRITICAL);
		put("SCALA_COMMAND_INJECTION", Reporter.Severity.CRITICAL);
		put("XXE_SAXPARSER", Reporter.Severity.CRITICAL);
		put("XXE_XMLREADER", Reporter.Severity.CRITICAL);
		put("XXE_DOCUMENT", Reporter.Severity.CRITICAL);
		put("XXE_DTD_TRANSFORM_FACTORY", Reporter.Severity.CRITICAL);
		put("XXE_XSLT_TRANSFORM_FACTORY", Reporter.Severity.CRITICAL);
		put("XXE_XMLSTREAMREADER", Reporter.Severity.CRITICAL);
		put("XXE_XPATH", Reporter.Severity.CRITICAL);
		put("CUSTOM_INJECTION", Reporter.Severity.CRITICAL);
		put("SQL_INJECTION", Reporter.Severity.CRITICAL);
		put("SQL_INJECTION_TURBINE", Reporter.Severity.CRITICAL);
		put("SQL_INJECTION_HIBERNATE", Reporter.Severity.CRITICAL);
		put("SQL_INJECTION_JDO", Reporter.Severity.CRITICAL);
		put("SQL_INJECTION_JPA", Reporter.Severity.CRITICAL);
		put("SQL_INJECTION_SPRING_JDBC", Reporter.Severity.CRITICAL);
		put("SQL_INJECTION_JDBC", Reporter.Severity.CRITICAL);
		put("SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE", Reporter.Severity.CRITICAL);
		put("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING", Reporter.Severity.CRITICAL);
		put("SQL_INJECTION_ANDROID", Reporter.Severity.CRITICAL);
		put("SQL_INJECTION_ANDROID", Reporter.Severity.CRITICAL);
		put("SCALA_SQL_INJECTION_SLICK", Reporter.Severity.CRITICAL);
		put("SCALA_SQL_INJECTION_ANORM", Reporter.Severity.CRITICAL);
		put("LDAP_INJECTION", Reporter.Severity.CRITICAL);
		put("JSP_INCLUDE", Reporter.Severity.CRITICAL);
		put("JSP_XSLT", Reporter.Severity.CRITICAL);
		put("MALICIOUS_XSLT", Reporter.Severity.CRITICAL);
		put("EL_INJECTION", Reporter.Severity.CRITICAL);
		put("OGNL_INJECTION", Reporter.Severity.CRITICAL);
		put("PATH_TRAVERSAL_IN", Reporter.Severity.HIGH);
		put("PATH_TRAVERSAL_OUT", Reporter.Severity.HIGH);
		put("SCALA_PATH_TRAVERSAL_IN", Reporter.Severity.HIGH);
		put("FILE_UPLOAD_FILENAME", Reporter.Severity.HIGH);
		put("XPATH_INJECTION", Reporter.Severity.HIGH);
		put("HAZELCAST_SYMMETRIC_ENCRYPTION", Reporter.Severity.HIGH);
		put("DES_USAGE", Reporter.Severity.HIGH);
		put("TDES_USAGE", Reporter.Severity.HIGH);
		put("RSA_NO_PADDING", Reporter.Severity.HIGH);
		put("HARD_CODE_KEY", Reporter.Severity.HIGH);
		put("XSS_REQUEST_WRAPPER", Reporter.Severity.HIGH);
		put("CIPHER_INTEGRITY", Reporter.Severity.HIGH);
		put("OBJECT_DESERIALIZATION", Reporter.Severity.HIGH);
		put("JACKSON_UNSAFE_DESERIALIZATION", Reporter.Severity.HIGH);
		put("SCALA_PLAY_SSRF", Reporter.Severity.HIGH);
		put("URLCONNECTION_SSRF_FD", Reporter.Severity.HIGH);
		put("JSP_SPRING_EVAL", Reporter.Severity.HIGH);
		put("JSP_JSTL_OUT", Reporter.Severity.HIGH);
		put("SMTP_HEADER_INJECTION", Reporter.Severity.MODERATE);
		put("WEAK_MESSAGE_DIGEST_MD5", Reporter.Severity.MODERATE);
		put("WEAK_MESSAGE_DIGEST_SHA1", Reporter.Severity.MODERATE);
		put("REDOS", Reporter.Severity.MODERATE);
		put("RSA_KEY_SIZE", Reporter.Severity.MODERATE);
		put("BLOWFISH_KEY_SIZE", Reporter.Severity.MODERATE);
		put("UNVALIDATED_REDIRECT", Reporter.Severity.MODERATE);
		put("PLAY_UNVALIDATED_REDIRECT", Reporter.Severity.MODERATE);
		put("SPRING_UNVALIDATED_REDIRECT", Reporter.Severity.MODERATE);
		put("STATIC_IV", Reporter.Severity.MODERATE);
		put("ECB_MODE", Reporter.Severity.MODERATE);
		put("PADDING_ORACLE", Reporter.Severity.MODERATE);
	}};


	Reporter(
			@NotNull final Project project,
			@NotNull final Module module,
			@NotNull final SortedBugCollection bugCollection,
			@NotNull final ProjectFilterSettings projectFilterSettings,
			@NotNull final ProgressIndicator indicator,
			@NotNull final AtomicBoolean cancellingByUser,
			final int analyzedClassCountOffset
	) {
		_project = project;
		this.module = module;
		_bugCollection = bugCollection;
		this.projectFilterSettings = projectFilterSettings;
		_indicator = indicator;
		_cancellingByUser = cancellingByUser;
		this.analyzedClassCountOffset = analyzedClassCountOffset;
		_transferToEDTQueue = new TransferToEDTQueue<Runnable>("Add New Bug Instance", new RunnableProcessor(), new Condition<Object>() {
			@Override
			public boolean value(final Object o) {
				return project.isDisposed() || _cancellingByUser.get() || _indicator.isCanceled();
			}
		}, 500);
	}


	private boolean checkCancel() {
		if (_canceled) {
			return true;
		}
		if (_indicator.isCanceled()) {
			cancelFindBugs();
			return true;
		}
		if (_cancellingByUser.get()) {
			cancelFindBugs();
			_indicator.cancel();
			return true;
		}
		return false;
	}


	@Override
	protected void doReportBug(@NotNull final BugInstance bug) {
		if (!projectFilterSettings.displayWarning(bug)) {
			return;
		}
		BugPattern bugPattern = bug.getBugPattern();
		String category = bugPattern.getCategory().toLowerCase();
		String pattern = bugPattern.getType();

		if( category.startsWith("security") && (acceptedList.contains(pattern)) ) {
			_bugCollection.add(severityRemap(bug));
			bugCount++;
			observeClass(bug.getPrimaryClass().getClassDescriptor());

			// Guarantee thread visibility *one* time.
			final AtomicReference<SortedBugCollection> bugCollectionRef = New.atomicRef(_bugCollection);
			final AtomicReference<BugInstance> bugRef = New.atomicRef(bug);
			final int analyzedClassCount = analyzedClassCountOffset + getProjectStats().getNumClasses();
			_transferToEDTQueue.offer(new Runnable() {
				/**
				 * Invoked by EDT.
				 */
				@Override
				public void run() {
					final Bug bug = new Bug(module, bugCollectionRef.get(), bugRef.get());
					MessageBusManager.publishNewBug(_project, bug, analyzedClassCount);
				}
			});
		}
	}


	public BugInstance severityRemap(BugInstance bug){
		BugPattern bugPattern = bug.getBugPattern();
		String pattern = bugPattern.getType();
		if (severityTransform.containsKey(pattern)){
			Reporter.Severity priority = severityTransform.get(pattern);
			if (null != priority) {
				bug.setPriority(priority.getSeverity());
			}
		}
		return bug;
	}


	@Override
	public ProjectStats getProjectStats() {
		return _bugCollection.getProjectStats();
	}


	@Override
	public void startArchive(final String s) {
	}


	@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
	@Override
	public void reportQueuedErrors() {
		// Report unique errors in order of their sequence
		final List<Error> errorList = new ArrayList<Error>(getQueuedErrors());
		if (!errorList.isEmpty()) {
			Collections.sort(errorList, QUEUED_ERRORS_COMPARATOR);

			final Map<String, Map<String, Throwable>> status = new HashMap<String, Map<String, Throwable>>();
			final String key = "The following errors occurred during FindBugs analysis:";
			status.put(key, new HashMap<String, Throwable>());

			final Map<String, Throwable> map = status.get(key);
			for (final Error error : errorList) {
				map.put(error.getMessage(), error.getCause());
			}

			//FindBugsPluginImpl.processError(status); TODO
		}

		final Set<String> missingClasses = getMissingClasses();
		if (!missingClasses.isEmpty()) {
			final Map<String, Map<String, Throwable>> status = new HashMap<String, Map<String, Throwable>>();
			final String key = "The following classes needed for SpotBugs analysis were missing:";
			status.put(key, new HashMap<String, Throwable>());

			final Map<String, Throwable> map = status.get(key);
			for (final String missingClass : missingClasses) {
				map.put(missingClass, null);
				LOGGER.info(missingClass);
			}
			//FindBugsPluginImpl.processError(status); TODO
		}
	}


	@Override
	public void finish() {
		EventDispatchThreadHelper.invokeAndWait(new EventDispatchThreadHelper.OperationAdapter() {
			@Override
			public void run() {
				_transferToEDTQueue.drain();
			}
		});
		_indicator.setText("Finished: Found " + bugCount + " bugs.");
		_indicator.finishNonCancelableSection();
	}


	boolean isCanceled() {
		return _canceled;
	}


	@Override
	public SortedBugCollection getBugCollection() {
		return _bugCollection;
	}


	@Override
	public void observeClass(@NotNull final ClassDescriptor classDescriptor) {
		if (checkCancel()) {
			return;
		}

		final String className = classDescriptor.getDottedClassName();
		_indicator.setText("Observing class: " + className);
		if (pass <= 0) {
			_indicator.setText("Prescanning... (found " + bugCount + ", checking " + className + ')');
		} else {
			_indicator.setText("Checking... (found " + bugCount + ", checking " + className + ')');
		}
	}

	private void cancelFindBugs() {
		Thread.currentThread().interrupt(); // causes break in FindBugs main loop
		_canceled = true;
	}

	@Override
	public void reportAnalysisError(final AnalysisError error) {
		// nothing to do, see reportQueuedErrors()
	}


	@Override
	public void reportMissingClass(final String missingClass) {
		// nothing to do, see reportQueuedErrors()
	}


	@Override
	public void finishArchive() {
		step();
	}


	@Override
	public void finishClass() {
		step();
	}


	@Override
	public void finishPerClassAnalysis() {
		_indicator.setText("Finishing analysis...");
	}


	@Override
	public void reportNumberOfArchives(final int numArchives) {
		beginStage("Scanning archives: ", numArchives);
		checkCancel(); // interrupt here has no effect, this is a FindBugs bug... bad for jumbo projects.
	}


	@Override
	public void startAnalysis(final int numClasses) {
		pass++;
		beginStage(ANALYZING_CLASSES_i18N, numClasses);
	}


	@Override
	public void predictPassCount(final int[] classesPerPass) {
		int expectedWork = 0;
		for (final int count : classesPerPass) {
			expectedWork += 2 * count;
		}
		expectedWork -= classesPerPass[0];
		_indicator.setText("Performing bug checking... " + expectedWork);
	}


	private void beginStage(final String stageName, final int goal) {
		stepCount = 0;
		this.goal = goal;
		_currentStageName = stageName;
		_indicator.setText2(stageName + " 0/" + this.goal);
	}


	private void step() {
		stepCount++;
		final int work = pass == 0 ? 1 : 2;
		_indicator.setText2(_currentStageName + ' ' + stepCount + '/' + goal + (ANALYZING_CLASSES_i18N.equals(_currentStageName) ? " (pass #" + work + "/2)" : ""));
	}


	private static class RunnableProcessor implements Processor<Runnable> {
		@Override
		public boolean process(Runnable runnable) {
			runnable.run();
			return true;
		}
	}


	private static final Comparator<Error> QUEUED_ERRORS_COMPARATOR = new Comparator<Error>() {
		@Override
		public int compare(final Error o1, final Error o2) {
			return Integer.signum(o1.getSequence() - o2.getSequence());
		}
	};
}
