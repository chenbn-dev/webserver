package cn.chenbonian.webserver.http;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Http协议相关内容定义
 *
 * @author chbn
 * @create 2020-05-07 11:04
 */
public class HttpContext {
  /** 回车符CR */
  public static final int CR = 13;
  /** 换行符LF */
  public static final int LF = 10;

  /*
   * 状态代码与对应状态描述
   * key:状态代码
   * value:状态描述
   */
  private static Map<Integer, String> status_code_reason_mapping = new HashMap<Integer, String>();

  /*
   *	介质类型映射
   *  key:资源后缀名
   *  value:介质类型(Content-Type对应的值)
   */
  private static Map<String, String> mime_mapping = new HashMap<String, String>();

  static {
    // 初始化静态成员
    initStatusMapping();
    initMimeMapping();
  }
  /** 初始化介质类型 */
  private static void initMimeMapping() {
    //		mime_mapping.put("html", "text/html");
    //		mime_mapping.put("png", "image/png");
    //		mime_mapping.put("gif", "image/gif");
    //		mime_mapping.put("jpg", "image/jpeg");
    //		mime_mapping.put("css", "text/css");
    //		mime_mapping.put("js", "application/javascript");
    /*
     * 解析conf/web.xml文件，将根标签中所有
     * 名为<mime-mapping>的子标签获取到，并
     * 将该标签中的子标签<extension>中间的文本
     * 作为key，子标签<mime-type>中间的文本作为
     * value保存到mime_mapping这个Map中完成
     * 初始化工作
     */
    try {
      SAXReader reader = new SAXReader();
      Document doc = reader.read(new File("./src/conf/web.xml"));
      Element root = doc.getRootElement();
      List<Element> mimeList = root.elements("mime-mapping");
      for (Element mime : mimeList) {
        String ext = mime.elementText("extension");
        String type = mime.elementText("mime-type");
        mime_mapping.put(ext, type);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /** 初始化状态代码与对应描述 */
  private static void initStatusMapping() {
    status_code_reason_mapping.put(200, "OK");
    status_code_reason_mapping.put(201, "Created");
    status_code_reason_mapping.put(202, "Accepted");
    status_code_reason_mapping.put(204, "No Content");
    status_code_reason_mapping.put(301, "Moved Permanently");
    status_code_reason_mapping.put(302, "Moved Temporarily");
    status_code_reason_mapping.put(304, "Not Modified");
    status_code_reason_mapping.put(400, "Bad Request");
    status_code_reason_mapping.put(401, "Unauthorized");
    status_code_reason_mapping.put(403, "Forbidden");
    status_code_reason_mapping.put(404, "Not Found");
    status_code_reason_mapping.put(500, "Internal Server Error");
    status_code_reason_mapping.put(501, "Not Implemented");
    status_code_reason_mapping.put(502, "Bad Gateway");
    status_code_reason_mapping.put(503, "Service Unavailable");
  }
  /**
   * 根据状态代码获取对应的状态描述
   *
   * @param code
   * @return
   */
  public static String getStatusReason(int code) {
    return status_code_reason_mapping.get(code);
  }
  /**
   * 根据资源后缀获取对应的介质类型
   *
   * @param ext
   * @return
   */
  public static String getMimeType(String ext) {
    return mime_mapping.get(ext);
  }

  public static void main(String[] args) {
    String type = getMimeType("png");
    System.out.println(type);
  }
}
