package com.experiment.algorithms;

import com.experiment.dfas.DFAOperation;
import com.experiment.dfas.NLStarResult;
import com.google.common.collect.Lists;

import de.learnlib.algorithms.nlstar.NLStarLearner;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.filter.statistic.oracle.CounterOracle;
import de.learnlib.oracle.equivalence.SampleSetEQOracle;
import de.learnlib.oracle.membership.SimulatorOracle;
import de.learnlib.util.Experiment;

import net.automatalib.automata.fsa.NFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.conformance.WMethodTestsIterator;

import net.automatalib.words.Alphabet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class NLStarAlgorithm {

    private static final DFAOperation dfaOperation = new DFAOperation();

    private static final int EXPLORATION_DEPTH = 4;

    public static void nlStarAlgorithm() {

        DFAOperation dfaOperation = new DFAOperation();

        // Folder containing serialized DFAs
        File dfasFolder = new File("random_dfas");
        File nlstarDFAsFolder = new File("nlstar_dfas");
        // Create or clear the CSV file

        // Ensure nlstar_dfas folder exists
        if (!nlstarDFAsFolder.exists()) {
            nlstarDFAsFolder.mkdirs();
        } else {
            // Clear existing files
            dfaOperation.deletePreviousFiles(nlstarDFAsFolder);
        }

        // Create or clear the CSV file
        File csvFile = new File("nlstar_dfas/nlstar.csv");

        // Load DFAs from the serialized file
        List<CompactDFA<Character>> loadedDFAs = dfaOperation.loadCompactDFAsFromFile(new File(dfasFolder, "dfas.ser"));

        // List to store learned DFAs using NL*
        List<NFA<?, Character>> learnedNFAs = Lists.newArrayList();


//        // Iterate over loaded DFAs and run NL* on each
//        for (int i = 0; i < loadedDFAs.size(); i++) {
//            CompactDFA<Character> currentDFA = loadedDFAs.get(i);
//
//            System.out.println("Running NL* on DFA " + (i + 1));
//
//            long startTime = System.currentTimeMillis();
//            NLStarResult learnedNFA = runNLStar(currentDFA, i + 1);
//            long endTime = System.currentTimeMillis();
//            long timeTaken = endTime - startTime;
//            System.out.println("Time taken: " + timeTaken + " ms");
//            learnedNFAs.add(learnedNFA.getLearnedNFA());
//        }

        try {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {
                // Write header to the CSV file
                writer.write("Index,NumStates, TimeTaken(ms),EquivalenceQueries, MembershipQueries");
                writer.newLine();

                // Iterate over loaded DFAs and run NL* on each
                for (int i = 0; i < loadedDFAs.size(); i++) {
                    CompactDFA<Character> currentDFA = loadedDFAs.get(i);

                    System.out.println("Running NL* on DFA " + (i + 1));

                    long startTime = System.currentTimeMillis();
                    NLStarResult learnedNFA = runNLStar(currentDFA, i + 1);
                    long endTime = System.currentTimeMillis();
                    long timeTaken = endTime - startTime;
                    System.out.println("Time taken: " + timeTaken + " ms");

                    // Record information to CSV
                    writer.write(String.format("%d,%d,%d,%d,%d",
                            i + 1,
                            learnedNFA.getNumStates(),
                            timeTaken,
                            learnedNFA.getEquivalenceQueries(),
                            learnedNFA.getMembershipQueries()));
                    writer.newLine();

                    learnedNFAs.add(learnedNFA.getLearnedNFA());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Serialize and save learned DFAs to a file
        dfaOperation.saveNFAsToFile(learnedNFAs, new File(nlstarDFAsFolder, "nfas.ser"));

    }

    private static NLStarResult runNLStar(CompactDFA<Character> dfa, int index) {

        // Get the alphabet of the DFA
        Alphabet<Character> alphabet = dfa.getInputAlphabet();

        // Create a membership oracle for the DFA
        final MembershipOracle<Character, Boolean> mqo = new SimulatorOracle<>(dfa);

        // Create a counter oracle for NL* with membership oracle
        final CounterOracle<Character, Boolean> nlstarCounter = new CounterOracle<>(mqo, "Membership Queries");

        // Create an instance of NL* learner with the alphabet and counter oracle
        final NLStarLearner<Character> nlstar = new NLStarLearner<>(alphabet, nlstarCounter);

        // Create a sample set equivalence oracle using W-method tests
        final SampleSetEQOracle<Character, Boolean> eqo = new SampleSetEQOracle<>(false);
        eqo.addAll(mqo, Lists.newArrayList(new WMethodTestsIterator<>(dfa, alphabet, EXPLORATION_DEPTH)));

        // Create an experiment with NL* learner, equivalence oracle, and alphabet
        final Experiment<NFA<?, Character>> nlstarExp = new Experiment<>(nlstar, eqo, alphabet);

        // Turn on time profiling
        nlstarExp.setProfile(true);

        // Enable logging of models
        nlstarExp.setLogModels(true);

        // Run the experiment
        nlstarExp.run();


        // Get the learned NFA model
        NFA<?, Character> result = nlstarExp.getFinalHypothesis();

        // Save the learned model to a file in dot format
//        String dotFileName = "nlstar_dfas/nlstar_dfa_" + index + ".dot";
//        dfaOperation.generateDOTFileNFA(result, alphabet, dotFileName);

        // Convert dot file to PDF
//        dfaOperation.convertDotToPDF(dotFileName, "nlstar_dfas/nlstar_dfa_" + index + ".pdf");

        // Print separator
        System.out.println("-------------------------------------------------------");

        // Print profiling results
//        System.out.println("Profiling Results:");
//        // store this in the list after the program over add this into graph
//        System.out.println(SimpleProfiler.getResults());


        // Print learning statistics
        System.out.println("Learning Statistics:");
        // Query : Equivalence queries
        System.out.println(nlstarExp.getRounds().getCount());
//        System.out.println(nlstarExp.getRounds().getSummary());

        // Print membership queries statistics
        System.out.println(nlstarCounter.getCount());
//        System.out.println(nlstarCounter.getStatisticalData().getSummary());

        // Print model statistics
        System.out.println("Model Statistics:");
        // Number of states
        System.out.println("States: " + result.size());
        // Number of alphabets
        System.out.println("Sigma: " + alphabet.size());

        // Print learned model in dot format
//        System.out.println("Learned Model:");
//        GraphDOT.write(result, alphabet, System.out);

        // Get the observation table of the NL* learner
//        ObservationTable<Character> observationTable = nlstar.getObservationTable();

        return new NLStarResult(result, nlstarExp.getRounds().getCount(), nlstarCounter.getCount());
    }
}
