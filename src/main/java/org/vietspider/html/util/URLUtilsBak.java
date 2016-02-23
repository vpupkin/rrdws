/***************************************************************************
 * Copyright 2004-2006 The VietSpider All rights reserved.  *
 **************************************************************************/
package org.vietspider.html.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;

import org.vietspider.chars.refs.RefsDecoder;
import org.vietspider.common.Application;
import org.vietspider.common.io.LogService;
import org.vietspider.common.text.SWProtocol;

public class URLUtilsBak {
  
  public final static char [] URICS  = {';', '?', ':', '@', '&', '=', '+', '$', ',', '/'};
  
  private final static char [] SPECS  = {
    '[', ']', '|', '<', '>', '^', '{', '}'
  };
  private final static String [] CODE_SPECS = {
    "%5B", "%5D", "%7C", "%3C", "%3E", "%5E", "%7B", "%7D"
  };
  
  private final static String AND_ENCODE  =  "&amp;";
  private final static String AND_DECODE  =  "&";
  
  private RefsDecoder decoder;
  
  static {
    Arrays.sort(URICS);
  }
  
  public URLUtilsBak() {
    decoder = new RefsDecoder();
  }
  
  public synchronized String createURL(URL url, String link) {
    link = createURL(url.getFile(), link);
    return createURL(url.getHost(), url.getPort(), url.getProtocol(), link);
  }

  public synchronized String createURL(String host, int port, String protocol, String link) {
    if(link.startsWith("http")
        || link.startsWith("https")
        || link.startsWith("ftp"))  return link;

    String url =protocol+"://"+host;
    if( port >= 0) url += ":"+String.valueOf( port);
    url += link;
    return url;
  }
  
  @SuppressWarnings("unused")
  public synchronized String createURL(String address, String link)  {
    return createURL(address, link, false);
  }

  @SuppressWarnings("unused")
  private synchronized String createURL(String address, String link, boolean expire)  {
    link = replaceSpace(link);
    if(link.startsWith("http")
        || link.startsWith("https")
        || link.startsWith("ftp")
        || link.startsWith("/"))  return link;
    
//    System.out.println(" vao "+address + " : "+link);
    String path  = "";
    String query = "";
    StringBuilder builder = new StringBuilder();
    
    try {
      URI uri = new URI(address);
      path = uri.getPath();
      query = uri.getQuery();
    }catch (URISyntaxException e) {
      if(!expire) return createURL(replaceSpace(address), link, true);
//      // e.  printStackTrace();
      LogService.getInstance().setMessage(e, "URLUtils 76: ");
    }catch (Exception e) {
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
      
      if(uri.getQuery() == null && link.indexOf('/') < 0 & query != null) {
        builder.append('?').append(query);
        if(query.length() > 0 && link.length() > 0 
            && Arrays.binarySearch(URLUtilsBak.URICS, query.charAt(query.length()-1)) < 0 
            && Arrays.binarySearch(URLUtilsBak.URICS, link.charAt(0)) < 0) builder.append('&');
      } 
      builder.append(link);
    }catch (URISyntaxException e) {
      if(!expire) return createURL(address, replaceSpace(link), true);
//      // e.  printStackTrace();
      LogService.getInstance().setMessage(e, "URLUtils 99: ");
    }catch (Exception e) {
      LogService.getInstance().setThrowable(e);
//      System.err.println(e.toString());
    }
    
    if(builder.length() > 0 && 
        Arrays.binarySearch(URLUtilsBak.URICS, builder.charAt(0)) < 0) {
      builder.insert(0, '/');
    }
    
    int idx = -1;
    while((idx = builder.indexOf("/..")) == 0) {
      builder = builder.delete(0, 3);
    }
    return builder.toString();
  }
  
  public String replaceSpace(String text) {
    if(text.indexOf(AND_ENCODE) > -1) {
      text = text.replaceAll(AND_ENCODE, AND_DECODE);
    }
    
    int idx = text.indexOf(SWProtocol.HTTP); 
    if(idx > 0) text = text.substring(idx);
    
    int start = 0;
    int i = 0;    
    StringBuilder builder = new StringBuilder(text.length() + 10);
    boolean hasSharp = false;
    int spec_idx = -1;
    while(i < text.length()) {
      char c = text.charAt(i);
      if((Character.isSpaceChar(c) || Character.isWhitespace(c))) {
        if(start < i) {
          builder.append(text.subSequence(start, i));
          start = i+1;
        }
        if(i > 0 && Arrays.binarySearch(URLUtilsBak.URICS, text.charAt(i-1)) > -1) {
          i++;
          continue;
        }
        while(i+1 < text.length() && 
              (Character.isSpaceChar(text.charAt(i+1)) 
                || Character.isWhitespace(text.charAt(i+1))) ) {
          i++;
        }
        start = i+1;
        if(i+1 < text.length() && Arrays.binarySearch(URLUtilsBak.URICS, text.charAt(i+1)) > -1) {
          i++;
          continue;
        }
        if(i < text.length() - 1) builder.append("%20"); 
      }
      else if(c == '#') hasSharp = true;
      else if((spec_idx = searchSpecs(c)) > -1 && !hasSharp) {
        if(start <= i) {
          builder.append(text.subSequence(start, i));
          builder.append(CODE_SPECS[spec_idx]);
          start = i+1;
        }
      }
      else if(c == '\\') {
        if(start <= i) {
          builder.append(text.subSequence(start, i));
          builder.append('/');
          start = i+1;
        }
      } else if(Character.isUnicodeIdentifierPart(c)) {
        if(start <= i) {
          builder.append(text.subSequence(start, i));
          try {
            builder.append(URLEncoder.encode(String.valueOf(c), Application.CHARSET));
          }catch (Exception e) {
            builder.append('c');
          }
          start = i+1;
        }
      } 
      i++;
    }   
    
    if(start < text.length() && start > 0) {
      builder.append(text.subSequence(start, text.length()));
    }
    
    if(builder.length() < 1) return text;
//    if(builder.charAt(builder.length() - 1) == '\'' || builder.charAt(builder.length() - 1) == '\"') {
//      builder.deleteCharAt(builder.length() - 1);
//    }
//    if(builder.charAt(0) == '\'' || builder.charAt(0) == '\"') builder.deleteCharAt(0);
    
    return new String(decoder.decode(builder.toString().toCharArray()));
  }
  
  private int searchSpecs(char c) {
    for(int i = 0; i < SPECS.length; i++) {
      if(SPECS[i] == c) return i;
    }
    return -1;
  }
  
  public boolean compareHost(String host1, String host2) {
    int i = host1.length() - 1;
    int j = host2.length() - 1;
    int counter = 0;
    while(i >= 0 && j >= 0) {
      if(Character.toLowerCase(host1.charAt(i)) != Character.toLowerCase(host2.charAt(j))) break;
      if(host1.charAt(i) == '.') counter++;
      i--;
      j--;
      if(i == -1 && (j == -1 || host2.charAt(j) == '.')) counter++;
      else if(j == -1 && (i  == -1 || host1.charAt(i) == '.')) counter++;
      if(counter >= 3) break;
    }
    if(counter >= 3) return true;
//    System.out.println(host1+ " : " + counter);
//    if(counter < 2) return false;
    if(host1.startsWith("www")) host1 = host1.substring(host1.indexOf('.') + 1);
    if(host2.startsWith("www")) host2 = host2.substring(host2.indexOf('.') + 1);
    return host1.equalsIgnoreCase(host2);
  }
  
  public synchronized int hashCode(URL url, String ignoresParam) {
    int hashCode = 0;
    
    // Generate the protocol part.
    String protocol = url.getProtocol();
    if (protocol != null) hashCode += protocol.hashCode();

    // Generate the host part.
    String host = url.getHost();
    if (host != null) hashCode += hashCodeHost(host);
    
    // Generate the path part.
    String path = url.getPath();
    if (path != null && !(path = path.trim()).isEmpty()) hashCode += path.hashCode();
    
    String query = url.getQuery();
    if(query != null && !(query = query.trim()).isEmpty()) {
      String [] elements = query.split("\\&");
      for(String element : elements) {
        if(element.isEmpty() || 
            (ignoresParam != null && element.startsWith(ignoresParam))) continue;
        hashCode += element.hashCode();
      }
    }
      
    // Generate the port part.
    if (url.getPort() == -1) hashCode += 80; else hashCode += url.getPort();

    // Generate the ref part.
    String ref = url.getRef();
    if (ref != null) hashCode += ref.hashCode();

    return hashCode;
  }
  
  private int hashCodeHost(String host) {
    int h = 0;
    int off = 0;
    while(off < 2 && off < host.length()) {
      if(Character.toLowerCase(host.charAt(off)) != 'w') break;
      off++;
    }
    
    if(off > 1) {
      while(off < host.length() && host.charAt(off) != '.') {
        off++;
      }
      if(off < host.length() && host.charAt(off) == '.') off++;
    } else {
      off = 0;
    }
    for (int i = off; i < host.length(); i++) {
      h = 31*h + Character.toLowerCase(host.charAt(off++));
    }
    return h;
  }

}
