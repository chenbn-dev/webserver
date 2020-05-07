package cn.chenbonian.webserver.core;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * WebServer 主类
 *
 * @author chbn
 * @create 2020-05-07 10:29
 */
public class WebServer {

  private ServerSocket server;
  private ExecutorService threadPool;

  /** 构造方法，用来初始化服务端 */
  public WebServer() {
    try {
      System.out.println("正在启动服务器...");
      server = new ServerSocket(8080);
      threadPool = Executors.newFixedThreadPool(50);
      System.out.println("服务器初始化完毕！");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void start() {
    try {
      while (true) {
        System.out.println("等待客户端连接...");
        Socket socket = server.accept();
        threadPool = Executors.newFixedThreadPool(50);
        System.out.println("一个客户端连接了！");
        // 启动一个线程，处理该客户端请求
        ClientHandler handler = new ClientHandler(socket);
        threadPool.execute(handler);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    WebServer server = new WebServer();
    server.start();
  }
}
