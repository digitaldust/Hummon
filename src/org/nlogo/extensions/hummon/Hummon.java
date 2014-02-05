/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nlogo.extensions.hummon;

import edu.uci.ics.jung.algorithms.scoring.DegreeScorer;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import org.nlogo.api.DefaultClassManager;
import org.nlogo.api.PrimitiveManager;

/**
 * Replication of Hummon's "Utility and dynamic social networks".
 * @author Simone Gabbriellini
 * @version 1.0
 */
public class Hummon extends DefaultClassManager {

    /** The graph to hold */
    public static UndirectedSparseGraph<Long, String> g = null;
    /**
     *
     */
    public static DegreeScorer<Long> degScor = null;
    /**
     * 
     * @param primitiveManager
     */
    @Override
    public void load(PrimitiveManager primitiveManager) {
        /** . */
        primitiveManager.addPrimitive("step-core-model", new CoreModel());
        /** . */
        primitiveManager.addPrimitive("degree-distribution", new DegreeDistribution());
        /** . */
        primitiveManager.addPrimitive("structural-fit", new StructuralFit());
        /** Invoke this primitive to initialize and clear everything. */
        primitiveManager.addPrimitive("initialize", new Initialize());
    }
}
