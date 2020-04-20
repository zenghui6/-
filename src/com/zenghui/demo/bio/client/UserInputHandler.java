package com.zenghui.demo.bio.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 用来处理用户的输入 的 线程 , 这样客户端就可以接收来自服务器的数据
 * 该线程要独立于主线程之外
 * @ClassName UserInputHandler
 * @Description: TODO
 * @Author zeng
 * @Date 2020/4/6
 **/
public class UserInputHandler implements Runnable {

    private ChatClient chatClient;

    public UserInputHandler(ChatClient chatClient){
        this.chatClient = chatClient;
    }

    @Override
    public void run() {

             try {
                 //等待用户输入消息
                 BufferedReader consoleReader = new BufferedReader(
                         new InputStreamReader(System.in)
                 );

                 while (true) {
                     String input = consoleReader.readLine();

                     //向服务器发送消息
                     chatClient.send(input);

                     //检查是否准备退出
                     if (chatClient.readyToQuit(input)){
                         break;
                     }
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             }
    }
}
