package com.experiment.algorithms;

import com.experiment.dfas.DFAOperation;
import com.experiment.dfas.DeterministicResult;
import de.learnlib.algorithms.lstar.dfa.ClassicLStarDFA;
import de.learnlib.algorithms.lstar.dfa.ClassicLStarDFABuilder;
import de.learnlib.api.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.filter.statistic.oracle.DFACounterOracle;
import de.learnlib.oracle.equivalence.DFAWMethodEQOracle;
import de.learnlib.oracle.membership.SimulatorOracle.DFASimulatorOracle;
import de.learnlib.util.Experiment.DFAExperiment;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.Alphabet;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LStarAlgorithm {

    private static final int EXPLORATION_DEPTH = 4;
    private static DFAOperation dfaOperation = new DFAOperation();

    public static void lStarAlgorithm() throws IOException {

        // Folder containing serialized DFAs
        File dfasFolder = new File("random_dfas");
        File lstarDFAsFolder = new File("lstar_dfas");

        // Ensure lstar_dfas folder exists
        if (!lstarDFAsFolder.exists()) {
            lstarDFAsFolder.mkdirs();
        } else {
            // Clear existing files
            dfaOperation.deletePreviousFiles(lstarDFAsFolder);
        }

        // Create or clear the CSV file
        File csvFile = new File("lstar_dfas/lstar.csv");

        // Load DFAs from the serialized file
        List<CompactDFA<Character>> loadedDFAs = dfaOperation.loadCompactDFAsFromFile(new File(dfasFolder, "dfas.ser"));

        // List to store learned DFAs using L*
        List<DFA<?, Character>> learnedDFAs = new ArrayList<>();

        // Iterate over loaded DFAs and run L* on each
//        for (int i = 0; i < loadedDFAs.size(); i++) {
//            CompactDFA<Character> currentDFA = loadedDFAs.get(i);
//
//            System.out.println("Running L* on DFA " + (i + 1));
//            long startTime = System.currentTimeMillis();
//            LStarResult learnedDFA = runLStar(currentDFA, i + 1);
//            long endTime = System.currentTimeMillis();
//            System.out.println("Time taken: " + (endTime - startTime) + " ms");
//            learnedDFAs.add(learnedDFA.getLearnedDFA());
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
                    DeterministicResult learnedDFA = runLStar(currentDFA, i + 1);
                    long endTime = System.currentTimeMillis();
                    long timeTaken = endTime - startTime;
                    System.out.println("Time taken: " + timeTaken + " ms");

                    // Record information to CSV
                    writer.write(String.format("%d,%d,%d,%d,%d",
                            i + 1,
                            learnedDFA.getNumStates(),
                            timeTaken,
                            learnedDFA.getEquivalenceQueries(),
                            learnedDFA.getMembershipQueries()));
                    writer.newLine();

                    learnedDFAs.add(learnedDFA.getLearnedDFA());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        // Serialize and save learned DFAs to a file
        dfaOperation.saveDFAsToFile(learnedDFAs, new File(lstarDFAsFolder, "dfas.ser"));
    }

    private static DeterministicResult runLStar(CompactDFA<Character> target, int index) {
        // Alphabet for the target DFA
        Alphabet<Character> inputs = dfaOperation.getInputAlphabets(target);

        // Construct a simulator membership query oracle
        DFAMembershipOracle<Character> sul = new DFASimulatorOracle<>(target);

        // Oracle for counting queries wraps SUL
        DFACounterOracle<Character> mqOracle = new DFACounterOracle<>(sul, "Membership Queries");

        // Construct L* instance
        ClassicLStarDFA<Character> lstar =
                new ClassicLStarDFABuilder<Character>().withAlphabet(inputs)
                        .withOracle(mqOracle)
                        .create();

        // Construct a W-method conformance test
        DFAWMethodEQOracle<Character> wMethod = new DFAWMethodEQOracle<>(mqOracle, EXPLORATION_DEPTH);

        // Construct a learning experiment
        DFAExperiment<Character> experiment = new DFAExperiment<>(lstar, wMethod, inputs);

        // Turn on time profiling
        experiment.setProfile(true);

        // Enable logging of models
        experiment.setLogModels(true);

        // Run the experiment
        experiment.run();

        // Get learned model
//        CompactDFA<Character> result = (CompactDFA<Character>) experiment.getFinalHypothesis();
        DFA<?, Character> result = experiment.getFinalHypothesis();

        // Report results
        System.out.println("-------------------------------------------------------");

        // Profiling
//        System.out.println("Profiling Results:");
//        System.out.println(SimpleProfiler.getResults());

//        // Learning statistics
        System.out.println("Learning Statistics:");
//        // equivalence queries
        System.out.println(experiment.getRounds().getSummary());
//
//        // membership queries
        System.out.println(mqOracle.getStatisticalData().getSummary());

        // Model statistics
        System.out.println("Model Statistics:");
        System.out.println("States: " + result.size());
        System.out.println("Sigma: " + inputs.size());

        // Show model
//        System.out.println("Learned Model:");
//        GraphDOT.write(result, inputs, System.out);

        // Save the learned model to a file
//        String learnedDFAFileName = "lstar_dfas/lstar_dfa_" + index + ".dot";
//        dfaOperation.generateDOTFileDFA(result, inputs, learnedDFAFileName);
//        dfaOperation.convertDotToPDF(learnedDFAFileName, "lstar_dfas/dfa_" + index + ".pdf");

//        System.out.println("Final observation table:");
//        new ObservationTableASCIIWriter<>().write(lstar.getObservationTable(), System.out);

        // Return the learned model
        return new DeterministicResult(result, experiment.getRounds().getCount(), mqOracle.getCount());
    }
}