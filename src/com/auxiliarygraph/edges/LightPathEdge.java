package com.auxiliarygraph.edges;

import com.auxiliarygraph.elements.LightPath;

/**
 * Created by Fran on 6/11/2015.
 */
public class LightPathEdge {

    private final double COST;
    private LightPath lightPath;

    public LightPathEdge(LightPath lightPath) {
        this.lightPath = lightPath;
        COST = 1 + lightPath.getPathElement().getTraversedEdges().size() * 10e-7;
//        COST = lightPath.getPathElement().getTraversedEdges().size();
//        COST = lightPath.getNumberOfMiniGridsUsedAlongLP();

    }

    public LightPath getLightPath() {
        return lightPath;
    }

    public double getCost() {
        return COST;
    }

}
