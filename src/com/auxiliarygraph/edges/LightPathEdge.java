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
        COST = 1 + (lightPath.getPathElement().getTraversedVertices().size() - 2) * 10e-7;
//        COST = lightPath.getPathElement().getTraversedVertices().size();
    }

    public LightPath getLightPath() {
        return lightPath;
    }

    public double getCost() {
        return COST;
    }

}
