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
    // revise this on the basis of page 224.
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
        for (Long l : Hummon.g.getVertices()) {
            // 
            degreeVector[l.intValue()] = Hummon.degScor.getVertexScore(l).doubleValue();
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
        //
        structuralFit.add(distNull);
        structuralFit.add(distStar);
        structuralFit.add(distBiStar);
        structuralFit.add(distShared);
        structuralFit.add(distComplete);
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
            // summ(empiric - theoric) ^ 2 
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
