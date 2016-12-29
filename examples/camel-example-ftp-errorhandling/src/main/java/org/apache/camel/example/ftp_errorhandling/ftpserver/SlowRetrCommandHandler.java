package org.apache.camel.example.ftp_errorhandling.ftpserver;

import org.mockftpserver.core.command.Command;
import org.mockftpserver.core.command.InvocationRecord;
import org.mockftpserver.core.command.ReplyCodes;
import org.mockftpserver.core.session.Session;
import org.mockftpserver.stub.command.RetrCommandHandler;

public class SlowRetrCommandHandler extends RetrCommandHandler {

    private byte[] fileContents = "This is CRA....".getBytes();

    public static final String PATHNAME_KEY = "pathname";

    public SlowRetrCommandHandler() {
        setPreliminaryReplyCode(ReplyCodes.TRANSFER_DATA_INITIAL_OK);
        setFinalReplyCode(425); // should never be sent
    }

    /**
     * @see org.mockftpserver.stub.command.AbstractStubDataCommandHandler#beforeProcessData(Command,
     *      Session, InvocationRecord)
     */
    protected void beforeProcessData(Command command, Session session, InvocationRecord invocationRecord) throws Exception {
        String filename = command.getRequiredParameter(0);
        invocationRecord.set(PATHNAME_KEY, filename);
    }

    /**
     * @see org.mockftpserver.stub.command.AbstractStubDataCommandHandler#processData(Command,
     *      Session, InvocationRecord)
     */
    protected void processData(Command command, Session session, InvocationRecord invocationRecord) {
        LOG.info("Sleep");
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOG.info("Closing data connection ..");
        session.closeDataConnection();
    }

}
