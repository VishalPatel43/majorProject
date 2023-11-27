package com.experiment;

import com.experiment.algorithms.LStarAlgorithm;
import com.experiment.algorithms.NLStarAlgorithm;
import com.experiment.algorithms.RivestSchapireAlgorithm;
import com.experiment.dfas.DFAOperation;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;

import java.io.*;
import java.util.List;

public class Main {

    private static final DFAOperation dfaOperation = new DFAOperation();

    private static void randomDFAs() {
        // Create a folder for random DFAs
        File dfasFolder = new File("random_dfas");

        DFAOperation dfaOperation = new DFAOperation();

        // Ensure lstar_dfas folder exists
        if (!dfasFolder.exists()) {
            // create the folder if it does not exist
            dfasFolder.mkdirs();
        } else {
            // Clear existing files (Delete previous DOT files and dfas.ser data)
            dfaOperation.deletePreviousFiles(dfasFolder);
        }

        List<CompactDFA<Character>> dfaList = dfaOperation.generateMultipleRandomDFAs();
    }

    private static void learningAlgorithms() throws IOException {
        String algorithmName = "lstar";
//        String algorithmName = "rs";
//        String algorithmName = "nlstar";

        switch (algorithmName) {
            case "lstar":
                LStarAlgorithm.lStarAlgorithm();
                break;
            case "rs": // RivestSchapire
                RivestSchapireAlgorithm.rsAlgorithm();
                break;
            case "nlstar":
                NLStarAlgorithm.nlStarAlgorithm();
                break;
            default:
                System.out.println("Invalid algorithm name");
                break;
        }
    }

    public static void main(String[] args) throws IOException {

        // Create random DFAs
//        randomDFAs();

        // Run learning algorithms
//        learningAlgorithms();

        LStarAlgorithm.lStarAlgorithm();
        RivestSchapireAlgorithm.rsAlgorithm();
        NLStarAlgorithm.nlStarAlgorithm();


//         Later, you can load the DFAs from the file
//        List<CompactDFA<Character>> loadedDFAs = dfaOperation.loadCompactDFAsFromFile(new File(dfasFolder, "dfas.ser"));


        // Now you can use the loaded DFAs as needed


        // check equivalence
//        String answer = dfaOperation.checkDFAEquivalenceString(dfaList.get(1), loadedDFAs.get(0), loadedDFAs.get(0).getInputAlphabet());
//        System.out.println(answer);


    }
}

// Make program which first generate the random DFAs or take the previously generated DFAs (which ask the number of states, alphabets)
// After that it's take that DFAs
// ask which algorithm should work on it


// Run the loop which takes all random dfa and runs L* on it then NL* then RS(L*)
// Store this into CSV file and then plot the graph
// Compare the results of L* and NL* and RS(L*) and see which one is better
// Take the number of Rounds (Equivalence queries) and number of Membership queries, Automata size and time taken
