/***************************************************************************
 * Copyright 2001-2009 The VietSpider         All rights reserved.  		 *
 **************************************************************************/
package org.vietspider.html.template;

import java.util.ArrayList;
import java.util.List;

import org.vietspider.html.HTMLNode;
import org.vietspider.html.Name;
import org.vietspider.html.NodeIterator;
import org.vietspider.html.path2.HTMLExtractor;
import org.vietspider.html.path2.NodePath;
import org.vietspider.html.path2.NodePathParser;
import org.vietspider.html.util.NodeHandler;
import org.vietspider.token.attribute.Attribute;
import org.vietspider.token.attribute.Attributes;

/** 
 * Author : Nhu Dinh Thuan
 *          nhudinhthuan@yahoo.com
 * Apr 13, 2009  
 */
public class VBulletinForumExtractor {
  
  private List<String> extractPaths = new ArrayList<String>();
  private String pagePath;
  private String titlePath;
  private String userPath = null;
  private String postPath = null;
  
  public void extract(HTMLNode root) {
   /* List<HTMLNode> list = searchDataNode(root);
    NodePathParser pathParser = new NodePathParser();
    for(int i = 0; i < list.size(); i++) {
      extractPaths.add(pathParser.toPath(list.get(i)).toString()); 
    }
  }
  
  private void searchDataNode(HTMLNode root) {*/
    NodePathParser pathParser = new NodePathParser();
    NodeIterator iterator = root.iterator();
   
    while(iterator.hasNext()) {
      HTMLNode node = iterator.next();
      if(!node.isNode(Name.DIV)) continue;
      Attributes attributes = node.getAttributes(); 
      Attribute attribute = attributes.get("id");
      if(attribute == null) continue;
      String value = attribute.getValue();
      if(value == null) continue;
      
      if("posts".equalsIgnoreCase(value)) {
        HTMLNode titleNode = searchTitleNode(root, node);
        if(titleNode != null) {
          titlePath = titleNode.getName()+"[0]";
          extractPaths.add(pathParser.toPath(titleNode).toString()); 
        }
        
        HTMLNode pageNode = searchPageNode(node);
        if(pageNode == null) pageNode = searchPageNode2(root);
        if(pageNode != null) {
          pagePath = "TABLE[0]";
          extractPaths.add(pathParser.toPath(pageNode).toString()); 
        }
        
        String threadPath = pathParser.toPath(node).toString();
        
        HTMLNode userNode = searchUserNode(node);
        if(userNode != null) {
          userPath = pathParser.toPath(userNode).toString();
          
          String path = userPath.substring(threadPath.length());
          int index = path.indexOf('[');
          if(index > -1) {
            path = path.substring(0, index+1) + "*" + path.substring(index+2, path.length());
          }
          if(titleNode != null && titleNode.isNode(Name.DIV)) {
            userPath = "DIV[1]" + path;
          } else {
            userPath = "DIV[0]" + path;
          }
          index = userPath.lastIndexOf("TD[");
          if(index > 0) {
            int end = userPath.indexOf(']', index);
            if(end > 0) {
              userPath = userPath.substring(0, index+3) + "i<2"+ userPath.substring(end);
            }
          }
        }
        if(userPath == null) return;
        
        
        HTMLNode postNode = searchContentNode(node, "post_message");
        if(postNode != null) {
          postPath = pathParser.toPath(postNode).toString();
          
          String path = postPath.substring(threadPath.length());
          int index = path.indexOf('[');
          if(index > -1) {
            path = path.substring(0, index+1) + "*" + path.substring(index+2, path.length());
          }
          
          if(titleNode != null && titleNode.isNode(Name.DIV)) {
            postPath = "DIV[1]" + path;            
          } else {
            postPath = "DIV[0]" + path;
          }
        } else {
          int start = userPath.indexOf("TD[");
          if(start > 0) {
            int end  = userPath.indexOf(']', start);
            try {
              int indexUser = Integer.parseInt(userPath.substring(start+3, end));
              postPath = userPath.substring(0, start) +"TD[" + String.valueOf(indexUser+1)+"]";
            } catch (Exception e) {
              // e.  printStackTrace();
            }
          }
           
        }
        
//        HTMLNode preNode = searchPreNode(node);
//        if(preNode != null) list.add(preNode);
        extractPaths.add(threadPath); 
      }
    }
  }
  
  private HTMLNode searchPageNode(HTMLNode root) {
    HTMLNode parent = root.getParent();
    if(parent == null) return null;
    NodeIterator iterator = parent.iterator();
    while(iterator.hasNext()) {
      HTMLNode node = iterator.next();
      if(!node.isNode(Name.CONTENT)) continue;
      String content  = node.getTextValue().toLowerCase();
      if(content.indexOf("page") < 0 
          && content.indexOf("trang") < 0) continue;
      HTMLNode table = upParent(node, Name.TABLE);
      if(table == null || !isPageList(table)) continue;
      return table;
    }
    return null;
  }
  
  private HTMLNode searchUserNode(HTMLNode root) {
    NodeIterator iterator = root.iterator();
    while(iterator.hasNext()) {
      HTMLNode node = iterator.next();
      if(!node.isNode(Name.A)) continue;
      Attributes attributes = node.getAttributes(); 
      Attribute attribute = attributes.get("class");
      if(attribute == null) continue;
      String value = attribute.getValue();
      if(value == null) continue;
      value = value.toLowerCase();
      if(value.indexOf("bigusername") > -1) return node;
    }
    return null;
  }
  
  private HTMLNode searchContentNode(HTMLNode root, String clazz) {
    NodeIterator iterator = root.iterator();
    while(iterator.hasNext()) {
      HTMLNode node = iterator.next();
      if(!node.isNode(Name.DIV)) continue;
      Attributes attributes = node.getAttributes(); 
      Attribute attribute = attributes.get("id");
      if(attribute == null) continue;
      String value = attribute.getValue();
      if(value == null) continue;
      value = value.toLowerCase();
      if(value.indexOf(clazz) > -1) {//"post_message"
        return upParent(node, Name.TD);
      }
    }
    return null;
  }
  
  private boolean isPageList(HTMLNode node) {
    NodeIterator iterator = node.iterator();
    while(iterator.hasNext()) {
      HTMLNode n = iterator.next();
      if(!n.isNode(Name.A)) continue;
      Attributes attributes = n.getAttributes();
      Attribute attribute = attributes.get("href");
      if(attribute == null) continue;
      String value = attribute.getValue();
      if(value == null) continue;
      if(value.indexOf("page=") > -1) return true;
    }
    return false;
  }
  
  private HTMLNode searchPageNode2(HTMLNode node) {
    NodeIterator iterator = node.iterator();
    HTMLNode table = null;
    while(iterator.hasNext()) {
      HTMLNode n = iterator.next();
      if(!n.isNode(Name.A)) continue;
      List<HTMLNode> children = n.getChildren();
      if(children == null 
          || children.size() != 1 
          || !children.get(0).isNode(Name.CONTENT)) continue;
      String text = children.get(0).getTextValue();
      try {
        Integer.parseInt(text.trim());
        table = upParent(n, Name.TABLE);
        break;
      } catch (Exception e) {
      }
    }
    if(table == null) return null;
    NodeHandler nodeHandler = new NodeHandler();
    iterator = node.iterator();
    while(iterator.hasNext()) {
      HTMLNode n = iterator.next();
      if(!n.isNode(Name.CONTENT)) continue;
      String text = n.getTextValue().toLowerCase().trim();
      if(text.startsWith("trang") || text.startsWith("page")) {
        if(nodeHandler.count(text) < 5) return table;
      }
    }
    return table;
  }
  
  
  private HTMLNode upParent(HTMLNode node, Name...names) {
    if(node == null) return null;
    for(int i = 0; i < names.length; i++) {
      if(node.isNode(names[i])) return node;
    }
    return upParent(node.getParent(), names);
  }
  
  private HTMLNode searchTitleNode(HTMLNode root, HTMLNode node) {
    HTMLExtractor extractor  = new HTMLExtractor();
    NodePathParser pathParser = new NodePathParser();
    String title = "title";
    try {
      NodePath nodePath  = pathParser.toPath("HEAD.TITLE");
      HTMLNode titleNode = extractor.lookNode(root, nodePath);
      if(titleNode.hasChildren()) {
        title  = titleNode.getChild(0).getTextValue();
      }
    } catch (Exception e) {
      return null;
    }
    
    HTMLNode parent = node.getParent();
    if(parent == null) return null;
    NodeIterator iterator = parent.iterator();
    while(iterator.hasNext()) {
      HTMLNode n = iterator.next();
      if(!n.isNode(Name.CONTENT)) continue;
      if(n.getParent().isNode(Name.A)) continue;
      String content = n.getTextValue();
      if(indexOf(title, content)) return upParent(n, Name.TD, Name.DIV, Name.STRONG);
    }
    return null;
  }
  
  
  
  private boolean indexOf(String title, String content) {
    int index = 0; 
    while(index < content.length()) {
      char c = content.charAt(index);
      if(Character.isLetterOrDigit(c)) break;
      index++;
    }
    if(index < content.length())  content = content.substring(index);
    
    index = content.length() - 1; 
    while(index > -1) {
      char c = content.charAt(index);
      if(Character.isLetterOrDigit(c)) break;
      index--;
    }
    
    if(index > 0) content = content.substring(0, index);
    
    index = title.indexOf(content);
    return index > -1 & index < content.length();
  }

  public List<String> getExtractPaths() { return extractPaths; }

  public String getPagePath() { return pagePath; }

  public String getTitlePath() { return titlePath; }

  public String getUserPath() { return userPath; }

  public String getPostPath() { return postPath; }

}
