package com.zenghui.demo.bio.client;

import java.io.*;
import java.net.Socket;

/**
 * 发生消息给服务器, 接收转发过来的消息
 * @ClassName ChatClient
 * @Description: TODO
 * @Author zeng
 * @Date 2020/4/6
 **/
public class ChatClient {

    private final String DEFAULT_SERVER_HOST = "127.0.0.1";
    private final int DEFAULT_SERVER_PORT = 9999;
    private final String QUIT ="quit";

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    /**
     * 客户端发生消息
     * 异常不在函数处理, 给调用者处理
     * @param msg 消息
     * @throws IOException
     */
    public void send(String msg) throws IOException {
        //输出流没有被关闭
        if (!socket.isOutputShutdown()){
            writer.write(msg+"\n");
            writer.flush();
        }
    }

    /**
     * 从服务端接收消息
     * @return
     * @throws IOException
     */
    public String receive() throws IOException {
        String msg = null;
        //输入流是开放状态
        if (!socket.isInputShutdown()){
            msg = reader.readLine();
        }
        return msg;
    }

    //检查用户是否准备退出
    public boolean readyToQuit(String msg){
        return QUIT.equals(msg);
    }

    public void close(){
        if (writer != null){
            try {
                System.out.println("关闭客户端socket");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void start(){
        try {
            //创建socket实例
            socket = new Socket(DEFAULT_SERVER_HOST,DEFAULT_SERVER_PORT);

            //创建IO 流
            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );
            writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())
            );

            //处理用户的输入, 因为等待用户输入是个阻塞的行为, 应该重新创个线程去专门等待用户输入,
            //否则没法接收数据
            new Thread(new UserInputHandler(this)).start();

            // 读取服务器转发的信息
            String msg = null;
            while ((msg = receive()) != null){
                System.out.println(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            close();
        }
    }

    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient();
        chatClient.start();
    }

}
