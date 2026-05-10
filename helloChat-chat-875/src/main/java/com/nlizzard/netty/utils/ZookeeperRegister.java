package com.nlizzard.netty.utils;

import com.nlizzard.pojo.netty.NettyServerNode;
import com.nlizzard.utils.JsonUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.net.InetAddress;

public class ZookeeperRegister {

    /** *
     * 注册Netty服务器节点到Zookeeper中
     * @param nodeName 节点名称
     * @param ip 节点ip地址
     * @param port 节点端口号
     */
    public static void registerNettyServer(String nodeName,
                                           String ip,
                                           Integer port) throws Exception {
        CuratorFramework zkClient = CuratorConfig.getClient();
        String path = "/" + nodeName;
        Stat stat = zkClient.checkExists().forPath(path);
        if (stat == null) {
            zkClient.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT).forPath(path);
        } else {
            System.out.println(stat.toString());
        }

        // 创建对应的临时节点，值可以放在线人数，默认为初始化的0
        NettyServerNode serverNode = new NettyServerNode();
        serverNode.setIp(ip);
        serverNode.setPort(port);
        String nodeJson = JsonUtils.objectToJson(serverNode);

        zkClient.create()
                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath(path + "/IM-", nodeJson.getBytes());
    }

    // 获取本机ip地址(内部ip，公网上线时，可以固定写死为公网ip地址)
    public static String getLocalIp() throws Exception {
        InetAddress addr = InetAddress.getLocalHost();
        return addr.getHostAddress();
    }
}
