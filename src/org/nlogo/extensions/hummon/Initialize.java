package org.nlogo.extensions.hummon;

import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Link;
import org.nlogo.agent.Turtle;
import org.nlogo.agent.World;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultReporter;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;

/**
 * Initialize the JUNG network and all the Java structures that holds data in a
 * single run of the model.
 *
 * @author Simone Gabbriellini
 */
public class Initialize extends DefaultReporter {

    /**
     *
     * @param argmnts
     * @param cntxt
     * @return 
     * @throws ExtensionException
     * @throws LogoException
     */
    @Override
    public Double report(Argument[] argmnts, Context cntxt) throws ExtensionException, LogoException {

        // retrieve world
        CoreModel.world = (World) cntxt.getAgent().world();
        // create graph with N turtles and L links
        CoreModel.g = new UndirectedSparseGraph<Long, String>();
        // create as many nodes as turtles in netlogo
        AgentSet.Iterator turtleIterator = CoreModel.world.turtles().iterator();
        //
        while (turtleIterator.hasNext()) {
            //
            Turtle node = (Turtle) turtleIterator.next();
            //
            long id = node.id;
            //
            CoreModel.g.addVertex(id);
        }
        // add as many links as links in netlogo - this is only active when density > 0
        AgentSet links = CoreModel.world.links();
        if (links.count() > 0) {
            AgentSet.Iterator linkIterator = CoreModel.world.links().iterator();
            //
            while (linkIterator.hasNext()) {
                //
                Link next = (Link) linkIterator.next();
                //
                long end1 = next.end1().id;
                //
                long end2 = next.end2().id;
                //
                String id = end1 + "-" + end2;
                //
                CoreModel.g.addEdge(id, end1, end2, EdgeType.UNDIRECTED);
            }
        }
        //System.out.println("how many links now " + CoreModel.g.getEdgeCount());
        // precalculate these values
        // set benefit from adding a link
        double benefit = (Double) CoreModel.world.getObserverVariableByName("BENEFIT");
        // set cost of adding a link
        double cost = (Double) CoreModel.world.getObserverVariableByName("COST");
        int N = CoreModel.g.getVertexCount();
        double min = benefit - Math.pow(benefit, 2);
        double max = benefit + ((N - 2) / 2) * Math.pow(benefit, 2);
        //
        if (cost < min) {
            // if cost are less than min expected network is a complete network
            return 2d;
        } else if (cost >= min && cost < max) {
            // if cost is between min and max expected network is a star network
            return 1d;
        } else if (cost >= max) {
            // if cost are higher than max expected network is a null network
            return 0d;
        } else {
            // do nothing
            throw new ExtensionException("value out of bound!");
        }
    }
}
