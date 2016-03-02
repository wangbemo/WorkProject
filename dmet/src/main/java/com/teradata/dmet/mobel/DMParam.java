/**
 * Copyright 2011 By Teradata China Co.Ltd. All rights reserved
 */
package com.teradata.dmet.mobel;

import java.io.Serializable;

/**
 * 数据源连接配置
 */
public class DMParam implements Serializable{
  private String name;
  private String value;

  public DMParam() {
  }

  public DMParam(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
