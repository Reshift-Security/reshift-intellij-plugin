package com.reshiftsecurity.results;

import edu.umd.cs.findbugs.BugRankCategory;

public class FindBugsRankRenamer {
    public static String rename(BugRankCategory bugRank) {
        if (bugRank == null) {
            return null;
        }
        switch (bugRank) {
            case SCARIEST:
                return "CRITICAL";
            case SCARY:
                return "HIGH";
            case TROUBLING:
                return "MODERATE";
            default:
                return bugRank.toString();
        }
    }
}
