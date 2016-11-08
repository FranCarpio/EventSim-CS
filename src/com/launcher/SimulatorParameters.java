package com.launcher;

import com.auxiliarygraph.NetworkState;
import com.filemanager.Results;
import com.graph.elements.edge.EdgeElement;
import com.graph.graphcontroller.Gcontroller;
import com.graph.path.PathElement;
import com.graph.path.pathelementimpl.PathElementImpl;
import com.inputdata.InputParameters;
import com.inputdata.elements.Source;
import com.inputdata.reader.ImportTopologyFromSNDFile;
import com.inputdata.reader.ReadFile;
import com.simulator.Scheduler;
import com.simulator.elements.Generator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * Created by Fran on 4/16/2015.
 */
public class SimulatorParameters {

    private static String networkFile;
    private static int numberOfTotalRequests, numberOfRequestForReports, numberOfRuns, modulationFormat, gridGranularity, numOfMiniGridsPerGB, txCapacityOfTransponders, policy, seedCounter;
    private static List<byte[]> listOfSeeds;
    private static List<Generator> listOfGenerators;
    private static int _runNumber = -1;
    private static boolean debugMode;
    private static final Logger log = LoggerFactory.getLogger(SimulatorParameters.class);

    /**
     * Function to start a set of simulations
     */
    public static void startSimulation() {

        /** Input network from a SNDLib file */
        new InputParameters(networkFile);
        new NetworkState(InputParameters.getGraph(), gridGranularity, txCapacityOfTransponders, numOfMiniGridsPerGB, setPaths(ImportTopologyFromSNDFile.getPaths()), policy);
        runSimulation();
    }

    /**
     * Function to run a simulation initializing the event handler and network inputdata
     */
    public static void runSimulation() {

        seedCounter = -1;

        if (_runNumber == numberOfRuns - 1) {
            System.exit(0);
        } else {
            _runNumber++;
            log.info("Preparing run number " + _runNumber);
        }

        log.info("Initializing the scheduler");
        /** Initialize the scheduler*/
        new Scheduler();

        /** Create new result files*/
        log.info("Creating new result files");
        new Results();

        InputParameters.readNetworkParameters();
        InputParameters.setNodes();
        log.info("Updating network state");
        new NetworkState(InputParameters.getGraph(), gridGranularity, txCapacityOfTransponders, numOfMiniGridsPerGB, setPaths(ImportTopologyFromSNDFile.getPaths()), policy);
        log.info("Initializing generators");
        listOfGenerators = new ArrayList<>();
        for (Source s : InputParameters.getListOfSources())
            listOfGenerators.add(new Generator(s.getVertex(), s.getListOfTrafficDemands(), s.getArrivalRate(), s.getTrafficClassProb(), s.getDestinationProb()));
        listOfGenerators.forEach(Generator::initialize);

        /** Run the simulation */
        log.info("Starting simulation...");
        Scheduler.startSim();
    }

    /**
     * Function to specify the paths
     *
     * @param paths
     */
    public static Set<PathElement> setPaths(List<String> paths) {

        List<String> listOfNodes;
        ArrayList<EdgeElement> listOfIntermediateLinks;
        Gcontroller graph = InputParameters.getGraph();
        Set<PathElement> setOfPathElements = new HashSet<>();

        for (String path : paths) {
            listOfNodes = new ArrayList<>();
            listOfIntermediateLinks = new ArrayList<>();
            String[] nodes = path.split("-");
            Collections.addAll(listOfNodes, nodes);


            for (int i = 0; i < listOfNodes.size() - 1; i++) {
                for (EdgeElement link : graph.getEdgeSet()) {
                    if (link.getSourceVertex().getVertexID()
                            .equals(listOfNodes.get(i))
                            && link.getDestinationVertex().getVertexID()
                            .equals(listOfNodes.get(i + 1)))
                        listOfIntermediateLinks.add(link);
                }
            }

            PathElement pathElement = new PathElementImpl(graph, graph.getVertex(listOfNodes.get(0)), graph.getVertex(listOfNodes.get(listOfNodes.size() - 1)), listOfIntermediateLinks);

            setOfPathElements.add(pathElement);
        }

        return setOfPathElements;

    }

    /**
     * Function to read the config file for the simulator
     */
    public static void readConfigFile(String pathFile) throws IOException {

        log.info("Reading the config file");
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
                        numberOfTotalRequests = Integer.parseInt(line);
                        break;
                    case 2:
                        numberOfRequestForReports = Integer.parseInt(line);
                        break;
                    case 3:
                        numberOfRuns = Integer.parseInt(line);
                        break;
                    case 4:
                        gridGranularity = Integer.parseInt(line);
                        break;
                    case 5:
                        modulationFormat = Integer.parseInt(line);
                        break;
                    case 6:
                        numOfMiniGridsPerGB = Integer.parseInt(line);
                        break;
                    case 7:
                        txCapacityOfTransponders = Integer.parseInt(line);
                        break;
                    case 8:
                        policy = Integer.parseInt(line);
                        break;
                    case 9:
                        if (line.equals("true"))
                            debugMode = true;
                        else
                            debugMode = false;
                        break;
                }
                lineCounter++;
            }
            line = ReadFile.readLine();
        }
        new ReadFile("seeds.txt");
        line = ReadFile.readLine();
        while (line != null) {
            if (!line.startsWith("#")) {
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
            line = ReadFile.readLine();
        }
    }

    public static byte[] getSeed() {
        seedCounter++;
        return listOfSeeds.get(seedCounter);
    }

    public static int getNumberOfTotalRequests() {
        return numberOfTotalRequests;
    }

    public static int getNumberOfRequestForReports() {
        return numberOfRequestForReports;
    }

    public static int get_runNumber() {
        return _runNumber;
    }

    public static int getModulationFormat() {
        return modulationFormat;
    }

    public static int getGridGranularity() {
        return gridGranularity;
    }

    public static int getNumberOfRuns() {
        return numberOfRuns;
    }

    public static String getFiberLinkStateParameter() {
        return "10000-10010 N12-N2";
    }

    public static boolean isDebugMode() {
        return debugMode;
    }
}