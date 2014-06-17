package org.nlogo.extensions.hummon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultReporter;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.util.MersenneTwisterFast;

/**
 * Measures distance from ideal-typical network structures.
 *
 * @author Simone Gabbriellini
 */
public class StructuralFit extends DefaultReporter {

    // hols how many turtles there are in the network
    int n;
    // holds mean degree of actual network
    double mean;
    double distNull;
    double distStar;
    double distBiStar;
    double distTriStar;
    double distFourStar;
    double distShared;
    double distComplete;

    /**
     * Reports a LogoList with the values of the best fitting structure, as well
     * as values for all the other structures.
     *
     * @param argmnts
     * @param cntxt
     * @return LogoList
     * @throws ExtensionException
     * @throws LogoException
     */
    @Override
    // revise this on the basis of page 234.
    public Object report(Argument[] argmnts, Context cntxt) throws ExtensionException, LogoException {
        // 0: null, 1: star, 2: shared, 3: complete
        LogoListBuilder structuralFit = new LogoListBuilder();
        // number of nodes in the network
        n = CoreModel.g.getVertexCount();
        // retrieve the degree vector
        int[] degreeVector = new int[n];
        // for each node in the network
        for (Long l : CoreModel.g.getVertices()) {
            // fill the degree vector
            degreeVector[l.intValue()] = CoreModel.g.degree(l);
        }
        // mean degree 
        mean = findMeanDegree(degreeVector);
        // sort the degree order - sort asc!!!
        Arrays.sort(degreeVector);
        // compare actual and ideal degree vectors
        distNull = findMeanSquaredDeviation(degreeVector, findIdealNullVector());
        distStar = findMeanSquaredDeviation(degreeVector, findIdealStarVector());
        distBiStar = findMeanSquaredDeviation(degreeVector, findIdealBiStarVector());
        distTriStar = findMeanSquaredDeviation(degreeVector, findIdealTriStarVector());
        distFourStar = findMeanSquaredDeviation(degreeVector, findIdealFourStarVector());
        distShared = findMeanSquaredDeviation(degreeVector, findIdealSharedVector());
        distComplete = findMeanSquaredDeviation(degreeVector, findIdealCompleteVector());
        // check for flickering condition
        
//        if((Double)CoreModel.world.getObserverVariableByName("GENERATIONS")==50d){
//            // find the right result
//            outcome = findNear(cntxt.getRNG());
//        } else {
//            
//        }
        // find the right result
        String outcome = findMin(cntxt.getRNG());
        // add to the logolist
        structuralFit.add(outcome);         // 0
        structuralFit.add(distNull);        // 1
        structuralFit.add(distStar);        // 2
        structuralFit.add(distBiStar);      // 3
        structuralFit.add(distShared);      // 4
        structuralFit.add(distComplete);    // 5
        structuralFit.add(distTriStar);     // 6
        structuralFit.add(distFourStar);    // 7
        // return logolist
        return structuralFit.toLogoList();
    }

    private double findMeanDegree(int[] degreeVector) {
        // reset mean before for this calculation
        mean = 0;
        for (int i = 0; i < degreeVector.length; i++) {
            mean += degreeVector[i];
        }
        return Math.round(mean / degreeVector.length);
    }

    private double[] findIdealSharedVector() {
        double[] idealSharedVector = new double[n];
        for (int i = 0; i < n; i++) {
            idealSharedVector[i] = mean;
        }
        return idealSharedVector;
    }

    private double findMeanSquaredDeviation(int[] degreeVector, double[] otherVector) {
        double results = 0.0;
        for (int i = 0; i < n; i++) {
            // sum(empiric - theoric) ^ 2 
            results += Math.pow((degreeVector[i] - otherVector[i]), 2);
        }
        return results / n;
    }

    private double[] findIdealStarVector() {
        double[] idealStarVector = new double[n];
        for (int i = 0; i < n; i++) {
            if (i == n - 1) { // because degreeVector is sorted ASC!!!
                idealStarVector[i] = n - 1;
            } else {
                idealStarVector[i] = 1;
            }
        }
        return idealStarVector;
    }

    private double[] findIdealBiStarVector() {
        double[] idealBiStarVector = new double[n];
        for (int i = 0; i < n; i++) {
            if (i == n - 1 || i == n - 2) { // because degreeVector is sorted ASC!!!
                idealBiStarVector[i] = n - 1;
            } else {
                idealBiStarVector[i] = 1;
            }
        }
        return idealBiStarVector;
    }
    
    private double[] findIdealTriStarVector() {
        double[] idealTriStarVector = new double[n];
        for (int i = 0; i < n; i++) {
            if (i == n - 1 || i == n - 2 || i == n - 3) { // because degreeVector is sorted ASC!!!
                idealTriStarVector[i] = n - 1;
            } else {
                idealTriStarVector[i] = 1;
            }
        }
        return idealTriStarVector;
    }
    
    private double[] findIdealFourStarVector() {
        double[] idealFourStarVector = new double[n];
        for (int i = 0; i < n; i++) {
            if (i == n - 1 || i == n - 2 || i == n - 3 || i == n -  4) { // because degreeVector is sorted ASC!!!
                idealFourStarVector[i] = n - 1;
            } else {
                idealFourStarVector[i] = 1;
            }
        }
        return idealFourStarVector;
    }

    private double[] findIdealCompleteVector() {
        double[] idealCompleteVector = new double[n];
        for (int i = 0; i < n; i++) {
            idealCompleteVector[i] = n - 1;
        }
        return idealCompleteVector;
    }

    private double[] findIdealNullVector() {
        double[] idealNullVector = new double[n];
        for (int i = 0; i < n; i++) {
            idealNullVector[i] = 0;
        }
        return idealNullVector;
    }

    /**
     * Find the actual result, check first for ideal-typical outcome then for
     * near perfect results.
     *
     * @param results, the list of distances from ideal results
     * @return a string with the name of the right value
     * @exception ExtensionException
     */
    private String findMin(MersenneTwisterFast rng) throws ExtensionException {
       
        // if it's null
        if (distNull == 0d && mean == 0d) {
            return "null";
        }
        // if it's complete
        if (distComplete == 0d && mean == (double) n - 1) {
            return "complete";
        }
        // if it's shared
        if (distShared == 0d && mean != 0d && mean != (double) (n - 1)) {
            return "shared";
        }
        // if it's star
        if (distStar == 0d) {
            return "star";
        }
        // if it's bistar
        if (distBiStar == 0d) {
            return "bistar";
        }
        if (distTriStar == 0d) {
            return "tristar";
        }
        if (distFourStar == 0d) {
            return "fourstar";
        }
        // 
        return findNear(rng);
    }

    private String findNearResult(Integer id) {
        String result;
        if (id == 0) {
            result = "near-null";
        } else if (id == 1) {
            result = "near-complete";
        } else if (id == 2) {
            result = "near-shared";
        } else if (id == 3) {
            result = "near-star";
        } else if (id == 4){
            result = "near-bistar";
        } else if (id == 5){
            result = "near-tristar";
        } else {
            result = "near-fourstar";
        }
        return result;
    }

    private String findNear(MersenneTwisterFast rng) {
        // if it's not a typical structure
        ArrayList<Double> values = new ArrayList<Double>(5);
        values.add(0, distNull);
        values.add(1, distComplete);
        values.add(2, distShared);
        values.add(3, distStar);
        values.add(4, distBiStar);
        values.add(5, distTriStar);
        values.add(6, distFourStar);
        ArrayList<Integer> selectables = new ArrayList<Integer>(5);
        Double min = Collections.min(values);
        for (int i=0; i<values.size();i++) {
            if (values.get(i).equals(min)) {
                selectables.add(i);
            }
        }
        if (selectables.size() == 1) {
            // 
            Integer id = selectables.get(0);
            // 
            return findNearResult(id);
        } else {
            // preference for star, because when valuesOf star = shared = bistar, then
            // you need to move a link to have a perfect star, but you need to ADD a link to
            // have a shared or bistar, thus the star is the most close structure, givent the
            // present amount of links
            if (selectables.contains(3)) {
                // 
                return "near-star";
            } else {
                // 
                Integer id = selectables.get(rng.nextInt(selectables.size()));
                // 
                return findNearResult(id);
            }
        }
    }
}
