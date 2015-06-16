package com.auxiliarygraph;

import com.auxiliarygraph.edges.LightPathEdge;
import com.auxiliarygraph.edges.SpectrumEdge;
import com.auxiliarygraph.elements.FiberLink;
import com.auxiliarygraph.elements.LightPath;
import com.auxiliarygraph.elements.Path;
import com.graph.elements.edge.EdgeElement;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Fran on 6/11/2015.
 */
public class AuxiliaryGraph {

    private List<SpectrumEdge> listOfSpectrumEdges;
    private List<LightPathEdge> listOfLightPathEdges;
    private final int GUARD_BAND = 2;
    private int bw;

    /**
     * Constructor class
     */
    public AuxiliaryGraph(String src, String dst, int bw) {
        listOfSpectrumEdges = new ArrayList<>();
        listOfLightPathEdges = new ArrayList<>();
        this.bw = bw;

        /** Search for candidate paths between S and D*/
        List<Path> listOfCandidatePaths = NetworkState.getListOfPaths(src, dst);

        /** For each candidate path, create new spectrum edges*/
        for (Path p : listOfCandidatePaths)
            for (EdgeElement e : p.getPathElement().getTraversedEdges())
                for (Integer i : NetworkState.getFiberLink(e.getEdgeID()).getFreeMiniGrids(bw + 2 * GUARD_BAND))
                    listOfSpectrumEdges.add(new SpectrumEdge(e, i));

        /** For each pre-existing lightpath ...*/
        for (LightPath lp : NetworkState.getListOfLightPaths(listOfCandidatePaths))
        /** If the lightpath can carry more traffic allocating more mini grids...*/
            if (lp.canBeExpanded(bw))
                listOfLightPathEdges.add(new LightPathEdge(lp));
    }


    public boolean runShortestPathAlgorithm(List<Path> listOfCandidatePaths) {

        List<SpectrumEdge> selectedSpectrumEdges = new ArrayList<>();
        List<LightPathEdge> selectedLightPathEdges = new ArrayList<>();

        /** For each possible path, calculate the costs*/
        for (Path path : listOfCandidatePaths)
            for (EdgeElement edge : path.getPathElement().getTraversedEdges())
                for (SpectrumEdge se : listOfSpectrumEdges)
                    if (se.getEdgeElement().equals(edge)) {
                    }

        return true;
    }

    public void setConnection(int bandwidth, List<SpectrumEdge> selectedSpectrumEdges, List<LightPathEdge> selectedLightPathEdges) {


        /** If the path contains spectrum edges then establish new lightpath **/
        if (selectedSpectrumEdges.size() != 0)
        /** Check how many continuous Spectrum edges are in the path and establish lightpath*/
            for (SpectrumEdge se : selectedSpectrumEdges) {
//                NetworkState.getLightPathMap().put();
                FiberLink fl = NetworkState.getFiberLinksMap().get(se.getEdgeElement());
                fl.setUsedMiniGrid(se.getSpectrumLayerIndex());
            }

        /** If the path contains lightpath edges, then route the request by allocating more subcarriers*/

        /** Update network state*/

    }
}
