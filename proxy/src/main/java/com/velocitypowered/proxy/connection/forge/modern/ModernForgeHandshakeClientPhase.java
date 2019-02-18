package com.velocitypowered.proxy.connection.forge.modern;

import com.velocitypowered.proxy.connection.client.ClientConnectionPhase;
import com.velocitypowered.proxy.connection.client.ClientPlaySessionHandler;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.packet.LoginPluginMessage;
import io.netty.buffer.ByteBuf;

/**
 * Tracks the current state of the handshake.
 */
public enum ModernForgeHandshakeClientPhase implements ClientConnectionPhase {
  NOT_STARTED {

  };

  @Override
  public boolean handle(ConnectedPlayer player, ClientPlaySessionHandler handler,
      LoginPluginMessage message) {
    if (!message.getChannel().equals(ModernForgeConstants.FORGE_LOGIN_MESSAGE_CHANNEL)) {
      // We wouldn't understand this message. Carry on.
      return false;
    }

    // Okay, so we do understand this message. All FML-wrapped login messages include a resource
    // location and the length of the following message. Read those out.
    ByteBuf payload = message.getData();
    String resourceLocator = ProtocolUtils.readString(payload);
    int messageLen = ProtocolUtils.readVarInt(payload);
    ByteBuf actualMessage = payload.readSlice(messageLen);

    return false;
  }
}
