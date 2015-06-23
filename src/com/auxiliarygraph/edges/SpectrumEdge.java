package com.auxiliarygraph.edges;

import com.graph.elements.edge.EdgeElement;

/**
 * Created by Fran on 6/11/2015.
 */
public class SpectrumEdge {

    private int spectrumLayerIndex;
    private EdgeElement edgeElement;
    private double cost;

    public SpectrumEdge(EdgeElement edgeElement, int spectrumLayerIndex) {
        this.cost = 1e-5 * spectrumLayerIndex;
//        this.cost = 1 + 1e-5 * spectrumLayerIndex;
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
