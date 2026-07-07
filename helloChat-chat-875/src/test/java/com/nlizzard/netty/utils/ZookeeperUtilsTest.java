package com.nlizzard.netty.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ZookeeperUtilsTest {

    @Test
    void nettyServerListPathMatchesMainServicePath() {
        assertEquals("/netty_server_list", ZookeeperUtils.nettyServerListPath());
    }

    @Test
    void nettyServerNodePathAppendsChildNodeName() {
        assertEquals("/netty_server_list/IM-0000000001", ZookeeperUtils.nettyServerNodePath("IM-0000000001"));
    }
}
