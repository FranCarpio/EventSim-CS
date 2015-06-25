package com.auxiliarygraph.elements;

import com.auxiliarygraph.NetworkState;
import com.graph.elements.edge.EdgeElement;
import com.graph.path.PathElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Fran on 6/11/2015.
 */
public class LightPath {

    private PathElement pathElement;
    private List<Integer> miniGridIds;
    private Map<Double, Connection> connectionMap;

    public LightPath(PathElement pathElement, int initialMiniGrid, int bwWithGB, int bw, Connection connection) {
        this.pathElement = pathElement;
        this.miniGridIds = new ArrayList<>();
        this.connectionMap = new HashMap<>();

        for (int i = initialMiniGrid; i < initialMiniGrid + bwWithGB; i++)
            miniGridIds.add(i);

        connectionMap.put(connection.getStartingTime(), connection);

        for (EdgeElement e : pathElement.getTraversedEdges()) {
            for (int i = 0; i < bw; i++)
                NetworkState.getFiberLinksMap().get(e.getEdgeID()).setUsedMiniGrid(miniGridIds.get(i));
            for (int i = bw; i < bwWithGB; i++)
                NetworkState.getFiberLinksMap().get(e.getEdgeID()).setGuardBandMiniGrid(miniGridIds.get(i));
        }
    }

    public PathElement getPathElement() {
        return pathElement;
    }

    public void expandLightPath(int bw, Connection connection) {
        int firstFreeMiniGrid = miniGridIds.size() + 1;
        for (int i = firstFreeMiniGrid; i < firstFreeMiniGrid + bw; i++) {
            miniGridIds.add(i);
            for (EdgeElement e : pathElement.getTraversedEdges())
                NetworkState.getFiberLink(e.getEdgeID()).setUsedMiniGrid(i);
        }
        connectionMap.put(connection.getStartingTime(), connection);
    }

    public boolean canBeExpanded(int n) {
        boolean canBeExpanded = true;

        for (EdgeElement e : pathElement.getTraversedEdges())
            if (!NetworkState.getFiberLink(e.getEdgeID()).areMiniGridsAvailable(miniGridIds.get(miniGridIds.size() - 1), n))
                canBeExpanded = false;

        return canBeExpanded;
    }

    public boolean containsMiniGrid(int miniGrid) {
        for (Integer i : miniGridIds)
            if (i.equals(miniGrid))
                return true;
        return false;
    }

    public void removeConnection(Connection connection) {
        connectionMap.remove(connection.getStartingTime());
        for (int i = 0; i < connection.getBw(); i++) {
            for (EdgeElement e : pathElement.getTraversedEdges())
                NetworkState.getFiberLink(e.getEdgeID()).setFreeMiniGrid(miniGridIds.get(miniGridIds.size() - 1));
            miniGridIds.remove(miniGridIds.size() - 1);
        }
    }

    public Map<Double, Connection> getConnectionMap() {
        return connectionMap;
    }

    public void releaseAllMiniGrids() {
        for (EdgeElement e : pathElement.getTraversedEdges())
            for (Integer i : miniGridIds)
                NetworkState.getFiberLink(e.getEdgeID()).setFreeMiniGrid(i);
    }
}
