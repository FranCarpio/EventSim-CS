package com.auxiliarygraph.elements;

import com.auxiliarygraph.NetworkState;
import com.graph.elements.edge.EdgeElement;
import com.graph.path.PathElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by Fran on 6/11/2015.
 */
public class LightPath {

    private PathElement pathElement;
    private List<Integer> miniGridIds;
    private Map<Double, Connection> connectionMap;
    private static final Logger log = LoggerFactory.getLogger(LightPath.class);

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

        if (canBeExpandedLeft(bw)) {
            int firstFreeMiniGrid = miniGridIds.get(0) - 1;
            for (int i = firstFreeMiniGrid; i > firstFreeMiniGrid - bw; i--) {
                miniGridIds.add(i);
                for (EdgeElement e : pathElement.getTraversedEdges())
                    NetworkState.getFiberLink(e.getEdgeID()).setUsedMiniGrid(i);
            }
            Collections.sort(miniGridIds);
        } else {
            int firstFreeMiniGrid = miniGridIds.get(miniGridIds.size() - 1) + 1;
            for (int i = firstFreeMiniGrid; i < firstFreeMiniGrid + bw; i++) {
                miniGridIds.add(i);
                for (EdgeElement e : pathElement.getTraversedEdges())
                    NetworkState.getFiberLink(e.getEdgeID()).setUsedMiniGrid(i);
            }
        }
        connectionMap.put(connection.getStartingTime(), connection);
    }

    public boolean canBeExpandedRight(int bw) {

        for (EdgeElement e : pathElement.getTraversedEdges())
            if (!NetworkState.getFiberLink(e.getEdgeID()).areNextMiniGridsAvailable(miniGridIds.get(miniGridIds.size() - 1) + 1, bw))
                return false;
        return true;
    }

    public boolean canBeExpandedLeft(int bw) {

        for (EdgeElement e : pathElement.getTraversedEdges())
            if (!NetworkState.getFiberLink(e.getEdgeID()).arePreviousMiniGridsAvailable(miniGridIds.get(0) - 1, bw))
                return false;
        return true;
    }

    public boolean containsMiniGrid(int miniGrid) {
        for (Integer i : miniGridIds)
            if (i.equals(miniGrid))
                return true;
        return false;
    }

    public void removeConnection(Connection connection) {

        connectionMap.remove(connection.getStartingTime());
        List<Integer> miniGridsToRemove = new ArrayList<>();

        for (int i = 0; i < miniGridIds.size(); i++)
            if(NetworkState.getFiberLink(pathElement.getTraversedEdges().get(0).getEdgeID()).getMiniGrid(miniGridIds.get(i)) == 1) {
                miniGridsToRemove.add(miniGridIds.get(i));
                if(miniGridsToRemove.size()==connection.getBw())
                    break;
            }
        
        for (int i = 0; i < miniGridsToRemove.size(); i++) {
            for (EdgeElement e : pathElement.getTraversedEdges())
                NetworkState.getFiberLink(e.getEdgeID()).setFreeMiniGrid(miniGridsToRemove.get(i));
            miniGridIds.remove(miniGridsToRemove.get(i));
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
