package org.nlogo.extensions.hummon;

import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultReporter;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;

/**
 * Returns the theoretical network models expected from the combination of cost
 * and benefits.
 *
 * @author Simone Gabbriellini
 */
public class TheoreticalExpectedResults extends DefaultReporter {

    /**
     * Returns the theoretical network models expected from the combination of
     * cost and benefits according to the equations in Hummon (2000) page 224.
     *
     * @param argmnts
     * @param cntxt
     * @return a string
     * @throws ExtensionException
     * @throws LogoException
     */
    @Override
    public String report(Argument[] argmnts, Context cntxt) throws ExtensionException, LogoException {
        // precalculate these values
        double min = CoreModel.benefit - Math.pow(CoreModel.benefit, 2);
        double max = CoreModel.benefit + ((CoreModel.N - 2) / 2) * Math.pow(CoreModel.benefit, 2);
        //
        if (CoreModel.cost < min) {
            // if cost are less than min expected network is a complete network
            return "complete";
        } else if (CoreModel.cost >= min && CoreModel.cost < max) {
            // if cost is between min and max expected network is a star network
            return "star";
        } else if (CoreModel.cost >= max) {
            // if cost are higher than max expected network is a null network
            return "null";
        } else {
            // do nothing
            throw new ExtensionException("value out of bound!");
        }
    }
}
