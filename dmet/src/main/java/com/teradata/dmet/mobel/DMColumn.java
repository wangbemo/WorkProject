/**
 * Copyright 2011 By Teradata China Co.Ltd. All rights reserved
 */
package com.teradata.dmet.mobel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据库字段
 */
public class DMColumn implements Serializable, Comparable{
  private String name;
  private Integer index = 0;
  private Map<String, Object> attrs = new HashMap<String, Object>();

  public DMColumn(String name) {
    this.name = name;
    setAttr("COLNAME", name);
  }

  public String getName() {
    return name;
  }

  public void setIndex(Integer index) {
    this.index = index;
  }

  public Integer getIndex() {
    return index;
  }

  public void setAttr(String attrName, Object value) {
    if (value == null) {
      attrs.remove(attrName);
    } else {
      attrs.put(attrName, value);
    }
  }

  public Map<String, Object> getAttrs() {
    return this.attrs;
  }

  public int compareTo(Object o) {
    if (o instanceof DMColumn) {
      return this.index.compareTo(((DMColumn) o).getIndex());
    }
    return 0;
  }
}
