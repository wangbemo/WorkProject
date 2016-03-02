/**
 * Copyright 2011 By Teradata China Co.Ltd. All rights reserved
 */
package com.teradata.dmet.service.impl;

import com.teradata.dmet.mobel.DMDatabase;
import com.teradata.dmet.mobel.DMDatasource;
import com.teradata.dmet.mobel.DMParam;
import com.teradata.dmet.mobel.DMTypeConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Teradata数据库的实现
 */
public class DBMetaTeradataPrivoder extends AbstractDBMetaJDBCPrivoder{
  public String getName() {
    return "Teradata";
  }

  public String getDriver() {
    return "com.teradata.jdbc.TeraDriver";
  }

  public String getURLTemplate() {
    return "jdbc:teradata://<IP>/client_charset=<CLIENT_CHARSET>,tmode=tera,charset=<CHARSET>,database=<DATABASE>,lob_support=off";
  }

  public Collection<DMParam> getAddition() {
    List<DMParam> addition = new ArrayList<DMParam>();
    addition.add(new DMParam("IP", "127.0.0.1"));
    addition.add(new DMParam("CHARSET", "ANSCII"));
    addition.add(new DMParam("CLIENT_CHARSET", "CP936"));
    addition.add(new DMParam("DATABASE", "DBC"));
    return addition;
  }

  public DMDatabase loadDatabase(DMDatasource datasoure,DMTypeConfig dmTypeConfig) {
    return null;
  }

}
