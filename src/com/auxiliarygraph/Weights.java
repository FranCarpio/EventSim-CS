package com.auxiliarygraph;

import com.auxiliarygraph.elements.LightPath;

/**
 * Created by Fran on 7/3/2015.
 */
public class Weights {

    private static final int POLICY = 4;
    private static double transponderEdgeCost;

    public Weights() {

        switch (POLICY) {
            /** MinLP */
            case 1:
                transponderEdgeCost = 1e3;
                break;
            /** MinHops */
            case 2:
                transponderEdgeCost = 0.5;
                break;
            /** MinTHP*/
            case 3:
                transponderEdgeCost = 0;
                break;
            /** LB*/
            case 4:
                transponderEdgeCost = 0;
                break;
        }
    }

    public static double getSpectrumEdgeCost(String edgeID, int spectrumLayerIndex, int hopsOfThePath) {
        /** MinLP */ /** MinHops */
//        return 1e-5 * spectrumLayerIndex;
        /** MinTHP*/
//        return  1 + 1e-5 * spectrumLayerIndex;
        /** LB*/
        return  NetworkState.getFiberLink(edgeID).getNumberOfMiniGridsUsed() + 1e-5 * spectrumLayerIndex;

//        if (hopsOfThePath == 2 && spectrumLayerIndex >= 67)
//            return  NetworkState.getFiberLink(edgeID).getNumberOfMiniGridsUsed() + 1e-5 * (double) spectrumLayerIndex / 1000;
//        else if (hopsOfThePath == 3 && spectrumLayerIndex >= 133)
//            return  NetworkState.getFiberLink(edgeID).getNumberOfMiniGridsUsed() + 1e-5 * (double) spectrumLayerIndex / 1000;
//        else return  NetworkState.getFiberLink(edgeID).getNumberOfMiniGridsUsed() + 1e-5 *  spectrumLayerIndex;

    }

    public static double getLightPathEdgeCost(LightPath lp) {
        /** MinLP */ /** MinHops */
//        return 1 + lp.getPathElement().getTraversedEdges().size() * 10e-7;
        /** MinTHP*/
//        return lp.getPathElement().getTraversedEdges().size();
        /** LB*/
        return lp.getNumberOfMiniGridsUsedAlongLP();
    }

    public static double getTransponderEdgeCost() {
        return transponderEdgeCost * 2;
    }


}
