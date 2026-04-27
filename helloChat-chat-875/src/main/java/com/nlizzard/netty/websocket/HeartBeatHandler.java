package com.nlizzard.netty.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 心跳助手类
 */
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt){
        // 判断evt是否是IdleStateEvent(空闲事件状态，包含 读空闲/写空闲/读写空闲)
        // 触发读空闲，踢掉客户端连接
        if (evt instanceof IdleStateEvent event) {
            if(event.state() == IdleState.READER_IDLE){
                // 当触发读空闲的时候，关闭channel
                ctx.channel().close();
            }
        }
    }
}

