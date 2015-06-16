package com.launcher;

import com.inputdata.InputParameters;
import com.inputdata.reader.ReadFile;
import com.simulator.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fran on 4/16/2015.
 */
public class SimulatorParameters {

    private static String networkFile;
    private static double simulationTime;
    private static int numberOfTotalRequests;
    private static int numberOfRuns;
    private static int _runNumber = -1;
    private static List<byte[]> listOfSeeds;
    private static int seedCounter = -1;
    private static boolean widthGuardBands;
    private static int numberOfCarriers;
    private static double carrierBandwidth;
    private static boolean isGrooming;
    private static int maxNumCarriersForGrooming;
    private static final Logger log = LoggerFactory.getLogger(SimulatorParameters.class);

    /**
     * Function to start a set of simulations
     */
    public static void startSimulation() {
        /** Input network from a SNDLib file */
        new InputParameters(networkFile);
        new InitializeSimulator();
        runSimulation();
    }

    /**
     * Function to run a simulation initializing the event handler and network inputdata
     */
    public static void runSimulation() {

        if(_runNumber==numberOfRuns-1){
            System.exit(0);
        }else {
            _runNumber ++;
            log.info("Starting run number "+_runNumber);
        }

        /** Initialize the scheduler*/
        new Scheduler();

        /** Input network from a SNDLib file */
        InitializeSimulator.start();

        /** Run the simulation */
        Scheduler.startSim();
    }

    /**
     * Function to read the config file for the simulator
     */
    public static void readConfigFile(String pathFile) throws IOException {

        listOfSeeds = new ArrayList<>();
        new ReadFile(pathFile);
        String line = ReadFile.readLine();
        int lineCounter = 0;
        while (line != null) {
            if (!line.startsWith("#")) {
                switch (lineCounter) {
                    case 0:
                        networkFile = line;
                        break;
                    case 1:
                        simulationTime = Double.parseDouble(line);
                        break;
                    case 2:
                        numberOfTotalRequests = Integer.parseInt(line);
                        break;
                    case 3:
                        numberOfRuns = Integer.parseInt(line);
                        break;
                    case 4:
                        if (line.equals("true"))
                            widthGuardBands = true;
                        break;
                    case 5:
                        numberOfCarriers = Integer.parseInt(line);
                        break;
                    case 6:
                        carrierBandwidth = Double.parseDouble(line);
                        break;
                    case 7:
                        if (line.equals("true"))
                            isGrooming = true;
                        break;
                    case 8:
                        maxNumCarriersForGrooming = Integer.parseInt(line);
                        break;
                    case 9:
//                        SeedGenerator seedGenerator = new SecureRandomSeedGenerator();
//                        byte [] seed;
//                        try {
//                            seed = seedGenerator.generateSeed(16);
//                            listOfSeeds.add(seed);
//                        } catch (SeedException e) {
//                            e.printStackTrace();
//                        }

                        while (line != null) {
                            line = line.replaceAll("\\s+", "");
                            byte[] seed = new BigInteger(line, 2).toByteArray();
                            if (seed.length == 17) {
                                byte[] seedCopy = new byte[16];
                                for (int i = 0; i < seed.length - 1; i++)
                                    seedCopy[i] = seed[i + 1];
                                listOfSeeds.add(seedCopy);
                            } else
                                listOfSeeds.add(seed);
                            line = ReadFile.readLine();
                        }
                }
                lineCounter++;
            }
            line = ReadFile.readLine();
        }
    }

    public static byte[] getSeed() {
        seedCounter++;
        return listOfSeeds.get(seedCounter);
    }

    /**
     * Getters
     */
    public static int getNumberOfTotalRequests() {
        return numberOfTotalRequests;
    }

    public static int get_runNumber() {
        return _runNumber;
    }

    public static int getNumberOfCarriers() {
        return numberOfCarriers;
    }

    public static double getCarrierBandwidth() {
        return carrierBandwidth;
    }

    public static boolean isGrooming() {
        return isGrooming;
    }

    public static boolean isWidthGuardBands() {
        return widthGuardBands;
    }

    public static int getMaxNumCarriersForGrooming() {
        return maxNumCarriersForGrooming;
    }

}