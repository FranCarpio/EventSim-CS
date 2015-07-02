package com.auxiliarygraph.edges;

import com.auxiliarygraph.NetworkState;
import com.graph.elements.edge.EdgeElement;

/**
 * Created by Fran on 6/11/2015.
 */
public class SpectrumEdge {

    private int spectrumLayerIndex;
    private EdgeElement edgeElement;
    private double cost;

    public SpectrumEdge(EdgeElement edgeElement, int spectrumLayerIndex, int bw) {
        this.cost = 1e-5 * spectrumLayerIndex;
//        this.cost = 1 + 1e-5 * spectrumLayerIndex;
//            this.cost = NetworkState.getFiberLink(edgeElement.getEdgeID()).getNumberOfMiniGridsUsed() + 1e-5 * spectrumLayerIndex;


//        if (bw == 4 && spectrumLayerIndex >= 128)
//            this.cost = NetworkState.getFiberLink(edgeElement.getEdgeID()).getNumberOfMiniGridsUsed() + 1e-5 * (double) spectrumLayerIndex / 1000;
//        else if (bw == 10 && spectrumLayerIndex >= 180)
//            this.cost = NetworkState.getFiberLink(edgeElement.getEdgeID()).getNumberOfMiniGridsUsed() + 1e-5 * (double) spectrumLayerIndex / 1000;
//        else
//            this.cost = NetworkState.getFiberLink(edgeElement.getEdgeID()).getNumberOfMiniGridsUsed() + 1e-5 * spectrumLayerIndex;


        this.edgeElement = edgeElement;
        this.spectrumLayerIndex = spectrumLayerIndex;
    }

    public int getSpectrumLayerIndex() {
        return spectrumLayerIndex;
    }

    public EdgeElement getEdgeElement() {
        return edgeElement;
    }

    public double getCost() {
        return cost;
    }
}
