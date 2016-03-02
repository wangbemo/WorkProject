/**
 * Copyright 2011 By Teradata China Co.Ltd. All rights reserved
 */
package com.teradata.dmet.service.impl;

import com.teradata.dmet.mobel.DMConfig;
import com.teradata.dmet.mobel.DMParam;
import com.teradata.dmet.service.DBMetaPrivoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 基于jdbc的抽象实现
 */
public abstract class AbstractDBMetaJDBCPrivoder implements DBMetaPrivoder{
  protected Logger logger = LoggerFactory.getLogger(this.getClass());

  protected void close(ResultSet resultSet) {
    if (resultSet != null) {
      try {
        resultSet.close();
      } catch (SQLException e) {
        logger.error(e.getMessage(), e);
      }
    }
  }

  protected void close(Statement statement) {
    if (statement != null) {
      try {
        statement.close();
      } catch (SQLException e) {
        logger.error(e.getMessage(), e);
      }
    }
  }

  public String createUrl(DMConfig config) {
    String ret = getURLTemplate();
    for (DMParam param : config.getAddition()) {
      ret = ret.replace("<" + param.getName() + ">", param.getValue());
    }
    return ret;
  }
}
