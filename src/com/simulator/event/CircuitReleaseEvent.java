package com.simulator.event;

import com.simulator.elements.Connection;
import com.simulator.elements.Generator;
import com.simulator.elements.TrafficFlow;
import jsim.event.Entity;
import jsim.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents a new release event in the simulator
 *
 * @author Fran
 */
public class CircuitReleaseEvent extends Event {

    /**
     * Generator responsible of the event
     */
    private Generator generator;
    /**
     * Flow that remove its connection
     */
    private TrafficFlow flow;
    /**
     * Connection to remove
     */
    private Connection connectionToRelease;

    private static final Logger log = LoggerFactory.getLogger(CircuitReleaseEvent.class);

    /**
     * Constructor class
     */
    public CircuitReleaseEvent(Entity entity, Generator generator, TrafficFlow flow, Connection connectionToRelease) {
        super(entity);
        this.generator = generator;
        this.flow = flow;
        this.connectionToRelease = connectionToRelease;
    }

    @Override
    public void occur() {

        /** Look for the connection and remove it*/
//        outerLoop:
//        for (SpectrumPath sp : flow.getSetOfSpectrumPaths()) {
//            for (Connection con : sp.getSetOfConnections())
//                if (con.equals(connectionToRelease)) {
//                    sp.removeConnection(connectionToRelease);
//                    if (sp.getSetOfConnections().size() == 0)
//                        flow.getSetOfSpectrumPaths().remove(sp);
//                    log.debug("Connection released: " + generator.getVertex().getVertexID() + "-" + flow.getDstNode().getVertexID());
//                    break outerLoop;
//                }
//        }
    }
}
