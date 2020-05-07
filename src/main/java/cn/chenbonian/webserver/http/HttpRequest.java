package cn.chenbonian.webserver.http;

import cn.chenbonian.webserver.core.EmptyRequestException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求对象 每个实例表示客户端发送过来的一个具体请求
 *
 * @author chbn
 * @create 2020-05-07 11:04
 */
public class HttpRequest {
  /*
   * 请求行相关信息定义
   */
  // 请求方式
  private String method;
  // 资源路径
  private String url;
  // 协议版本
  private String protocol;

  // url中的请求部分
  private String requestURI;
  // url中的参数部分
  private String queryString;
  // 每个参数
  private Map<String, String> parameters = new HashMap<String, String>();

  /*
   * 消息头相关信息定义
   */
  private Map<String, String> headers = new HashMap<String, String>();

  /*
   * 消息正文相关信息定义
   */

  // 客户端连接相关信息
  private Socket socket;
  private InputStream in;

  /**
   * 初始化请求
   *
   * @throws EmptyRequestException
   */
  public HttpRequest(Socket socket) throws EmptyRequestException {
    try {
      this.socket = socket;
      this.in = socket.getInputStream();
      /*
       * 解析请求
       * 1:解析请求行
       * 2:解析消息头
       * 3:解析消息正文
       */
      parseRequestLine();
      parseHeaders();
      parseContent();

    } catch (EmptyRequestException e) {
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  /**
   * 解析请求行
   *
   * @throws EmptyRequestException
   */
  private void parseRequestLine() throws EmptyRequestException {
    System.out.println("开始解析请求行...");
    try {
      String line = readLine();
      System.out.println("请求行:" + line);
      /*
       * 将请求行进行拆分，将每部分内容
       * 对应的设置到属性上。
       */
      String[] data = line.split("\\s");
      if (data.length != 3) {
        // 空请求
        throw new EmptyRequestException();
      }
      method = data[0];
      url = data[1];
      // 进一步解析URL
      parseURL();
      protocol = data[2];

      System.out.println("method:" + method);
      System.out.println("url:" + url);
      System.out.println("protocol:" + protocol);
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println("请求行解析完毕!");
  }
  /**
   * 进一步解析URL url有可能会有两种格式:带参数和不带参数 1,不带参数如: /myweb/index.html
   *
   * <p>2,带参数如: /myweb/reg?username=zhangsan&password=123456&nickname=asan&age=22
   */
  private void parseURL() {
    /*
     * 首先判断当前url是否含有参数,判断的
     * 依据是看url是否含有"?",含有则认为
     * 这个url是包含参数的，否则直接将url
     * 赋值给requestURI即可。
     *
     *
     * 若有参数:
     * 1:将url按照"?"拆分为两部分，第一部分
     *   为请求部分，赋值给requestURI
     *   第二部分为参数部分，赋值给queryString
     *
     * 2:再对queryString进一步拆分，先按照"&"
     *   拆分出每个参数，再将每个参数按照"="
     *   拆分为参数名与参数值，并存入parameters
     *   这个Map中。
     *
     * 解析过程中要注意url的几个特别情况:
     * 1:url可能含有"?"但是没有参数部分
     * 如:
     * /myweb/reg?
     *
     * 2:参数部分有可能只有参数名没有参数值
     * 如:
     * /myweb/reg?username=&password=123&age=16...
     */
    if (url.indexOf("?") != -1) {
      // 按照"?"拆分
      String[] data = url.split("\\?");
      requestURI = data[0];
      // 判断?后面是否有参数
      if (data.length > 1) {
        queryString = data[1];
        // 进一步解析参数部分
        parseParameter(queryString);
      }
    } else {
      // 不含有?
      requestURI = url;
    }

    System.out.println("requestURI:" + requestURI);
    System.out.println("queryString:" + queryString);
    System.out.println("parameters:" + parameters);
  }

  /** 解析消息头 */
  private void parseHeaders() {
    System.out.println("开始解析消息头...");
    try {
      /*
       * 解析消息头的流程:
       * 循环调用readLine方法，读取每一个消息头，当readLine方法返回值为空字符串时停止循环(因为返回空字符串说明单独读取了CRLF而这是作为消息头结束的标志)
       * 在读取到每个消息头后，根据": "(冒号空格)进行拆分，并将消息头的名字做为key，消息头对应的值作为value保存到属性headers这个Map中完成解析工作
       */
      while (true) {
        String line = readLine();
        if ("".equals(line)) {
          break;
        }
        String[] data = line.split(":\\s");
        headers.put(data[0], data[1]);
      }
      System.out.println("headers:" + headers);
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("消息头解析完毕!");
  }
  /** 解析消息正文 */
  private void parseContent() {
    System.out.println("开始解析消息正文...");
    /*
     * 根据消息头是否含有Content-Length决定该请求是否含有消息正文
     */
    try {
      if (headers.containsKey("Content-Length")) {
        // 含有消息正文的
        int length = Integer.parseInt(headers.get("Content-Length"));
        // 读取消息正文内容
        byte[] data = new byte[length];
        in.read(data);

        /*
         * 根据消息头Content-Type判断该
         * 消息正文的数据类型
         */
        String contentType = headers.get("Content-Type");
        // 判断是否为form表单提交数据
        if ("application/x-www-form-urlencoded".equals(contentType)) {
          /*
           * 该正文内容相当于原GET请求地址栏里
           * url中“?”右侧内容
           */
          String line = new String(data, "ISO8859-1");
          System.out.println("正文内容:" + line);
          parseParameter(line);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println("消息正文解析完毕!");
  }
  /**
   * 解析参数 格式:name=value&name=value&...
   *
   * @param line
   */
  private void parseParameter(String line) {
    /*
     * 现将参数中的"%XX"的内容按照对应
     * 字符集(浏览器通常用UTF-8)还原为
     * 对应文字
     */
    try {
      /*
       * URLDecoder的decode方法可以将给定的字符串中的"%XX"内容转为对应2进制字节
       * 然后按照给定的字符集将这些字节还原
       * 为对应字符并替换这些"%XX"部分，然后
       * 将换好的字符串返回
       * 比如line的内容为:
       * username=%E9%99%88%E6%9F%8F%E5%B9%B4&password=123456&nickname=chbn&age=22
       * 转码完毕后为:
       * username=陈柏年&password=123456&nickname=chbn&age=22
       *
       */
      System.out.println("对参数转码前:" + line);
      line = URLDecoder.decode(line, "UTF-8");
      System.out.println("对参数转码后:" + line);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    // 按照&拆分出每一个参数
    String[] paraArr = line.split("&");
    // 遍历每个参数进行拆分
    for (String para : paraArr) {
      // 再按照"="拆分每个参数
      String[] paras = para.split("=");
      if (paras.length > 1) {
        // 该参数有值
        parameters.put(paras[0], paras[1]);
      } else {
        // 没有值
        parameters.put(paras[0], null);
      }
    }
  }

  /**
   * 读取一行字符串，当连续读取CR,LF时停止 并将之前的内容以一行字符串形式返回。
   *
   * @return
   * @throws IOException
   */
  private String readLine() throws IOException {
    StringBuilder builder = new StringBuilder();
    // 本次读取的字节
    int d = -1;
    // c1表示上次读取的字符，c2表示本次读取的字符
    char c1 = 'a', c2 = 'a';
    while ((d = in.read()) != -1) {
      c2 = (char) d;
      if (c1 == HttpContext.CR && c2 == HttpContext.LF) {
        break;
      }
      builder.append(c2);
      c1 = c2;
    }
    return builder.toString().trim();
  }

  public String getMethod() {
    return method;
  }

  public String getUrl() {
    return url;
  }

  public String getProtocol() {
    return protocol;
  }
  /**
   * 根据给定的消息头的名字获取对应消息头的 值
   *
   * @param name
   * @return
   */
  public String getHeader(String name) {
    return headers.get(name);
  }

  public String getRequestURI() {
    return requestURI;
  }

  public String getQueryString() {
    return queryString;
  }
  /**
   * 根据给定的参数名获取对应的参数值
   *
   * @param name
   * @return
   */
  public String getParameter(String name) {
    return parameters.get(name);
  }
}