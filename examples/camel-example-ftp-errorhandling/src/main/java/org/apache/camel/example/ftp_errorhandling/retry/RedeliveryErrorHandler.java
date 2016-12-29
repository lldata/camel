package org.apache.camel.example.ftp_errorhandling.retry;

import org.apache.camel.*;
import org.apache.camel.builder.DefaultErrorHandlerBuilder;
import org.apache.camel.builder.ErrorHandlerBuilder;
import org.apache.camel.processor.DefaultErrorHandler;
import org.apache.camel.processor.RedeliveryPolicy;
import org.apache.camel.processor.exceptionpolicy.ExceptionPolicyStrategy;
import org.apache.camel.spi.RouteContext;
import org.apache.camel.util.CamelLogger;

import java.lang.reflect.Field;
import java.util.concurrent.ScheduledExecutorService;

/**
 * ErrorHandler that preserves the RedeliveryPolicy and passes it on to be used in RetryPollingConsumerPollStrategy
 *
 * @author Lasse Lindgard (@lldata)
 */
public class RedeliveryErrorHandler extends DefaultErrorHandler {
  /**
   * Creates the default error handler.
   *
   * @param camelContext                 the camel context
   * @param output                       outer processor that should use this default error handler
   * @param logger                       logger to use for logging failures and redelivery attempts
   * @param redeliveryProcessor          an optional processor to run before redelivery attempt
   * @param redeliveryPolicy             policy for redelivery
   * @param exceptionPolicyStrategy      strategy for onException handling
   * @param retryWhile                   retry while
   * @param executorService              the {@link ScheduledExecutorService} to be used for redelivery thread pool. Can be <tt>null</tt>.
   * @param onPrepareProcessor           a custom {@link Processor} to prepare the {@link Exchange} before
   *                                     handled by the failure processor / dead letter channel.
   * @param onExceptionOccurredProcessor a custom {@link Processor} to process the {@link Exchange} just after an exception was thrown.
   */
  public RedeliveryErrorHandler(
      CamelContext camelContext,
      Processor output,
      CamelLogger logger,
      Processor redeliveryProcessor,
      RedeliveryPolicy redeliveryPolicy,
      ExceptionPolicyStrategy exceptionPolicyStrategy,
      Predicate retryWhile,
      ScheduledExecutorService executorService,
      Processor onPrepareProcessor,
      Processor onExceptionOccurredProcessor) {
    super(camelContext, output, logger, redeliveryProcessor, redeliveryPolicy, exceptionPolicyStrategy, retryWhile, executorService,
        onPrepareProcessor, onExceptionOccurredProcessor);
  }

  private final static String REDELIVERY_POLICY = "ErrorHandlerRedeliveryPolicy";

  protected boolean deliverToFailureProcessor(final Processor processor, final boolean isDeadLetterChannel, final Exchange exchange,
      final RedeliveryData data, final AsyncCallback callback) {
    RedeliveryPolicy policy = redeliveryPolicy;
    try {
      // get currentRedeliveryPolicy as this is the one with the redelivery policies etc. from the onException handler
      Field field = data.getClass().getDeclaredField("currentRedeliveryPolicy");
      field.setAccessible(true);
      policy = (RedeliveryPolicy) field.get(data);
    } catch (Exception e) {
      log.debug("Could not get currentRedeliveryPolicy by reflection", e);
    }
    exchange.getIn().setHeader(REDELIVERY_POLICY, policy);
    return super.deliverToFailureProcessor(processor, isDeadLetterChannel, exchange, data, callback);
  }

  public static RedeliveryPolicy getRedeliveryPolicy(Exchange exchange) {
    return exchange.getIn().getHeader(REDELIVERY_POLICY, RedeliveryPolicy.class);
  }

  public static MyErrorHandlerBuilder builder() {
    return new MyErrorHandlerBuilder();
  }

  public static class MyErrorHandlerBuilder extends DefaultErrorHandlerBuilder {
    @Override
    public Processor createErrorHandler(RouteContext routeContext, Processor processor) throws Exception {
      RedeliveryErrorHandler answer = new RedeliveryErrorHandler(routeContext.getCamelContext(), processor, getLogger(), getOnRedelivery(),
          getRedeliveryPolicy(), getExceptionPolicyStrategy(), getRetryWhilePolicy(routeContext.getCamelContext()),
          getExecutorService(routeContext.getCamelContext()), getOnPrepareFailure(), getOnExceptionOccurred());
      // configure error handler before we can use it
      configure(routeContext, answer);
      return answer;
    }

    @Override
    public ErrorHandlerBuilder cloneBuilder() {
      MyErrorHandlerBuilder answer = new MyErrorHandlerBuilder();
      cloneBuilder(answer);
      return answer;
    }
  }
}
