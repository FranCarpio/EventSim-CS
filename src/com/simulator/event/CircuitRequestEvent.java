package com.simulator.event;

import com.filemanager.Results;
import com.inputdata.elements.TrafficClass;
import com.model.Generator;
import com.model.elements.Connection;
import com.model.elements.Flow;
import com.simulator.Scheduler;
import jsim.event.Entity;
import jsim.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents a new request event in the simulator
 *
 * @author Fran
 */
public class CircuitRequestEvent extends Event {

    /**
     * Generator responsible of the event
     */
    private Generator generator;
    /**
     * TrafficClass that generates the request
     */
    private TrafficClass trafficClass;

    private static final Logger log = LoggerFactory.getLogger(CircuitRequestEvent.class);

    /**
     * Constructor class
     */
    public CircuitRequestEvent(Entity entity, Generator generator,
                               TrafficClass trafficClass) {
        super(entity);
        this.generator = generator;
        this.trafficClass = trafficClass;

    }

    @Override
    public void occur() {

        double holdingTime;
        boolean isUnKnown = generator.getRandomUnknown(trafficClass.getType());

        /** If it is unknown, get the mean holding time*/
        if (isUnKnown)
            holdingTime = trafficClass.getMeanHoldingTime();
        /** If not, generate random holding time from distribution */
        else
            holdingTime = trafficClass.getHoldingTimeDistribution().execute();

        /** Get a random destination following a uniform distribution */
        Flow selectedFlow = generator.getRandomFlow(trafficClass.getType());

        Connection connection = selectedFlow.setConnection(trafficClass.getBw(), holdingTime, isUnKnown);

        /**If connection is established, then, add release event*/
        if (connection != null) {
            Event event = new CircuitReleaseEvent(new Entity(holdingTime),generator,selectedFlow,connection);
            Scheduler.schedule(event, holdingTime);
            log.debug("Added release event: " + generator.getVertex().getVertexID() + "-" + selectedFlow.getDstNode().getVertexID());
//            Results.writeHoldingTime(generator,selectedFlow,trafficClass.getType(),isUnKnown,holdingTime);
        } else { /**if not, increase blocking counter*/
            selectedFlow.increaseBlockingCounter(trafficClass.getType(), isUnKnown);
            log.debug("Connection is blocked");
        }

        /** Increase request counter for this flow */
        selectedFlow.increaseFlowRequestCounter(trafficClass.getType());

        /*********************** Results *************************/
        Results.writeBlockingResults(generator, selectedFlow);
        Results.writeLinkUtilizationResults();
        Results.increaseRequestCounter();

        /** Add a new request event */
        TrafficClass nextTrafficClass = generator.getRandomPort();
        double nextInterArrivalTime = generator.getRequestDistribution().execute();
        Event event = new CircuitRequestEvent(new Entity(nextInterArrivalTime), generator, nextTrafficClass);
        Scheduler.schedule(event, nextInterArrivalTime);
        log.debug("Added request event: " + generator.getVertex().getVertexID() + "-" + selectedFlow.getDstNode().getVertexID());
//        Results.writeInterArrivalTime(generator, selectedFlow,trafficClass.getType(),nextInterArrivalTime);
    }
}
