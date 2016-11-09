package com.launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static int requestCounter;
    private static final Logger log = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) throws IOException {

        SimulatorParameters.readConfigFile("nsf-config.txt");
        SimulatorParameters.startSimulation();
    }

    public static void increaseRequestCounter() {
        requestCounter++;
        if (requestCounter >= SimulatorParameters.getTotalNumOfRequests()) {
            log.info("Processed Requests: " + requestCounter / 1000 + "K");
            SimulatorParameters.runSimulation();
        }
        if (requestCounter % 10000 == 0)
            log.info("Processed Requests: " + requestCounter / 1000 + "K");
    }

    public static int getRequestCounter() {
        return requestCounter;
    }
}
