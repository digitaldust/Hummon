package org.nlogo.extensions.hummon;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.nlogo.agent.World;
import org.nlogo.api.AgentException;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultReporter;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.api.LogoListBuilder;

/**
 * Step the model of a tick, by retrieving the best move for a selected turtle
 * as well as its new utility value.
 *
 * @author Simone Gabbriellini
 * @version 3.0
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
    public static Boolean useDegree;
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
     * The JUNG graph.
     */
    public static UndirectedSparseGraph<Long, String> g;
    /**
     * actors list for this turtle.
     */
    static ArrayList<Long> actors;
    /**
     * holds what to do with each actor.
     */
    static ArrayList<String> whatToDo;
    /**
     * holds the utility for each actor.
     */
    static ArrayList<Double> utilities;
    /**
     * holds the current distances matrix.
     */
    static DijkstraDistance<Long, String> alg;

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
        benefit = (Double) world.getObserverVariableByName("BENEFIT");
        // set cost of adding a link
        cost = (Double) world.getObserverVariableByName("COST");
        // get fitness function
        fitness = world.getObserverVariableByName("FITNESS").toString();
        // get fitness function
        useDegree = (Boolean) world.getObserverVariableByName("USE-DEGREE");
        // get tie formation method
        tieFormation = world.getObserverVariableByName("TIE-FORMATION").toString();
        // holds the list of other actors plus the calling turtle
        actors = new ArrayList<Long>(g.getVertexCount());
        // holds what to do with each actor
        whatToDo = new ArrayList<String>(g.getVertexCount());
        // holds the utility for each actor
        utilities = new ArrayList<Double>(g.getVertexCount());
        // create distances matrix
        alg = new DijkstraDistance<Long, String>(g);
        // behave according to tie formation rules
        try {
            // retrieve calling agent
            Long self = cntxt.getAgent().id();
            // try all possible modifications
            tryAllModifications(self);
            // maximize utility according to Jackson-Wolinsky
            LogoList results = maximizeUtility(self);
            // return the logolist with results: [what-to-do, self, other]
            return results;
        } catch (AgentException ex) {
            // throws exception
            throw new ExtensionException(ex);
        }
    }

    /**
     * Tries all possible changes to the network and save the results in the
     * ArrayLists declared by the calling agent.
     *
     * @param self
     * @throws ExtensionException
     */
    private void tryAllModifications(Long self) throws ExtensionException {
        // challenge each others
        for (Long other : g.getVertices()) {
            // if it's self
            if (self.equals(other)) {
                // computer status-quo utility
                utilities.add(findUtility(self));
                // whatToDo is status-quo
                whatToDo.add("status-quo");
                // actor is myself
                actors.add(self);
            } else {
                // create the label for the link to add or remove
                String id = makeEdgeId(self, other);
                // find right order
                String[] splitted = id.split("-");
                // decide what to do
                String what;
                if (g.isNeighbor(self, other)) {
                    what = "remove";
                } else {
                    what = "add";
                }
                // 
                double oldFitOther = Double.NEGATIVE_INFINITY;
                double newFitOther = Double.NEGATIVE_INFINITY;
                // check for mutual
                if ((what.equals("remove") && tieFormation.equals("u-add m-remove"))
                        || (what.equals("add") && tieFormation.equals("m-add u-remove"))
                        || (tieFormation.equals("m-add m-remove"))) {
                    // cache old fit for other before any manipulation
                    oldFitOther = findUtility(other);
                }
                // make changes to the network
                modifyNetworkConditions(id, splitted, what);
                // find new utility for self
                utilities.add(findUtility(self));
                // check for mutual
                if ((what.equals("remove") && tieFormation.equals("u-add m-remove"))
                        || (what.equals("add") && tieFormation.equals("m-add u-remove"))
                        || (tieFormation.equals("m-add m-remove"))) {
                    // cache new fit for other
                    newFitOther = findUtility(other);
                }
                // restore initial conditions
                if (what.equals("remove")) {
                    modifyNetworkConditions(id, splitted, "add");
                } else {
                    modifyNetworkConditions(id, splitted, "remove");
                }
                // update actors
                actors.add(other);
                // update whatToDo
                if (tieFormation.equals("u-add u-remove")) {
                    // just do it
                    whatToDo.add(what);
                } else if (tieFormation.equals("u-add m-remove")) {
                    // if add
                    if (what.equals("add")) {
                        // just do it
                        whatToDo.add(what);
                    } else {
                        // check it
                        checkIt(what, oldFitOther, newFitOther);
                    }
                } else if (tieFormation.equals("m-add u-remove")) {
                    // if add
                    if (what.equals("add")) {
                        // check it
                        checkIt(what, oldFitOther, newFitOther);
                    } else {
                        // just do it
                        whatToDo.add(what);
                    }
                } else if (tieFormation.equals("m-add m-remove")) {
                    // check it
                    checkIt(what, oldFitOther, newFitOther);
                } else {
                    throw new ExtensionException("Problem in find behaviour");
                }
            }
        }
    }

    /**
     * Modifies the network according to the possible rules: if self and other
     * are already connected, then breaks the connection and vice-versa.
     *
     * @param self
     * @param other
     * @param id
     * @param splitted
     * @return A string with what has been done to the network, either remove a
     * link or add a link
     * @throws ExtensionException
     */
    private void modifyNetworkConditions(String id, String[] splitted, String what) throws ExtensionException {
        // holds results
        boolean manipulate;
        // if we are linked now
        if (what.equals("remove")) {
            // remove it either to try a modification or to restore initial conditions
            manipulate = g.removeEdge(id);
        } else {
            // add it either to try a modification or to restore initial conditions
            manipulate = g.addEdge(id, Long.valueOf(splitted[0]), Long.valueOf(splitted[1]), EdgeType.UNDIRECTED);
        }
        // if adding/removing went wrong (manipulate=false)
        if (!manipulate) {
            // raise exception
            throw new ExtensionException("Failed to add/remove an edge.");
        }
    }

    /**
     * Helper method, simply builds a new id label for a link, by performing a
     * comparison between node id values according to the NetLogo rule for
     * undirected links.
     *
     * @param self
     * @param addressee
     * @return
     */
    private static String makeEdgeId(Long source, Long target) {
        // holds the new id for the link between self and addressee
        String id;
        // if source id is the lower value
        if (source < target) {
            // then source comes first
            id = source + "-" + target;
        } else {
            // then target comes first
            id = target + "-" + source;
        }
        // return the new label
        return id;
    }

    /**
     * Checks if, under mutual condition, the change is possible or a status-quo
     * is required.
     *
     * @param what
     * @param oldFitOther
     * @param newFitOther
     */
    private void checkIt(String what, Double oldFitOther, Double newFitOther) {
        // if add, it has to be mutual, thus check
        if (oldFitOther > newFitOther) {
            // you cannot do it
            whatToDo.add("status-quo");
        } else {
            // just do it
            whatToDo.add(what);
        }
    }

    /**
     * Finds utility value for a specific node: accounts for two different
     * fitness function that drops more or less violently distant nodes.
     *
     * @param self
     * @return the new utility value
     */
    public static double findUtility(Long self) {
        // reset the distances matrix before computing utility, in order to have an updated matrix after modifications
        alg.reset();
        // store the final utility value
        double u = 0d;
        // for each node in the network
        for (Long other : g.getVertices()) {
            // get the distance from self to other
            Double get = (Double) alg.getDistance(self, other);
            //System.out.println("i am " + self + " you are " + other + " our distance is " + get);
            // if no direct/indirect path exists, DijkstraDistance reports null
            if (get == null) {
                // do nothing: self has no direct/indirect connections to other
            } else if (get == 0.0) {
                // do nothing: no cost/benefit from myself
            } else if (get == 1.0) {
                // there is a direct link, so utility is benefit ^ 1 - cost
                u += (benefit - cost);
            } else {
                // no cost because it is not a direct link, see description on page 226
                if (fitness.equals("new")) {
                    // fitness according to tij-(tij-1 * 0.5)
                    //get -= ((get - 1) * 0.5);
                    // inverse of the squared distance
                    u += benefit * (1 / Math.pow(get, 2));
                    //  
                } else {
                    // fitness according to benefit ^ geodesic distance
                    u += (Math.pow(benefit, get));
                }
            }
        }
        //System.out.println("Done.");
        return u;
    }

    /**
     * Find max utility in utilities vector - if more than one is found, then
     * one at random is extracted.
     *
     * @param utilities an array of double with all the utilities
     * @param world netlogo world
     * @return int
     */
    private static int findOneOfMaxUtilityIndex() {
        // max utility value
        Double maximum = Collections.max(utilities);
        // holds the indices of the maximum values
        List<Integer> maximumIndices = new ArrayList<Integer>(utilities.size());
        // for each utility value
        for (Double d : utilities) {
            // if value equals max utility value
            if (d.equals(maximum)) {
                // add its id to the indices
                maximumIndices.add(utilities.indexOf(d));
            }
        }
        // pick a random index from the list of indices with max value
        int selectedIndex = world.mainRNG.nextInt(maximumIndices.size());
        // return a random index from indices
        return maximumIndices.get(selectedIndex);
    }

    /**
     *
     * @param id
     * @param actors
     * @param utilities
     * @param whatToDo
     * @param mainRNG
     * @return
     */
    private LogoList maximizeUtility(Long self) throws ExtensionException, AgentException {

        // find maximum utility value after all the possible modifications
        int winId = findOneOfMaxUtilityIndex();
        // results
        LogoListBuilder results = new LogoListBuilder();
        // make the new edge label already formatted with NetLogo logic for undirected links
        String id = makeEdgeId(self, actors.get(winId));
        // add what to do
        boolean go = true;
        if (useDegree) {
            // in case we have to add a new link, check both capacities first
            if (whatToDo.get(winId).equals("add")) {
                Integer neighborCount = g.getNeighborCount(actors.get(winId));
                Double maxTies = (Double) world.getTurtle(actors.get(winId)).getTurtleOrLinkVariable("MAX-TIES");
                Integer neighborCountMe = g.getNeighborCount(self);
                Double maxTiesMe = (Double) world.getTurtle(self).getTurtleOrLinkVariable("MAX-TIES");
                if ((neighborCount < maxTies) && (neighborCountMe < maxTiesMe)) {
                    results.add(whatToDo.get(winId));
                    go = true;
                } else {
                    results.add("status-quo");
                    go = false;
                }
            } else { // if it's status-quo or remove
                results.add(whatToDo.get(winId));
            }
        } else { // if capacity is not used, then simply go on
            results.add(whatToDo.get(winId));
        }
        // find the right sequence of whos
        String[] splitted = id.split("-");
        // add from who
        results.add(Double.valueOf(splitted[0]));
        // add to whom
        results.add(Double.valueOf(splitted[1]));
        // modify the JUNG network accordingly
        if (whatToDo.get(winId).equals("status-quo")) {
            // nothing more to do
        } else if (whatToDo.get(winId).equals("add")) {
            if (useDegree) {
                if (go) {
                    // update self's utility only if something's changed
                    world.getTurtle(self).setTurtleOrLinkVariable("UTILITY", utilities.get(winId));
                    // add the link
                    boolean addEdge = g.addEdge(id, Long.valueOf(splitted[0]), Long.valueOf(splitted[1]), EdgeType.UNDIRECTED);
                    // if something went wrong
                    if (!addEdge) {
                        // throw exception
                        throw new ExtensionException("cannot add a link");
                    }
                }
            } else {
                // update self's utility only if something's changed
                world.getTurtle(self).setTurtleOrLinkVariable("UTILITY", utilities.get(winId));
                // add the link
                boolean addEdge = g.addEdge(id, Long.valueOf(splitted[0]), Long.valueOf(splitted[1]), EdgeType.UNDIRECTED);
                // if something went wrong
                if (!addEdge) {
                    // throw exception
                    throw new ExtensionException("cannot add a link");
                }
            }
        } else {
            // update self's utility only if something's changed
            world.getTurtle(self).setTurtleOrLinkVariable("UTILITY", utilities.get(winId));
            // remove the link
            boolean removeEdge = g.removeEdge(id);
            // if something went wrong
            if (!removeEdge) {
                // throw exception
                throw new ExtensionException("cannot remove a link");
            }
        }
        // return what to do
        return results.toLogoList();
    }
}
