package com.launcher;

import com.algorithms.Algorithm;
import com.algorithms.FF;

import java.io.IOException;
import java.util.Date;

/**
 * Created by Fran on 4/16/2015.
 */
public class Launcher {

    private static Date date;
    private static Algorithm algorithm;

    public static void main(String[] args) throws IOException {

        date = new Date();
        SimulatorParameters.readConfigFile("config.txt");

        algorithm = new FF();
        SimulatorParameters.startSimulation();
    }

    public static Date getDate() {
        return date;
    }

    public static Algorithm getAlgorithm() {
        return algorithm;
    }
}
