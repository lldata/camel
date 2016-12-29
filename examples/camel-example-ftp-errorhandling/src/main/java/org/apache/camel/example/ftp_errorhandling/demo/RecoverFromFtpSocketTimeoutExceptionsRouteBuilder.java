/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.example.ftp_errorhandling.demo;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.example.ftp_errorhandling.retry.RedeliveryErrorHandler;

import java.net.SocketTimeoutException;

/**
 * <pre>
 * Camel route that demonstrates a way to recover from network errors in a FTP scenario
 *
 * Notes:
 * 1: Uses a custom errorhandler. Otherwise the redelivery settings will not be available to the retryHandler
 *    See also http://camel.apache.org/error-handler.html
 * 2: The errorhandler can be configured with a default redelivery policy
 * 3: Uses a bridgeErrorHandler to pass control to the RedeliveryErrorHandler
 *    See also http://camel.apache.org/file2.html
 * 4: A custom pollStrategy must be configured to point to the RetryPollingConsumerStrategy.
 *    See also http://camel.apache.org/polling-consumer.html
 * 5: Setup exception handler to handle the nested SocketTimeoutException from the FTP client
 *    See also http://camel.apache.org/exception-clause.html
 * 6: Override RedeliveryPolicy for this route. Overrides the defaults from the general ErrorHandler
 * </pre>
 * @author Lasse Lindgard (@lldata)
 */
public class RecoverFromFtpSocketTimeoutExceptionsRouteBuilder extends RouteBuilder {

  @Override
    public void configure() throws Exception {
        // 1
        errorHandler(RedeliveryErrorHandler.builder()
            .maximumRedeliveries(25) // 2
        );

        from("ftp://localhost:2121/?username=foo&password=bar" +
                "&timeout=500" + // set timeout low so get the timeouts
                "&autoCreate=false" + // Don't want to connect while the route is builded initially
                "&consumer.bridgeErrorHandler=true" + // 3
                "&scheduler=spring&scheduler.cron=0+0/1+*+?+*+*" + // poll every minute
                "&pollStrategy=#retryHandler" // 4
        )
          .routeId("ftp1")
          .onException(SocketTimeoutException.class) // 5
            .maximumRedeliveries(2)  // 6
            .useExponentialBackOff() // 6
            .backOffMultiplier(2)    // 6
            .process("retryHandler") // 7
          .end()
          .to("file:target/download")
          .log("Downloaded file ${file:name} complete.");
    }
}