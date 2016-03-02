/**
 * Copyright 2011 By Teradata China Co.Ltd. All rights reserved
 */
package com.teradata.dmet.mobel;

import java.io.Serializable;
import java.util.*;

/**
 * 数据库对象
 */
public class DMObject implements Serializable, Comparable{
  private String name;
  private EnumDMObjectType type;
  private Map<String, Object> attrs;
  private Map<String, DMColumn> cols;

  private void init() {
    attrs = new HashMap<String, Object>();
    cols = new HashMap<String, DMColumn>();
  }

  public DMObject(String name, EnumDMObjectType type) {
    init();
    this.type = type;
    setName(name);
  }

  public void setName(String name) {
    this.name = name;
    this.setAttr("OBJNAME", name);
  }

  public String getName() {
    return name;
  }

  public EnumDMObjectType getType() {
    return type;
  }

  public void setAttr(String attrName, Object value) {
    if (value == null) {
      attrs.remove(attrName);
    } else {
      attrs.put(attrName, value);
    }
  }

  public void addCol(DMColumn column) {
    column.setIndex(cols.size() + 1);// 记录字段顺序
    cols.put(column.getName(), column);
  }

  public void setColumnAttr(String colName, String attrName, Object value) {
    DMColumn column = cols.get(colName);
    if (column != null) {
      column.setAttr(attrName, value);
    }
  }

  public List<DMColumn> getColumns() {
    List<DMColumn> list = new ArrayList<DMColumn>();
    for (DMColumn column : cols.values()) {
      column.setAttr("DBNAME", this.getAttrs().get("DBNAME"));
      column.setAttr("OBJNAME", this.name);
      list.add(column);
    }
    Collections.sort(list);// 排序后返回
    return list;
  }

  public Map<String, Object> getAttrs() {
    return this.attrs;
  }

  public int compareTo(Object o) {
    if (o instanceof DMObject) {
      return this.name.compareTo(((DMObject) o).getName());
    }
    return 0;
  }
}
