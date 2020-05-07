package cn.chenbonian.webserver.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
/**
 * 响应对象 该类的每一个实例用于表示一个具体要给客户端 响应的内容 一个响应包含: 状态行，响应头，响应正文
 *
 * @author chbn
 * @create 2020-05-07 11:04
 */
public class HttpResponse {
  /*
     状态行相关信息定义
  */
  // 状态代码
  private int statusCode = 200;
  // 状态描述
  private String statusReason = "OK";

  /*
     响应头相关信息定义
  */
  private Map<String, String> headers = new HashMap<String, String>();

  /*
    响应正文相关信息定义
  */
  // 响应的实体文件
  private File entity;

  // 与连接相关信息定义
  private Socket socket;
  private OutputStream out;

  public HttpResponse(Socket socket) {
    try {
      this.socket = socket;
      this.out = socket.getOutputStream();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /** 将当前响应内容发送给客户端 */
  public void flush() {
    /*
     * 响应客户端:
     * 1:发送状态行
     * 2:发送响应头
     * 3:发送响应正文
     */
    try {
      sendStatusLine();
      sendHeaders();
      sendContent();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /** 发送状态行 */
  private void sendStatusLine() {
    try {
      String line = "HTTP/1.1" + " " + statusCode + " " + statusReason;
      println(line);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * 向客户端发送一行字符串,发送后会自动发送CR,LF
   *
   * @param line
   */
  private void println(String line) {
    try {
      out.write(line.getBytes("ISO8859-1"));
      out.write(HttpContext.CR); // written CR
      out.write(HttpContext.LF); // written LF
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /** 发送响应头 */
  private void sendHeaders() {
    try {
      // 遍历headers，将所有响应头发送
      Set<Entry<String, String>> set = headers.entrySet();
      for (Entry<String, String> header : set) {
        String key = header.getKey();
        String value = header.getValue();
        String line = key + ": " + value;
        println(line);
      }

      // 单独发送CRLF，表示响应头部分结束
      println("");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  /** 发送响应正文 */
  private void sendContent() {
    try (FileInputStream fis = new FileInputStream(entity); ) {
      byte[] data = new byte[1024 * 10];
      int len = -1;
      while ((len = fis.read(data)) != -1) {
        out.write(data, 0, len);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public File getEntity() {
    return entity;
  }
  /**
   * 设置响应实体文件，在设置的同时会根据文件类型
   * 自动添加对应的Content-Type与Content-Length
   * 这两个响应头。
   * @param entity
   */
  public void setEntity(File entity) {
    this.entity = entity;
    //根据给定的文件自动设置对应的Content-Type与Content-Length
    this.headers.put("Content-Length", entity.length()+"");
    //获取资源后缀名，去HttpContext中获取对应的介质类型
    //获取资源文件名
    String fileName = entity.getName();
    int index = fileName.lastIndexOf(".")+1;
    String ext = fileName.substring(index);
    String contentType = HttpContext.getMimeType(ext);
    this.headers.put("Content-Type",contentType);
  }

  public int getStatusCode() {
    return statusCode;
  }
  /**
   * 设置状态代码，设置后会自动将对应的描述
   * 设置好
   * @param statusCode
   */
  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
    this.statusReason = HttpContext.getStatusReason(statusCode);
  }

  public String getStatusReason() {
    return statusReason;
  }

  public void setStatusReason(String statusReason) {
    this.statusReason = statusReason;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }
  /**
   * 添加指定的响应头信息
   * @param name 响应头的名字
   * @param value 响应头对应的值
   */
  public void putHeader(String name,String value) {
    this.headers.put(name, value);
  }
}
