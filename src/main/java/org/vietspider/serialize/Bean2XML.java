/***************************************************************************
 * Copyright 2003-2006 by VietSpider - All rights reserved.  *
 *    *
 **************************************************************************/
package org.vietspider.serialize;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.vietspider.parser.xml.XMLDocument;
import org.vietspider.parser.xml.XMLNode;
import org.vietspider.token.TypeToken;
import org.vietspider.token.attribute.Attribute;
import org.vietspider.token.attribute.AttributeParser;
import org.vietspider.token.attribute.Attributes;

/**
 *  Author : Nhu Dinh Thuan
 *          Email:nhudinhthuan@yahoo.com
 * Apr 9, 2007
 */
public class Bean2XML implements XMLMapper {
  
  private static Bean2XML MAPPER = new Bean2XML();
  
  public final static Bean2XML getInstance() { return MAPPER; } 
  
  private ReflectUtil reflectUtil;
//  private HashMap<Class<?>, XMLMapper> mappers;
  
  public Bean2XML() {
    reflectUtil = new ReflectUtil();
//    mappers = new HashMap<Class<?>, XMLMapper>();
  }
  
  public XMLDocument toXMLDocument(Object bean) throws Exception {
    NodeMap map = bean.getClass().getAnnotation(NodeMap.class);
    if(map == null) return null;
    XMLNode node = new XMLNode(map.value().toCharArray(), map.value(), TypeToken.TAG);
    toXML(bean, node);
    return new XMLDocument(node);
  }

  public void toXML(Object bean, XMLNode node)  throws Exception  {
    NodeMap map = bean.getClass().getAnnotation(NodeMap.class);
    if (map == null) return;
    toXMLValue(bean.getClass(), map.depth(), bean, node);
  }
  
  private void toXMLValue(Class<?> clazz, int depth, Object bean, XMLNode node)  throws Exception  {
    Field [] fields = clazz.getDeclaredFields();
    for(Field field : fields) {
      NoMap map = field.getAnnotation(NoMap.class);
      if(map != null) continue;
      if(field.getType().isArray()) {
        fromArray(field, bean, node);
      } else if (Collection.class.isAssignableFrom(field.getType())) {
        fromCollection(field, bean, node);
      } else if (Map.class.isAssignableFrom(field.getType())) {
        fromMap(field, bean, node);
      } else {
        toField(field, bean, node);
      }
    }
    depth--;
    if(depth <= 0 || clazz.getSuperclass() == null) return;    
    toXMLValue(clazz.getSuperclass(), depth, bean, node);
  }
  
  protected void toField(Field field, Object bean, XMLNode node) throws Exception {
    if(bean == null) return;
    NodeMap map = field.getAnnotation(NodeMap.class);
    if(map == null) return;
    
    String name = map.value();    
    boolean isAttribute = map.attribute();    
    boolean isCDATA = map.cdata();
    
    Class<?> type = field.getType();
    Object value = null;
    field.setAccessible(true);
  
    if(type.isPrimitive()) {
      try{
        value = getValue(type, field, bean);
      }catch (Exception e) {
      }     
      if(isAttribute){
        setAttribute(node, name, (String)value); 
      } else {
        if(isCDATA) value = "<![CDATA[" + value + "]]>";
        setChild(node, name, (String)value);
      }
      return;
    }
    
    value = getValue(field, bean);
    
    if(type.isEnum()) {
      if(value == null) return ;
      if(isAttribute) {
        setAttribute(node, name, value.toString());
      } else {
        if(isCDATA) value = "<![CDATA[" + value + "]]>";
        setChild(node, name, value.toString());
      }
      return;
    }
    
    if(reflectUtil.isPrimitiveType(type)) {
      if(value == null) value = "";
      if(isAttribute) {
        setAttribute(node, name, value.toString());
      } else {
        if(isCDATA) value = "<![CDATA[" + value + "]]>";
        setChild(node, name, value.toString());
      }
      return;
    }
    
//    XMLMapper mapper = mappers.get(type);
//    if(mapper == null) {    
//      if(value == null) value = "";
//      if(isAttribute) {
//        setAttribute(node, name, value.toString());
//      } else {
//        setChild(node, name, value.toString());
//      }
//      return;
//    }
    
    XMLNode child = new XMLNode(name.toCharArray(), name, TypeToken.TAG);   
    node.getChildren().add(child);
    if(value == null)  throw new NullPointerException(field.getName() + " is null");
    NodeMap valueMap = value.getClass().getAnnotation(NodeMap.class);
    if(valueMap == null) {
      toXMLValue(value.getClass(), 1, value, child);
      return;
    }
    XMLNode valueNode  = new XMLNode(valueMap.value().toCharArray(), valueMap.value(), TypeToken.TAG);
    child.addChild(valueNode);
    toXMLValue(value.getClass(), map.depth(), value, valueNode);
  }
  
  
  private void fromCollection(Field field, Object bean, XMLNode parent) {
    NodesMap map = field.getAnnotation(NodesMap.class);
    if(map == null || bean == null) return;
    
    String name = map.value(); 
    XMLNode node = new XMLNode(name.toCharArray(), name, TypeToken.TAG);
    
    field.setAccessible(true);
    Object values = getValue(field, bean);
    Collection<?> collection = (Collection<?>) values;
    setAttribute(node, "type", values.getClass().getName()); 
    
    Object [] array = new Object[collection.size()];
    collection.toArray(array);
    fromArray(node, array, map);
    if(collection.size() > 0) parent.getChildren().add(node);
  }
  
  private void fromMap(Field field, Object bean, XMLNode parent) throws Exception {
    field.setAccessible(true);
   
    PropertiesMap propertiesMap = field.getAnnotation(PropertiesMap.class);
    Map<?, ?> map = (Map<?, ?>)getValue(field, bean); 
    if(propertiesMap == null || map == null || map.size() < 1) return;
    String itemName = propertiesMap.item();
    boolean isAttribute = propertiesMap.attribute();
    boolean isCDATA = propertiesMap.cdata();
    
    XMLNode node = new XMLNode(propertiesMap.value().toCharArray(), propertiesMap.value(), TypeToken.TAG);
    parent.getChildren().add(node);
    parent = node;
    
    String keyName = "key";
    String keyValue = "value";
    
    
    setAttribute(node, "type", map.getClass().getName()); 
    Iterator<?> iter = map.keySet().iterator();
    
    while(iter.hasNext()) {
      Object key = iter.next();
      Object value = map.get(key);
      if(value == null) value = "";
      node = new XMLNode(itemName.toCharArray(), itemName, TypeToken.TAG);
      parent.getChildren().add(node);

      mapValue2XMLData(key, keyName, isAttribute, isCDATA, node);
      mapValue2XMLData(value, keyValue, isAttribute, isCDATA, node);
    }
  }
  
  private void mapValue2XMLData(Object value, String name,
      boolean isAttribute, boolean isCDATA, XMLNode node) throws Exception {
    Class<?> type = value.getClass();
    if(type.isPrimitive() || reflectUtil.isPrimitiveType(type) || type.isEnum()) {
      if(isAttribute){
        setAttribute(node, name, String.valueOf(value)); 
      } else {
        if(isCDATA) value = "<![CDATA[" + value + "]]>";
        setChild(node, name, String.valueOf(value));
      }
      return ;
    } 
    NodeMap valueMap = type.getAnnotation(NodeMap.class);
    if(valueMap == null) {
      toXMLValue(type, 1, value, node);
      return;
    }
    XMLNode valueNode  = new XMLNode(valueMap.value().toCharArray(), valueMap.value(), TypeToken.TAG);
    node.addChild(valueNode);
    toXMLValue(type, valueMap.depth(), value, valueNode);
  }
  
  private void fromArray(Field field, Object bean, XMLNode parent) {
    NodesMap map = field.getAnnotation(NodesMap.class);
    if(map == null) return;
    Object values = getValue(field, bean);
    if(values == null) return;
    
    field.setAccessible(true);
    String name = map.value(); 
    XMLNode node = new XMLNode(name.toCharArray(), name, TypeToken.TAG);
    
    int length = fromArray(node, values, map);
    if(length > 0) parent.getChildren().add(node);
//    System.out.println(values + " : "+ map.value() +" : " + length);
  }
  
  private int fromArray(XMLNode node, Object values, NodesMap map) {
    if(values == null) return 0;
    int length = Array.getLength(values);
    Class<?> type;
    boolean isCDATA = map.cdata();
    for(int i = 0; i < length; i++) {
      String itemName = map.item();
      Object value = Array.get(values, i);
//      if(itemName.length() < 1) {
//        NodeMap itemMap = value.getClass().getAnnotation(NodeMap.class);
//        if(itemMap != null) itemName = itemMap.value();
//      }
      if(value == null) continue;
      type = value.getClass();
      
      if(isCDATA) value = "<![CDATA[" + value.toString() + "]]>";
      
      if(type.isPrimitive() || reflectUtil.isPrimitiveType(type) || type.isEnum()) {
        if(itemName.trim().length() < 1) itemName = type.getSimpleName();
        XMLNode child = new XMLNode(itemName.toCharArray(), itemName, TypeToken.TAG);
        node.getChildren().add(child);
        child.addChild(new XMLNode(value.toString().toCharArray(), null, TypeToken.CONTENT));
        continue;
      }
//      XMLMapper mapper = mappers.get(value.getClass());
//      if(mapper == null) {
//        XMLNode child = new XMLNode(itemName.toCharArray(), itemName, TypeToken.TAG);
//        node.getChildren().add(child);
//        child.addChild(new XMLNode(value.toString().toCharArray(), null, TypeToken.CONTENT));
//        continue;
//      }
      try {  
        NodeMap valueMap = value.getClass().getAnnotation(NodeMap.class);
        if(valueMap == null) {
          XMLNode child = new XMLNode(itemName.toCharArray(), itemName, TypeToken.TAG);
          node.getChildren().add(child);
          toXML(value, child);
          return length;
        }
        XMLNode child = null;
        XMLNode valueNode  = new XMLNode(valueMap.value().toCharArray(), valueMap.value(), TypeToken.TAG);
        if(itemName.trim().length() < 1)  {
          child = valueNode;
        } else {
          child = new XMLNode(itemName.toCharArray(), itemName, TypeToken.TAG);
          child.addChild(valueNode);             
        }
        toXML(value, valueNode);
        node.getChildren().add(child);
      }catch (Exception e) {
        // e.  printStackTrace();
      }
    }
    return length;
  }

  private Object getValue(Field field, Object bean) {
    Object value = null;
    Method method = null;
    try {
      method = reflectUtil.getGetterMethod(bean.getClass(), field);
      if(method != null) value = method.invoke(bean);
    } catch (Exception e) {
      // e.  printStackTrace();
    }
    if(value != null)  return value;
    try {
      value = field.get(bean);
    }catch (Exception e) {
      value = null;
    }
    return value;
  }
  
  private String getValue(Class<?> type, Field field, Object bean) throws Exception {
    try{
      Method method = reflectUtil.getGetterMethod(bean.getClass(), field);
      if(method != null) return String.valueOf(method.invoke(bean));
    }catch (Exception e) {
      // e.  printStackTrace();
    }
    if(type == boolean.class) return String.valueOf(field.getBoolean(bean));
    if(type == byte.class) return String.valueOf(field.getByte(bean));
    if(type == char.class) return String.valueOf(field.getChar(bean));
    if(type == short.class) return String.valueOf(field.getShort(bean));
    if(type == int.class) return String.valueOf(field.getInt(bean));
    if(type == long.class) return String.valueOf(field.getLong(bean));
    if(type == float.class) return String.valueOf(field.getFloat(bean));
    if(type == double.class) return String.valueOf(field.getDouble(bean));
    return "";
  }
  
  private void setChild(XMLNode parent, String name, String value) {
    List<XMLNode> children = parent.getChildren();
    for(XMLNode ele : children) {
      if(ele.getName().equals(name)){
        ele.addChild(new XMLNode(value.toCharArray(), null, TypeToken.CONTENT));
        return;
      }
    }   
    XMLNode node = new XMLNode(name.toCharArray(), name, TypeToken.TAG);
    node.addChild(new XMLNode(value.toCharArray(), null, TypeToken.CONTENT));
    children.add(node);
  }
  
  private void setAttribute(XMLNode parent, String name, String value) {
    Attributes attrs = AttributeParser.parse(parent);
    for(Attribute ele : attrs) {
      if(ele.getName().equals(name)){
        ele.setValue(value);
        return ;
      }
    }
    Attribute attr = new Attribute(name, value);
    attrs.add(attr);
  }
  
}
