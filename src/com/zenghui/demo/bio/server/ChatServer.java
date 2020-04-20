package com.zenghui.demo.bio.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName ChatServer
 * @Description: TODO
 * @Author zeng
 * @Date 2020/4/6
 **/
public class ChatServer {

    private final int DEFAULT_PORT = 9999;
    private final String QUIT = "quit";

    private ServerSocket serverSocket;
    // 保存在线用户 已经 向他们发信息所用到的 Writer
    private Map<Integer, Writer> connectedClients;

    public ChatServer(){
        connectedClients = new HashMap<>();
    }

    /**
     * 获取客户端的连接
     * 对connectedClients 操作有线程安全性问题
     * @param socket
     */
    public synchronized void addClient(Socket socket) throws IOException {
        if (socket != null){
            int port = socket.getPort();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())
            );
            connectedClients.put(port,writer);
            System.out.println("客户端[" + port + "]连接到客户端");
        }
    }

    /**
     * 客户端下线
     * @param socket
     * @throws IOException
     */
    public synchronized void removeClient(Socket socket) throws IOException {
        if (socket != null){
            int port = socket.getPort();
            if (connectedClients.containsKey(port)){
                //writer 是装饰者模式的 外层, writer关闭 包裹的socket 也会关闭
                connectedClients.get(port).close();
            }
            connectedClients.remove(port);
            System.out.println("客户端[" + port + "]已断开连接");
        }
    }

    /**
     * 转发消息,转发给其他客户端
     * 读取也可能有线程安全性, 别的刚写入的没有读到
     * @param socket 发送者 的客户端 socket
     * @param fwdMsg 发送的消息
     */
    public synchronized void forwardMessage(Socket socket, String fwdMsg) throws IOException {
        for (Integer id : connectedClients.keySet()) {
            //将消息转发给非 发送者
            if (!id.equals(socket.getPort())){
                Writer writer = connectedClients.get(id);
                writer.write(fwdMsg);
                writer.flush();
            }
        }
    }

    /**
     * 是否客户端退出
     * @param msg
     * @return
     */
    public boolean readyToQuit(String msg){
        return QUIT.equals(msg);
    }

    /**
     * 关闭 serverSocket
     */
    public  synchronized void close(){
        if (serverSocket != null){
            try {
                serverSocket.close();
                System.out.println("关闭serverSocket");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void start(){
        //绑定监听端口
        try {
            serverSocket = new ServerSocket(DEFAULT_PORT);
            System.out.println("启动服务器,监听端口: " + DEFAULT_PORT +"...");

            //监听是否有客户端连接
            while (true){
                //等待客户端连接
                Socket socket = serverSocket.accept();
                // BIO 要为每个连接创建 CharHandler 线程
                new Thread( new ChatHandler(this,socket)).start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            close();
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }

}
