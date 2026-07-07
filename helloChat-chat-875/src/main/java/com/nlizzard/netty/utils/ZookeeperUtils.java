package com.nlizzard.netty.utils;

import com.nlizzard.netty.config.RuntimeConfig;
import com.nlizzard.pojo.netty.NettyServerNode;
import com.nlizzard.utils.JsonUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.net.InetAddress;
import java.util.List;

public class ZookeeperUtils {

    private static final String NETTY_SERVER_LIST_PATH = "/netty_server_list";

    public static String nettyServerListPath() {
        return NETTY_SERVER_LIST_PATH;
    }

    public static String nettyServerNodePath(String nodeName) {
        return NETTY_SERVER_LIST_PATH + "/" + nodeName;
    }

    /** *
     * 注册Netty服务器节点到Zookeeper中
     * @param nodeName 节点名称
     * @param ip 节点ip地址
     * @param port 节点端口号
     */
    public static void registerNettyServer(String nodeName,
                                           String ip,
                                           Integer port) throws Exception {
        CuratorFramework zkClient = CuratorUtils.getClient();
        String path = nettyServerListPath();
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
                .forPath(nettyServerNodePath("IM-"), nodeJson.getBytes());
    }

    // 获取本机ip地址(内部ip，TODO: 公网上线时，可以固定写死为公网ip地址)
    public static String getLocalIp() throws Exception {
        InetAddress addr = InetAddress.getLocalHost();
        return RuntimeConfig.advertisedHost(addr.getHostAddress());
    }


    /**
     * 增加在线人数
     * @param serverNode netty服务器节点信息
     */
    public static void incrementOnlineCounts(NettyServerNode serverNode) throws Exception {
        dealOnlineCounts(serverNode, 1);
    }

    /**
     * 减少在线人数
     * @param serverNode netty服务器节点信息
     */
    public static void decrementOnlineCounts(NettyServerNode serverNode) throws Exception {
        dealOnlineCounts(serverNode, -1);
    }

    /**
     * 处理在线人数的增减
     * @param serverNode netty服务器节点信息
     * @param counts 增减的在线人数数量，增加为正数，减少为负数
     */
    public static void dealOnlineCounts(NettyServerNode serverNode,
                                        Integer counts) throws Exception {

        CuratorFramework zkClient = CuratorUtils.getClient();

        // 获取分布式读写锁，保证在更新在线人数时的线程安全
        InterProcessReadWriteLock readWriteLock = new InterProcessReadWriteLock(zkClient,
                "/rw-locks");
        readWriteLock.writeLock().acquire();

        try {

            String path = nettyServerListPath();
            List<String> list = zkClient.getChildren().forPath(path);
            for (String node:list) {
                String pendingNodePath = nettyServerNodePath(node);
                String nodeValue = new String(zkClient.getData().forPath(pendingNodePath));
                NettyServerNode pendingNode = JsonUtils.jsonToPojo(nodeValue,
                        NettyServerNode.class);

                // 如果ip和端口匹配，则当前路径的节点则需要累加或者累减
                if (pendingNode.getIp().equals(serverNode.getIp()) &&
                        (pendingNode.getPort().intValue() == serverNode.getPort().intValue())) {
                    pendingNode.setOnlineCounts(pendingNode.getOnlineCounts() + counts);
                    String nodeJson = JsonUtils.objectToJson(pendingNode);
                    zkClient.setData().forPath(pendingNodePath, nodeJson.getBytes());
                }
            }
        } finally {
            readWriteLock.writeLock().release();
        }
    }

}
