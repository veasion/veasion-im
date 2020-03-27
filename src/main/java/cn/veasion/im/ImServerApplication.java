package cn.veasion.im;

import cn.veasion.im.bean.MessageTypeEnum;
import cn.veasion.im.bean.MsgData;
import cn.veasion.im.server.ImServer;
import cn.veasion.im.util.Constant;

import java.util.Scanner;

/**
 * ImServiceApplication
 *
 * @author Veasion
 * @description
 * @date 2020/3/24
 */
public class ImServerApplication {

    public static void main(String[] args) throws Exception {
        ImServer server = new ImServer();

        server.bind(Constant.PORT, true);
        System.out.println("Server Startup Success.");

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String msg = scanner.next();
            if ("exit".equalsIgnoreCase(msg)) {
                break;
            } else {
                System.out.println("服务端：" + msg);
                server.sendToAll(MsgData.of(msg, MessageTypeEnum.TEST_MESSAGE));
            }
        }

        server.close();
    }

}
