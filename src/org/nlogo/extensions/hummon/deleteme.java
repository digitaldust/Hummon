/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nlogo.extensions.hummon;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;

/**
 *
 * @author digitaldust
 */
public class deleteme {
    private static UndirectedSparseGraph<Long, String> g;
    private static DijkstraDistance<Long, String> alg;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        g = new UndirectedSparseGraph<Long, String>();
        // add some vertices
        for(long i=0;i<5;i++){
            g.addVertex(i);
        }
        // add some edges
        g.addEdge("0-1", 0l, 1l, EdgeType.UNDIRECTED);
        g.addEdge("0-2", 0l, 2l, EdgeType.UNDIRECTED);
        g.addEdge("1-3", 1l, 3l, EdgeType.UNDIRECTED);
        g.addEdge("3-4", 3l, 4l, EdgeType.UNDIRECTED);
        alg = new DijkstraDistance<Long, String>(g);
        for(Long n:g.getVertices()){
            for(Long m:g.getVertices()){
                System.out.println(n+"-"+m+" dist "+alg.getDistance(n, m));
            }
        }
        System.out.println("TOPA\n\n\n");
        g.addEdge("2-4", 2l, 4l, EdgeType.UNDIRECTED);
        alg.reset();
        for(Long n:g.getVertices()){
            for(Long m:g.getVertices()){
                System.out.println(n+"-"+m+" dist "+alg.getDistance(n, m));
            }
        }
    }
    
}
