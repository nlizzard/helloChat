package com.nlizzard.netty;

import com.nlizzard.netty.websocket.WSServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Netty 服务的启动类(服务器)
 */
public class ChatServer {
    public static void main(String[] args) throws Exception {

        // 定义主从线程组
        // 定义主线程池，用于接受客户端的连接，但是不做任何处理
        EventLoopGroup bossGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        // 定义从线程池，处理主线程池交过来的任务，真正进行业务处理的线程池
        EventLoopGroup workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

        try {
            // 构建Netty服务器
            ServerBootstrap server = new ServerBootstrap();     // 服务的启动类
            server.group(bossGroup, workerGroup)                // 把主从线程池组放入到启动类中
                    .channel(NioServerSocketChannel.class)      // 设置Nio的双向通道
                    .childHandler(new WSServerInitializer());   // 设置处理器，用于处理workerGroup

            // 启动server，并且绑定端口号875，同时启动方式为"同步"
            System.out.println("Netty Server 启动了，正在监听端口875...");
            ChannelFuture channelFuture = server.bind(875).sync();

            // 监听关闭的channel
            channelFuture.channel().closeFuture().sync();
            System.out.println("Netty Server 关闭了。");
        } finally {
            // 优雅关闭线程池组
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
