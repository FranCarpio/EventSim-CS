package com.auxiliarygraph;

import com.auxiliarygraph.edges.LightPathEdge;
import com.auxiliarygraph.edges.SpectrumEdge;
import com.auxiliarygraph.elements.Connection;
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
    //    private final double TRANSPONDER_EDGE_COST = 0;
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

    public double addTransponderCost() {
        double transponderCost = 0;

        if (bwWithGB / NetworkState.getTxCapacityOfTransponders() == 0)
            transponderCost += TRANSPONDER_EDGE_COST * 2;
        else {
            transponderCost += TRANSPONDER_EDGE_COST * 2 * bwWithGB / NetworkState.getTxCapacityOfTransponders();
        }

        return transponderCost;
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
        List<SpectrumEdge> listOfSpectrumEdges = new ArrayList<>();
        SpectrumEdge se;
        List<LightPathEdge> listLightPathEdges = getLightPathEdges(miniGrid);
        for (LightPathEdge lpe : listLightPathEdges)
            layerCost += lpe.getCost();

        for (EdgeElement e : p.getPathElement().getTraversedEdges())
            if (getNumberOfSpectrumEdges(e, miniGrid) == bwWithGB)
                if ((se = getSpectrumEdge(e, miniGrid + bwWithGB - 1)) != null) {
                    listOfSpectrumEdges.add(se);
                    for (int i = miniGrid; i < miniGrid + bwWithGB; i++)
                        layerCost += getSpectrumEdge(e, i).getCost();
                }

        for (int i = 0; i < listOfSpectrumEdges.size(); i++) {
            if (i == listOfSpectrumEdges.size() - 1) layerCost += addTransponderCost();
            else if (i == 0) continue;
            else if (!listOfSpectrumEdges.get(i).getEdgeElement().getSourceVertex().equals(listOfSpectrumEdges.get(i - 1).getEdgeElement().getDestinationVertex()))
                continue;
            else layerCost += addTransponderCost();
        }


        /**Check if there is continuous path*/
        int counterPath = 0;
        for (EdgeElement e : p.getPathElement().getTraversedEdges()) {
            outerLoop:
            for (LightPathEdge lpe : listLightPathEdges)
                for (EdgeElement ee : lpe.getLightPath().getPathElement().getTraversedEdges())
                    if (ee.equals(e)) {
                        counterPath++;
                        break outerLoop;
                    }
            outerLoop:
            for (SpectrumEdge see : listOfSpectrumEdges)
                if (see.getEdgeElement().equals(e)) {
                    counterPath++;
                    break outerLoop;
                }
        }

        if (counterPath != p.getPathElement().getTraversedEdges().size())
            layerCost = Double.MAX_VALUE;

        return layerCost;
    }

    public void setConnection(Path path, int miniGrid) {

        Set<LightPath> newLightPaths = new HashSet<>();
        List<SpectrumEdge> selectedSpectrumEdges = new ArrayList<>();

        newConnection = new Connection(currentTime, ht, bw, feature);

        List<LightPathEdge> selectedLightPathEdges = getLightPathEdges(miniGrid);

        SpectrumEdge se;
        for (EdgeElement e : path.getPathElement().getTraversedEdges())
            if ((se = getSpectrumEdge(e, miniGrid)) != null)
                selectedSpectrumEdges.add(se);


        /** If the path contains spectrum edges then establish new lightpath **/
        if (!selectedSpectrumEdges.isEmpty()) {
            int srcIndex = 0;
            for (int i = 0; i < selectedSpectrumEdges.size(); i++) {
                if (i == selectedSpectrumEdges.size() - 1) {
                    newLightPaths.add(new LightPath(
                            NetworkState.getPathElement(selectedSpectrumEdges.get(srcIndex).getEdgeElement().getSourceVertex().getVertexID(),
                                    selectedSpectrumEdges.get(i).getEdgeElement().getDestinationVertex().getVertexID()),
                            miniGrid, bwWithGB, bw,newConnection));
                } else if (i == 0) continue;
                else if (selectedSpectrumEdges.get(i).getEdgeElement().getSourceVertex().equals(selectedSpectrumEdges.get(i - 1).getEdgeElement().getDestinationVertex()))
                    continue;
                else {
                    newLightPaths.add(new LightPath(
                            NetworkState.getPathElement(selectedSpectrumEdges.get(srcIndex).getEdgeElement().getSourceVertex().getVertexID(),
                                    selectedSpectrumEdges.get(i).getEdgeElement().getDestinationVertex().getVertexID()),
                            miniGrid, bwWithGB, bw , newConnection));
                    srcIndex = i;
                }
            }
        }


        /** Expand existing lightpaths*/
        if (!selectedLightPathEdges.isEmpty())
            for (LightPathEdge lightPathEdge : selectedLightPathEdges)
                lightPathEdge.getLightPath().expandLightPath(bw, newConnection);

        NetworkState.getListOfLightPaths().addAll(newLightPaths);
    }

    public int getNumberOfSpectrumEdges(EdgeElement e, int spectrumLayerIndex) {
        int counter = 0;
        for (int i = spectrumLayerIndex; i < spectrumLayerIndex + bwWithGB; i++)
            if (getSpectrumEdge(e, i) != null)
                counter++;
        return counter;
    }

    public SpectrumEdge getSpectrumEdge(EdgeElement e, int spectrumLayerIndex) {
        for (SpectrumEdge se : listOfSE)
            if (se.getEdgeElement().equals(e) && se.getSpectrumLayerIndex() == spectrumLayerIndex)
                return se;
        return null;
    }

    public List<LightPathEdge> getLightPathEdges(int miniGridIndex) {

        List<LightPathEdge> lightPathEdges = new ArrayList<>();

        for (LightPathEdge lpe : listOfLPE)
            if (lpe.getLightPath().containsMiniGrid(miniGridIndex))
                lightPathEdges.add(lpe);

        return lightPathEdges;
    }

    public Connection getNewConnection() {
        return newConnection;
    }
}
