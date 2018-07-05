package cn.mrray.blockchain.core.socket.server;

import cn.mrray.blockchain.core.socket.common.Const;
import org.springframework.stereotype.Component;
import org.tio.server.AioServer;
import org.tio.server.ServerGroupContext;
import org.tio.server.intf.ServerAioHandler;
import org.tio.server.intf.ServerAioListener;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * server启动器
 */
@Component
public class BlockServerStarter {

    @PostConstruct
    public void serverStart() throws IOException {
        ServerAioHandler serverAioHandler = new BlockServerAioHandler();
        ServerAioListener serverAioListener = new BlockServerAioListener();
        ServerGroupContext serverGroupContext = new ServerGroupContext(Const.GROUP_NAME, serverAioHandler, serverAioListener);
        AioServer aioServer = new AioServer(serverGroupContext);
        //本机启动服务
        aioServer.start(null, Const.PORT);
    }
}
