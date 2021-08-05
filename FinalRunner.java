/*
FINAL PROJECT PROBLEM 2
MULTITHREADING FOR TRAVELING SALESMAN PROBLEM
THIS 'VERSION' IS FOR DISTANCE IN TIME BETWEEN BUILDINGS

ANNOTATION 12/11/2020 -> AFTER MUCH DELIBERATION AND FAILURE TO RUN AT 13! (13 BUILDINGS) I REVISITED AND CHANGED FROM
A BRUTE FORCE PERMUTATION BUILDER TO "Heap's Algorithm" IMPLEMENTATION. I FOUND SOME GOOD INFO AT THIS LINK AND ADAPTED THE IMPLEMENTATION
FOR ARRAYLIST AND STRING, SO IT RUNS IN MY EXISTING CONSTRUCT
give credit where its due for the logic to implement Heap's Algorithm: https://www.geeksforgeeks.org/heaps-algorithm-for-generating-permutations/

YARRI BRYN
DS730
12/11/2020


NOTE 12/11/2020 1032AM: AS YOU MAY SEE ABOVE THIS WAS DECOMPILED AFTER A DATA LOSS. PLEASE EXCUSE POOR COMMENTS AS THE
    DATA LOSS WAS SIGNIFICANT AND FILE SYSTEM RESTORE NOR GITHUB BACKUP WAS COMPREHENSIVE IN
    SOLVING THE ISSUE BECAUSE OF MANY VERSION CHANGES.
 */
import java.io.*;
import java.util.*;

public class FinalRunner {
    private static int optimalCost = Integer.MAX_VALUE;
    private static ArrayList<String> optimalRoute = new ArrayList<>();

    public FinalRunner() {
    }
    // use sync method to check against results in each thread (selected results :))
    public static synchronized void evaluateBestInThread(ArrayList<String> route, int cost) {
        if (cost < optimalCost) {
            optimalCost = cost;
            optimalRoute = route;
        }

    }
    // use this helper integer as an intermediate check in the thread to allow for abandoning permutations if they have a
    // cost exceeds this threshold
    public static int bestKnownCost() {
        return optimalCost;
    }

    // obviously writes to a file
    public static void writeToFile(ArrayList<String> route, int cost) {
        String routeJoined = String.join(" -> ", route);
        try {
            PrintWriter output = new PrintWriter(new FileWriter("output2.txt"));
            System.out.println("Best Route: " + routeJoined);
            System.out.println("Best Cost: " + cost);
            output.println("optimal route: " + routeJoined + "\ncost for optimal route: "+cost);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        // ------------------------start reading in and creating our static data-----------------------------------
        HashMap<String, ArrayList<Integer>> distanceMatrix = new HashMap<>();
        ArrayList<String> allBldgsData = new ArrayList<>();
        Scanner s = new Scanner(new File("input2.txt"));
        while (s.hasNextLine()) {
            String currentLine = s.nextLine();
            String[] values = currentLine.split(" :? ?"); // splitting on any combination of " " or " :" or "  " or" : "
            ArrayList<Integer> distances = new ArrayList<>(); //create a new ArrayList each time so each person has own list
            // first item in the array is the building so we don't need that
            for (int index = 1; index < values.length; index++) {
                distances.add(Integer.parseInt(values[index]));
            }
            distanceMatrix.put(values[0], distances);
            allBldgsData.add(values[0]);

        }

        // store the first index to a variable to add to front and end of each perm (beacuse we start and end in the same place)
        String firstBldg = allBldgsData.get(0);
        //System.out.println("allBldgsData: "+allBldgsData);
        // store a copy of allBldgsData into a new variable to be consumed by the permutationHelper
        ArrayList<String> trimmedBldgsData = new ArrayList<String>(allBldgsData);
        // pop off the first building in the trimmed arrayList because it has no effect on the permutations,
        // // in fact it reduces dimensionality in the permutations
        trimmedBldgsData.remove(0);
        // the number of threads we need is equal to the number of the buildings we go to next,
        // from our starting building, so the building thats the target oif first -> 'next'
        // rely on the cpu / os to allocate the resources?
        int numWorkers = trimmedBldgsData.size();
        ThreadedPermutations[] workers = new ThreadedPermutations[numWorkers];

        for(int i = 0; i < numWorkers; i++) {
            // make sure we use i to modify trimmedBuildings to pop off the ith elem in the each thread for optimization
            workers[i] = new ThreadedPermutations(i, firstBldg, trimmedBldgsData, allBldgsData, distanceMatrix);
            workers[i].start();
        }
        // Make sure the workers are finished before the Runner finishes.
        for(int i=0; i<workers.length; i++){
            //If the worker thread is already done, this will just continue on to the next thread.
            try{
                workers[i].join();
            } catch(Exception e){
                System.out.println("Something went wrong with worker thread "+i+".");
            }
        }

        writeToFile(optimalRoute, optimalCost);
        System.out.format("Time to run: %.2f seconds", (System.currentTimeMillis() - start) / 1000.0F);
    }
}
