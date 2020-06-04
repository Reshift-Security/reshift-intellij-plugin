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
package com.reshiftsecurity.plugins.intellij.intentions;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDeclarationStatement;
import com.intellij.psi.PsiDocCommentOwner;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.reshiftsecurity.plugins.intellij.common.ExtendedProblemDescriptor;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.97
 */
public class SuppressReportBugForClassIntentionAction extends SuppressReportBugIntentionAction {

	public SuppressReportBugForClassIntentionAction(final ExtendedProblemDescriptor problemDescriptor) {
		super(problemDescriptor);
	}


	@Override
	@Nullable
	protected PsiDocCommentOwner getContainer(final PsiElement element) {
		PsiDocCommentOwner container = super.getContainer(element);
		if (container == null) {
			return null;
		}
		while (container != null) {
			final PsiClass parentClass = PsiTreeUtil.getParentOfType(container, PsiClass.class);
			if ((parentClass == null || container.getParent() instanceof PsiDeclarationStatement || container.getParent() instanceof PsiClass) && container instanceof PsiClass) {
				return container;
			}
			container = parentClass;
		}
		return container;
	}


	@Override
	@NotNull
	public String getText() {
		return super.getText() + " for class";
	}
}
