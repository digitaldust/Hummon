package org.nlogo.extensions.hummon;

import edu.uci.ics.jung.algorithms.scoring.DegreeScorer;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultReporter;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.api.LogoListBuilder;

/**
 * Finds the degrees vector of the network and reports it back into NetLogo
 * @author Simone Gabbriellini
 */
public class DegreeDistribution extends DefaultReporter {

    /**
     * Finds the degrees vector of the Hummon.g network and report the list to NetLogo.
     * 
     * @param argmnts
     * @param cntxt
     * @return a LogoList
     * @throws ExtensionException
     * @throws LogoException
     */
    @Override
    public LogoList report(Argument[] argmnts, Context cntxt) throws ExtensionException, LogoException {
        /** typical outcome of the scorer is a degree vector like:
         * self 0 degree 3.0
         * self 1 degree 3.0
         * self 2 degree 3.0
         * self 3 degree 5.0
         * self 4 degree 5.0
         * self 5 degree 3.0
         * self 6 degree 4.0
         * self 7 degree 5.0
         * self 8 degree 6.0
         * self 9 degree 3.0. */
        // find the degree scorer for the network
        Hummon.degScor = new DegreeScorer<Long>(Hummon.g);
        // build an empty LogoList
        LogoListBuilder degreeDistribution = new LogoListBuilder();
        // for each node in the network
        for (Long t : Hummon.g.getVertices()) {
            // DEBUG
            //System.out.println("self " + t + " degree " + Hummon.degScor.getVertexScore(t).doubleValue());
            // add the corresponding value to the LogoList
            degreeDistribution.add(Hummon.degScor.getVertexScore(t).doubleValue());
        }
        // return the list to NetLogo
        return degreeDistribution.toLogoList();
    }
}
