/**
 * Copyright 2011 By Teradata China Co.Ltd. All rights reserved
 */
package com.teradata.dmet.mobel;

import java.io.Serializable;
import java.sql.Connection;

/**
 * 内部使用的数据源对象
 */
public class DMDatasource implements Serializable{
  private DMConfig config;
  private Connection connection;

  public DMConfig getConfig() {
    return config;
  }

  public void setConfig(DMConfig config) {
    this.config = config;
  }

  public Connection getConnection() {
    return connection;
  }

  public void setConnection(Connection connection) {
    this.connection = connection;
  }

  public DMDatasource(DMConfig config, Connection connection) {
    this.config = config;
    this.connection = connection;
  }
}
