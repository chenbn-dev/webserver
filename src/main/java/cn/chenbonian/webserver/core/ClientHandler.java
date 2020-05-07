package cn.chenbonian.webserver.core;

import cn.chenbonian.webserver.http.HttpRequest;
import cn.chenbonian.webserver.http.HttpResponse;
import cn.chenbonian.webserver.servlets.HttpServlet;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

/**
 * 处理客户端请求
 *
 * @author chbn
 * @create 2020-05-07 10:55
 */
public class ClientHandler implements Runnable {

  private Socket socket;

  public ClientHandler(Socket socket) {
    this.socket = socket;
  }

  public void run() {
    try {
      /*
       * 主流程:
       * 1:解析请求
       * 2:处理请求
       * 3:发送响应
       */
      // 1.准备工作
      // 1.1 解析请求，创建请求对相关
      HttpRequest request = new HttpRequest(socket);
      // 1.2 创建相应对象
      HttpResponse response = new HttpResponse(socket);

      // 2. 处理请求
      // 2.1 获取请求的资源路径
      String url = request.getRequestURI();

      // 判断该请求是否为请求业务
      String servletName = ServerContext.getServletName(url);
      if (servletName != null) {
        System.out.println("ClientHandler:正在加载" + servletName);
        Class cls = Class.forName(servletName);
        HttpServlet servlet = (HttpServlet) cls.newInstance();
        servlet.service(request, response);
      } else {
        // 2.2:根据资源路径去webapps目录中寻找该资源
        File file = new File("webapps" + url);
        if (file.exists()) {
          System.out.println("找到该资源!");
          // 向响应对象中设置要响应的资源内容
          response.setEntity(file);
        } else {
          // 设置状态代码404
          response.setStatusCode(404);
          // 设置404页面
          response.setEntity(new File("webapps/root/404.html"));
          System.out.println("资源不存在!");
        }
      }
      // 3响应客户端
      response.flush();
      // http://localhost:8088/myweb/index.html
    } catch (EmptyRequestException e) {
      /*
       * 实例化HttpRequest时若发现是空请求时该构造方法会将该异常抛出，这里不做任何处理，直接在finally中与客户端断开即可
       */
      System.out.println("空请求.");
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      // 与客户端断开连接
      try {
        socket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
