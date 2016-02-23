/***************************************************************************
 * Copyright 2004-2006 The VietSpider All rights reserved.  *
 **************************************************************************/
package org.vietspider.chars;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.vietspider.common.Application;
import org.vietspider.common.io.LogService;
import org.vietspider.common.text.SWProtocol;

public class URLUtilsBak {

  public final static char [] URICS  = {';', '?', ':', '@', '&', '=', '+', '$', ',', '/'};

  static {
    Arrays.sort(URICS);
  }

  protected URLEncoder encoder; 

  public URLUtilsBak() {
    encoder = new URLEncoder(); 
  }

  public synchronized String createURL(URL url, String link) {
    link = createURL(url.getFile(), link);
    return createURL(url.getHost(), url.getPort(), url.getProtocol(), link);
  }

  public synchronized String createURL(String host, int port, String protocol, String link) {
    if(SWProtocol.isHttp(link))  return link;

    String url =protocol+"://"+host;
    if( port >= 0) url += ":"+String.valueOf(port);
    url += link;
    return url;
  }

  @SuppressWarnings("unused")
  public synchronized String createURL(String address, String link)  {
    link = encoder.encode(link);
    address = encoder.encode(address);
    
    if(SWProtocol.isHttp(link) || link.startsWith("/"))  return link;

    String path  = "";
    String query = null;
    
    StringBuilder builder = new StringBuilder();

    try {
      URI uri = new URI(address);
      path = uri.getPath();
      query = uri.getQuery();
    } catch (URISyntaxException e) {
//    // e.  printStackTrace();
      if(SWProtocol.isHttp(address)) {
        LogService.getInstance().setMessage(e, "URLUtils 76: ");
      }
    } catch (Exception e) {
      LogService.getInstance().setThrowable(e);
    }
    
    try {
      URI uri = new URI(link);
      if(path != null && path.length() > 0 && path.charAt(path.length()-1) != '/' 
        && uri.getPath() != null && uri.getPath().trim().length() > 0) {
        builder.append(path.subSequence(0, path.lastIndexOf('/')+1));
      } else {
        builder.append(path);
      }
      
      if(uri.getQuery() == null && link.indexOf('/') < 0 
          && (link.indexOf('?') > -1  ||
              (query != null && query.length() > 0 && query.charAt(query.length()-1) == '='))) {
        if(query == null) query = "";
        builder.append('?').append(query);
        if(query.length() > 0 && link.length() > 0 
            && Arrays.binarySearch(URICS, query.charAt(query.length()-1)) < 0 
            && Arrays.binarySearch(URICS, link.charAt(0)) < 0) builder.append('&');
      } 

      builder.append(link);
    } catch (URISyntaxException e) {
//    // e.  printStackTrace();
      if(SWProtocol.isHttp(link)) {
        LogService.getInstance().setMessage(e, "URLUtils 99: ");
      }
    } catch (Exception e) {
      LogService.getInstance().setThrowable(e);
    }

    if(builder.length() > 0 && 
        Arrays.binarySearch(URLUtilsBak.URICS, builder.charAt(0)) < 0) {
      builder.insert(0, '/');
    }

    int idx = -1;
    while((idx = builder.indexOf("/..")) == 0) {
      builder = builder.delete(0, 3);
    }

//  System.out.println(" ra "+ builder);
    return builder.toString();
  }

  public static Map<String, String> getParams(String s) throws Exception {
    if(s == null) return null;
    Map<String, String> ps = new HashMap<String, String>();
    String[] paramStrs = s.split("\\&");
    for(String psStr: paramStrs){
      psStr= URLDecoder.decode(psStr, Application.CHARSET);
      int index = psStr.indexOf('='); 
      if(index < 0) continue;
      String key = psStr.substring(0, index);
      String value = psStr.substring(index+1);
//    System.out.print("Key=" +key);
//    System.out.println(" || Value=" +value);
//    System.out.println("-------------------------------");
      ps.put(key, value);
    }
    return ps;
  }

  public String getCanonical(String address) {
    if (address == null) return null;
    int index = SWProtocol.lastIndexOf(address);
    if(index > 0) return normalize(address);

    index = address.indexOf('/');
    if(index < 0) return address;
    int dotIndex = address.indexOf('.');
    if(dotIndex > 0 &&  dotIndex < index) {
      return normalize("http://"+address);
    }

    try {
      URI uri = new URI(address);
      return uri.normalize().toString();
    } catch (Exception e) {
      return address;
    }

  }

  private String normalize(String address)  {
    try {
      URL url  = new URL(address);
      String path = url.getPath();
      URI uri = new URI(encoder.encode(normalizePath(path)));
      String newPath = uri.normalize().toString();
      int index = address.indexOf(path);
      return address.substring(0, index) + newPath + address.substring(index+path.length());
//      return address.replaceAll(path, newPath);
    } catch (MalformedURLException e) {
      LogService.getInstance().setMessage("APPLICATION", e, address);
      return address;
    } catch (Exception e) {
      LogService.getInstance().setThrowable("APPLICATION", e, address);
      return address;
    }
  }
  
  private String normalizePath(String path) {
    int index = 0;
    StringBuilder builder = new StringBuilder();
    while(index < path.length()) {
      char c = path.charAt(index);
      if(c == '/') {
        if(index > 0){
          if(path.charAt(index-1) != '/') builder.append(c);
        } else {
          builder.append(c);
        }
      } else {
        builder.append(c);
      }
      index++;
    }
    return builder.toString();
  }

  public URLEncoder getEncoder() { return encoder; }
  
  public static void main(String[] args) {
    String address = "http://hiephoioto.com/sales/detail/d58f0f/../***-BAN-TRA-GOP-XE-HYUNDAI-25C---XE-KHACH-45C---49C-$$$/";
    
    URLUtilsBak utils = new URLUtilsBak();
    System.out.println(address);
    System.out.println(utils.getCanonical(address));
  }
}

