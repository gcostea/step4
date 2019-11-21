package net.herbert.step4;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class Application {

    public static void main(String... args) {
        var bossGroup = new EpollEventLoopGroup();
        var workerGroup = new EpollEventLoopGroup();
        try {
            var serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(EpollServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<EpollSocketChannel>() {
                        @Override
                        protected void initChannel(EpollSocketChannel channel) {
                            channel.pipeline()
                                    .addLast(new HttpRequestDecoder())
                                    .addLast(new HttpRequestEncoder())
                                    .addLast(new HttpRequestHandler());
                        }
                    });
            serverBootstrap.bind(8080)
                    .sync()
                    .channel()
                    .closeFuture()
                    .sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    private static class HttpRequestHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (msg instanceof HttpRequest) {
                var request = (HttpRequest) msg;
                //if (request.uri().endsWith("/cities")) {
                    var response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                    var test = "Test";
                    response.headers().add("Content-Type", "text/plain");
                    response.headers().add("Content-Length", test.getBytes().length);
                    response.content().writeBytes(test.getBytes());
                    var channelFuture = ctx.write(response);
                    channelFuture.addListener(ChannelFutureListener.CLOSE);
                //}
            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            ctx.close();
        }
    }

}
