package com.zenghui.demo.bio.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * @ClassName ChatHandler
 * @Description: TODO
 * @Author zeng
 * @Date 2020/4/6
 **/
public class ChatHandler implements Runnable {

    //服务端
    private ChatServer server;
    //客户端
    private Socket socket;

    public ChatHandler(ChatServer server, Socket socket){
        this.server = server;
        this.socket = socket;
    }

    //BIO 每对应一个客户都要创建一个 ChatHandler 用来传递消息

    @Override
    public void run() {
        try {
            //存储 新上线用户
            server.addClient(socket);

            //读取用户发送的消息
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            String msg = null;
            //客户端还在输入信息
            while ((msg = reader.readLine()) != null){
                String fwdMsg = "客户端[" + socket.getPort()+"]:" + msg + "\n";
                System.out.print(fwdMsg);

                //将消息转发给聊天室里在线的其他用户
                server.forwardMessage(socket, fwdMsg);

                //检查用户是否准备退出
                if (server.readyToQuit(msg)){
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            //客户端下线, 从在线表中删除
            try {
                server.removeClient(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
