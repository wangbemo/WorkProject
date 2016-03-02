/**
 * Copyright 2011 By Teradata China Co.Ltd. All rights reserved
 */
package com.teradata.dmet.mobel;

import java.io.Serializable;
import java.util.List;

/**
 * 数据库连接配置对象
 */
public class DMConfig implements Serializable{
  private String driver;
  private String un;
  private String pw;
  private List<DMParam> addition;

  public String getDriver() {
    return driver;
  }

  public void setDriver(String driver) {
    this.driver = driver;
  }

  public String getUn() {
    return un;
  }

  public void setUn(String un) {
    this.un = un;
  }

  public String getPw() {
    return pw;
  }

  public void setPw(String pw) {
    this.pw = pw;
  }

  public List<DMParam> getAddition() {
    return addition;
  }

  public void setAddition(List<DMParam> addition) {
    this.addition = addition;
  }
}
