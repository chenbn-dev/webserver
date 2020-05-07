package cn.chenbonian.webserver.servlets;

import cn.chenbonian.webserver.http.HttpRequest;
import cn.chenbonian.webserver.http.HttpResponse;

import java.io.RandomAccessFile;
import java.util.Arrays;

/**
 * 处理注册业务
 *
 * @author chbn
 * @create 2020-05-07 11:05
 */
public class RegServlet extends HttpServlet {
  public void service(HttpRequest request, HttpResponse response) {
    /*
     * 注册大致流程:
     * 1:获取用户提交的注册信息
     * 2:将注册信息写入文件user.dat
     * 3:响应客户端注册成功的页面
     */
    System.out.println("开始处理注册业务!!!");
    /*
     * 1
     * 通过request.getParameter()方法获取用户提交上来的数据时，传递的参数这个字符串的值应当是页面中form表单里对应的输入框的名字(name属性的值)
     */
    String username = request.getParameter("username");
    String password = request.getParameter("password");
    String nickname = request.getParameter("nickname");
    int age = Integer.parseInt(request.getParameter("age"));
    System.out.println("username:" + username);
    System.out.println("password:" + password);
    System.out.println("nickname:" + nickname);
    System.out.println("age:" + age);

    /*
     * 2
     * 每条记录占100字节，其中用户名，密码昵称为字符串，各占32字节。
     * 年龄为int值占4字节。写入到user.dat文件。
     */
    try (RandomAccessFile raf = new RandomAccessFile("user.dat", "rw"); ) {
      // 现将指针移动到文件末尾
      raf.seek(raf.length());
      // 写用户名
      // 1先将用户名转成对应的一组字节
      byte[] data = username.getBytes("UTF-8");
      // 2将该数组扩容为32字节
      data = Arrays.copyOf(data, 32);
      // 3将该字节数组一次性写入文件
      raf.write(data);
      // 写密码
      data = password.getBytes("UTF-8");
      data = Arrays.copyOf(data, 32);
      raf.write(data);
      // 写昵称
      data = nickname.getBytes("UTF-8");
      data = Arrays.copyOf(data, 32);
      raf.write(data);
      // 写年龄
      raf.writeInt(age);
      System.out.println("注册完毕!");
      // 3响应客户端注册成功页面
      forward("./myweb/reg_success.html", request, response);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
