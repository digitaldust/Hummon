package org.nlogo.extensions.hummon;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.util.ArrayList;
import java.util.List;
import org.nlogo.agent.Turtle;
import org.nlogo.agent.World;
import org.nlogo.api.AgentException;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultReporter;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.api.Syntax;

/**
 *
 * @author Simone Gabbriellini
 * @version 2
 * @see Hummon (2000) Utility and Dynamic Social Networks. Social Networks
 * 22:221-249
 */
public class CoreModel extends DefaultReporter {

    /**
     * how much benefit comes with a new link.
     */
    public static double benefit;
    /**
     * how much does it cost to have a link.
     */
    public static double cost;
    /**
     * one out of two, old or new, with the new one distance is less penalized.
     */
    public static String fitness;
    /**
     * set a maximum for the number of links a turtle can have.
     */
    public static boolean useDegree;
    /**
     * holds network size.
     */
    public static int N;
    /**
     * just holds the index of this variable for turtles.
     */
    public static int utility;
    /**
     * just holds the index of this variable for turtles.
     */
    public static int acted;
    /**
     * one of: - unilateral add and remove - unilateral add mutual remove -
     * mutual add unilateral remove - mutual add and remove.
     */
    public static String tieFormation;
    /**
     * holds NetLogo world.
     */
    public static World world;

    /**
     * syntax to be used for this reporter.
     *
     * @return
     */
    @Override
    public Syntax getSyntax() {
        // 
        return Syntax.reporterSyntax(new int[]{Syntax.NumberType()}, Syntax.ListType());
    }

    /**
     * Step Hummon's model.
     *
     * @param argmnts
     * @param cntxt
     * @return
     * @throws ExtensionException
     * @throws LogoException
     */
    @Override
    public LogoList report(Argument[] argmnts, Context cntxt) throws ExtensionException, LogoException {

        // set benefit from adding a link
        CoreModel.benefit = (Double) CoreModel.world.getObserverVariableByName("BENEFIT");
        // set cost of adding a link
        CoreModel.cost = (Double) CoreModel.world.getObserverVariableByName("COST");
        // get fitness function
        CoreModel.fitness = CoreModel.world.getObserverVariableByName("FITNESS").toString();
        // use a limit to number of links for each turtle
        CoreModel.useDegree = (Boolean) CoreModel.world.getObserverVariableByName("USE-DEGREE");
        // get tie formation method
        CoreModel.tieFormation = CoreModel.world.getObserverVariableByName("TIE-FORMATION").toString();
        // Pick a random turtle's id
        long self = argmnts[0].getIntValue();
        // result logolist
        LogoList behave = null;
        // behave differently according to tie formation rule
        if (tieFormation.equals("u-add u-remove")) {
            try {
                // behave according to unilateral tie formation
                behave = findBehaveUnilateral(self, world);
            } catch (AgentException ex) {
                // throws exception
                throw new ExtensionException(ex);
            }
        } else if (tieFormation.equals("m-add m-remove")) {
            try {
                // TODO: not implemented yet
                behave = findBehaveMutual();
            } catch (AgentException ex) {
                // throws exception
                throw new ExtensionException(ex);
            }
        } else if (tieFormation.equals("u-add m-remove")) {
            try {
                // TODO: not implemented yet
                behave = findBehaveUnilateralMutual();
            } catch (AgentException ex) {
                // throws exception
                throw new ExtensionException(ex);
            }
        } else {
            try {
                // TODO: not implemented yet
                behave = findBehaveMutualUnilateral();
            } catch (AgentException ex) {
                // throws exception
                throw new ExtensionException(ex);
            }
        }
        // do this only if the network has been changed
        if (!behave.get(0).equals("status-quo")) {
            try {
                // update network stats
                updateNetworkUtilities();
            } catch (AgentException ex) {
                // throws exception
                throw new ExtensionException(ex);
            }
        }
        // returns a LogoList with:
        // what to do: either "status-quo", "add" or "remove"
        // focal agent: myself 
        // other agent: self
        return behave;
    }

    private LogoList findBehaveUnilateral(Long self, World world) throws AgentException, ExtensionException {
        // holds the list of actors
        Long[] actors = new Long[CoreModel.N];
        // holds what to do with each actor
        String[] whatToDo = new String[CoreModel.N];
        // holds the utility for each actor
        double[] utilities = new double[CoreModel.N];
        // utility of self 
        utilities[0] = (Double) world.getTurtle(self).getVariable("UTILITY");
        // limit of self
        double limit = (Double) world.getTurtle(self).getVariable("MAX-TIES");
        // what to do with self, i.e. status quo
        whatToDo[0] = "status-quo";
        // placeholder for first position
        actors[0] = self;
        // self acted
        world.getTurtle(self).setTurtleVariable(acted, (Boolean) true);
        // initialize counter at 1, i.e. 0 + self
        int counter = 1;
        // pick the degree limit of this agent
        double actual = Hummon.g.degree(self);
        // then find all possible modification to the network 
        for (Long t : Hummon.g.getVertices()) {
            // crea un array con le possibili soluzioni e poi decidi
            if (!t.equals(self)) {
                // if there is a limit on max number of links for the agent
                if (useDegree) {
                    // limit exceeded, flag all the removable links
                    if (limit - actual < 0) {
                        //System.out.println(limit-actual);
                        // check if self an t are linked
                        if (Hummon.g.isNeighbor(self, t)) {
                            // try to remove the link
                            utilities[counter] = 10000;
                            whatToDo[counter] = "remove";
                            actors[counter] = t;
                        } else {
                            utilities[counter] = 0;
                            whatToDo[counter] = "status-quo";
                            actors[counter] = t;
                        }
                    } else {
                        // if there is no path, try to add it
                        if (!Hummon.g.isNeighbor(self, t)) {
                            double limitt = (Double) world.getTurtle(t).getVariable("MAX-TIES");
                            // try to add only if t can accept it according to limitt
                            if (Hummon.g.degree(t) < limitt) {
                                // try to add a link and see what happens
                                utilities[counter] = makeChangesUnilateral(self, t, true);
                                whatToDo[counter] = "add";
                                actors[counter] = t;
                            }
                            // else if there is a path, try to remove it
                        } else {
                            // try to remove the link
                            utilities[counter] = makeChangesUnilateral(self, t, false);
                            whatToDo[counter] = "remove";
                            actors[counter] = t;
                        }
                    }
                } else { // if there is NOT a limit on number of links for the agent (degree can be up to N-1)
                    // if there is no path, try to add it
                    if (!Hummon.g.isNeighbor(self, t)) {
                        // try to add a link and see what happens
                        utilities[counter] = makeChangesUnilateral(self, t, true);
                        whatToDo[counter] = "add";
                        actors[counter] = t;
                        // else if there is a path, try to remove it
                    } else {
                        // try to remove the link
                        utilities[counter] = makeChangesUnilateral(self, t, false);
                        whatToDo[counter] = "remove";
                        actors[counter] = t;
                    }
                }
                counter += 1;
            }
        }
        // DEBUG
//        for(int i=0;i<actors.length;i++){
//            System.out.println(self + " " + actors[i] + " what " + whatToDo[i] + " utility " + utilities[i]);
//        }
        // 
        int winner = findMaxUtility(utilities, world);
        // results
        LogoListBuilder results = new LogoListBuilder();
        String id = makeEdgeId(self, actors[winner]);
        if (whatToDo[winner].equals("status-quo")) {
            // nothing to do
            results.add("status-quo");
            results.add(self.doubleValue());
            results.add(actors[winner].doubleValue());
        } else if (whatToDo[winner].equals("add")) {
            // add the link
            boolean addEdge = Hummon.g.addEdge(id, actors[winner], self, EdgeType.UNDIRECTED);
            //System.out.println("aggiungo il link " + addEdge);
            // build result
            results.add("add");
            results.add(self.doubleValue());
            results.add(actors[winner].doubleValue());
        } else {
            // remove the link
            boolean removeEdge = Hummon.g.removeEdge(id);
            //System.out.println("rimuovo il link " + id + " " + removeEdge);
            // build results
            results.add("remove");
            if (self.doubleValue() < actors[winner].doubleValue()) {
                results.add(self.doubleValue());
                results.add(actors[winner].doubleValue());
            } else {
                results.add(actors[winner].doubleValue());
                results.add(self.doubleValue());
            }
        }
        // return what to do
        return results.toLogoList();
    }

    /**
     * Explore what happens when a node try to add/remove a link unilaterally.
     * @param self
     * @param t
     * @param b boolean value, either add (true) or remove (false)
     * @return double
     * @throws ExtensionException
     */
    private double makeChangesUnilateral(Long self, Long t, boolean b) throws ExtensionException {

        // create the label for this link
        String id = makeEdgeId(self, t);
        // holds result of remove or adding procedure
        boolean manipulate;
        if (b) {// if a link must be added
            // add the link
            manipulate = Hummon.g.addEdge(id, t, self, EdgeType.UNDIRECTED);
        } else {// if a link must be removed
            // remove the link
            manipulate = Hummon.g.removeEdge(id);
        }
        // if adding/removing went wrong (manipulate=false)
        if (!manipulate) {
            // raise exception
            throw new ExtensionException("Failed to add/remove an edge.");
        }
        // find new distances after adding/deletion
        DijkstraDistance<Long, String> alg = new DijkstraDistance<Long, String>(Hummon.g);
        // calculate new utility for self
        double u = findUtility(alg, self);
        // restore initial conditions
        if (b) {// if a link has been added
            // remove it to restore initial conditions
            manipulate = Hummon.g.removeEdge(id);
        } else {// if a link has been removed
            // add it to restore initial conditions
            manipulate = Hummon.g.addEdge(id, t, self, EdgeType.UNDIRECTED);
        }
        // if adding/removing went wrong (manipulate=false)
        if (!manipulate) {
            // raise exception
            throw new ExtensionException("Failed to add/remove an edge.");
        }
        // return the utility value after this manipulation
        return u;
    }

    /**
     * Calculates new utility values for each turtle. Does not need to return 
     * anything because it pushes new utility values to the turtles in NetLogo.
     * @throws AgentException
     */
    public static void updateNetworkUtilities() throws AgentException {
        // find distances after all modifications
        DijkstraDistance<Long, String> alg = new DijkstraDistance<Long, String>(Hummon.g);
        // for each node in the network
        for (Long v : Hummon.g.getVertices()) {
            // find new utility value
            double u = findUtility(alg, v);
            // retrieve the turtle with id = v
            Turtle turtle = world.getTurtle(v);
            // add the new utility value to this turtle
            turtle.setTurtleVariable(utility, (Double) u);
        }
    }

    /**
     * Finds utility value for a specific node.
     * @param alg
     * @param self
     * @return
     */
    private static double findUtility(DijkstraDistance<Long, String> alg, Long self) {
        double u = 0;
        for (Long other : Hummon.g.getVertices()) {
            Double get = (Double) alg.getDistance(self, other);
            // use the fitness according to the selection
            if (fitness.equals("new")) {
                // if no direct/indirect path is found, DijkstraDistance reports null
                if (get == null) {
                    // self is not connected with other, so no benefit/cost is charged
                } else if (get == 0.0) {
                    // no cost/benefit from myself
                    // do nothing
                } else if (get == 1.0) {
                    // there is a direct link, so utility is benefit ^ 1 - cost
                    u += (benefit - cost);
                } else {
                    // no cost because it is not a direct link, see description on page 226
                    // fitness according to tij-(tij-1 * 0.5)
                    double newGet = get - ((get - 1) * 0.5);
                    u += (Math.pow(benefit, newGet));
                }
            } else {
                // if no direct/indirect path is found, DijkstraDistance reports null
                if (get == null) {
                    // self is not connected with other, so no benefit/cost is charged
                } else if (get == 0.0) {
                    // no cost/benefit from myself
                    // do nothing
                } else if (get == 1.0) {
                    // there is a direct link, so utility is benefit ^ 1 - cost
                    u += (benefit - cost);
                } else {
                    // no cost because it is not a direct link, see description on page 226
                    // fitness according to benefit ^ geodesic distance
                    u += (Math.pow(benefit, get));
                }
            }
        }
        return u;
    }

    /**
     * Builds a new id label for a link, by performing a comparison between 
     * node id values according to the NetLogo rule for undirected links.
     * @param self
     * @param addressee
     * @return
     */
    private String makeEdgeId(Long source, Long target) {
        // holds the new id for the link between self and addressee
        String id;
        // if source id is the lower value
        if (source.doubleValue() < target.doubleValue()) {
            // then source comes first
            id = source.longValue() + "-" + target.longValue();
        } else {
            // then target comes first
            id = target.longValue() + "-" + source.longValue();
        }
        // return the new label
        return id;
    }

    /**
     * Find max utility in utilities vector - if more than one is found, then
     * one at random is extracted.
     *
     * @param utilities an array of double with all the utilities
     * @param world the world of netlogo
     * @return int
     */
    private int findMaxUtility(double[] utilities, World world) {
        double max = Double.NEGATIVE_INFINITY;
        for (Double d : utilities) {
            if (d > max) {
                max = d;
            }
        }
        // holds all the maximum values
        List<Integer> indices = new ArrayList<Integer>();
        // for each utility value
        for (int i = 0; i < utilities.length; i++) {
            // if value equals max utility value
            if (utilities[i] == max) {
                // add its id to the indices list
                indices.add(i);
            }
        }
        // return a random index from indices
        return indices.get(world.mainRNG.nextInt(indices.size()));
    }

    private LogoList findBehaveUnilateralMutual() throws AgentException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private LogoList findBehaveMutualUnilateral() throws AgentException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private LogoList findBehaveMutual() throws AgentException, ExtensionException {
        throw new ExtensionException("Not supported yet.");
    }
}
