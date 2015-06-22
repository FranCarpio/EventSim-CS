package com.auxiliarygraph;

import com.auxiliarygraph.edges.LightPathEdge;
import com.auxiliarygraph.edges.SpectrumEdge;
import com.auxiliarygraph.elements.Connection;
import com.auxiliarygraph.elements.FiberLink;
import com.auxiliarygraph.elements.LightPath;
import com.auxiliarygraph.elements.Path;
import com.graph.elements.edge.EdgeElement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Created by Fran on 6/11/2015.
 */
public class AuxiliaryGraph {

    private List<LightPathEdge> listOfLPE;
    private List<SpectrumEdge> listOfSE;
    private final int GUARD_BAND = NetworkState.getNumOfMiniGridsPerGB();
    private int bwWithGB;
    private int bw;
    private final double TRANSPONDER_EDGE_COST = 1e3;
    private Connection newConnection;
    private double currentTime;
    private double ht;
    private boolean feature;

    /**
     * Constructor class
     */
    public AuxiliaryGraph(String src, String dst, int b, double currentTime, double ht, boolean feature) {
        listOfLPE = new ArrayList<>();
        listOfSE = new ArrayList<>();
        this.bw = 1;
        this.bwWithGB = bw + 2 * GUARD_BAND;
        this.currentTime = currentTime;
        this.ht = ht;
        this.feature = feature;

        /** Search for candidate paths between S and D*/
        List<Path> listOfCandidatePaths = NetworkState.getListOfPaths(src, dst);

        /** For each candidate path, create new spectrum edges*/
        for (Path p : listOfCandidatePaths)
            for (EdgeElement e : p.getPathElement().getTraversedEdges()) {
                List<Integer> freeMiniGrids = NetworkState.getFiberLink(e.getEdgeID()).getFreeMiniGrids(bwWithGB);
                if (freeMiniGrids.size() >= bwWithGB)
                    for (Integer i : freeMiniGrids)
                        listOfSE.add(new SpectrumEdge(e, i));
            }

        /** For each pre-existing lightpath ...*/
        for (LightPath lp : NetworkState.getListOfLightPaths(listOfCandidatePaths))
        /** If the lightpath can carry more traffic allocating more mini grids...*/
            if (lp.canBeExpanded(bw))
                listOfLPE.add(new LightPathEdge(lp));
    }


    public boolean runShortestPathAlgorithm(List<Path> listOfCandidatePaths) {

        double cost;
        double minCost = Double.MAX_VALUE;
        Path selectedPath = null;
        int selectedMiniGrid = 0;

        /** For each possible path, calculate the costs*/
        for (Path path : listOfCandidatePaths) {
            int numOfMiniGrids = NetworkState.getFiberLinksMap().get(path.getPathElement().getTraversedEdges().get(0).getEdgeID()).getTotalNumberOfMiniGrids();
            for (int i = 1; i <= numOfMiniGrids; i++) {
                cost = calculateTheCostForMiniGrid(path, i);
                if (cost < minCost) {
                    minCost = cost;
                    selectedPath = path;
                    selectedMiniGrid = i;
                }
            }
        }

        if (minCost != Double.MAX_VALUE) {
            setConnection(selectedPath, selectedMiniGrid);
            return true;
        } else
            return false;
    }

    public double calculateTheCostForMiniGrid(Path p, int miniGrid) {
        double layerCost = 0;
        LightPathEdge lpe;
        SpectrumEdge se;

        for (EdgeElement e : p.getPathElement().getTraversedEdges()) {
            if ((lpe = getLightPathEdge(e, miniGrid)) != null) {
                layerCost += lpe.getCost();
            } else if ((se = getSpectrumEdge(e, miniGrid)) != null) {
                if (getSpectrumEdge(e, miniGrid + bwWithGB - 1) != null) {
                    for (int i = miniGrid; i < miniGrid + bwWithGB; i++)
                        layerCost += getSpectrumEdge(e, i).getCost();
                    if (bwWithGB / NetworkState.getTxCapacityOfTransponders() == 0)
                        layerCost += TRANSPONDER_EDGE_COST * 2;
                    else {
                        layerCost += TRANSPONDER_EDGE_COST * 2 * bwWithGB / NetworkState.getTxCapacityOfTransponders();
                    }
                } else {
                    layerCost = Double.MAX_VALUE;
                    break;
                }
            } else {
                layerCost = Double.MAX_VALUE;
                break;
            }
        }

        return layerCost;
    }

    public void setConnection(Path path, int miniGrid) {

        Set<LightPath> newLightPaths = new HashSet<>();
        List<LightPathEdge> selectedLightPathEdges = new ArrayList<>();
        List<SpectrumEdge> selectedSpectrumEdges = new ArrayList<>();

        newConnection = new Connection(currentTime, ht, bw, feature);

        LightPathEdge lpe;
        SpectrumEdge se;
        for (EdgeElement e : path.getPathElement().getTraversedEdges()) {
            if ((lpe = getLightPathEdge(e, miniGrid)) != null)
                selectedLightPathEdges.add(lpe);
            if ((se = getSpectrumEdge(e, miniGrid)) != null)
                selectedSpectrumEdges.add(se);
        }

        /** If the path contains spectrum edges then establish new lightpath **/
        if (selectedSpectrumEdges.size() > 1) {
            int srcIndex = 0;
            for (int i = 0; i < selectedSpectrumEdges.size() - 1; i++) {
                if (selectedSpectrumEdges.get(i).getEdgeElement().getDestinationVertex().equals(selectedSpectrumEdges.get(i + 1).getEdgeElement().getSourceVertex()))
                    continue;
                else {
                    newLightPaths.add(new LightPath(
                            NetworkState.getPathElement(selectedSpectrumEdges.get(srcIndex).getEdgeElement().getSourceVertex().getVertexID(),
                                    selectedSpectrumEdges.get(i).getEdgeElement().getDestinationVertex().getVertexID()),
                            miniGrid, bwWithGB, newConnection));
                    srcIndex = i;
                }
            }
        } else if (selectedSpectrumEdges.size() == 1) {
            newLightPaths.add(new LightPath(
                    NetworkState.getPathElement(selectedSpectrumEdges.get(0).getEdgeElement().getSourceVertex().getVertexID(),
                            selectedSpectrumEdges.get(0).getEdgeElement().getDestinationVertex().getVertexID()),
                    miniGrid, bwWithGB, newConnection));
        }

        /** If the path contains lightpath edges, then route the request by allocating more subcarriers*/
        if (!selectedLightPathEdges.isEmpty())
            for (LightPathEdge lightPathEdge : selectedLightPathEdges) {
                lightPathEdge.getLightPath().expandLightPath(bw);
                lightPathEdge.getLightPath().addNewConnection(newConnection);
            }

        /** Update network state*/
        /** for each new light path, extend allocating more sub-carriers*/
        for (LightPath lp : newLightPaths) {
            for (EdgeElement e : lp.getPathElement().getTraversedEdges()) {
                FiberLink fl = NetworkState.getFiberLinksMap().get(e.getEdgeID());
                fl.setUsedMiniGrid(lp.getMiniGridIds());
            }
        }
        NetworkState.getListOfLightPaths().addAll(newLightPaths);
    }

    public List<SpectrumEdge> getListOfSpectrumEdges(EdgeElement e) {
        List<SpectrumEdge> listOfSpectrumEdges = new ArrayList<>();

        for (SpectrumEdge se : this.listOfSE)
            if (se.getEdgeElement().equals(e))
                listOfSpectrumEdges.add(se);

        return listOfSpectrumEdges;
    }

    public SpectrumEdge getSpectrumEdge(EdgeElement e, int spectrumLayerIndex) {
        for (SpectrumEdge se : listOfSE)
            if (se.getEdgeElement().equals(e) && se.getSpectrumLayerIndex() == spectrumLayerIndex)
                return se;
        return null;
    }

    public LightPathEdge getLightPathEdge(EdgeElement e, int miniGridIndex) {

        for (LightPathEdge lpe : listOfLPE) {
            for (EdgeElement edge : lpe.getLightPath().getPathElement().getTraversedEdges()) {
                if (!edge.equals(e)) continue;
                if (lpe.getLightPath().containsMiniGrid(miniGridIndex))
                    return lpe;
            }
        }

        return null;
    }

    public Connection getNewConnection() {
        return newConnection;
    }
}
