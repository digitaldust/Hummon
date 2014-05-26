package org.nlogo.extensions.hummon;

import java.util.Arrays;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultReporter;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoListBuilder;

/**
 * Measure distance from ideal-typical network structures.
 *
 * @author Simone Gabbriellini
 */
public class StructuralFit extends DefaultReporter {

    /**
     *
     * @param argmnts
     * @param cntxt
     * @return
     * @throws ExtensionException
     * @throws LogoException
     */
    @Override
    // revise this on the basis of page 234.
    public Object report(Argument[] argmnts, Context cntxt) throws ExtensionException, LogoException {
        // 0: null, 1: star, 2: shared, 3: complete
        LogoListBuilder structuralFit = new LogoListBuilder();
        // distance from empty network
        double distNull;
        // distance from star
        double distStar;
        // distance from bi-star
        double distBiStar;
        // distance from shared
        double distShared;
        // distance from complete
        double distComplete;
        // retrieve the degree vector
        double[] degreeVector = new double[CoreModel.N];
        // for each node in the network
        for (Long l : CoreModel.g.getVertices()) {
            // 
            degreeVector[l.intValue()] = CoreModel.degScor.getVertexScore(l).doubleValue();
        }
        // sort the degree order - sort asc!!!
        Arrays.sort(degreeVector);
        // compare to ideal null
        double[] idealNullVector = findIdealNullVector();
        // compare to ideal star
        double[] idealStarVector = findIdealStarVector();
        // compare to ideal bi-star
        double[] idealBiStarVector = findIdealBiStarVector();
        // compare to ideal shared
        double[] idealSharedVector = findIdealSharedVector(degreeVector);
        // compare to ideal complete
        double[] idealCompleteVector = findIdealCompleteVector();
        // returns
        distNull = findMeanSquaredDeviation(degreeVector, idealNullVector);
        distStar = findMeanSquaredDeviation(degreeVector, idealStarVector);
        distBiStar = findMeanSquaredDeviation(degreeVector, idealBiStarVector);
        distShared = findMeanSquaredDeviation(degreeVector, idealSharedVector);
        distComplete = findMeanSquaredDeviation(degreeVector, idealCompleteVector);
        // pick the one that is the most right...
        // null = 0; star = 1; bistar = 2; shared = 3; complete = 4;
        String outcome = "none";
        // minimum value
        double min = Double.POSITIVE_INFINITY;
        // check if an ideal configuration has been reached
        if (distNull == 0) {
            outcome = "null";
        } else if (distComplete == 0) {
            outcome = "complete";
        } else if (distShared == 0) {
            outcome = "shared";
        } else if (distBiStar == 0) {
            outcome = "bistar";
        } else if (distStar == 0) {
            outcome = "star";
        } else {
            if (distNull < min) {
                // do nothing
                outcome = "near-null";
                min = distNull;
            }
            if (distComplete < min) {
                outcome = "near-complete";
                min = distComplete;
            }
            if (distShared < min) {
                outcome = "near-shared";
                min = distShared;
            }
            if (distBiStar < min) {
                outcome = "near-bistar";
                min = distBiStar;
            }
            if (distStar < min) {
                outcome = "near-star";
            }
        }
        // 
        structuralFit.add(outcome);           // 0
        structuralFit.add(distNull);        // 1
        structuralFit.add(distStar);        // 2
        structuralFit.add(distBiStar);      // 3
        structuralFit.add(distShared);      // 4
        structuralFit.add(distComplete);    // 5
        // return logolist
        return structuralFit.toLogoList();
    }

    private double[] findIdealSharedVector(double[] degreeVector) {
        double mean = 0;
        for (int i = 0; i < degreeVector.length; i++) {
            mean += degreeVector[i];
        }
        mean = Math.round(mean / degreeVector.length);
        double[] idealSharedVector = new double[CoreModel.N];
        for (int i = 0; i < CoreModel.N; i++) {
            idealSharedVector[i] = mean;
        }
        return idealSharedVector;
    }

    private double findMeanSquaredDeviation(double[] degreeVector, double[] otherVector) {
        double results = 0.0;
        for (int i = 0; i < CoreModel.N; i++) {
            // sum(empiric - theoric) ^ 2 
            results += Math.pow((degreeVector[i] - otherVector[i]), 2);
        }
        return results / CoreModel.N;
    }

    private double[] findIdealStarVector() {
        double[] idealStarVector = new double[CoreModel.N];
        for (int i = 0; i < CoreModel.N; i++) {
            if (i == CoreModel.N - 1) { // because degreeVector is sorted ASC!!!
                idealStarVector[i] = CoreModel.N - 1;
            } else {
                idealStarVector[i] = 1;
            }
        }
        return idealStarVector;
    }

    private double[] findIdealBiStarVector() {
        double[] idealBiStarVector = new double[CoreModel.N];
        for (int i = 0; i < CoreModel.N; i++) {
            if (i == CoreModel.N - 1 || i == CoreModel.N - 2) { // because degreeVector is sorted ASC!!!
                idealBiStarVector[i] = CoreModel.N - 1;
            } else {
                idealBiStarVector[i] = 1;
            }
        }
        return idealBiStarVector;
    }

    private double[] findIdealCompleteVector() {
        double[] idealCompleteVector = new double[CoreModel.N];
        for (int i = 0; i < CoreModel.N; i++) {
            idealCompleteVector[i] = CoreModel.N - 1;
        }
        return idealCompleteVector;
    }

    private double[] findIdealNullVector() {
        double[] idealNullVector = new double[CoreModel.N];
        for (int i = 0; i < CoreModel.N; i++) {
            idealNullVector[i] = 0;
        }
        return idealNullVector;
    }

}
