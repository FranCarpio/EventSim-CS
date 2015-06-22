package com.auxiliarygraph.elements;

import com.auxiliarygraph.NetworkState;
import com.graph.elements.edge.EdgeElement;
import com.graph.path.PathElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fran on 6/11/2015.
 */
public class LightPath {

    private PathElement pathElement;
    private List<Integer> miniGridIds;

    public LightPath(PathElement pathElement, int initialMiniGrid, int bw) {
        this.pathElement = pathElement;
        this.miniGridIds = new ArrayList<>();
        for (int i = initialMiniGrid; i < initialMiniGrid + bw; i++)
            miniGridIds.add(i);
    }

    public PathElement getPathElement() {
        return pathElement;
    }

    public void expandLightPath(int bw) {
        int firstFreeMiniGrid = miniGridIds.size() + 1;
        for (int i = firstFreeMiniGrid; i < firstFreeMiniGrid + bw; i++) {
            miniGridIds.add(i);
            for (EdgeElement e : pathElement.getTraversedEdges())
                NetworkState.getFiberLink(e.getEdgeID()).setUsedMiniGrid(i);
        }
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

    public boolean containsMiniGrid(int miniGrid) {
        for (Integer i : miniGridIds)
            if (i.equals(miniGrid))
                return true;
        return false;
    }

    public List<Integer> getMiniGridIds() {
        return miniGridIds;
    }
}
