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
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * TCP Server for companion plugin connections using Netty
 */
public class MenuServer {

    private final GeyserMenuExtension extension;
    private final DynamicMenuHandler dynamicMenuHandler;
    private final ButtonManager buttonManager;
    private final Map<String, ClientHandler> connectedClients = new ConcurrentHashMap<>();
    
    // Rate limiting: track recent connection attempts per IP
    private static final int MAX_CONNECTIONS_PER_IP = 5;
    private static final long RATE_LIMIT_WINDOW_MS = 10_000; // 10 seconds
    private final Map<String, long[]> connectionAttempts = new ConcurrentHashMap<>();

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
                            // Rate limit: reject connections from IPs connecting too frequently
                            String remoteIp = ((InetSocketAddress) ch.remoteAddress()).getAddress().getHostAddress();
                            if (isRateLimited(remoteIp)) {
                                extension.logger().warning("Rate limited connection from " + remoteIp + " (too many attempts). Closing.");
                                ch.close();
                                return;
                            }
                            
                            ChannelPipeline pipeline = ch.pipeline();

                            // SSL for secure connection (if enabled)
                            if (useSsl && finalSslContext != null) {
                                pipeline.addLast(finalSslContext.newHandler(ch.alloc()));
                            }

                            // Idle timeout: close connections with no activity after 30 seconds
                            // This helps clean up port scanners and stale connections
                            pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                            pipeline.addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
                                    if (evt instanceof IdleStateEvent) {
                                        extension.logger().debug("Closing idle connection from " + ctx.channel().remoteAddress());
                                        ctx.close();
                                    }
                                }
                            });

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
    
    /**
     * Checks if an IP address has exceeded the maximum connection rate.
     * Tracks connection timestamps in a sliding window.
     */
    private boolean isRateLimited(String ip) {
        long now = System.currentTimeMillis();
        long[] timestamps = connectionAttempts.compute(ip, (key, existing) -> {
            if (existing == null) {
                return new long[]{now};
            }
            // Filter out old timestamps outside the window
            long cutoff = now - RATE_LIMIT_WINDOW_MS;
            long[] recent = java.util.Arrays.stream(existing)
                .filter(t -> t > cutoff)
                .toArray();
            // Add current timestamp
            long[] updated = new long[recent.length + 1];
            System.arraycopy(recent, 0, updated, 0, recent.length);
            updated[recent.length] = now;
            return updated;
        });
        return timestamps.length > MAX_CONNECTIONS_PER_IP;
    }
}
