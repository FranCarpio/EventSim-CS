package com.launcher;

import java.io.IOException;

/**
 * Created by Fran on 4/16/2015.
 */
public class Launcher {

    /**
     * Main class to launch the simulator
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        SimulatorParameters.readConfigFile("nsf-config.txt");
        SimulatorParameters.startSimulation();
    }

}
