/*
ANNOTATION 12/11/2020 -> AFTER MUCH DELIBERATION AND FAILURE TO RUN AT 13! (13 BUILDINGS) I REVISITED AND CHANGED FROM
A BRUTE FORCE PERMUTATION BUILDER TO "Heap's Algorithm" IMPLEMENTATION. 
I FOUND SOME GOOD INFO AT THIS LINK AND ADAPTED THE IMPLEMENTATION
FOR ARRAYLIST AND STRING, SO IT RUNS IN MY EXISTING CONSTRUCT

I give credit where its due for the logic to implement 
Heap's Algorithm: https://www.geeksforgeeks.org/heaps-algorithm-for-generating-permutations/

YARRI BRYN
DS730
12/11/2020

*/

import java.util.*;

public class ThreadedPermutations extends Thread {
    // the i-th value for the filtered arrayList, which we need to use here (in popping off that element as a constant.
    private int outerIndex;
    // filtered arraylist for this thread.
    private ArrayList<String> trimmedBldgsData;
    // the first building that is, on thread start, not in our evaulation beacuse it is a constant (we start/end there)
    private String firstBldg;
    // original ordered building list and the hashmap containing the NxN distance matrix corresponding to N buildings
    private ArrayList<String> allBldgsData;
    private HashMap<String, ArrayList<Integer>> distanceMatrix;
    // instantiate a bestScore value that we work on in this thread only
    private static int bestCost = Integer.MAX_VALUE;


    // create constructor for the runner
    public ThreadedPermutations(int outerIndex, String firstBldg, ArrayList<String> trimmedBldgsData, ArrayList<String> allBldgsData, HashMap<String, ArrayList<Integer>> distanceMatrix) {
        this.outerIndex = outerIndex;
        this.trimmedBldgsData = trimmedBldgsData;
        this.firstBldg = firstBldg;
        this.allBldgsData = allBldgsData;
        this.distanceMatrix = distanceMatrix;
    }

    // --- call this method later to check each permutation. attempted to improve performance by implementing a stop if
    // --- we no longer are better than the best cost overall
    public static int checkPermutation(ArrayList<String> allBldgsData, ArrayList<String> route, HashMap<String, ArrayList<Integer>> distanceMatrix) {
        int routeCost = 0;
        for(int j = 1; j < route.size(); j++) {
            // thisKey is our last key, so we can get the distance to the next (current) key as defined by the j-th element.
            String thisKey = allBldgsData.get(allBldgsData.indexOf(route.get(j - 1)));
            // check our routecost against the master bestCost. if we have exceeded it in any permutation then
            // we can just break and return some unusable value as a truth criterion for further passing over the result
            if (routeCost >= FinalRunner.bestKnownCost()) {
                return -1;
            }
            // if we get here, we still have a viable route and should keep calcuating the routeCost
            if (j == route.size() - 1) {
                routeCost += distanceMatrix.get(thisKey).get(0);
            } else {
                routeCost += distanceMatrix.get(thisKey).get(allBldgsData.indexOf(route.get(j)));
            }
        }
        return routeCost;
    }

    // THIS IS WHERE HEAP'S ALGORITHM IS BUILT OUT AS MY PERMUTATION GENERATOR METHOD
    public static void heapsAlgorithm(ArrayList<String> a, int size, int n, ArrayList<String> allBldgsData, HashMap<String, ArrayList<Integer>> distanceMatrix, String firstBldg, String nextBldg) {
        // once size decrements to 1, we have a permutation.
        // at this point we want to test this permutation and see if its the best route so far, if so
        // then we can call the synchronized method to evaluate at the master level
        if (size == 1) {
            // by entering at this point we have some ArrayList that is ready to eval
            ArrayList<String> testRoute = new ArrayList<>(a);
            // need to add back our constants -> start, next (2nd) bldg according to this thread, end (same as start)
            testRoute.add(0,firstBldg);
            testRoute.add(1,nextBldg);
            testRoute.add(firstBldg);
            int testCost = checkPermutation(allBldgsData, testRoute, distanceMatrix);

            // so long as we dont have -1 as our testCost, our test cost is better than
            // the stored value in FinalRunner for the best cost so far
            if (testCost != -1) {
                // sanity check in case we released the sync and more was written (not sure if this happens?)
                if (testCost < FinalRunner.bestKnownCost()) {
                    FinalRunner.evaluateBestInThread(testRoute, testCost);
                }
                bestCost = FinalRunner.bestKnownCost();
            }
        }

        // so long as our size > 1, we recursively traverse the potential permutations :)
        // i still get tripped up by recursive functions!
        for (int i = 0; i < size; i++) {
            heapsAlgorithm(a, size-1, n, allBldgsData, distanceMatrix, firstBldg, nextBldg);

            // if size is odd, we swap first and last elems, else if size is even we swap i-th and last elems
            if (size % 2 == 1) {
                String temp = a.get(0);
                a.set(0, a.get(size-1));
                a.set(size-1, temp);
            } else {
                String temp = a.get(i);
                a.set(i, a.get(size-1));
                a.set(size-1, temp);
            }
        }
    }


    // heaps algorithm substitutes for the previous triple loop in the following run method.
    public void run() {
        String nextBldg = this.trimmedBldgsData.get(this.outerIndex);
        ArrayList<String> finalTrimBldgs = new ArrayList<>(this.trimmedBldgsData);
        finalTrimBldgs.remove(this.outerIndex);
        int size = finalTrimBldgs.size();

        // call the heaps algorithm method defined above
        heapsAlgorithm(finalTrimBldgs, size, size, allBldgsData, distanceMatrix, firstBldg, nextBldg);
    }
}