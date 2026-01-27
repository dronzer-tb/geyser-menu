package com.geysermenu.extension.network;

import com.geysermenu.extension.GeyserMenuExtension;
import com.geysermenu.extension.forms.DynamicMenuHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.CharsetUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * TCP Server for companion plugin connections using Netty
 */
public class MenuServer {

    private final GeyserMenuExtension extension;
    private final DynamicMenuHandler dynamicMenuHandler;
    private final ButtonManager buttonManager;
    private final Map<String, ClientHandler> connectedClients = new ConcurrentHashMap<>();

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private SslContext sslContext;
    private boolean sslEnabled = false;

    public MenuServer(GeyserMenuExtension extension) {
        this.extension = extension;
        this.dynamicMenuHandler = new DynamicMenuHandler(extension);
        this.buttonManager = new ButtonManager();
    }

    public void start() {
        try {
            // Try to create SSL context if enabled in config
            if (extension.config().isEnableSsl()) {
                try {
                    SelfSignedCertificate ssc = new SelfSignedCertificate();
                    sslContext = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
                    sslEnabled = true;
                    extension.logger().info("SSL enabled for TCP server");
                } catch (Exception e) {
                    extension.logger().warning("Failed to initialize SSL, falling back to non-SSL: " + e.getMessage());
                    sslEnabled = false;
                }
            }

            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();

            final boolean useSsl = sslEnabled;
            final SslContext finalSslContext = sslContext;

            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();

                            // SSL for secure connection (if enabled)
                            if (useSsl && finalSslContext != null) {
                                pipeline.addLast(finalSslContext.newHandler(ch.alloc()));
                            }

                            // Frame decoder/encoder - max 1MB messages
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4));
                            pipeline.addLast(new LengthFieldPrepender(4));

                            // String codec
                            pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
                            pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));

                            // Business logic handler
                            pipeline.addLast(new ClientHandler(extension, MenuServer.this));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            String host = extension.config().getTcpHost();
            int port = extension.config().getTcpPort();

            serverChannel = bootstrap.bind(host, port).sync().channel();
            extension.logger().info("GeyserMenu TCP server started on " + host + ":" + port + (sslEnabled ? " (SSL)" : ""));

        } catch (Exception e) {
            extension.logger().error("Failed to start TCP server", e);
        }
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }

    public void stop() {
        try {
            if (serverChannel != null) {
                serverChannel.close().sync();
            }

            // Disconnect all clients
            for (ClientHandler client : connectedClients.values()) {
                client.disconnect("Server shutting down");
            }
            connectedClients.clear();

        } catch (Exception e) {
            extension.logger().error("Error stopping TCP server", e);
        } finally {
            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
            }
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
            }
        }
        extension.logger().info("GeyserMenu TCP server stopped");
    }

    public void registerClient(String identifier, ClientHandler client) {
        connectedClients.put(identifier, client);
        extension.logger().info("Client connected: " + identifier);
    }

    public void unregisterClient(String identifier) {
        connectedClients.remove(identifier);
        // Also remove any buttons registered by this client
        buttonManager.unregisterButtons(identifier);
        extension.logger().info("Client disconnected: " + identifier);
    }

    public ClientHandler getClient(String identifier) {
        return connectedClients.get(identifier);
    }

    public Map<String, ClientHandler> getConnectedClients() {
        return connectedClients;
    }

    public DynamicMenuHandler getDynamicMenuHandler() {
        return dynamicMenuHandler;
    }

    public ButtonManager getButtonManager() {
        return buttonManager;
    }

    public GeyserMenuExtension getExtension() {
        return extension;
    }
}
