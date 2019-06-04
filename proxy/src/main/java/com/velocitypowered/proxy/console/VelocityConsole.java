package com.velocitypowered.proxy.console;

import static com.velocitypowered.api.permission.PermissionFunction.ALWAYS_TRUE;

import com.velocitypowered.api.event.permission.PermissionsSetupEvent;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.proxy.VelocityServer;
import java.util.List;

import com.velocitypowered.proxy.permission.AbstractPermissionSubject;
import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;
import net.kyori.text.serializer.legacy.LegacyComponentSerializer;
import net.minecrell.terminalconsole.SimpleTerminalConsole;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.io.IoBuilder;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;

public final class VelocityConsole extends AbstractPermissionSubject implements ConsoleCommandSource {

  private static final Logger logger = LogManager.getLogger(VelocityConsole.class);

  private final SimpleTerminalConsole terminal = new SimpleTerminalConsole() {

    @Override
    protected LineReader buildReader(LineReaderBuilder builder) {
      return super.buildReader(builder
        .appName("Velocity")
        .completer((reader, parsedLine, list) -> {
          try {
            boolean isCommand = parsedLine.line().indexOf(' ') == -1;
            List<String> offers = VelocityConsole.this.server.getCommandManager()
              .offerSuggestions(VelocityConsole.this, parsedLine.line());
            for (String offer : offers) {
              if (isCommand) {
                list.add(new Candidate(offer.substring(1)));
              } else {
                list.add(new Candidate(offer));
              }
            }
          } catch (Exception e) {
            logger.error("An error occurred while trying to perform tab completion.", e);
          }
        })
      );
    }

    @Override
    protected boolean isRunning() {
      return !VelocityConsole.this.server.isShutdown();
    }

    @Override
    protected void runCommand(final String command) {
      try {
        if (!VelocityConsole.this.server.getCommandManager().execute(VelocityConsole.this, command)) {
          sendMessage(TextComponent.of("Command not found.", TextColor.RED));
        }
      } catch (Exception e) {
        logger.error("An error occurred while running this command.", e);
      }
    }

    @Override
    protected void shutdown() {
      VelocityConsole.this.server.shutdown(true);
    }
  };
  private final VelocityServer server;

  public VelocityConsole(VelocityServer server) {
    super(ALWAYS_TRUE);
    this.server = server;
  }

  @Override
  public void sendMessage(Component component) {
    logger.info(LegacyComponentSerializer.legacy().serialize(component));
  }

  public SimpleTerminalConsole getTerminal() {
    return this.terminal;
  }

  /**
   * Sets up {@code System.out} and {@code System.err} to redirect to log4j.
   */
  public void setupStreams() {
    System.setOut(IoBuilder.forLogger(logger).setLevel(Level.INFO).buildPrintStream());
    System.setErr(IoBuilder.forLogger(logger).setLevel(Level.ERROR).buildPrintStream());
  }

  /**
   * Sets up permissions for the console.
   */
  public void setupPermissions() {
    PermissionsSetupEvent event = new PermissionsSetupEvent(this, s -> ALWAYS_TRUE);
    // we can safely block here, this is before any listeners fire
    this.permissionFunction = this.server.getEventManager().fire(event).join().createFunction(this);
  }

}
