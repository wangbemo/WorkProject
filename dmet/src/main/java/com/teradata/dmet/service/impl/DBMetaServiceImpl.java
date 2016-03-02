/**
 * Copyright 2011 By Teradata China Co.Ltd. All rights reserved
 */
package com.teradata.dmet.service.impl;

import com.teradata.dmet.mobel.*;
import com.teradata.dmet.service.DBMetaPrivoder;
import com.teradata.dmet.service.DBMetaService;
import com.teradata.dmet.service.ExcelTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

/**
 * 数据库实现类,提供建议的数据源管理功能
 * 数据库连接与数据读取由内部按照类型注册的Privoder提供
 */
public class DBMetaServiceImpl implements DBMetaService{
  private static Logger logger = LoggerFactory.getLogger(DBMetaServiceImpl.class);
  private static final Map<String, DMDatasource> cons = new HashMap<String, DMDatasource>();
  private static final Map<String, DBMetaPrivoder> prvs = new HashMap<String, DBMetaPrivoder>();

  /**
   * 安装多个数据库实现
   *
   * @param privoders 数据库实现
   */
  public void setPrivoders(List<DBMetaPrivoder> privoders) {
    for (DBMetaPrivoder privoder : privoders) {
      install(privoder);
    }
  }

  public void install(DBMetaPrivoder privoder) {
    prvs.put(privoder.getDriver(), privoder);
    logger.debug("install {}", privoder.getDriver());
  }

  private DBMetaPrivoder load(DMConfig config) {
    if (!prvs.containsKey(config.getDriver())) {
      throw new RuntimeException("系统不支持!");
    }
    return prvs.get(config.getDriver());
  }

  private DMDatasource recive(String dbkey) {
    if (!cons.containsKey(dbkey)) {
      throw new RuntimeException("未知数据源!");
    }
    return cons.get(dbkey);
  }

  public Collection<DBMetaPrivoder> getSupport() {
    return prvs.values();
  }

  public String register(DMConfig config) {
    Connection connection;
    DBMetaPrivoder privoder = load(config);
    try {
      Class.forName(config.getDriver());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("驱动程序缺失!", e);
    }
    try {
      logger.info("尝试连接{}数据库", privoder.getName());
      connection = DriverManager.getConnection(privoder.createUrl(config), config.getUn(), config.getPw());
    } catch (SQLException e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException("连接参数不正确!", e);
    }
    String key = new StringBuilder(privoder.getName()).append(System.currentTimeMillis()).toString();
    cons.put(key, new DMDatasource(config, connection));
    logger.info("成功连接数据库:{}", key);
    return key;
  }

  public DMDatabase loadDatabase(String dbkey ,DMTypeConfig dmTypeConfig) {
    DMDatasource datasource = recive(dbkey);
    DBMetaPrivoder privoder = load(datasource.getConfig());
    logger.info("开始解析{}数据库", privoder.getName());
    return privoder.loadDatabase(datasource , dmTypeConfig);
  }

  @SuppressWarnings("unchecked")
  public void export(DMExportConfig config, String path, List<DMDatabase> databases) {
    FileOutputStream out = null;
    ExcelTranslator translator;
    try {
      translator = new ExcelTranslator(config.getTemplateFile());
      logger.info("使用模板{}导出至{}", config.getTemplateFile(), config.getWriteFile());
      logger.info("开始清理工作目录");
      removeAll(new File(path));
      Map<String, Object> info = new HashMap<String, Object>();
      info.put("SYS", config.getSys());
      info.put("VER", config.getVer());
      translator.export(info, 0);
      
      // 以下数字含义参见企业级元数据导出模板Sheet的序号与配置行号
      logger.info("开始导出库信息");
      exportDatabase(databases, translator, 2, 4);
      logger.info("开始导出表与字段信息");
      exportObjAndCol(databases, EnumDMObjectType.TABLE, path, translator, 3, 5, 4, 5);
      logger.info("开始导出视图与字段信息");
      exportObjAndCol(databases, EnumDMObjectType.VIEW, path, translator, 7, 4, 8, 4);
      //logger.info("开始导出存储与参数信息");
      //exportObjAndCol(databases, EnumDMObjectType.PROCEDURE, path, translator, 9, 4, 10, 4);
      //logger.info("开始导出序列与参数信息");
      //exportSeq(databases, translator, 11, 5);
      logger.info("开始导出其它对象信息");
      exportOther(databases, translator, path);// 包括表的主键及索引和表外键，其他类型对象信息 sheet5,sheet6,sheet12 
      translator.export(Collections.EMPTY_LIST, 13, 4);// 暂时没有其它补充属性需要导出 sheet13
      
      out = new FileOutputStream(new File(config.getWriteFile()));
      translator.write(out);
      logger.info("导出{}完成", config.getWriteFile());
    } catch (IOException e) {
      System.out.println(e.getMessage());
      logger.error("模板或导出文件配置错误", e.getMessage());
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    } finally {
      releaseOut(out);
    }
  }

  private void exportDatabase(List<DMDatabase> databases, ExcelTranslator translator, int sheetIndex, int dataRow) {
    List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
    Map<String, Object> db;
    for (DMDatabase database : databases) {
      db = new HashMap<String, Object>();
      db.put("DBNAME", database.getName());
      db.put("SCHEMA", database.getName());
      db.put("TYPE", database.getType());
      data.add(db);
    }
    translator.export(data, sheetIndex, dataRow);
  }

  private void exportObjAndCol(List<DMDatabase> databases, EnumDMObjectType type, String path, ExcelTranslator translator, int objSheet, int objRow, int colSheet, int colRow) {
    List<Map<String, Object>> objs = new ArrayList<Map<String, Object>>();
    List<Map<String, Object>> cols = new ArrayList<Map<String, Object>>();
    Map<String, Object> obj;
    for (DMDatabase database : databases) {
      for (DMObject object : database.loadObjects(type)) {
        obj = object.getAttrs();
        // 对象需要处理DDL与分区信息
        obj.put("DDL", parseDDL((String) obj.get("DDL"), object.getName(), path));
        obj.put("PARTITION", parseDDL((String) obj.get("PARTITION"), object.getName(), path));
        objs.add(object.getAttrs());
        for (DMColumn column : object.getColumns()) {
          cols.add(column.getAttrs());
        }
      }
    }
    translator.export(objs, objSheet, objRow);
    translator.export(cols, colSheet, colRow);
  }

  private void exportSeq(List<DMDatabase> databases, ExcelTranslator translator, int sheetIndex, int dataRow) {
    List<Map<String, Object>> data;
    data = new ArrayList<Map<String, Object>>();
    for (DMDatabase database : databases) {
      for (DMObject object : database.loadObjects(EnumDMObjectType.SEQUENCE)) {
        data.add(object.getAttrs());
      }
    }
    translator.export(data, sheetIndex, dataRow);
  }

  private void exportOther(List<DMDatabase> databases, ExcelTranslator translator, String path) {
    List<Map<String, Object>> data;
    List<Map<String, Object>> indcols = new ArrayList<Map<String, Object>>();// 索引字段
    List<Map<String, Object>> fks = new ArrayList<Map<String, Object>>();// 约束字段
    data = new ArrayList<Map<String, Object>>();
    String subtype, thtype;// 二级与三级分类
    Map<String, Object> obj, col;
    for (DMDatabase database : databases) {
      for (DMObject object : database.loadObjects(EnumDMObjectType.ORTHER)) {
        obj = object.getAttrs();
        subtype = String.valueOf(obj.get("SUBTYPE"));
        // 仅处理带有DDL的对象
        if (obj.containsKey("DDL")) {
          obj.put("DDL", parseDDL((String) obj.get("DDL"), object.getName(), path));
          data.add(obj);
          if ("CONSTRAINT".equals(subtype)) {
            thtype = (String) object.getAttrs().get("CONTYPE");
            if ("R".equals(thtype)) {
              for (DMColumn column : object.getColumns()) {
                col = column.getAttrs();
                col.put("OBJNAME", object.getAttrs().get("OBJNAME"));
                col.put("PNAME", object.getAttrs().get("PNAME"));
                col.put("RNAME", object.getAttrs().get("RNAME"));
                fks.add(col);
              }
            } else if (!"C".equals(thtype)) {
              for (DMColumn column : object.getColumns()) {
                col = column.getAttrs();
                col.put("SUBTYPE", "U".equals(thtype) ? "UNIQUE":"PK");
                col.put("OBJNAME", object.getAttrs().get("OBJNAME"));
                col.put("PNAME", object.getAttrs().get("PNAME"));
                col.put("RNAME", object.getAttrs().get("RNAME"));
                indcols.add(col);
              }
            }
          } else 
        	  if ("INDEX".equals(subtype)) {
            for (DMColumn column : object.getColumns()) {
              col = column.getAttrs();
              col.put("SUBTYPE", "INDEX");
              col.put("OBJNAME", object.getAttrs().get("OBJNAME"));
              col.put("PNAME", object.getAttrs().get("PNAME"));
              indcols.add(col);
            }
          }
        } else {
          logger.warn("忽略{}对象{}", subtype, object.getName());
        }
      }
    }
    //translator.export(data, 12, 4);// 其它对象
    translator.export(indcols, 5, 4);// 主键与索引字段
    translator.export(fks, 6, 4);// 外键
  }

  public void release(String dbkey) {
    DMDatasource datasource = recive(dbkey);
    logger.info("释放数据库{}连接", dbkey);
    releaseConnection(datasource.getConnection());
  }

  private void releaseConnection(Connection connection) {
    if (connection != null) {
      try {
        connection.close();
      } catch (SQLException e) {
        logger.error(e.getMessage(), e);
      }
    }
  }

  /**
   * 清理工作
   */
  public void destroy() {
    for (DMDatasource datasource : cons.values()) {
      releaseConnection(datasource.getConnection());
    }
  }

  String parseDDL(String ddl, String objName, String path) {
    String ret = ddl;
    // excel 2003版单cell 容量32k字符,这里设置30000是出于稳妥
    if (ddl != null && ddl.length() > 30000) {
      ret = new StringBuilder(objName).append(".ddl").toString();
      OutputStream out = null;
      try {
        out = new FileOutputStream(new StringBuilder(path).append("/ddl/").append(ret).toString());
        byte[] bytes = ddl.getBytes();
        for (byte aByte : bytes) {
          out.write(aByte);
        }
      } catch (Exception e) {
        logger.error("{}属性存储失败!", e);
      } finally {
        releaseOut(out);
      }
    }
    return ret;
  }

  private void releaseOut(OutputStream out) {
    if (out != null) {
      try {
        out.flush();
      } catch (IOException e) {
        logger.error(e.getMessage());
      }
      try {
        out.close();
      } catch (IOException e) {
        logger.error(e.getMessage());
      }
    }
  }

  void removeAll(File dir) {
    if (dir.isDirectory()) {
      for (File file : dir.listFiles()) {
        removeAll(file);
      }
    } else {
      dir.delete();
    }
  }
}
