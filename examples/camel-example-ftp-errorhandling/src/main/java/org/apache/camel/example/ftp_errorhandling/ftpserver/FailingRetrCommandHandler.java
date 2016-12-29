package org.apache.camel.example.ftp_errorhandling.ftpserver;

import org.mockftpserver.core.command.Command;
import org.mockftpserver.core.command.InvocationRecord;
import org.mockftpserver.core.command.ReplyCodes;
import org.mockftpserver.core.session.Session;
import org.mockftpserver.stub.command.RetrCommandHandler;

public class FailingRetrCommandHandler extends RetrCommandHandler {

    private byte[] fileContents = "This is CRA....".getBytes();

    public static final String PATHNAME_KEY = "pathname";

    public FailingRetrCommandHandler() {
        setPreliminaryReplyCode(ReplyCodes.TRANSFER_DATA_INITIAL_OK);
        setFinalReplyCode(425); // should never be sent
    }

    /**
     * @see org.mockftpserver.stub.command.AbstractStubDataCommandHandler#beforeProcessData(org.mockftpserver.core.command.Command,
     *      org.mockftpserver.core.session.Session, org.mockftpserver.core.command.InvocationRecord)
     */
    protected void beforeProcessData(Command command, Session session, InvocationRecord invocationRecord) throws Exception {
        String filename = command.getRequiredParameter(0);
        invocationRecord.set(PATHNAME_KEY, filename);
    }

    /**
     * @see org.mockftpserver.stub.command.AbstractStubDataCommandHandler#processData(org.mockftpserver.core.command.Command,
     *      org.mockftpserver.core.session.Session, org.mockftpserver.core.command.InvocationRecord)
     */
    protected void processData(Command command, Session session, InvocationRecord invocationRecord) {
        LOG.info("Closing data connection ..");
        session.closeDataConnection();
    }

}
