package com.auxiliarygraph;

import com.auxiliarygraph.elements.FiberLink;
import com.auxiliarygraph.elements.LightPath;
import com.auxiliarygraph.elements.Path;
import com.graph.elements.edge.EdgeElement;
import com.graph.elements.vertex.VertexElement;
import com.graph.graphcontroller.Gcontroller;
import com.graph.path.PathElement;

import java.util.*;

/**
 * Created by Fran on 6/12/2015.
 */
public class NetworkState {

    private Gcontroller graph;
    private static Map<String, FiberLink> fiberLinksMap;
    private static List<LightPath> listOfLightPaths;
    private static List<Path> listOfPaths;
    private static int txCapacityOfTransponders;
    private static int numOfMiniGridsPerGB;

    public NetworkState(Gcontroller graph, int granularity, int spectrumWidth, int txCapacityOfTransponders, int numOfMiniGridsPerGB, Set<PathElement> setOfPathElements) {

        this.graph = graph;
        this.fiberLinksMap = new HashMap<>();
        this.listOfLightPaths = new ArrayList<>();
        this.listOfPaths = new ArrayList<>();
        this.txCapacityOfTransponders = txCapacityOfTransponders / granularity;
        this.numOfMiniGridsPerGB = numOfMiniGridsPerGB;

        for (PathElement pe : setOfPathElements)
            listOfPaths.add(new Path(pe));

        for (EdgeElement edgeElement : graph.getEdgeSet())
            fiberLinksMap.put(edgeElement.getEdgeID(), new FiberLink(granularity, spectrumWidth, edgeElement));
    }

    public static List<LightPath> getListOfLightPaths(List<Path> listOfCandidatePaths) {
        List<LightPath> listOfLightPaths = new ArrayList<>();

        for (Path p : listOfCandidatePaths) {
            List<VertexElement> vertexElements = p.getPathElement().getTraversedVertices();
            for (int i = 0; i < vertexElements.size() - 1; i++)
                for (int j = 1; j < vertexElements.size() - 1; j++)
                    listOfLightPaths.addAll(getListOfLightPaths(vertexElements.get(i), vertexElements.get(j)));
        }
        return listOfLightPaths;
    }

    public static List<LightPath> getListOfLightPaths(VertexElement src, VertexElement dst) {
        List<LightPath> lightPaths = new ArrayList<>();

        for (LightPath lp : listOfLightPaths)
            if (lp.getPathElement().getSource().equals(src) && lp.getPathElement().getDestination().equals(dst))
                lightPaths.add(lp);

        return lightPaths;
    }

    public static FiberLink getFiberLink(String edgeID) {
        return fiberLinksMap.get(edgeID);
    }

    public static Map<String, FiberLink> getFiberLinksMap() {
        return fiberLinksMap;
    }

    public static List<Path> getListOfPaths(String src, String dst) {

        List<Path> listOfCandidatePaths = new ArrayList<>();
        for (Path p : listOfPaths)
            if (p.getPathElement().getSourceID().equals(src) && p.getPathElement().getDestinationID().equals(dst))
                listOfCandidatePaths.add(p);

        return listOfCandidatePaths;
    }

    public static void setNewLightPath(LightPath lp) {
        listOfLightPaths.add(lp);
    }

    public static int getTxCapacityOfTransponders() {
        return txCapacityOfTransponders;
    }

    public static int getNumOfMiniGridsPerGB() {
        return numOfMiniGridsPerGB;
    }
}
