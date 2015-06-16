package com.auxiliarygraph.elements;

import com.auxiliarygraph.NetworkState;
import com.graph.elements.edge.EdgeElement;
import com.graph.path.PathElement;

import java.util.List;

/**
 * Created by Fran on 6/11/2015.
 */
public class LightPath {

    private PathElement pathElement;
    private List<Integer> miniGridIds;

    public LightPath(PathElement pathElement, List<Integer> miniGridIds) {
        this.pathElement = pathElement;
        this.miniGridIds = miniGridIds;
    }

    public PathElement getPathElement() {
        return pathElement;
    }

    public void addMiniGrids(List<Integer> miniGridIds) {
        this.miniGridIds.addAll(miniGridIds);
    }

    public void removeMiniGrids(List<Integer> miniGridIds) {
        for (Integer i : miniGridIds)
            this.miniGridIds.remove(i);
    }

    public boolean canBeExpanded(int n) {
        boolean canBeExpanded = true;

        for (EdgeElement e : pathElement.getTraversedEdges())
            if (!NetworkState.getFiberLink(e.getEdgeID()).areMiniGridsAvailable(miniGridIds.get(miniGridIds.size() - 1) + 1, n))
                canBeExpanded = false;

        return canBeExpanded;
    }
}
