package net.herbert.step4;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import net.herbert.step4.model.City;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Application {

    public static void main(String... args) throws ClassNotFoundException {
        Class.forName("org.h2.Driver");
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
                                    .addLast(new HttpResponseEncoder())
                                    /* Your code bellow */
                                    .addLast(new HttpRequestHandler());
                                    /* Your code above */
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
                var response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                var content = "";
                if (request.uri().contains("/cities")) {
                    /* Your actual code bellow */
                    content = getCitiesFromDatabase().stream()
                                .map(City::getName)
                                .collect(Collectors.joining(", "));
                    /* Your actual code above */
                }
                response.headers().add("Content-Type", "text/plain");
                response.headers().add("Content-Length", content.getBytes().length);
                response.content().writeBytes(content.getBytes());
                var channelFuture = ctx.write(response);
                channelFuture.addListener(ChannelFutureListener.CLOSE);
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

    private static List<City> getCitiesFromDatabase() {
        var cities = new ArrayList<City>();
        try (var connection = DriverManager.getConnection("jdbc:h2:file:" + DB_LOCATION, "test", "");
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery("SELECT * FROM cities")) {
            while (resultSet.next()) {
                var city = new City();
                city.setName(resultSet.getString("name"));
                city.setCountry(resultSet.getString("country"));
                city.setSubcountry(resultSet.getString("subcountry"));
                city.setGeonameid(resultSet.getInt("geonameid"));
                cities.add(city);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cities;
    }

    private static final String DB_LOCATION = "/home/costea/Work/Arezzosky/TechHerbert2019/Data/cities";
}
