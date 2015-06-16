package com.launcher;

import com.filemanager.Results;
import com.graph.elements.edge.EdgeElement;
import com.graph.graphcontroller.Gcontroller;
import com.graph.path.PathElement;
import com.graph.path.pathelementimpl.PathElementImpl;
import com.inputdata.InputParameters;
import com.inputdata.reader.ImportTopologyFromSNDFile;
import com.model.Generator;
import com.model.elements.Fiber;
import com.model.elements.Flow;
import com.model.elements.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class to initialize the simulator with the Network inputdata
 * <p>
 * Created by Fran on 6/5/2015.
 */
public class InitializeSimulator {

    private static List<Generator> listOfGenerators;
    private static List<Fiber> listOfFibers = new ArrayList<>();
    private static Set<Path> setOfPaths = new HashSet<>();
    private static List<PathElement> listOfPathElements = new ArrayList<>();

    private static final Logger log = LoggerFactory.getLogger(InitializeSimulator.class);


    public InitializeSimulator() {
        /** Set the network com.inputdata.elements*/
        setLinks();
        setGenerators();

        /** Set paths*/
        setPaths(ImportTopologyFromSNDFile.getPaths());
    }

    public static void start() {
        /** Create new result files*/
        new Results();

        /** Reset generators with lambda values */
        InputParameters.setNodes(SimulatorParameters.get_runNumber());
        setGenerators();

        /** Generate the firsts events for each generator*/
        generateInitialEventsForEachGenerator();
    }

    /**
     * Function to set the generators
     */
    public static void setGenerators() {

        listOfGenerators = new ArrayList<>();
        listOfGenerators.addAll(InputParameters.getListOfSources().stream().map(node -> new Generator(node.getVertex(), node.getListOfTrafficDemands(), node.getArrivalRate(), node.getTrafficClassProb(), node.getDestinationProb())).collect(Collectors.toList()));
    }

    /**
     * Function to specify the paths
     *
     * @param paths
     */
    public void setPaths(List<String> paths) {

        List<String> listOfNodes;
        ArrayList<EdgeElement> listOfIntermediateLinks;
        Gcontroller graph = InputParameters.getGraph();

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

            for (Generator generator : listOfGenerators) {
                if (!pathElement.getSource().getVertexID()
                        .equals(generator.getVertex().getVertexID()))
                    continue;
                for (Flow f : generator.getListOfFlows()) {
                    if (!pathElement.getDestination().getVertexID()
                            .equals(f.getDstNode().getVertexID()))
                        continue;
                    listOfPathElements.add(pathElement);
                    log.info("Path Element: " + pathElement.getVertexSequence());
                    break;
                }
            }
        }

        for (PathElement p : listOfPathElements) {
            List<Fiber> pathFibers = new ArrayList<>();
            for (EdgeElement edge : p.getTraversedEdges())
                pathFibers.addAll(listOfFibers.stream().filter(fiber -> edge.getSourceVertex().equals(fiber.getSrcNode()) && edge.getDestinationVertex().equals(fiber.getDstNode())).collect(Collectors.toList()));
            setOfPaths.add(new Path(p, pathFibers));
        }
    }

    /**
     * Function to initialize link com.inputdata.elements
     */
    public void setLinks() {

        listOfFibers.addAll(InputParameters.getGraph().getEdgeSet().stream().map(edge -> new Fiber(edge.getSourceVertex(), edge
                .getDestinationVertex(), SimulatorParameters.getNumberOfCarriers(), SimulatorParameters.getCarrierBandwidth())).collect(Collectors.toList()));
    }

    /**
     * Function to initialize the generators
     */
    public static void generateInitialEventsForEachGenerator() {
        listOfGenerators.forEach(Generator::initialize);
    }

    /**
     * Getters and setters
     */

    public static List<Fiber> getListOfFibers() {
        return listOfFibers;
    }

    public static Set<Path> getSetOfPaths() {
        return setOfPaths;
    }
}
