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
package com.reshiftsecurity.plugins.intellij.messages;

import com.intellij.openapi.project.Project;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.reshiftsecurity.plugins.intellij.common.EventDispatchThreadHelper;
import com.reshiftsecurity.plugins.intellij.common.util.New;
import com.reshiftsecurity.plugins.intellij.core.Bug;
import com.reshiftsecurity.plugins.intellij.core.FindBugsResult;
import com.reshiftsecurity.plugins.intellij.core.FindBugsState;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public final class MessageBusManager {

	private static final Map<Project, MessageBus> _busByProject = New.map();

	private MessageBusManager() {
	}

	@NotNull
	private static MessageBus of(@NotNull final Project project) {
		MessageBus ret = _busByProject.get(project);
		if (ret == null) {
			ret = new MessageBus(project);
			_busByProject.put(project, ret);
		}
		return ret;
	}

	public static <L extends AnalysisStateListener> void subscribeAnalysisState(@NotNull final Project project, @NotNull final Object subscriber, @NotNull final L handler) {
		subscribe(project, subscriber, AnalysisStartedListener.TOPIC, handler);
		subscribe(project, subscriber, AnalysisAbortingListener.TOPIC, handler);
		subscribe(project, subscriber, AnalysisAbortedListener.TOPIC, handler);
		subscribe(project, subscriber, AnalysisFinishedListener.TOPIC, handler);
	}

	/**
	 * Note that you can only subscribe *one* {@code handler} per {@code topic}.
	 * If {@code subscriber} has subscribed the topic already, nothing is done.
	 *
	 * @param project    ..
	 * @param subscriber ..
	 * @param topic      ..
	 * @param handler    ..
	 * @param <L>.
	 */
	public static <L> void subscribe(@NotNull final Project project, @NotNull final Object subscriber, @NotNull final Topic<L> topic, @NotNull final L handler) {
		EventDispatchThreadHelper.checkEDT();
		of(project).subscribe(subscriber, topic, handler);
	}

	public static void publishClear(@NotNull final Project project) {
		EventDispatchThreadHelper.checkEDT();
		FindBugsState.set(project, FindBugsState.Cleared);
		publish(project, ClearListener.TOPIC).clear();
	}

	public static void publishNewBug(@NotNull final Project project, @NotNull final Bug bug, final int analyzedClassCount) {
		EventDispatchThreadHelper.checkEDT();
		publish(project, NewBugListener.TOPIC).newBug(bug, analyzedClassCount);
	}

	public static void publishAnalysisStarted(@NotNull final Project project) {
		EventDispatchThreadHelper.checkEDT();
		FindBugsState.set(project, FindBugsState.Started);
		publish(project, AnalysisStartedListener.TOPIC).analysisStarted();
	}

	public static void publishAnalysisStartedToEDT(@NotNull final Project project) {
		EventDispatchThreadHelper.checkNotEDT();
		EventDispatchThreadHelper.invokeLater(() -> {
			FindBugsState.set(project, FindBugsState.Started);
			publish(project, AnalysisStartedListener.TOPIC).analysisStarted();
		});
	}

	public static void publishAnalysisAborting(@NotNull final Project project) {
		EventDispatchThreadHelper.checkEDT();
		FindBugsState.set(project, FindBugsState.Aborting);
		publish(project, AnalysisAbortingListener.TOPIC).analysisAborting();
	}

	public static void publishAnalysisAbortedToEDT(@NotNull final Project project) {
		EventDispatchThreadHelper.checkNotEDT();
		EventDispatchThreadHelper.invokeLater(() -> {
			FindBugsState.set(project, FindBugsState.Aborted);
			publish(project, AnalysisAbortedListener.TOPIC).analysisAborted();
		});
	}

	public static void publishAnalysisFinishedToEDT(@NotNull final Project project, @NotNull final FindBugsResult result, @Nullable final Throwable error) {
		EventDispatchThreadHelper.checkNotEDT();
		/*
		 * Guarantee thread visibility *one* time.
		 */
		final AtomicReference<FindBugsResult> resultRef = New.atomicRef(result);
		final AtomicReference<Throwable> errorRef = New.atomicRef(error);
		EventDispatchThreadHelper.invokeLater(() -> {
			FindBugsState.set(project, FindBugsState.Finished);
			publish(project, AnalysisFinishedListener.TOPIC).analysisFinished(resultRef.get(), errorRef.get());
		});
	}

	@NotNull
	private static <L> L publish(@NotNull final Project project, @NotNull final Topic<L> topic) {
		EventDispatchThreadHelper.checkEDT();
		return of(project).publisher(topic);
	}

	public static void dispose(@NotNull final Project project) {
		EventDispatchThreadHelper.checkEDT();
		_busByProject.remove(project);
	}
}
