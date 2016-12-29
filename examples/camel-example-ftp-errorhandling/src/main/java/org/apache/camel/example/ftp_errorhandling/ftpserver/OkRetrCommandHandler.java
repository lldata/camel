package org.apache.camel.example.ftp_errorhandling.ftpserver;

import org.mockftpserver.stub.command.RetrCommandHandler;

public class OkRetrCommandHandler extends RetrCommandHandler {

    private byte[] fileContents = "This is CRA....".getBytes();

    public static final String PATHNAME_KEY = "pathname";

    public OkRetrCommandHandler() {
        super();
        setFileContents(fileContents);
    }
}
