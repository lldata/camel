package org.apache.camel.example.ftp_errorhandling.retry;

import org.apache.camel.Consumer;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.processor.RedeliveryPolicy;
import org.apache.camel.spi.PollingConsumerPollStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A custom PollingConsumerPollStrategy that cooperates with a bridgeErrorHandler to fail and retry polling, if an exception happens during the poll
 * Note that the code gets the RedeliveryPolicy, but it does not implement all features (yet)
 *
 * @author Lasse Lindgard (@lldata)
 */
public class RetryPollingConsumerPollStrategy implements PollingConsumerPollStrategy, Processor {
    private static final Logger log = LoggerFactory.getLogger(RetryPollingConsumerPollStrategy.class);

    // keep state in a map, so this strategy will work in a program with more than one endpoint
    Map<String, State> stateMap = new LinkedHashMap<>();

    private State getState(Endpoint endpoint) {
      String id = endpoint.getEndpointUri();
      State state = stateMap.get(id);
      if (state == null) {
        state = new State();
        stateMap.put(id, state);
      }

      return state;
    }

    class State {
      public boolean inRetryMode;
      public RedeliveryPolicy redeliveryPolicy;
      public long previousRedeliveryDelay = 0;

      public boolean inRetryMode() {
        return inRetryMode;
      }

      public void reset() {
        this.inRetryMode = false;
      }
    }

    @Override
    public boolean begin(Consumer consumer, Endpoint endpoint) {
      log.trace("Begin poll");
      getState(endpoint).reset();

      return true;
    }

    @Override
    public void commit(Consumer consumer, Endpoint endpoint, int polledMessages) {
      if (getState(endpoint).inRetryMode()) {
        log.trace("Force poll to fail and call RetryPollingConsumerPollStrategy.rollback");
        throw new IllegalStateException("Forcing poll to fail and call RetryPollingConsumerPollStrategy.rollback");
      } else {
        log.trace("Comitting poll");
      }
    }

    @Override
    public boolean rollback(Consumer consumer, Endpoint endpoint, int retryCounter, Exception cause) throws Exception {
      State state = getState(endpoint);
      if (retryCounter < state.redeliveryPolicy.getMaximumRedeliveries()) {
        try {
          state.previousRedeliveryDelay = state.redeliveryPolicy.calculateRedeliveryDelay(state.previousRedeliveryDelay, retryCounter);
          // TODO use redeliveryPolicy redeliveryPolicy.getRetryAttemptedLogLevel()
          log.debug("Redelivery number={} delayed_for={} ms. ", retryCounter, state.previousRedeliveryDelay);
          Thread.sleep(state.previousRedeliveryDelay);
          log.debug("Marking poll as failed - retry {}/{}", retryCounter, state.redeliveryPolicy.getMaximumRedeliveries());
        } catch (InterruptedException e) {
          log.warn("Redelivery sleep interrupted", e);
        }
        // return true to tell Camel that it should retry the poll immediately
        return true;
      }
      else {
        // okay we give up do not retry anymore
        // TODO use redeliveryPolicy.getRetriesExhaustedLogLevel()
        log.trace("Redeliveries exchaused after {} attepmts. Giving up.", state.redeliveryPolicy.getMaximumRedeliveries());
        return false;
      }
    }

    @Override
    public void process(Exchange exchange) throws Exception {
      State state = getState(exchange.getFromEndpoint());
      RedeliveryPolicy redeliveryPolicyFromExceptionHandler = RedeliveryErrorHandler.getRedeliveryPolicy(exchange);
      if (redeliveryPolicyFromExceptionHandler != null) {
        state.redeliveryPolicy = redeliveryPolicyFromExceptionHandler;
      } else {
        state.redeliveryPolicy = new RedeliveryPolicy();
      }
      log.trace("RedeliveryPolicy {}", state.redeliveryPolicy);
      state.inRetryMode = true;
    }
  }