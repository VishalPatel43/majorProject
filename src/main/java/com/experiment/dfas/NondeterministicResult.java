package com.experiment.dfas;

import net.automatalib.automata.fsa.NFA;

public class NondeterministicResult {
    private final NFA<?, Character> learnedNFA;
    //    private final long timeTaken;
    private final long membershipQueries;
    private final long equivalenceQueries;

    public NondeterministicResult(NFA<?, Character> learnedNFA, long equivalenceQueries, long membershipQueries) {
        this.learnedNFA = learnedNFA;
//        this.timeTaken = timeTaken;
        this.equivalenceQueries = equivalenceQueries;
        this.membershipQueries = membershipQueries;
    }

    public NFA<?, Character> getLearnedNFA() {
        return learnedNFA;
    }

//    public long getTimeTaken() {
//        return timeTaken;
//    }

    public long getMembershipQueries() {
        return membershipQueries;
    }

    public long getEquivalenceQueries() {
        return equivalenceQueries;
    }

    public int getNumStates() {
        return learnedNFA.size();
    }
}