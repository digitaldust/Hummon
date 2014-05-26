package org.nlogo.extensions.hummon;

import edu.uci.ics.jung.algorithms.scoring.DegreeScorer;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.util.ArrayList;
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
     * The graph to hold
     */
    public static UndirectedSparseGraph<Long, String> g = null;
    /**
     *
     */
    public static DegreeScorer<Long> degScor = null;

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

        /**
         * Also here after initialize because you can vary parameters as the
         * simulation goes on. *
         */
        // set benefit from adding a link
        benefit = (Double) world.getObserverVariableByName("BENEFIT");
        // set cost of adding a link
        cost = (Double) world.getObserverVariableByName("COST");
        // get fitness function
        fitness = world.getObserverVariableByName("FITNESS").toString();
        // use a limit to number of links for each turtle
        useDegree = (Boolean) world.getObserverVariableByName("USE-DEGREE");
        // get tie formation method
        tieFormation = world.getObserverVariableByName("TIE-FORMATION").toString();
        // behave according to tie formation rules
        try {
            // holds the list of actors
            Long[] actors = new Long[CoreModel.N];
            // holds what to do with each actor
            String[] whatToDo = new String[CoreModel.N];
            // holds the utility for each actor
            double[] utilities = new double[CoreModel.N];
            // create distances matrix
            DijkstraDistance<Long, String> alg = new DijkstraDistance<Long, String>(g);
            // this is the utility of status-quo
            utilities[0] = findUtility(alg, cntxt.getAgent().id());
            //System.out.println(cntxt.getAgent().id() + " utility is " + utilities[0]);
            // first place of whatToDo is status-quo
            whatToDo[0] = "status-quo";
            // placeholder for first position
            actors[0] = cntxt.getAgent().id();
            /**
             * Returns a LogoList with: what to do (either "status-quo", "add"
             * or "remove") focal; agent: myself; other agent: self.
             */
            if (tieFormation.equals("u-add u-remove")) {
                return findUAddUDel(cntxt.getAgent().id(), actors, whatToDo, utilities);
            } else if (tieFormation.equals("u-add m-remove")) {
                return findUAddMDel(cntxt.getAgent().id(), actors, whatToDo, utilities);
            } else if (tieFormation.equals("m-add u-remove")) {
                return findMAddUDel(cntxt.getAgent().id(), actors, whatToDo, utilities);
            } else if (tieFormation.equals("m-add m-remove")) {
                return findMAddMDel(cntxt.getAgent().id(), actors, whatToDo, utilities);
            } else {
                throw new ExtensionException("Problem in find behaviour");
            }
        } catch (AgentException ex) {
            // throws exception
            throw new ExtensionException(ex);
        }
    }

    private LogoList findUAddMDel(long id, Long[] actors, String[] whatToDo, double[] utilities) throws AgentException, ExtensionException {

        // simple counter for the arrays
        int counter = 1;
        // ask every other actor
        for (Long t : g.getVertices()) {
            // if it's me
            if (!t.equals(id)) {
                // if we are neighbors, then try to remove mutually
                if (g.isNeighbor(id, t)) {
                    // new utility for this move
                    utilities[counter] = makeChangesMutual(id, t, false);
                    // 
                    if (utilities[counter] == Double.NEGATIVE_INFINITY) {
                        // this is a remove move
                        whatToDo[counter] = "status quo";
                    } else {
                        // this is a remove move
                        whatToDo[counter] = "remove";
                    }
                    // with actor t
                    actors[counter] = t;
                } else { // if we are not yet neighbors, try to add unilaterally
                    // new utility for this move
                    utilities[counter] = makeChangesUnilateral(id, t, true);
                    // this is an add move
                    whatToDo[counter] = "add";
                    // with actor t
                    actors[counter] = t;
                }
                // increment counter
                counter++;
            }
        }
        // report the right move, which is the one with the highest utility (or a random one if two or more
        // equivalent utilities are found)
        return maximizeUtility(id, actors, utilities, whatToDo);
    }

    private LogoList findMAddUDel(long id, Long[] actors, String[] whatToDo, double[] utilities) throws AgentException, ExtensionException {

        // simple counter for the arrays
        int counter = 1;
        // ask every other actor
        for (Long t : g.getVertices()) {
            // if it's me
            if (!t.equals(id)) {
                // if we are neighbors, then try to remove unilaterally
                if (g.isNeighbor(id, t)) {
                    // new utility for this move
                    utilities[counter] = makeChangesMutual(id, t, false);
                    // 
                    if (utilities[counter] == Double.NEGATIVE_INFINITY) {
                        // this is a remove move
                        whatToDo[counter] = "status quo";
                    } else {
                        // this is a remove move
                        whatToDo[counter] = "remove";
                    }
                    // with actor t
                    actors[counter] = t;
                } else { // if we are not yet neighbors, try to add mutually
                    // new utility for this move
                    utilities[counter] = makeChangesMutual(id, t, true);
                    // 
                    if (utilities[counter] == Double.NEGATIVE_INFINITY) {
                        // this is a remove move
                        whatToDo[counter] = "status quo";
                    } else {
                        // this is a remove move
                        whatToDo[counter] = "add";
                    }
                    // with actor t
                    actors[counter] = t;
                }
                // increment counter
                counter++;
            }
        }
        // report the right move, which is the one with the highest utility (or a random one if two or more
        // equivalent utilities are found)
        return maximizeUtility(id, actors, utilities, whatToDo);
    }

    private LogoList findMAddMDel(long id, Long[] actors, String[] whatToDo, double[] utilities) throws AgentException, ExtensionException {

        // simple counter for the arrays
        int counter = 1;
        // ask every other actor
        for (Long t : g.getVertices()) {
            // if it's me
            if (!t.equals(id)) {
                // if we are neighbors, then try to remove mutually
                if (g.isNeighbor(id, t)) {
                    // new utility for this move
                    utilities[counter] = makeChangesMutual(id, t, false);
                    // 
                    if (utilities[counter] == Double.NEGATIVE_INFINITY) {
                        // this is a remove move
                        whatToDo[counter] = "status quo";
                    } else {
                        // this is a remove move
                        whatToDo[counter] = "remove";
                    }
                    // with actor t
                    actors[counter] = t;
                } else { // if we are not yet neighbors, try to add mutually
                    // new utility for this move
                    utilities[counter] = makeChangesMutual(id, t, true);
                    // 
                    if (utilities[counter] == Double.NEGATIVE_INFINITY) {
                        // this is a remove move
                        whatToDo[counter] = "status quo";
                    } else {
                        // this is a remove move
                        whatToDo[counter] = "add";
                    }
                    // with actor t
                    actors[counter] = t;
                }
                // increment counter
                counter++;
            }
        }
        // report the right move, which is the one with the highest utility (or a random one if two or more
        // equivalent utilities are found)
        return maximizeUtility(id, actors, utilities, whatToDo);
    }

    /**
     *
     * @param id
     * @param actors
     * @param whatToDo
     * @param utilities
     * @return LogoList
     * @throws AgentException
     * @throws ExtensionException
     */
    private LogoList findUAddUDel(Long id, Long[] actors, String[] whatToDo, double[] utilities) throws AgentException, ExtensionException {

        // simple counter for the arrays, starts from 1 because position 0
        // is occupied by self examining status quo
        int counter = 1;
        // ask every other actor
        for (Long t : g.getVertices()) {
            // if it's me
            if (!t.equals(id)) {
                // if we are already neighbors, then try to remove unilaterally
                if (g.isNeighbor(id, t)) {
                    // new utility for this move
                    utilities[counter] = makeChangesUnilateral(id, t, false);
                    // this is a remove move
                    whatToDo[counter] = "remove";
                    // with actor t
                    actors[counter] = t;
                    // if we are not yet neighbors, try to add
                } else {
                    // new utility for this move
                    utilities[counter] = makeChangesUnilateral(id, t, true);
                    // this is an add move
                    whatToDo[counter] = "add";
                    // with actor t
                    actors[counter] = t;
                }
                // increment counter
                counter++;
            }
        }
        // report the right move, which is the one with the highest utility (or a random one if two or more
        // equivalent utilities are found)
        return maximizeUtility(id, actors, utilities, whatToDo);
    }

    /**
     * Explore what happens when a node try to add/remove a link unilaterally;
     * this method leave alg unthouched because it always resets alg to its
     * original values after the modifications has been done.
     *
     * @param s the asking agent
     * @param t the other agent
     * @param b boolean value, either add (true) or remove (false)
     * @return the utility value after the modification
     * @throws ExtensionException
     */
    private double makeChangesUnilateral(long s, Long t, boolean b) throws ExtensionException {
        // create the label for this link
        String id = makeEdgeId(s, t);
        // holds result of remove or adding procedure
        boolean manipulate;
        if (b) {// if a link must be added
            // add the link
            manipulate = g.addEdge(id, t, s, EdgeType.UNDIRECTED);
        } else {// if a link must be removed
            // remove the link
            manipulate = g.removeEdge(id);
        }
        // if adding/removing went wrong (manipulate=false)
        if (!manipulate) {
            // raise exception
            throw new ExtensionException("Failed to add/remove an edge.");
        }
        // find new distances after adding/deletion
        DijkstraDistance<Long, String> alg = new DijkstraDistance<Long, String>(g);
        // calculate new utility for self
        double u = findUtility(alg, s);
        // restore initial conditions
        if (b) {// if a link has been added
            // remove it to restore initial conditions
            manipulate = g.removeEdge(id);
        } else {// if a link has been removed
            // add it to restore initial conditions
            manipulate = g.addEdge(id, t, s, EdgeType.UNDIRECTED);
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
     * Make change accounting for mutual addition/deletion of ties.
     *
     * @param self
     * @param t
     * @param b
     * @return
     * @throws ExtensionException
     * @throws AgentException
     */
    private double makeChangesMutual(Long s, Long t, boolean b) throws ExtensionException, AgentException {
        // 
        DijkstraDistance<Long, String> alg = new DijkstraDistance<Long, String>(g);
        // calculate old utility for other
        double oldFitT = findUtility(alg, t);
        // create the label for this link
        String id = makeEdgeId(s, t);
        // holds result of remove or adding procedure
        boolean manipulate;
        if (b) {// if a link must be added
            manipulate = g.addEdge(id, s, t, EdgeType.UNDIRECTED);
        } else {// if a link must be removed
            // remove the link
            manipulate = g.removeEdge(id);
        }
        // if adding/removing went wrong (manipulate=false)
        if (!manipulate) {
            // raise exception
            throw new ExtensionException("Failed to add/remove an edge.");
        }
        double u = Double.NEGATIVE_INFINITY;
        // 
        DijkstraDistance<Long, String> alg2 = new DijkstraDistance<Long, String>(g);
        // 
        double newFitT = findUtility(alg2, t);
        // 
        if (oldFitT <= newFitT) {
            // 
            u = findUtility(alg2, s);
        }
        // restore initial conditions after computing new utility for self, you dumb ass...
        if (b) {
            // remove it to restore initial conditions
            manipulate = g.removeEdge(id);
        } else {
            // add it to restore initial conditions
            manipulate = g.addEdge(id, s, t, EdgeType.UNDIRECTED);
        }
        // if adding/removing went wrong (manipulate=false)
        if (!manipulate) {
            // raise exception
            throw new ExtensionException("Failed to add/remove an edge in makeChangesMutual() to re-establish initial condition.");
        } 
        //
        return u;
    }

    /**
     * UTILITIES METHODS: THESE LOOK CORRECT.
     */
    /**
     * Finds utility value for a specific node: accounts for two different
     * fitness function that drops more or less violently distant nodes. TODO:
     * still have to perfect this because no evident results emerge...
     *
     * @param alg
     * @param self
     * @return utility value
     */
    public static double findUtility(DijkstraDistance<Long, String> alg, Long self) {
        // 
        double u = 0;
        // for each node in the network
        for (Long other : g.getVertices()) {
            // get the distance from self to other
            Double get = (Double) alg.getDistance(self, other);
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
                    get -= ((get - 1) * 0.5);
                }
                // fitness according to benefit ^ geodesic distance
                u += (Math.pow(benefit, get));
            }
        }
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
    private static int findMaxUtility(double[] utilities) {

        // start with the first value
        double maximum = utilities[0];
        // for all element
        for (int i = 1; i < utilities.length; i++) {
            // if an element is bigger than maximum
            if (utilities[i] > maximum) {
                // then it's the new maximum
                maximum = utilities[i];
            }
        }
        // holds all the maximum values - make it short for performances
        List<Integer> indices = new ArrayList<Integer>();
        // for each utility value
        for (int i = 0; i < utilities.length; i++) {
            // if value equals max utility value
            if (utilities[i] == maximum) {
                // add its id to the indices list
                indices.add(i);
            }
        }
        // return a random index from indices
        return indices.get(world.mainRNG.nextInt(indices.size()));
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
        if (source.doubleValue() < target.doubleValue()) {
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
     *
     * @param id
     * @param actors
     * @param utilities
     * @param whatToDo
     * @param mainRNG
     * @return
     */
    private LogoList maximizeUtility(Long s, Long[] actors, double[] utilities, String[] whatToDo) throws ExtensionException, AgentException {
        // find maximum utility value after all the possible modifications
        int winId = findMaxUtility(utilities);
        //System.out.println(s + " choice is to " + whatToDo[winId] + " with utility " + utilities[winId]);
        // results
        LogoListBuilder results = new LogoListBuilder();
        // make the new edge label already formatted with NetLogo logic for undirected links
        String id = makeEdgeId(s, actors[winId]);
        // if all utilities are Double.NaN, self cannot do anything in order not to harm others
        if (whatToDo[winId].equals("status-quo")) {
            // nothing to do
            results.add("status-quo");
            results.add(s.doubleValue());
            results.add(actors[winId].doubleValue());
        } else if (whatToDo[winId].equals("add")) {
            // add the link
            boolean addEdge = g.addEdge(id, actors[winId], s, EdgeType.UNDIRECTED);
            // if something went wrong
            if (addEdge) {
                // update utility
                world.getTurtle(s).setTurtleVariable(utility, (Double) utilities[winId]);
                // build result
                results.add("add");
                results.add(s.doubleValue());
                results.add(actors[winId].doubleValue());
            } else {
                // throw exception
                throw new ExtensionException("cannot add a link");
            }
        } else {
            // remove the link
            boolean removeEdge = g.removeEdge(id);
            if (removeEdge) {
                // update utility
                world.getTurtle(s).setTurtleVariable(utility, (Double) utilities[winId]);
                // build results
                results.add("remove");
                String[] splitted = id.split("-");
                results.add(Double.valueOf(splitted[0]));
                results.add(Double.valueOf(splitted[1]));
            } else {
                throw new ExtensionException("cannot remove a link");
            }
        }
        // return what to do
        return results.toLogoList();
    }
}
