package com.nlizzard.netty.websocket;

import com.nlizzard.pojo.netty.NettyServerNode;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatHandlerTest {

    @Test
    void closedChannelWithoutConnectInitHasNoServerNodeToUpdate() {
        Optional<NettyServerNode> serverNode = ChatHandler.findServerNodeForClosedChannel(
                "unbound-channel",
                key -> {
                    throw new AssertionError("Redis should not be queried when no user is bound");
                });

        assertTrue(serverNode.isEmpty());
    }

    @Test
    void closedChannelWithMissingRedisValueHasNoServerNodeToUpdate() {
        String channelId = "channel-with-missing-server-node";
        UserChannelSession.putUserChannelIdRelation(channelId, "user-1");

        Optional<NettyServerNode> serverNode = ChatHandler.findServerNodeForClosedChannel(channelId, key -> null);

        assertTrue(serverNode.isEmpty());
    }

    @Test
    void closedChannelWithBoundServerNodeCanUpdateOnlineCounts() {
        String channelId = "channel-with-server-node";
        UserChannelSession.putUserChannelIdRelation(channelId, "user-2");
        Function<String, String> redisGetter = key -> "{\"ip\":\"localhost\",\"port\":875,\"onlineCounts\":0}";

        Optional<NettyServerNode> serverNode = ChatHandler.findServerNodeForClosedChannel(channelId, redisGetter);

        assertTrue(serverNode.isPresent());
        assertEquals("localhost", serverNode.get().getIp());
        assertEquals(875, serverNode.get().getPort());
    }
}
