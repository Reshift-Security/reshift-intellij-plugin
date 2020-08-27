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

package com.reshiftsecurity.results;

import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import net.sf.cglib.core.Local;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

@Tag(value = "securityIssue")
public class SecurityIssue implements Comparable<SecurityIssue> {
    @Attribute
    public String issueHash;

    @Attribute
    public int cweId;

    @Attribute
    public String categoryName;

    @Attribute
    public String classFQN;

    @Attribute
    public String trimmedCode;

    @Attribute
    public int lineNumber;

    @Attribute
    public String code;

    @Attribute
    public String methodFullSignature;

    @Attribute
    public Boolean isFixed = false;

    @Attribute
    public Boolean isNew = true;

    @Attribute
    public String detectionDatetime;

    @Attribute
    public String fixDatetime;

    private boolean hasSameMethodAs(SecurityIssue securityIssue) {
        return methodFullSignature.equalsIgnoreCase(securityIssue.methodFullSignature);
    }
    
    private boolean hasSameCodeAs(SecurityIssue securityIssue) {
        return StringUtils.deleteWhitespace(code)
                .equalsIgnoreCase(StringUtils.deleteWhitespace(securityIssue.code));
    }

    private boolean hasSameLineNumberAs(SecurityIssue securityIssue) {
        return lineNumber == securityIssue.lineNumber;
    }

    private boolean hasSameCWEAs(SecurityIssue securityIssue) {
        return cweId == securityIssue.cweId;
    }

    private boolean hasSameHashAs(SecurityIssue securityIssue) {
        return issueHash.equalsIgnoreCase(securityIssue.issueHash);
    }

    private boolean hasSameClassAs(SecurityIssue securityIssue) {
        return classFQN.equalsIgnoreCase(securityIssue.classFQN);
    }

    public boolean isSameAs(SecurityIssue securityIssue) {
        Boolean hasSameCWE = hasSameCWEAs(securityIssue);
        Boolean hasSameClass = hasSameClassAs(securityIssue);
        Boolean hasSameCode = hasSameCodeAs(securityIssue);
        Boolean hasSameLineNumber = hasSameLineNumberAs(securityIssue);
        Boolean hasSameMethod = hasSameMethodAs(securityIssue);
        Boolean hasSameHash = hasSameHashAs(securityIssue);

        if (hasSameCWE) {
            if (hasSameClass && hasSameCode && hasSameLineNumber) {
                return true;
            }

            if (hasSameMethod && hasSameCode) {
                return true;
            }

            if (hasSameHash && hasSameLineNumber) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int compareTo(@NotNull SecurityIssue issue) {
        return isSameAs(issue) ? 0 : -1;
    }
}
