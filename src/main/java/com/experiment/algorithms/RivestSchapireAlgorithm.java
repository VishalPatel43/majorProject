package com.experiment.algorithms;

import com.experiment.dfas.DFAOperation;
import com.experiment.dfas.DeterministicResult;
import de.learnlib.algorithms.rivestschapire.RivestSchapireDFA;
import de.learnlib.algorithms.rivestschapire.RivestSchapireDFABuilder;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.filter.statistic.oracle.DFACounterOracle;
import de.learnlib.oracle.equivalence.DFAWMethodEQOracle;
import de.learnlib.oracle.membership.SimulatorOracle;
import de.learnlib.util.Experiment;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.Alphabet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RivestSchapireAlgorithm {

    private static final int EXPLORATION_DEPTH = 4;
    private static DFAOperation dfaOperation = new DFAOperation();

    public static void rsAlgorithm() throws IOException {

        // Folder containing serialized DFAs
        File dfasFolder = new File("random_dfas");
        File rsDFAsFolder = new File("rivest_schapire_dfas");

        // Ensure rivest_schapire_dfas folder exists
        if (!rsDFAsFolder.exists()) {
            rsDFAsFolder.mkdirs();
        } else {
            // Clear existing files
            dfaOperation.deletePreviousFiles(rsDFAsFolder);
        }

        // Create or clear the CSV file
        File csvFile = new File("rivest_schapire_dfas/rs.csv");

        // Load DFAs from the serialized file
        List<CompactDFA<Character>> loadedDFAs = dfaOperation.loadCompactDFAsFromFile(new File(dfasFolder, "dfas.ser"));

        // List to store learned DFAs using RivestSchapire
        List<DFA<?, Character>> learnedDFAs = new ArrayList<>();

        // Iterate over loaded DFAs and run RivestSchapire on each
//        for (int i = 0; i < loadedDFAs.size(); i++) {
//            CompactDFA<Character> currentDFA = loadedDFAs.get(i);
//
//            System.out.println("Running RivestSchapire on DFA " + (i + 1));
//             // Record the start time
//            long startTime = System.currentTimeMillis();
//            DFA<?, Character> learnedDFA = runRivestSchapire(currentDFA, i + 1);
//            // Record the end time
//            long endTime = System.currentTimeMillis();
//            System.out.println("Time taken: " + (endTime - startTime) + " ms");
//
//            learnedDFAs.add(learnedDFA);
//        }

        try {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {
                // Write header to the CSV file
                writer.write("Index,NumStates, TimeTaken(ms),EquivalenceQueries, MembershipQueries");
                writer.newLine();

                // Iterate over loaded DFAs and run NL* on each
                for (int i = 0; i < loadedDFAs.size(); i++) {
                    CompactDFA<Character> currentDFA = loadedDFAs.get(i);

                    System.out.println("Running RS on DFA " + (i + 1));

                    long startTime = System.currentTimeMillis();
                    DeterministicResult learnedDFA = runRivestSchapire(currentDFA, i + 1);
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
        dfaOperation.saveDFAsToFile(learnedDFAs, new File(rsDFAsFolder, "dfas.ser"));
    }

    private static DeterministicResult runRivestSchapire(CompactDFA<Character> target, int index) {


        // Alphabet for the target DFA
        Alphabet<Character> inputs = dfaOperation.getInputAlphabets(target);

        // Construct a simulator membership query oracle
        MembershipOracle.DFAMembershipOracle<Character> sul = new SimulatorOracle.DFASimulatorOracle<>(target);
//        final MembershipOracle<Character, Boolean> mqo = new SimulatorOracle<>(target);

        // Oracle for counting queries wraps SUL
        DFACounterOracle<Character> mqOracle = new DFACounterOracle<>(sul, "Membership Queries");
//        final CounterOracle<Character, Boolean> mqOracle = new CounterOracle<>(mqo, "Membership Queries");

        // Create a RivestSchapireDFA instance
        RivestSchapireDFA<Character> rs =
                new RivestSchapireDFABuilder<Character>().withAlphabet(inputs)
                        .withOracle(mqOracle)
                        .create();

        // Construct a W-method conformance test
        DFAWMethodEQOracle<Character> wMethod = new DFAWMethodEQOracle<>(mqOracle, EXPLORATION_DEPTH);

        // Construct a learning experiment
        Experiment.DFAExperiment<Character> rsExperiment = new Experiment.DFAExperiment<>(rs, wMethod, inputs);

        // Turn on time profiling
        rsExperiment.setProfile(true);

        // Enable logging of models
        rsExperiment.setLogModels(true);

        // Run the experiment
        rsExperiment.run();


        // Get the learned model
        DFA<?, Character> result = rsExperiment.getFinalHypothesis();

        // Save the learned model to a file in dot format
//        String learnedDFAFileName = "rivest_schapire_dfas/rs_dfa_" + index + ".dot";
//        dfaOperation.generateDOTFileDFA(result, inputs, learnedDFAFileName);
//        dfaOperation.convertDotToPDF(learnedDFAFileName, "rivest_schapire_dfas/rs_dfa_" + index + ".pdf");

        // Print separator
        System.out.println("-------------------------------------------------------");

        // Print profiling results
//        System.out.println("Profiling Results:");
////        System.out.println("Time: " + totalTime);
//        System.out.println(SimpleProfiler.getResults());

        // Print learning statistics
        System.out.println("Learning Statistics:");
        System.out.println(rsExperiment.getRounds().getSummary());

        // Print membership queries statistics
        System.out.println(mqOracle.getStatisticalData().getSummary());

        // Print model statistics
        System.out.println("Model Statistics:");
        System.out.println("States: " + result.size());
        System.out.println("Sigma: " + inputs.size());

        // Print learned model in dot format
//        System.out.println("Learned Model:");
//        GraphDOT.write(result, inputs, System.out);

        // Return the learned model
//        System.out.println("Final observation table:");
//        new ObservationTableASCIIWriter<>().write(rs.getObservationTable(), System.out);

        return new DeterministicResult(result, rsExperiment.getRounds().getCount(), mqOracle.getCount());
    }
}
