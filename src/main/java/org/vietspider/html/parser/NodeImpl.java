/***************************************************************************
 * Copyright 2001-2006 VietSpider         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.vietspider.html.parser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.vietspider.chars.CharsDecoder;
import org.vietspider.chars.CharsEncoder;
import org.vietspider.chars.SpecChar;
import org.vietspider.common.Application;
import org.vietspider.html.HTMLNode;
import org.vietspider.html.Name;
import org.vietspider.html.NodeChildHandler;
import org.vietspider.html.NodeConfig;
import org.vietspider.html.NodeIterator;
import org.vietspider.html.Tag;
import org.vietspider.token.TypeToken;
import org.vietspider.token.attribute.AttributeParser;

/**
 * Created by VietSpider
 * Author : Nhu Dinh Thuan
 *          thuan.nhu@exoplatform.com
 * Sep 14, 2006  
 */

public class NodeImpl extends HTMLNode {

  private static final long serialVersionUID = 1L;
  
  private transient boolean isOpen = false;
  private transient int type = TypeToken.CONTENT ;
  
  protected char [] value ;
  protected byte [] byteValue ;
  
  protected volatile List<HTMLNode> cachedTokens;
 
  
  public NodeImpl(char[] value, Name name){ 
    super(name);
    setValue(value);
  }

  public NodeImpl(char[] value, Name name, int type){
    super(name);
    this.type = type;
    children = new NodeList();
    NodeConfig config = HTML.getConfig(name);
    if(config.end() != Tag.FORBIDDEN && type == TypeToken.TAG) isOpen = true;
    setValue(value);
  }

  public boolean isOpen() { return isOpen; }
  public void setIsOpen(boolean open) { isOpen = open; }

  public int getType() { return type; }
  public void setType(int type) { 
	  this.type = type; 
  }
  
  public void addChild(HTMLNode node)  {
    if(addInternalChild(node)) clearToken();
  }
  
  boolean addInternalChild(HTMLNode node){
    if(children == null 
        || getConfig().end() == Tag.FORBIDDEN) return false;
    NodeImpl impl = (NodeImpl) node;
    impl.setParentNode(this);
    children.addElement(impl);
    return true;
  }

  public void addChild(int i, HTMLNode node) {
    if(addInternalChild(i, node)) clearToken();
  }
  
  boolean addInternalChild(int i, HTMLNode node) {
    if(children == null || i < 0) return false;
    if(i > children.size()) {
      addInternalChild(node);
      return true;
    }
    NodeImpl impl = (NodeImpl)node;
    clearToken();
    impl.setParentNode(this);
    children.addElement(i, impl);
    return true;
  }
  
  public void setChild(int i, HTMLNode node) {
    if(setInternalChild(i, node)) clearToken();
  }

  boolean setInternalChild(int i, HTMLNode node) {
    if(children == null 
        || i < 0 || i >= children.size()) return false;
    NodeImpl impl = (NodeImpl)node;
    impl.setParentNode(this);
    children.setElement(i, impl);
    return true;
  }
  
  public void removeChild(HTMLNode node) {
    if(removeInternalChild(node)) clearToken();
  }

  boolean removeInternalChild(HTMLNode node) {
    if(children == null) return false;
    int i = children.indexOf(node);
    if(i < 0) return false;
    NodeImpl impl = (NodeImpl)node;
    impl.setParentNode(null);
    children.removeElement(impl);
    return true;
  }

  public void clearChildren() {
    if(children == null || children.size() < 1) return;
    clearToken();
    children.clearElements();
  }

  private void setParentNode(HTMLNode parent) { 
    this.parent = parent;
  }

  public StringBuilder buildValue(StringBuilder builder) {
		// if(value.length < 1) return builder;
		if (isBeautify() && builder.length() > 0) { 
			builder.append(SpecChar.n);
		}
		boolean isTag = name != Name.CONTENT && name != Name.COMMENT
				&& name != Name.CODE_CONTENT;
		if (isTag && Name.DOCTYPE != this.name) {
			builder.append('<');
		}
		if (type == TypeToken.CLOSE) {
			builder.append('/');
		}
		char[] startTmp = getValue();
		builder.append(startTmp);
		if (isTag && Name.DOCTYPE != this.name ) {
			builder.append('>');
		}
		if (type == TypeToken.CLOSE || getConfig().hidden()) {
			return builder;
		}

		if (children == null) {
			return builder;
		}
		for (HTMLNode ele : children) {
			
			if (isBeautify()) {
				StringBuilder tmpBuilder = new StringBuilder();  
				ele.buildValue(tmpBuilder);
				for (String nextLine:tmpBuilder.toString().split("\n")){
					builder.append("\n   "+nextLine+"");
				}
			}else{
				ele.buildValue(builder);
			}
			
		}
		if (getConfig().end() != Tag.FORBIDDEN) {
			Name endTmp = getName();
			String strEndTmp = endTmp.toString();
			// >8-0
			String sEndTmp = new String(startTmp);
			if (sEndTmp.startsWith(strEndTmp.toLowerCase())) {
				strEndTmp = strEndTmp.toLowerCase();
			} else if (sEndTmp.startsWith(strEndTmp.toUpperCase())) {
				strEndTmp = strEndTmp.toUpperCase();
			}
			if (isBeautify()) {
				builder.append(SpecChar.n);
			}

			builder.append('<').append('/').append(strEndTmp).append('>');
		}
		return builder;
	}

  public void setValue(char [] chars) {
    if(chars.length < 5555) {
      this.value = chars;
      return;
    }

    if(name == Name.CONTENT ||
        name == Name.CODE_CONTENT || name == Name.COMMENT ) { 
      try {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        GZIPOutputStream zip = new GZIPOutputStream(outputStream);
        
//        System.out.println(new String(chars));
        Charset charset = Charset.forName(Application.CHARSET);
        CharsEncoder encoder = new CharsEncoder(charset, charset.name());
        
//        zip.write(new String(chars).getBytes(Application.CHARSET));
        zip.write(encoder.encode(chars, 0, chars.length));
        zip.close();
        

        value = null;
        byteValue = outputStream.toByteArray();

//        System.out.println("=== >" + chars.length + " : " + byteValue.length);
//        System.out.println(new String(getValue()));
        return;
      } catch (Exception e) {
//        // e.  printStackTrace();
      }
    }
    this.value = chars;
  }

  public char[] getValue() { 
    if(value != null) return value;
    try {
      ByteArrayInputStream byteInput = new ByteArrayInputStream(byteValue);
      GZIPInputStream gzip = new GZIPInputStream(byteInput);

      ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
      int read = -1;
      byte [] bytes = new byte[16*1024];
      while((read = gzip.read(bytes)) != -1) {
        byteOutput.write(bytes, 0, read);
      }

      bytes = byteOutput.toByteArray();
      return CharsDecoder.decode(Application.CHARSET, bytes, 0, bytes.length);
//      ByteBuffer byteBuffer = ByteBuffer.wrap(byteOutput.toByteArray());
//      Charset charset = Charset.forName(Application.CHARSET);
//      CharBuffer charBuffer = charset.decode(byteBuffer);
//      return charBuffer.array();
    } catch (Exception e) {
      return value;
    }
  }

  public boolean isTag() {
    return type != TypeToken.CONTENT 
            && type != TypeToken.COMMENT  
            && type != TypeToken.CODE_CONTENT; 
  }

  public void clone(HTMLNode nodeParent) {
    if(type == TypeToken.CONTENT
        || type == TypeToken.COMMENT 
        || type == TypeToken.CODE_CONTENT) {
      ((NodeImpl)nodeParent).addChild(new NodeImpl(getValue(), name));
      return;
    }

    NodeImpl newImpl = new NodeImpl(getValue(), name, type);
    nodeParent.addChild(newImpl);
    if(children == null) return;
    for(int i = 0; i < children.size(); i++) {
      children.get(i).clone(newImpl);
    }
  }

  Iterator<HTMLNode> childIterator() { 
    return children.iteratorElement(); 
  }
  
  public NodeIterator iterator() { 
    return new NodeIteratorImpl(tokens()); 
  }
  
  public NodeIterator iterator(List<HTMLNode> ignores) {
    return new NodeIteratorImpl(tokens(ignores));
  }
  
  protected List<HTMLNode> tokens() {
    if(cachedTokens != null) return cachedTokens;
    cachedTokens = new ArrayList<HTMLNode>();
    cachedTokens.add(this);
    if(children == null) return cachedTokens;
    
    for(int i = 0; i < children.size(); i++) {
      NodeImpl node = (NodeImpl)children.get(i);
      if(node == null || node.tokens() == null) continue;
      if(cachedTokens == null) break;
      cachedTokens.addAll(node.tokens());
    }
    return cachedTokens;
  }
  
  protected List<HTMLNode> tokens(List<HTMLNode> ignores) {
    ArrayList<HTMLNode> newTokens = new ArrayList<HTMLNode>();
    if(ignores.contains(this)) return newTokens; 
    newTokens.add(this);
    if(children == null) return newTokens;
    for(int i = 0; i < children.size(); i++) {
      NodeImpl node = (NodeImpl)children.get(i); 
      newTokens.addAll(node.tokens(ignores));
    }
    return newTokens;
  }
  
  public void traverse(NodeChildHandler handler) {
    for(int i = 0; i < children.size(); i++) {
      handler.handle(i, children.get(i));
    }
  }
  
  private void clearToken() {
    cachedTokens = null;
    if(parent == null) return;
    ((NodeImpl)parent).clearToken();
  }
  
//  private void removeToken(HTMLNode node) {
//    if(cachedTokens != null) {
//      cachedTokens.remove(node);
//    }
//    if(parent == null) return;
//    ((NodeImpl)parent).removeToken(node);
//  }
  
  public static class NodeIteratorImpl implements NodeIterator {

    protected Iterator<HTMLNode> iterator;

    public NodeIteratorImpl(List<HTMLNode> tokens) {
      this.iterator = tokens.iterator();
    }

    public boolean hasNext() {
      return iterator.hasNext();
    }

    public HTMLNode next() {
      return iterator.next();
    }
  }
  
  public String name() { return name.name(); }

@Override
public StringBuilder builXHTML(StringBuilder builder) {
	return  builXHTML( builder, 0);
}
String TABBEDCHARS = "  ";
public StringBuilder builXHTML(StringBuilder builder, int LEVEL) {
	
	// if(value.length < 1) return builder;
	if (isBeautify() && builder.length() > 0) { 
		builder.append(SpecChar.n);
	}
	String nameTmp = getName().toString();
	boolean isTag = name != Name.CONTENT && name != Name.COMMENT && name != Name.CODE_CONTENT;
	
	// <.... ??
	if (isTag && Name.DOCTYPE != this.name)  
	{
		builder.append('<');
		char[] startTmp = getValue(); 
		
		String startUp = new String(startTmp);
		
		int cutStart = startUp.toUpperCase().indexOf(nameTmp.toUpperCase());
		int cutEnd = cutStart + nameTmp.length();
		try{
			String prefixTmp = startUp .substring(0, cutStart);
			String suffixTmp = startUp .substring( cutEnd );
			// replace by formatted attts
			suffixTmp  = " "+AttributeParser.parse(this); 
			startUp  = prefixTmp+
					   nameTmp +
					   suffixTmp;
		}catch(java.lang.StringIndexOutOfBoundsException e){
			System.out.println("!!!!!!!!!"+cutStart+">>>"+cutEnd+">>>"+startUp);
			// e.  printStackTrace();
		}
		builder.append(startUp);
		builder.append('>');
		//http://xhtml.com/en/xhtml/reference/script/
		if ("SCRIPT".equals( nameTmp.toUpperCase()) &&  this.getTextValue().indexOf("<![CDATA[")==-1  ){
			builder.append("\n//<![CDATA[\n");
		}
	}
	if (children == null) {
		if (!isTag  )  {
			char[] startTmp = getValue();
			String startUp = new String(startTmp);
			builder.append(startUp);
		}
		//return builder;
	}else{
		for (HTMLNode ele : children) {
			if (isBeautify()) {
				StringBuilder tmpBuilder = new StringBuilder();  
				ele.builXHTML(tmpBuilder,LEVEL+1);
				for (String nextLine:tmpBuilder.toString().split("\n")){
					builder.append("\n"+TABBEDCHARS+nextLine+"");
				}
			}else{
				ele.builXHTML(builder,LEVEL+1);
			}
		}
	}
	
	// </... ???
	if (isTag && Name.DOCTYPE != this.name)  
	{

		if (isBeautify()) {
			builder.append("\n");
//			for (int i=0;i<LEVEL-1;i++){
//				builder.append(TABBEDCHARS);
//			}
		}
		//http://xhtml.com/en/xhtml/reference/script/
		if ("SCRIPT".equals( nameTmp.toUpperCase() ) &&  this.getTextValue().indexOf("<![CDATA[")==-1  ){
			builder.append("\n//]]>");
		}
		
		builder.append("</");
		builder.append(nameTmp);
		builder.append('>');
	}
	return builder;
}

  /*public static void next(NodeImpl node) {
//  System.out.println(node.getName() + "  : " + node.hashCode() 
//  + " | " + node.nextNode.getName() + " : " + node.nextNode.hashCode());
    if(node.getChildren() == null)  return;
    for(int i = 0; i < node.getChildren().size(); i++) {
      next((NodeImpl)node.getChildren().get(i));
    }
//  System.out.println("==========================================");
  }*/

}
