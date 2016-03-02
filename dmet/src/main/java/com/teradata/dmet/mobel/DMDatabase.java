/**
 * Copyright 2011 By Teradata China Co.Ltd. All rights reserved
 */
package com.teradata.dmet.mobel;

import java.io.Serializable;
import java.util.*;

/**
 * 数据库对象
 */
public class DMDatabase implements Serializable{
  private String name;// 库名
  private String type;// 库类型
  private Map<String, DMObject> objects;// 库内各种对象

  private void init() {
    objects = new HashMap<String, DMObject>();
  }

  public DMDatabase(String name, String type) {
    init();
    this.name = name;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public void addDMObject(String objName, DMObject object) {
    objects.put(objName, object);
  }

  public void addObjectColumn(String objName, DMColumn column) {
    DMObject object = objects.get(objName);
    if (object != null) {
      object.addCol(column);
    }
  }

  public void setObjectAttr(String objName, String attrName, Object value) {
    DMObject object = objects.get(objName);
    if (object != null) {
      object.setAttr(attrName, value);
    }
  }

  public List<DMObject> loadObjects(EnumDMObjectType type) {
    List<DMObject> list = new ArrayList<DMObject>();
    if (type == null) {
      list.addAll(objects.values());// 没有指定类型返回全部
    } else {
      // 遍历挑出指定类型的对象
      for (DMObject object : objects.values()) {
        if (type.equals(object.getType())) {
          object.setAttr("DBNAME", name);
          list.add(object);
        }
      }
    }
    Collections.sort(list);// 排序后返回
    return list;
  }
}
