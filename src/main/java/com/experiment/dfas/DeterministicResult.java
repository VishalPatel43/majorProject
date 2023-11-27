package com.experiment.dfas;

import net.automatalib.automata.fsa.DFA;


public class DeterministicResult {
    private final DFA<?, Character> learnedDFA;
    //    private final long timeTaken;
    private final long membershipQueries;
    private final long equivalenceQueries;

    public DeterministicResult(DFA<?, Character> learnedDFA, long equivalenceQueries, long membershipQueries) {
        this.learnedDFA = learnedDFA;
//        this.timeTaken = timeTaken;
        this.equivalenceQueries = equivalenceQueries;
        this.membershipQueries = membershipQueries;
    }

    public DFA<?, Character> getLearnedDFA() {
        return learnedDFA;
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
        return learnedDFA.size();
    }
}

