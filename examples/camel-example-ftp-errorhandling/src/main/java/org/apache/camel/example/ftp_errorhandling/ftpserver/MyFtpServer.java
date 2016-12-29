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
package org.apache.camel.example.ftp_errorhandling.ftpserver;

import org.mockftpserver.stub.StubFtpServer;
import org.mockftpserver.stub.command.ListCommandHandler;
import org.mockftpserver.stub.command.PwdCommandHandler;

import java.util.Locale;

/**
 * Main class that can download files from an existing FTP server.
 */
public final class MyFtpServer {

    private MyFtpServer() {
    }

    public static void main(String[] args) throws Exception {

        // Setup StubFtpServer
        Locale.setDefault(Locale.US);
        StubFtpServer stubFtpServer = new StubFtpServer();
        stubFtpServer.setServerControlPort(2121);

        // setup pwd
        PwdCommandHandler pwd = new PwdCommandHandler();
        pwd.setDirectory("foo/bar");
        stubFtpServer.setCommandHandler("PWD", pwd);

        // Simulate two files available
        ListCommandHandler listCommandHandler = new ListCommandHandler();
        listCommandHandler.setDirectoryListing("11-09-01 12:30PM 406348 File2350.log\n11-01-01 1:30PM <DIR>  archive");
        stubFtpServer.setCommandHandler("LIST", listCommandHandler);

        // Make the RETR command fail...
        // FailingRetrCommandHandler retrCoHa = new FailingRetrCommandHandler();
        CompositeCommandHandler retrHandler = new CompositeCommandHandler(
            new SlowRetrCommandHandler(),
            new SlowRetrCommandHandler(),
            // new FailingRetrCommandHandler(),
            new OkRetrCommandHandler()
        );

        stubFtpServer.setCommandHandler("RETR", retrHandler);
        // StubFtpServer.setReplyTextBaseName("org/mockftpserver/ReplyText.properties");

        stubFtpServer.start();

        // Main main = new Main();
        // main.enableTrace();

        // main.addRouteBuilder(new RecoverFromFtpSocketTimeoutExceptionsRouteBuilder());
        // main.run();
    }

}
