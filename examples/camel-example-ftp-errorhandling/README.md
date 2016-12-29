# Camel FTP example

### Introduction

An advanced example which shows how to errorhandling on a scheduled endpoint, like FTP.
Uses a custom PollingConsumerPollStrategy to handle the retry logic and a custom ErrorHandler to get the configured retry configuration to the PollingConsumerPollStrategy, 

This example provides a custom FTP server, that allow us to test network timeouts.

### Build

You will need to compile this example first:

	mvn compile

### Run
#### Server
First start the server.

	mvn compile exec:java -Pserver

* This will start a FTP server on port 2121. Login is foo/bar
* You can connect to the server with any FTP client
* The FTP server is configured to respond slowly/fail two out of three GET requests.

#### Client
Then open a new prompt and start the demo route

	mvn compile exec:java -Pclient

* The client will poll the FTP server every minute and attempt to download the file it holds.
* The server will respond too slowly, which causes a SocketTimeoutException
* The client will handle the error and poll again, using the configured RedeliveryPolicy

### Documentation

This example is only documented in this file and in the code of RecoverFromFtpSocketTimeoutExceptionsRouteBuilder

You can enable verbose logging by adjustung the `src/main/resources/log4j.properties` file as documented in the file.

### Forum, Help, etc

If you hit a problem, try https://stackoverflow.com/  