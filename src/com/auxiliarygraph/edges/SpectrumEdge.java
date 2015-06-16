package com.auxiliarygraph.edges;

import com.graph.elements.edge.EdgeElement;

/**
 * Created by Fran on 6/11/2015.
 */
public class SpectrumEdge {

    private int spectrumLayerIndex;
    private EdgeElement edgeElement;
    private final double COST = 1e-5 * spectrumLayerIndex;
    private final double TRANSPONDER_EDGE_COST = 1e3;

    public SpectrumEdge(EdgeElement edgeElement, int spectrumLayerIndex) {
        this.edgeElement = edgeElement;
        this.spectrumLayerIndex = spectrumLayerIndex;
    }

    public double getCOST() {
        return COST;
    }

    public double getTRANSPONDER_EDGE_COST() {
        return TRANSPONDER_EDGE_COST;
    }

    public int getSpectrumLayerIndex() {
        return spectrumLayerIndex;
    }

    public EdgeElement getEdgeElement() {
        return edgeElement;
    }
}
