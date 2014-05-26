package org.nlogo.extensions.hummon;

import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Link;
import org.nlogo.agent.Turtle;
import org.nlogo.agent.World;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultCommand;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;

/**
 * Initialize the internal network taking values from the netlogo world.
 *
 * @author Simone Gabbriellini
 */
public class Initialize extends DefaultCommand {

    /**
     *
     * @param argmnts
     * @param cntxt
     * @throws ExtensionException
     * @throws LogoException
     */
    @Override
    public void perform(Argument[] argmnts, Context cntxt) throws ExtensionException, LogoException {

        // retrieve world
        CoreModel.world = (World) cntxt.getAgent().world();
        // set number of nodes
        Number n = (Number) CoreModel.world.getObserverVariableByName("HOW-MANY-TURTLES");
        CoreModel.N = n.intValue();
        // clear previous degree distribution
        CoreModel.degScor = null;
        // set utility ID
        CoreModel.utility = CoreModel.world.turtlesOwnIndexOf("UTILITY");
        // set benefit from adding a link
        CoreModel.benefit = (Double) CoreModel.world.getObserverVariableByName("BENEFIT");
        // set cost of adding a link
        CoreModel.cost = (Double) CoreModel.world.getObserverVariableByName("COST");
        // set tie formation
        CoreModel.tieFormation = (String) CoreModel.world.getObserverVariableByName("TIE-FORMATION");
        // set fitness function
        CoreModel.fitness = CoreModel.world.getObserverVariableByName("FITNESS").toString();
        // set indez of acted? variable
        CoreModel.acted = CoreModel.world.turtlesOwnIndexOf("ACTED?");
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
}
