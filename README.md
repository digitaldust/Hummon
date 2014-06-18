Hummon
======

## WHAT IS IT?

This model is a replication of Hummon's model as outlined in the paper "Utility and Dynamic Social Networks". The model implements a dynamic process of utility maximizing social agents embedded within social networks. 
Actors make social choices under four combinations of tie formation and deletion rules: unilateral and mutual tie formation, and unilateral and mutual tie deletion.
A utility function then selects the move that maximize agent's utility, and the consequent modification is applied to the network (including no modifications).
This dynamic process generates different types of social networks, according to a combination of parameters that can vary (namely, benefit of a tie, cost of a tie, link formation process, initial network density and network size).

## HOW IT WORKS

Each simulation starts with a network of a specified size and density. At each time step, an agent is selected at random with replacement. 
The selected agent explore the network trying to select the move which maximize his utility. He has the possibility to leave the network untouched, add a link to another agent or remove a link that he already has with another agent. In order to select which move brings him the highest utility, he tries to add or remove a link with all the other agents. Utility is computed according to:

    utility =  ∑benefit^distance - ∑cost

where distance is the relational distance from the other agents in the network. Even if a move appears to be the best one, the link formation process might prevent to execute it because the other agent's utility could be negatively affected (see next section).
Once the right legal move is selected, the agent update his utility and make the changes effective by manipulating the network. Since this process is computationally intensive because of computation of the distance matrix required (more than once) at each tick, an extension in Java has been written to speed up the model.
When all agents have been selected once, a generation is concluded and the whole schedule is repeated until a stop condition is met.
Stop conditions are of three kind:

- an ideal type structure (null, complete, star, bi-star, three-star, four-star or shared) is found;
- a near-ideal type structure (near-null, near-complete, near-star and so on) is found, using a simple procedure: the actual degree distribution is confronted with all the ideal type degree distributions and the one that minimize the total mean squared deviation from the idea structures is picked
- a time condition is met because of a flickering condition emerges where agents cycle between the same structures.

## HOW TO USE IT

Select the network size and density before hitting the setup button, then press the go button. The link formation process can be selected using a drop-down menu (works at runtime):

- "u-add u-remove": the selected agent adds or removes links without considering if the other agent's utility gets lowered;
- "u-add m-remove": the selected agent adds links without considering if the other agent's utility gets lowered but remove links only if other agent's utility is not negatively affected;
- "m-add u-remove": the selected agent removes links without considering if the other agent's utility gets lowered but adds links only if other agent's utility is not negatively affected;
- "m-add m-remove": the selected agent adds or removes links only if other agent's utility is not negatively affected.

The "benefit" and "cost" sliders set the values for benefit and cost for each link. The sliders, according to Hummon, vary in the range .05 to .95 with an increment of .05

The "fitness" drop-down menu selects between the old utility calculation (Hummon) and a newer one that punishes less long paths. In this latter case, the original formula:
    benefit ^ distance 
is changed to:
    benefit / (distance^2)
which punishes longer paths less severely.

The "use-capacity" switch selects if agents has a limit to the maximum number of links they can have. In that case, such limit is assigned randomly in the range 1 to N-1.

Plots and monitors help to understand what the simulation is doing at each generation, providing the user with a detailed description of what structural network is more close to the simulated network. It is also shown the theoretical expectation according to Jackson-Wolinsky and the actual best fit as reported by the Java extension.

## THINGS TO NOTICE AND TRY

As reported in the companion paper, three things are worth noticing:
	- if fitness = old and use-capacity = off and 3 ≤ size ≤ 10 (Hummon's conditions): the model replicates Hummon's results as presented in Figure 10 of the cited paper (average similarity .91);
	- if Hummon's conditions are used but size > 10: new structures are possible, like bi-star, three-star and so on, especially for higher initial densities;
	- if Hummon's conditions are used but fitness = new: complete structures are now the most frequent structural outcome;
	- if Hummon's conditions are used but use-capacity = on: complete structures disappear no matter the combination of benefit, cost and link formation process.

## NETLOGO FEATURES

The Hummon extension gives some new reporters that help to speed up computations:

- initialize: clear the workspace and initialize a JUNG network with the same size and density (and nodes capacity) as the NetLogo network. It reports the theoretical outcome for the present combination of size, benefit and costs;
- step-core-model:
- structural-fit: reports a logolist with:
 - the label of the best fitting structure;
 - the distance from a null structure;
 - the distance from a star structure;
 - the distance from a bi-star structure;
 - the distance from a complete structure;
 - the distance from a shared structure;
 - the distance from a tri-star structure;
 - the distance from a four-star structure;
- degree-distribution: reports the degree distribution of the network.

## CREDITS AND REFERENCES

Hummon, N.P. (2000) "Utility and dynamic social networks", <i>Social Networks</i>, 22, pp 221-249

©2014 by Simone Gabbriellini, GEMASS, CNRS & Paris-Sorbonne
