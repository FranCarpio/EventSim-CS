package com.auxiliarygraph.elements;

import com.graph.elements.edge.EdgeElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Fran on 6/11/2015.
 */
public class FiberLink {

    /**
     * Map<Integer, Integer> miniGrids
     * id, 0 ==> free
     * id, 1 ==> used
     * id, 2 ==> reserved
     */
    private Map<Integer, Integer> miniGrids;
    private EdgeElement edgeElement;
    private int totalNumberOfMiniGrids;

    public FiberLink(int granularity, int spectrumWidth, EdgeElement edgeElement) {
        this.edgeElement = edgeElement;
        miniGrids = new HashMap<>();
        totalNumberOfMiniGrids = spectrumWidth / granularity;
        for (int i = 1; i <= totalNumberOfMiniGrids; i++) {
            miniGrids.put(i, 0);
        }
    }

    public List<Integer> getFreeMiniGrids(int n) {

        List<Integer> freeMiniGrids = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : miniGrids.entrySet()) {
            if (entry.getValue() == 0)
                freeMiniGrids.add(entry.getKey());
        }

        if (n > 1) {
            int counter = 1;
            for (int i = 1; i < freeMiniGrids.size() - 1; i++) {
                if (counter < n) {
                    if (freeMiniGrids.get(i) != freeMiniGrids.get(i - 1) + 1)
                        freeMiniGrids.remove(i - 1);
                    else
                        counter++;
                }
                if (counter == n) {
                    counter = 0;
                    i++;
                }

            }
        }

        return freeMiniGrids;
    }

    public boolean areMiniGridsAvailable(int startingPoint, int n) {
        boolean isAvailable = true;
        for (int i = startingPoint; i < startingPoint + n; i++)
            if (miniGrids.get(i) == 1)
                isAvailable = false;
        return isAvailable;
    }

    public double getUtilization() {

        double utilization = 0;

        for (int i = 0; i < miniGrids.size(); i++) {
        }

        return utilization;
    }

    public void setFreeMiniGrid(int id) {
        miniGrids.replace(id, miniGrids.get(id), 0);
    }

    public void setUsedMiniGrid(int id) {
        miniGrids.replace(id, miniGrids.get(id), 1);
    }

    public void setUsedMiniGrid(List<Integer> ids) {
        for(Integer id: ids)
            miniGrids.replace(id, miniGrids.get(id), 1);
    }

    public void setReservedMiniGrid(int id) {
        miniGrids.replace(id, miniGrids.get(id), 2);
    }

    public int getTotalNumberOfMiniGrids() {
        return totalNumberOfMiniGrids;
    }

    public EdgeElement getEdgeElement() {
        return edgeElement;
    }
}
