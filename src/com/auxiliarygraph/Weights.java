package com.auxiliarygraph;

import com.auxiliarygraph.elements.LightPath;

/**
 * Created by Fran on 7/3/2015.
 */
public class Weights {

    private static double transponderEdgeCost;

    public Weights(int policy) {

        switch (policy) {
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

    public static double getSpectrumEdgeCost(String edgeID, int spectrumLayerIndex, int bw) {
        return 1e-5 * spectrumLayerIndex;
//        return  1 + 1e-5 * spectrumLayerIndex;
//        return  NetworkState.getFiberLink(edgeID).getNumberOfMiniGridsUsed() + 1e-5 * spectrumLayerIndex;

//        if (bw == 4 && spectrumLayerIndex >= 128)
//            return  NetworkState.getFiberLink(edgeID).getNumberOfMiniGridsUsed() + 1e-5 * (double) spectrumLayerIndex / 1000;
//        else if (bw == 10 && spectrumLayerIndex >= 180)
//            return NetworkState.getFiberLink(edgeID).getNumberOfMiniGridsUsed() + 1e-5 * (double) spectrumLayerIndex / 1000;
//        else
//            return NetworkState.getFiberLink(edgeID).getNumberOfMiniGridsUsed() + 1e-5 * spectrumLayerIndex;

    }

    public static double getLightPathEdgeCost(LightPath lp) {
        return 1 + lp.getPathElement().getTraversedEdges().size() * 10e-7;
//        return lp.getPathElement().getTraversedEdges().size();
//        return lp.getNumberOfMiniGridsUsedAlongLP();
    }

    public static double getTransponderEdgeCost() {
        return transponderEdgeCost;
    }


}
