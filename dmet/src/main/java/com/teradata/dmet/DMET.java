/**
 * Copyright 2012 By Teradata China Co.Ltd. All rights reserved
 */
package com.teradata.dmet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.teradata.dmet.mobel.DMConfig;
import com.teradata.dmet.mobel.DMDatabase;
import com.teradata.dmet.mobel.DMExportConfig;
import com.teradata.dmet.mobel.DMParam;
import com.teradata.dmet.mobel.DMTypeConfig;
import com.teradata.dmet.service.DBMetaPrivoder;
import com.teradata.dmet.service.impl.DBMetaOraclePrivoder;
import com.teradata.dmet.service.impl.DBMetaServiceImpl;

/**
 * 库导出主体程序
 */
public class DMET{
  private static Logger logger = LoggerFactory.getLogger(DMET.class);

  public static void main(String[] args) {
    String path = null;
    if (args != null && args.length > 0) {
      path = args[0];
    }
    if (path == null) {
      logger.error("没有指定工作路径");
      throw new RuntimeException();
    }
    logger.info("使用路径{}", path);
    Properties properties = new Properties();
    try {
      properties.load(new FileInputStream(new File(fmtPath(path, "", "dmet.properties"))));
    } catch (IOException e) {
      logger.error("指定工作路径下找不到配置文件:dmet.properties", e);
      throw new RuntimeException();
    }
    int num;
    try {
      num = Integer.parseInt(properties.getProperty("dmet.db.num", "1"));
    } catch (NumberFormatException e) {
      logger.error("读取数字edmt.db.num遇到异常");
      throw new RuntimeException();
    }
    List<DMConfig> configs = new ArrayList<DMConfig>(num);
    List<DMTypeConfig> tcs = new ArrayList<DMTypeConfig>(num);
    List<DMParam> addition;
    for (int i = 1; i <= num; i++) {
      DMConfig config = new DMConfig();
      config.setDriver(properties.getProperty(new StringBuilder("dmet.db.driver.").append(i).toString()));
      config.setUn(properties.getProperty(new StringBuilder("dmet.db.un.").append(i).toString()));
      config.setPw(properties.getProperty(new StringBuilder("dmet.db.pw.").append(i).toString()));
      addition = new ArrayList<DMParam>();
      addition.add(new DMParam("IP", properties.getProperty(new StringBuilder("dmet.db.ip.").append(i).toString())));
      addition.add(new DMParam("PORT", properties.getProperty(new StringBuilder("dmet.db.port.").append(i).toString())));
      addition.add(new DMParam("SID", properties.getProperty(new StringBuilder("dmet.db.sid.").append(i).toString())));
      config.setAddition(addition);
      configs.add(config);
      
    //test
      DMTypeConfig tc = new DMTypeConfig();
      tc.setTable(properties.getProperty(new StringBuilder("dmet.export.table.").append(i).toString()));
      tc.setTableInclude(properties.getProperty(new StringBuilder("dmet.export.tableInclude.").append(i).toString()));
      tc.setView(properties.getProperty(new StringBuilder("dmet.export.view.").append(i).toString()));
      tc.setViewInclude(properties.getProperty(new StringBuilder("dmet.export.viewInclude.").append(i).toString()));
      tc.setIndex(properties.getProperty(new StringBuilder("dmet.export.index.").append(i).toString()));
      tc.setIndexInclude(properties.getProperty(new StringBuilder("dmet.export.indexInclude.").append(i).toString()));
      tcs.add(tc);
    }
    DMExportConfig exportConfig = new DMExportConfig();
    exportConfig.setTemplateFile(fmtPath(path, "main", getProp(properties, "dmet.export.template", "template.xlsx")));
    exportConfig.setWriteFile(fmtPath(path, "out", getProp(properties, "dmet.export.out", "out.xlsx")));
    exportConfig.setSys(getProp(properties, "dmet.sys.name", "企业级数据字典管理系统"));
    exportConfig.setVer(properties.getProperty("dmet.sys.ver", ""));
    logger.debug("装载导出工具");
    
    DBMetaServiceImpl service = new DBMetaServiceImpl();
    List<DBMetaPrivoder> privoders = new ArrayList<DBMetaPrivoder>();
    privoders.add(new DBMetaOraclePrivoder());
    service.setPrivoders(privoders);
    logger.debug("开始连接数据库");
    List<String> keys = new ArrayList<String>(num);
    for (DMConfig config : configs) {
      keys.add(service.register(config));
    }
    
    logger.debug("开始解析数据库");
    List<DMDatabase> databases = new ArrayList<DMDatabase>();
    
	for(int j=0;j<num;j++){
		databases.add(service.loadDatabase(keys.get(j),tcs.get(j)));
        service.release(keys.get(j));// 抓取后释放连接
    }
	
        
    logger.debug("开始导出数据库");
    service.export(exportConfig, new StringBuilder(path).append("/out/").toString(), databases);
  }

  private static String fmtPath(String path, String dir, String file) {
    String ret = new StringBuilder(path).append("/").append(dir).append("/").append(file).toString();
    ret = ret.replaceAll("\\\\", "/");
    ret = ret.replaceAll("/+", "/");
    return ret;
  }

  private static String getProp(Properties properties, String attr, String defValue) {
    String value = properties.getProperty(attr);
    if (value != null) {
      try {
        value = new String(value.getBytes("ISO8859-1"), "GBK");
      } catch (UnsupportedEncodingException e) {
        logger.error(e.getMessage());
      }
    } else {
      value = defValue;
    }
    return value;
  }
}