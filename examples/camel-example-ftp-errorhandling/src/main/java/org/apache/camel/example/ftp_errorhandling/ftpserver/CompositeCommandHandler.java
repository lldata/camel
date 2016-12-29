package org.apache.camel.example.ftp_errorhandling.ftpserver;

import org.mockftpserver.core.command.AbstractCommandHandler;
import org.mockftpserver.core.command.Command;
import org.mockftpserver.core.command.CommandHandler;
import org.mockftpserver.core.command.ReplyTextBundleAware;
import org.mockftpserver.core.session.Session;

import java.util.Collection;
import java.util.ResourceBundle;

/**
 * <p>
 * Special CommandHandler for situations, when you want the server to respond differently, depending the number of calls made.
 * The last handler provided will be used for repeated calls.
 * </p><p>
 * One example of a scenario would be to emulate a socket error on the first attempt and letting a retry succeed.
 * </p>
 * @author Lasse Lindgard (@lldata)
 * @version
 */
public class CompositeCommandHandler extends AbstractCommandHandler {
  private CommandHandler[] handlers;
  private int current;
  private Strategy strategy = Strategy.LOOP;

  enum Strategy { LOOP, REPEAT_LAST }

  /**
   * @param handlers a non empty array of handlers - the last handler will be used for repeated calls
   */
  public CompositeCommandHandler(CommandHandler... handlers) {
    assert (handlers != null || handlers.length == 0);
    this.handlers = handlers;
    this.current = 0;
    LOG.debug("initialized with {} handlers", handlers.length);
  }

  public CompositeCommandHandler(Collection<CommandHandler> handlerCollection) {
    this(handlerCollection.toArray(new CommandHandler[handlerCollection.size()]));
  }

  @Override
  public void handleCommand(Command command, Session session) throws Exception {
    LOG.info("Using handler #{}", current);
    handlers[current].handleCommand(command, session);
    if (current < handlers.length - 1) {
      current++;
    } else {
      // at last element - what to do?
      if (strategy == Strategy.LOOP) {
        current = 0;
      } else if (strategy == Strategy.REPEAT_LAST) {
        current = handlers.length - 1;
      }
    }
  }

  public void setReplyTextBundle(ResourceBundle replyTextBundle) {
    for (CommandHandler handler : handlers) {
      if (handler instanceof ReplyTextBundleAware) {
        ReplyTextBundleAware replyTextBundleAware = (ReplyTextBundleAware) handler;
        replyTextBundleAware.setReplyTextBundle(replyTextBundle);
      }
    }
  }

}
