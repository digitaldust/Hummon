package org.nlogo.extensions.hummon;

import org.nlogo.api.DefaultClassManager;
import org.nlogo.api.PrimitiveManager;

/**
 * Replication of Hummon's "Utility and dynamic social networks".
 * @author Simone Gabbriellini
 * @version 3.0
 */
public class Hummon extends DefaultClassManager {

    
    /**
     * Adds some primitives to the NetLogo environment.
     * @param primitiveManager
     */
    @Override
    public void load(PrimitiveManager primitiveManager) {
        /** reports the best move for a turtle. */
        primitiveManager.addPrimitive("step-core-model", new CoreModel());
        /** reports the degree distribution of the actual network. */
        primitiveManager.addPrimitive("degree-distribution", new DegreeDistribution());
        /** reports the best structural fit for the model when . */
        primitiveManager.addPrimitive("structural-fit", new StructuralFit());
        /** Invoke this primitive to initialize and clear everything. */
        primitiveManager.addPrimitive("initialize", new Initialize());
        /** Invoke this primitive to initialize and clear everything. */
        primitiveManager.addPrimitive("find-expected", new TheoreticalExpectedResults());
    }
}
