package com.auxiliarygraph;

import com.auxiliarygraph.edges.LightPathEdge;
import com.auxiliarygraph.edges.SpectrumEdge;
import com.auxiliarygraph.elements.Connection;
import com.auxiliarygraph.elements.LightPath;
import com.auxiliarygraph.elements.Path;
import com.graph.elements.edge.EdgeElement;
import com.graph.elements.vertex.VertexElement;
import com.graph.path.PathElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(AuxiliaryGraph.class);

    /**
     * Constructor class
     */
    public AuxiliaryGraph(String src, String dst, int b, double currentTime, double ht, boolean feature) {
        listOfLPE = new ArrayList<>();
        listOfSE = new ArrayList<>();
        this.bw = b;
        this.bwWithGB = bw + /**2 ***/GUARD_BAND;
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
                        listOfSE.add(new SpectrumEdge(e, i, bw));
            }

        /** For each pre-existing lightpath ...*/
        for (LightPath lp : NetworkState.getListOfLightPaths(listOfCandidatePaths))
        /** If the lightpath can carry more traffic allocating more mini grids...*/
            if (lp.canBeExpandedLeft(bw) || lp.canBeExpandedRight(bw))
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

        /**Add light path edges costs*/
        List<LightPathEdge> listLightPathEdges = getLightPathEdges(p, miniGrid);
        for (LightPathEdge lpe : listLightPathEdges)
            layerCost += lpe.getCost();

        /**Add spectrum edges costs*/
        for (EdgeElement e : p.getPathElement().getTraversedEdges())
            if (getNumberOfSpectrumEdges(e, miniGrid) == bwWithGB)
                for (int i = miniGrid; i < miniGrid + bwWithGB; i++) {
                    se = getSpectrumEdge(e, i);
                    listOfSpectrumEdges.add(se);
                    layerCost += se.getCost();
                }

        /**Add transponder edges costs*/
        if (!listOfSpectrumEdges.isEmpty()) {
            layerCost += addTransponderCost();
            for (int i = 1; i < listOfSpectrumEdges.size() - 1; i++) {
                if (listOfSpectrumEdges.get(i).getEdgeElement().equals(listOfSpectrumEdges.get(i - 1).getEdgeElement()))
                    continue;
                if (!listOfSpectrumEdges.get(i).getEdgeElement().getSourceVertex().equals(listOfSpectrumEdges.get(i - 1).getEdgeElement().getDestinationVertex()))
                    layerCost += addTransponderCost();
            }
        }


        /**Check if there is continuous path*/
        int counterPath = 0;
        for (LightPathEdge lpe : listLightPathEdges)
            for (EdgeElement ee : lpe.getLightPath().getPathElement().getTraversedEdges())
                counterPath++;
        for (int i = 0; i < listOfSpectrumEdges.size() - 1; i++) {
            if (i == 0)
                counterPath++;
            else if (!listOfSpectrumEdges.get(i).getEdgeElement().equals(listOfSpectrumEdges.get(i - 1).getEdgeElement()))
                counterPath++;
        }

        if (counterPath < p.getPathElement().getTraversedEdges().size())
            layerCost = Double.MAX_VALUE;

        return layerCost;
    }

    public void setConnection(Path path, int miniGrid) {

        Set<LightPath> newLightPaths = new HashSet<>();
        List<SpectrumEdge> selectedSpectrumEdges = new ArrayList<>();

        newConnection = new Connection(currentTime, ht, bw, feature);

        List<LightPathEdge> selectedLightPathEdges = getLightPathEdges(path, miniGrid);

        SpectrumEdge se;
        for (EdgeElement e : path.getPathElement().getTraversedEdges())
            if ((se = getSpectrumEdge(e, miniGrid)) != null)
                selectedSpectrumEdges.add(se);


        /** If the path contains spectrum edges then establish new lightpath **/
        if (!selectedSpectrumEdges.isEmpty()) {
            List<VertexElement> vertexes = new ArrayList<>();
            if (selectedSpectrumEdges.size() == 1) {
                vertexes.add(selectedSpectrumEdges.get(0).getEdgeElement().getSourceVertex());
                vertexes.add(selectedSpectrumEdges.get(0).getEdgeElement().getDestinationVertex());
                newLightPaths.add(new LightPath(NetworkState.getPathElement(vertexes), miniGrid, bwWithGB, bw, newConnection));
            } else {
                vertexes.add(selectedSpectrumEdges.get(0).getEdgeElement().getSourceVertex());
                for (int i = 1; i < selectedSpectrumEdges.size(); i++) {
                    if (selectedSpectrumEdges.get(i).getEdgeElement().getSourceVertex().equals(selectedSpectrumEdges.get(i - 1).getEdgeElement().getDestinationVertex()))
                        vertexes.add(selectedSpectrumEdges.get(i).getEdgeElement().getSourceVertex());
                    if (!selectedSpectrumEdges.get(i).getEdgeElement().getSourceVertex().equals(selectedSpectrumEdges.get(i - 1).getEdgeElement().getDestinationVertex())) {
                        vertexes.add(selectedSpectrumEdges.get(i - 1).getEdgeElement().getDestinationVertex());
                        newLightPaths.add(new LightPath(NetworkState.getPathElement(vertexes), miniGrid, bwWithGB, bw, newConnection));
                        vertexes = new ArrayList<>();
                        vertexes.add(selectedSpectrumEdges.get(i).getEdgeElement().getSourceVertex());
                    }
                    if (i == selectedSpectrumEdges.size() - 1) {
                        vertexes.add(selectedSpectrumEdges.get(i).getEdgeElement().getDestinationVertex());
                        newLightPaths.add(new LightPath(NetworkState.getPathElement(vertexes), miniGrid, bwWithGB, bw, newConnection));
                    }
                }
            }
        }

        /** Expand existing lightpaths*/
        if (!selectedLightPathEdges.isEmpty())
            for (LightPathEdge lightPathEdge : selectedLightPathEdges)
                if (lightPathEdge.getLightPath().canBeExpandedLeft(bw))
                    lightPathEdge.getLightPath().expandLightPathOnLeftSide(bw, newConnection);
                else
                    lightPathEdge.getLightPath().expandLightPathOnRightSide(bw, newConnection);

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

    public List<LightPathEdge> getLightPathEdges(Path p, int miniGridIndex) {

        List<LightPathEdge> lightPathEdges = new ArrayList<>();

        for (LightPathEdge lpe : listOfLPE)
            if (lpe.getLightPath().containsMiniGrid(miniGridIndex)) {
                if (comparePaths(p.getPathElement(), lpe.getLightPath().getPathElement()))
                    lightPathEdges.add(lpe);
            }

        return lightPathEdges;
    }

    public boolean comparePaths(PathElement mainPath, PathElement pathToCompare) {

        for (EdgeElement e : pathToCompare.getTraversedEdges()) {
            if (!mainPath.containsEdge(e))
                return false;
        }
        return true;
    }

    public Connection getNewConnection() {
        return newConnection;
    }
}
