package cn.veasion.im;

import cn.veasion.im.bean.MessageTypeEnum;
import cn.veasion.im.bean.MsgData;
import cn.veasion.im.client.ImClient;
import cn.veasion.im.util.Constant;

import java.util.Scanner;

/**
 * ImClientApplication
 *
 * @author Veasion
 * @description
 * @date 2020/3/24
 */
public class ImClientApplication {

    public static void main(String[] args) throws Exception {
        ImClient client = new ImClient(Constant.REMOTE_HOST, Constant.PORT);

        client.connect();
        System.out.println("连接成功！");

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String msg = scanner.next();
            if ("exit".equalsIgnoreCase(msg)) {
                break;
            } else {
                System.out.println("客户端：" + msg);
                client.sendMsg(MsgData.of(msg, MessageTypeEnum.TEST_MESSAGE));
            }
        }

        client.close();
    }

}
