/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.example.ftp_errorhandling.demo;

import org.apache.camel.example.ftp_errorhandling.retry.RetryPollingConsumerPollStrategy;
import org.apache.camel.main.Main;

/**
 * Main class for the example
 */
public final class ClientMain {

  private ClientMain() {
  }

  public static void main(String[] args) throws Exception {
    Main main = new Main();
    // Register the retry handler as a bean, since we need to refer to the same instance more than once in the route builder.
    // This will normally be handled by making the pollstrategy a Spring Bean
    main.bind("retryHandler", new RetryPollingConsumerPollStrategy());
    main.addRouteBuilder(new RecoverFromFtpSocketTimeoutExceptionsRouteBuilder());
    main.run();
  }

}
