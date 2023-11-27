package com.experiment.dfas;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.NFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.fsa.impl.compact.CompactNFA;
import net.automatalib.util.automata.equivalence.DeterministicEquivalenceTest;
import net.automatalib.util.automata.minimizer.hopcroft.HopcroftMinimization;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.GrowingMapAlphabet;

import java.io.*;
import java.util.*;

public class DFAOperation {

    // generate random DFA
//    public CompactDFA<Character> generateRandomDFA() {
//        // Define the alphabet
//        Alphabet<Character> alphabet = new GrowingMapAlphabet<>();
//
//        // Add symbols to the alphabet
//        alphabet.add('a');
//        alphabet.add('b');
//
//        // Set the number of states and create a random DFA
//        int numStates = 1000;
//        Random random = new Random();
////        DFA<?, Character> randomDFA = RandomAutomata.randomDFA(random, numStates, alphabet);
//        CompactDFA<Character> randomDFA = RandomAutomata.randomDFA(random, numStates, alphabet);
//
//        // Generate DOT file
//        String dotFileName = "randomDFA.dot";
//        generateDOTFile(randomDFA, alphabet, dotFileName);
//
//        return randomDFA;
//    }

    // generate random DFA

    // Generate 5000 random DFAs in which each DFA has at least 5 accepting states and make sure create the Random DFA in which we have 5 to 300 states of DFA
    public CompactDFA<Character> generateRandomDFA() {
        // Define the alphabet
        Alphabet<Character> alphabet = new GrowingMapAlphabet<>();

        // Add symbols to the alphabet
        alphabet.add('a');
        alphabet.add('b');
//        alphabet.add('c');

        // Set the number of states randomly between 10 and 300
        int numStates = new Random().nextInt(291) + 10;
//        int numStates = 100;

        // Ensure there are at least 5 accepting states
        int numAcceptingStates = Math.max(5, new Random().nextInt(numStates));

        // Create a random set of accepting states
        Set<Integer> acceptingStates = new HashSet<>();
        Random random = new Random();
        while (acceptingStates.size() < numAcceptingStates) {
            acceptingStates.add(random.nextInt(numStates));
        }

        // Create a random DFA
        CompactDFA<Character> randomDFA = RandomAutomata.randomDFA(random, numStates, alphabet);

        // Set the accepting states
        acceptingStates.forEach(state -> randomDFA.setAccepting(state, true));

        // Generate DOT file
//        String dotFileName = "randomDFA.dot";
//        generateDOTFile(randomDFA, alphabet, dotFileName);

        // minimize DFA
        CompactDFA<Character> minimizedDFA = minimizeDFA(randomDFA, alphabet);

        return minimizedDFA;
    }

    public List<CompactDFA<Character>> generateMultipleRandomDFAs() {
        List<CompactDFA<Character>> dfaList = new ArrayList<>();

        int numDFAs = 5000;

        // Create a folder for random DFAs
        File dfasFolder = new File("random_dfas");

        // Create the folder if it does not exist
        dfasFolder.mkdirs();

        // Delete previous DOT files and dfas.ser data
        deletePreviousFiles(dfasFolder);

        // Generate and store random DFAs
        for (int i = 0; i < numDFAs; i++) {
            CompactDFA<Character> randomDFA = generateRandomDFA();
            dfaList.add(randomDFA);

            // Save DOT files in folders named random_dfa_i
            String dotFileName = "random_dfas/random_dfa_" + (i + 1) + ".dot";
            generateDOTFileDFA(randomDFA, randomDFA.getInputAlphabet(), dotFileName);
//            convertDotToPDF(dotFileName, "random_dfas/random_dfa_" + (i + 1) + ".pdf");
        }

        // Serialize and save DFAs to a file
        saveCompactDFAsToFile(dfaList, new File(dfasFolder, "dfas.ser"));

        return dfaList;
    }


    public Alphabet<Character> getInputAlphabets(CompactDFA<Character> dfa) {
        return dfa.getInputAlphabet();
    }

    // minimize DFA
    public CompactDFA<Character> minimizeDFA(CompactDFA<Character> dfa, Alphabet<Character> alphabet) {
        CompactDFA<Character> minimizedDFA = HopcroftMinimization.minimizeDFA(dfa, alphabet);
        return minimizedDFA;
    }

    // check equivalence dfa
    public <I> String checkDFAEquivalenceString(DFA<?, I> dfa1, DFA<?, I> dfa2, Alphabet<I> alphabet) {
        // Create an instance of DeterministicEquivalenceTest
        DeterministicEquivalenceTest<I> equivalenceTest = new DeterministicEquivalenceTest<>(dfa1);

        // Check for equivalence and get a separating word
        Word<I> separatingWord = equivalenceTest.findSeparatingWord(dfa2, alphabet);

        if (separatingWord == null) {
            return "DFAs are equivalent";
        } else {
            return "DFAs are not equivalent. Separating word: " + separatingWord;
        }
    }

    // print dfa
    public <S, I> void printDFA(DFA<S, I> dfa, Alphabet<I> alphabet) {
        System.out.println("Random DFA:");

        S initialState = dfa.getInitialState();

        for (S state : dfa.getStates()) {
            System.out.print("State " + state + ": ");

            // Mark the initial state
            if (state.equals(initialState)) {
                System.out.print("(Initial) ");
            }

            // Mark final states
            if (dfa.isAccepting(state)) {
                System.out.print("(Final) ");
            }

            for (I input : alphabet) {
                S successor = dfa.getSuccessor(state, input);
                System.out.print("(" + input + " -> " + successor + ") ");
            }
            System.out.println();
        }
    }


    // generate dot file
    public <S, I> void generateDOTFileDFA(DFA<S, I> dfa, Alphabet<I> alphabet, String fileName) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println("digraph RandomDFA {");

            // Add starting state
            writer.println("  __start0 [label=\"\", shape=none];");
            writer.println("  __start0 -> " + dfa.getInitialState() + " [label=\"\"];");

            for (S state : dfa.getStates()) {
                writer.print("  " + state);

                // Mark the initial state
                if (state.equals(dfa.getInitialState())) {
                    writer.print(" [shape=circle]");
                }

                // Mark final states
                if (dfa.isAccepting(state)) {
                    writer.print(" [shape=doublecircle]");
                }

                writer.println();

                for (I input : alphabet) {
                    S successor = dfa.getSuccessor(state, input);
                    writer.println("  " + state + " -> " + successor + " [label=\"" + input + "\"]");
                }
            }
            writer.println("}");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("DOT file generated: " + fileName);
    }

    // NFA to dot file
    public <S, I> void generateDOTFileNFA(NFA<S, I> nfa, Alphabet<I> alphabet, String fileName) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println("digraph RandomNFA {");

            // Add starting state
            writer.println("  __start0 [label=\"\", shape=none];");

            // Connect starting state to each initial state
            for (S initialState : nfa.getInitialStates()) {
                writer.println("  __start0 -> " + initialState + " [label=\"\"];");
            }

            for (S state : nfa.getStates()) {
                writer.print("  " + state);

                // Mark the initial states
                if (nfa.getInitialStates().contains(state)) {
                    writer.print(" [shape=circle]");
                }

                // Mark final states
                if (nfa.isAccepting(state)) {
                    writer.print(" [shape=doublecircle]");
                }

                writer.println();

                for (I input : alphabet) {
                    for (S successor : nfa.getSuccessors(state, input)) {
                        writer.println("  " + state + " -> " + successor + " [label=\"" + input + "\"]");
                    }
                }
            }
            writer.println("}");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("DOT file generated: " + fileName);
    }


    // convert dot to pdf
    public void convertDotToPDF(String dotFileName, String pdfFileName) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("dot", "-Tpdf", dotFileName, "-o", pdfFileName);
            Process process = processBuilder.start();
            process.waitFor();
            System.out.println("PDF file generated: " + pdfFileName);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void deletePreviousFiles(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.isDirectory()) {
                    file.delete();
                }
            }
        }
        // Delete dfas.ser data
        File dataFile = new File(folder, "dfas.ser");
        dataFile.delete();
    }

    public void saveCompactDFAsToFile(List<CompactDFA<Character>> dfas, File file) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(dfas);
            System.out.println("CompactDFA saved to file: " + file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<CompactDFA<Character>> loadCompactDFAsFromFile(File file) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<CompactDFA<Character>>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void saveDFAsToFile(List<DFA<?, Character>> dfas, File file) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(dfas);
            System.out.println("DFA saved to file: " + file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<DFA<?, Character>> loadDFAFromFile(File file) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<DFA<?, Character>>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void saveCompactNFAsToFile(List<CompactNFA<Character>> nfas, File file) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(nfas);
            System.out.println("CompactNFA saved to file: " + file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<CompactNFA<Character>> loadCompactNFAFromFile(File file) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<CompactNFA<Character>>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void saveNFAsToFile(List<NFA<?, Character>> nfas, File file) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(nfas);
            System.out.println("NFA saved to file: " + file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<NFA<?, Character>> loadNFAFromFile(File file) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<NFA<?, Character>>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
