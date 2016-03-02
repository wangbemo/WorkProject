/**
 * Copyright 2011 By Teradata China Co.Ltd. All rights reserved
 */
package com.teradata.dmet.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.management.ManagementFactory;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.teradata.dmet.mobel.DMColumn;
import com.teradata.dmet.mobel.DMDatabase;
import com.teradata.dmet.mobel.DMDatasource;
import com.teradata.dmet.mobel.DMObject;
import com.teradata.dmet.mobel.DMParam;
import com.teradata.dmet.mobel.DMTypeConfig;
import com.teradata.dmet.mobel.EnumDMObjectType;

/**
 * Oracle meta信息提供实现类
 */
public class DBMetaOraclePrivoder extends AbstractDBMetaJDBCPrivoder{
  public static final String DB_TYPE = "Oracle";
  public static final String DRIVER_NAME = "oracle.jdbc.driver.OracleDriver";

  public String getName() {
    return DB_TYPE;
  }

  public String getDriver() {
    return DRIVER_NAME;
  }

  public String getURLTemplate() {
    return "jdbc:oracle:thin:@//<IP>:<PORT>/<SID>";
  }

  public Collection<DMParam> getAddition() {
    List<DMParam> addition = new ArrayList<DMParam>();
    addition.add(new DMParam("IP", "127.0.0.1"));
    addition.add(new DMParam("PORT", "1521"));
    addition.add(new DMParam("SID", "ORCL"));
    return addition;
  }
  
  /**
   * 
   * 写个方法 传入对象类型，对象类型参数
   * param1 = T_BIZ_VIEW,T_ALTERATION_INFO
   * param2 = Y
   * 
   */
  private void analyConfig(Statement st,DMDatabase database,EnumDMObjectType type , String param1, String param2){
	  StringBuffer limit = new StringBuffer();
	  String parwh = null ;
	  if("".equals(param1)){
		  //不导出任何数据
		  logger.debug(type+"对象类型设置为空，不导出任何数据");
	  }else if("*".equals(param1)){
		  if("Y".equals(param2)){
			  //导出所有的数据 默认
			  if(EnumDMObjectType.TABLE.equals(type) || EnumDMObjectType.VIEW.equals(type)){
				  logger.info("开始读取库内对象");
				  loadObject(st, database,parwh,type);// 读取库内所有对象
				  logger.info("开始读取库内表与视图的字段信息");
				  loadTableViewColumn(st, database,parwh,type);// 读取表和视图的字段
			  }else if (EnumDMObjectType.INDEX.equals(type)){
				  logger.info("开始读取库内索引");
				  loadIndex(st, database,parwh,type);// 读取索引
				  logger.info("开始读取库内索引字段信息");
				  loadIndexColumn(st, database,parwh,type);// 读取索引字段
			  }
		  }else if("N".equals(param2)){
			  //不导出任何数据
			  logger.info(type+"对象类型设置为空，不导出任何数据");
		  }
	  }else if(param1.length()>0 && !"*".equals(param1)){ 
		  String[] str = param1.split(",");
		  for(int i=0;i<str.length;i++){
			 limit.append(" '").append(str[i]).append("' ").append(",");
		  }
		  limit.deleteCharAt(limit.lastIndexOf(","));
		  if("Y".equals(param2)){
			  //配置where条件
			  parwh = " IN (" + limit.toString() + ") ";
			  if(EnumDMObjectType.TABLE.equals(type) || EnumDMObjectType.VIEW.equals(type)){
				  logger.info("开始读取库内对象");
				  loadObject(st, database,parwh,type);// 读取库内所有对象
				  logger.info("开始读取库内表与视图的字段信息");
				  loadTableViewColumn(st, database,parwh,type);// 读取表和视图的字段
			  }else if (EnumDMObjectType.INDEX.equals(type)){
				  logger.info("开始读取库内索引");
				  loadIndex(st, database,parwh,type);// 读取索引
				  logger.info("开始读取库内索引字段信息");
				  loadIndexColumn(st, database,parwh,type);// 读取索引字段
			  }
		  }else if("N".equals(param2)){
			  parwh = " NOT IN (" + limit.toString() + ") ";
			  if(EnumDMObjectType.TABLE.equals(type) || EnumDMObjectType.VIEW.equals(type)){
				  logger.info("开始读取库内对象");
				  loadObject(st, database,parwh,type);// 读取库内所有对象
				  logger.info("开始读取库内表与视图的字段信息");
				  loadTableViewColumn(st, database,parwh,type);// 读取表和视图的字段
			  }else if (EnumDMObjectType.INDEX.equals(type)){
				  logger.info("开始读取库内索引");
				  loadIndex(st, database,parwh,type);// 读取索引
				  logger.info("开始读取库内索引字段信息");
				  loadIndexColumn(st, database,parwh,type);// 读取索引字段
			  }
		  }
	  }
  }

  public DMDatabase loadDatabase(DMDatasource datasoure ,DMTypeConfig dmTypeConfig) {
    DMDatabase database = null;
    Statement st = null;
    ResultSet rs = null;
    try {
    	database = new DMDatabase(datasoure.getConfig().getUn().toUpperCase(), DB_TYPE);
    	// 为下面的检索构建statement
    	st = datasoure.getConnection().createStatement();
    	
    	String table = dmTypeConfig.getTable();//tc.getTable();
    	String tableInclude = dmTypeConfig.getTableInclude();//tc.getTableInclude();
    	String view = dmTypeConfig.getView();//tc.getView();
    	String viewInclude = dmTypeConfig.getViewInclude();//tc.getViewInclude();
    	String index = dmTypeConfig.getIndex();//tc.getIndex();
    	String indexInclude = dmTypeConfig.getIndexInclude();//tc.getIndexInclude();
    	// 解析表
    	if(!"".equals(table)){
    		analyConfig(st,database,EnumDMObjectType.TABLE,table,tableInclude);
    	}
    	// 解析视图 : 需判断表是否在，如果不存在则需提示用户增加对应的表
    	if(!"".equals(view)){
    		String flag = loadTableExists(st,database,dmTypeConfig);
    		if("0".equals(flag)){
    			analyConfig(st,database,EnumDMObjectType.VIEW,view,viewInclude);
    		}else if(!"0".equals(flag) && !"1".equals(flag)){
    			String name = ManagementFactory.getRuntimeMXBean().getName();  
    			String pid = name.split("@")[0];  
    			try {
    				logger.info("解析视图失败，配置信息中对象依赖不合法，请重新配置视图依赖的表。需要配置的表为 ：" + flag);
					Runtime.getRuntime().exec("cmd.exe /c taskkill /f /pid " + pid);
					Runtime.getRuntime().exec("cmd.exe pause ");
					logger.info("程序退出。");
					Runtime.getRuntime().exit(0);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    	        //System.out.println("kill progress : "+pid);    
    			//analyConfig(st,database,EnumDMObjectType.VIEW,view,viewInclude);
    		}
    	}
		if(!"".equals(index)){
			analyConfig(st,database,EnumDMObjectType.INDEX,index,indexInclude);
		}

    	
//    	logger.info("开始读取库内对象");
//    	loadObject(st, database);// 读取库内所有对象
//    	logger.info("开始读取库内表与视图的字段信息");
//    	loadTableViewColumn(st, database);// 读取表和视图的字段
//    	logger.info("开始读取库内索引");
//    	loadIndex(st, database);// 读取索引
//    	logger.info("开始读取库内索引字段信息");
//    	loadIndexColumn(st, database);// 读取索引字段
//      logger.info("开始读取库内约束信息");
//      loadCons(st, database);// 读取约束
//      logger.info("开始读取库内约束字段信息");
//      loadConColumn(st, database);// 读取约束字段
//      logger.info("开始读取库内存储过程字段信息");
//      loadProColumn(st, database);// 读取存储过程字段
//      logger.info("开始读取库内Sequence信息");
//      loadSequence(st, database);// 读取seq信息
    	logger.info("开始处理库对象的DDL");
    	loadDDL(datasoure, database);// 读取对象DDL
    	logger.info("库解析完毕");
    } catch (SQLException e) {
      logger.error(e.getMessage(), e);
    } finally {
      close(rs);
      close(st);
    }
    return database;
  }


  private EnumDMObjectType convert(String type) {
    String _type = type != null ? type.toUpperCase():"";
    if ("TABLE".equals(_type)) {
      return EnumDMObjectType.TABLE;
    } else if ("VIEW".equals(_type)) {
      return EnumDMObjectType.VIEW;
    } else if ("SEQUENCE".equals(_type)) {
      return EnumDMObjectType.SEQUENCE;
    } else if ("PROCEDURE".equals(_type) || "FUNCTION".equals(_type)) {
      return EnumDMObjectType.PROCEDURE;
    }
    return EnumDMObjectType.ORTHER;
  }

  private String convertObjname(String objname) {
    if (objname != null) {
      if (!objname.toUpperCase().equals(objname)) {
        return "\"" + objname + "\"";
      }
    }
    return objname;
  }

  private String reciveObjname(String objname) {
    if (objname != null) {
      if (objname.startsWith("\"")) {
        return objname.substring(1, objname.length() - 1);
      }
    }
    return objname;
  }
  
  // 解析视图，先判断视图中解析的表是否存在，
  private String loadTableExists(Statement st ,DMDatabase database, DMTypeConfig dmTypeConfig){
	  ResultSet rs = null;
	  try {
		  String table = dmTypeConfig.getTable();//tc.getTable();
		  String view = dmTypeConfig.getView();//tc.getView();
		  StringBuffer sb = new StringBuffer();
		  String[] str = view.split(",");
		  String[] strTable = table.split(",");
		  String SQL_GET_TBL_EXT ;
		  if("".equals(view)){
			  logger.info("不导出任何视图 ，不存在视图和表的依赖关系。");
			  return "1"; // return false
		  }else if("*".equals(view) && "*".equals(table)){
			  //SQL_GET_TBL_EXT = " select distinct REFERENCED_NAME from USER_DEPENDENCIES WHERE type ='VIEW' ";
			  return "0"; // return true;
		  }else if("*".equals(table) && "".equals(view)){
			  return "1";
		  }else if("".equals(table) && "*".equals(view)){
			  SQL_GET_TBL_EXT = " select distinct REFERENCED_NAME from USER_DEPENDENCIES WHERE type ='VIEW' ";
		  }else if(table.length()>0 && !"*".equals(table)){
			  for(int i=0;i<strTable.length;i++){
				  sb.append(" '").append(strTable[i]).append("' ").append(",");
			  }
			  sb.deleteCharAt(sb.lastIndexOf(","));
			  SQL_GET_TBL_EXT = " select distinct REFERENCED_NAME from USER_DEPENDENCIES WHERE type ='VIEW' AND REFERENCED_NAME not in (" +sb+ ") ";
		  }else{
			  for(int i=0;i<str.length;i++){
				  sb.append(" '").append(str[i]).append("' ").append(",");
			  }
			  sb.deleteCharAt(sb.lastIndexOf(","));
			  SQL_GET_TBL_EXT = "select referenced_name from USER_DEPENDENCIES WHERE name in ( "+sb+" ) ";
		  }
		  rs = st.executeQuery(SQL_GET_TBL_EXT);
		  String s = ""; ;
		  while(rs.next()){
			  String refTable = rs.getString("REFERENCED_NAME");
			  if(table.contains(refTable)){
				  continue;
			  }else{
				  s += refTable + ",";
			  }
		  }
		  return s; // return false
	} catch (Exception e) {
		logger.error(e.getMessage(), e);
	} finally {
		close(rs);
    }
	return "1"; // return false
  }

  
  
  private void loadObject(Statement st, DMDatabase database,String parwh,EnumDMObjectType objtype) {
    ResultSet rs = null;
    try {
      // 循环体内临时用
      DMObject object;
      EnumDMObjectType etype;
      String name, subname, type, add;
      // 读取库内对象(表,视图,索引)
      String SQL_GET_OBJ = "SELECT A1.OBJECT_NAME,A1.OBJECT_TYPE,A1.SUBOBJECT_NAME,A1.TEMPORARY,A2.COMMENTS" +
      " FROM  USER_OBJECTS A1 LEFT JOIN USER_TAB_COMMENTS A2" +
      " ON A1.OBJECT_NAME = A2.TABLE_NAME " +
      " WHERE A1.STATUS = 'VALID' " ;
      if(objtype != null && !"".equals(objtype)){
    	  SQL_GET_OBJ = SQL_GET_OBJ + " AND A1.OBJECT_TYPE = '" + objtype + "' ";
      }
      if(parwh != null && !"".equals(parwh)){
    	  SQL_GET_OBJ = SQL_GET_OBJ + " AND A1.OBJECT_NAME " + parwh.toUpperCase();
      }
      SQL_GET_OBJ = SQL_GET_OBJ + " ORDER BY A1.OBJECT_NAME";
      
      rs = st.executeQuery(SQL_GET_OBJ);
      while (rs.next()) {
        type = rs.getString("OBJECT_TYPE");
        etype = convert(type);// 转为枚举
        name = convertObjname(rs.getString("OBJECT_NAME"));
        object = new DMObject(name, etype);
        add = "";// 一般情况下不需要增加附加信息
        // 分区使用副名称,并且记录表名
        if (EnumDMObjectType.TABLE.equals(etype)) {
          object.setAttr("SUBTYPE", "Y".equals(rs.getString("TEMPORARY")) ? "GLOBAL TEMPORARY":"TABLE");
        } 
        //else if (EnumDMObjectType.PROCEDURE.equals(etype)) {
        //  object.setAttr("SUBTYPE", type);
        //} 
        else if (EnumDMObjectType.ORTHER.equals(etype)) {
          add = type + ".";// 其它类型对象需要增加附加的类型前缀
          // 类型转换
          if ("DATABASE LINK".equals(type)) {
            type = "DB_LINK";
          }
          object.setAttr("SUBTYPE", type);// 其它对象需要记录子类型
          subname = convertObjname(rs.getString("SUBOBJECT_NAME"));
          if (subname != null && subname.length() > 0) {
            object.setName(subname);// 使用子类型作为对象名称
            object.setAttr("PNAME", name);// 记录所属对象名称
          }
          if ("TABLE PARTITION".equals(type)) {
            database.setObjectAttr(name, "PARTITION", "--"); // 记录这里仅标记表分区,先记录一个非空的注释,最后再补齐
          }
        }
        object.setAttr("COMMENT", rs.getString("COMMENTS"));

        database.addDMObject(add + name, object);
        logger.debug("找到{}对象:{}", type, object.getName());
      }
    } catch (SQLException e) {
      logger.error(e.getMessage(), e);
    } finally {
      close(rs);
    }
  }

  private void convertColumnType(DMColumn column, String type, int cl, int pl, int sl) {
    if ("NUMBER".equalsIgnoreCase(type)) {
      if (pl == 0 && sl == 0) {
        type = "INTEGE";// 整形在oracle中是以number(0)存储
      }
      cl = pl;
    } else if ("FLOAT".equalsIgnoreCase(type)) {
      cl = pl;// oracle 精度等于长度
    } else if (type != null && type.startsWith("TIMESTAMP")) {
      type = "TIMESTAMP";// oracle TIMESTAMP 带有 精度
    }
    column.setAttr("TYPE", type);
    column.setAttr("SIZE", cl);
    column.setAttr("SCALE", sl);
  }



  private void loadTableViewColumn(Statement st, DMDatabase database,String parwh,EnumDMObjectType type) {
	  // 读取库内对象字段(表与视图)
    String SQL_GET_COL = "SELECT A1.TABLE_NAME" +
    ",A1.COLUMN_NAME,A1.DATA_TYPE,NVL(A1.CHAR_LENGTH,0) AS CLEN" +
    ",NVL(A1.DATA_PRECISION,0) AS PLEN,NVL(A1. DATA_SCALE,0) AS SLEN" +
    ",A1.NULLABLE,A1.DATA_DEFAULT,A1.CHARACTER_SET_NAME" +
    ",A2.COMMENTS FROM USER_TAB_COLUMNS A1" +
    " LEFT JOIN USER_COL_COMMENTS A2 ON A2.TABLE_NAME = A1.TABLE_NAME " +
    " LEFT JOIN USER_OBJECTS A3 ON A1.TABLE_NAME = A3.OBJECT_NAME " +
    " AND A2.COLUMN_NAME = A1.COLUMN_NAME WHERE 1=1 " ;
    if(type != null && !"".equals(type)){
    	SQL_GET_COL = SQL_GET_COL + " AND A3.OBJECT_TYPE =  '" + type + "' ";
    }
    if(parwh != null && !"".equals(parwh)){
	  SQL_GET_COL = SQL_GET_COL + " AND A1.TABLE_NAME " + parwh.toUpperCase();
    }
    SQL_GET_COL = SQL_GET_COL + " ORDER BY A1.TABLE_NAME,A1.COLUMN_ID";
    
    ResultSet rs = null;
    try {
      rs = st.executeQuery(SQL_GET_COL);
      String objname;
      DMColumn column;
      while (rs.next()) {
        column = new DMColumn(rs.getString("COLUMN_NAME"));
        convertColumnType(column, rs.getString("DATA_TYPE"), rs.getInt("CLEN"), rs.getInt("PLEN"), rs.getInt("SLEN"));
        column.setAttr("NULL", rs.getString("NULLABLE"));
        column.setAttr("COMMENT", rs.getString("COMMENTS"));
        objname = convertObjname(rs.getString("TABLE_NAME"));
        database.addObjectColumn(objname, column);// 默认为table and view类型
        logger.debug("找到对象{}字段:{}", objname, column.getName());
      }
    } catch (SQLException e) {
      logger.error(e.getMessage(), e);
    } finally {
      close(rs);
    }
  }

  

  private void loadIndex(Statement st, DMDatabase database, String parwh,EnumDMObjectType objtype) {
	
	// 读取库内索引信息(仅索引)
	String SQL_INDEX = "SELECT TABLE_NAME,INDEX_NAME,INDEX_TYPE" +
	    ",TABLESPACE_NAME FROM USER_INDEXES WHERE UNIQUENESS <> 'UNIQUE' AND STATUS = 'VALID'" ;
	if(parwh != null && !"".equals(parwh)){
		SQL_INDEX = SQL_INDEX + " AND INDEX_NAME " + parwh.toUpperCase();
    }
	SQL_INDEX = SQL_INDEX + " ORDER BY TABLE_NAME,INDEX_NAME";
    ResultSet rs = null;
    try {
      rs = st.executeQuery(SQL_INDEX);
      String objname, name, type;
      DMObject object;
      while (rs.next()) {
        name = convertObjname(rs.getString("INDEX_NAME"));
        objname = convertObjname(rs.getString("TABLE_NAME"));
        type = rs.getString("INDEX_TYPE");
        object = new DMObject(name, EnumDMObjectType.ORTHER);
        object.setAttr("PNAME", objname);
        object.setAttr("SUBTYPE", "INDEX");
        object.setAttr("INDTYPE", type);
        database.addDMObject("INDEX." + name, object);// 特别指定为index
        logger.debug("找到对象{}的索引:{}", objname, name);
      }
    } catch (SQLException e) {
      logger.error(e.getMessage(), e);
    } finally {
      close(rs);
    }
  }


  private void loadIndexColumn(Statement st, DMDatabase database , String parwh,EnumDMObjectType type) {
   // 读取索引字段
    String SQL_IND_COL = "SELECT A1.INDEX_NAME" +
    ",A1.COLUMN_NAME,A1.DESCEND FROM USER_IND_COLUMNS A1" +
    " LEFT JOIN USER_INDEXES A2 ON A1.INDEX_NAME = A2.INDEX_NAME" +
    " WHERE A2.UNIQUENESS <> 'UNIQUE' AND STATUS = 'VALID' " ;
	if(parwh != null && !"".equals(parwh)){
		SQL_IND_COL = SQL_IND_COL + " AND A1.INDEX_NAME " + parwh.toUpperCase();
    }
	SQL_IND_COL = SQL_IND_COL + "ORDER BY INDEX_NAME,COLUMN_POSITION";  
    ResultSet rs = null;
    try {
      rs = st.executeQuery(SQL_IND_COL);
      DMColumn column;
      String objname, name;
      while (rs.next()) {
        name = rs.getString("COLUMN_NAME");
        objname = rs.getString("INDEX_NAME");
        column = new DMColumn(name);
        column.setAttr("DESCEND", rs.getString("DESCEND"));
        database.addObjectColumn("INDEX." + objname, column);// 特别指定为index
        logger.debug("找到索引{}的字段:{}", objname, name);
      }
    } catch (SQLException e) {
      logger.error(e.getMessage(), e);
    } finally {
      close(rs);
    }
  }

  // 读取库内约束
  private static final String SQL_CONSTRAINT = "SELECT TABLE_NAME,CONSTRAINT_NAME" +
    ",CONSTRAINT_TYPE,SEARCH_CONDITION FROM USER_CONSTRAINTS WHERE STATUS = 'ENABLED' AND VALIDATED = 'VALIDATED' ORDER BY CONSTRAINT_NAME";
  private static final String SQL_RCONSTRAINT = "select A1.TABLE_NAME,A1.CONSTRAINT_NAME" +
    ",A1.CONSTRAINT_TYPE,A2.TABLE_NAME AS RNAME FROM USER_CONSTRAINTS A1 LEFT" +
    " JOIN USER_CONSTRAINTS A2 ON A1.R_CONSTRAINT_NAME = A2.CONSTRAINT_NAME" +
    " WHERE A1.R_CONSTRAINT_NAME IS NOT NULL AND A1.STATUS = 'ENABLED' AND A1.VALIDATED = 'VALIDATED' ORDER BY A1.TABLE_NAME";

  private void loadCons(Statement st, DMDatabase database) {
    ResultSet rs = null;
    try {
      rs = st.executeQuery(SQL_CONSTRAINT);
      DMObject object;
      String objname, name;
      while (rs.next()) {
        name = convertObjname(rs.getString("CONSTRAINT_NAME"));
        objname = convertObjname(rs.getString("TABLE_NAME"));
        object = new DMObject(name, EnumDMObjectType.ORTHER);
        object.setAttr("PNAME", objname);
        object.setAttr("SUBTYPE", "CONSTRAINT");
        object.setAttr("CONTYPE", rs.getString("CONSTRAINT_TYPE"));
        object.setAttr("CON", rs.getString("SEARCH_CONDITION"));
        database.addDMObject("CONSTRAINT." + name, object);// 特别声明为CONSTRAINT
        logger.debug("找到对象{}的约束:{}", name, name);
      }
      close(rs);
      rs = st.executeQuery(SQL_RCONSTRAINT);
      while (rs.next()) {
        name = rs.getString("CONSTRAINT_NAME");
        objname = convertObjname(rs.getString("TABLE_NAME"));
        object = new DMObject(name, EnumDMObjectType.ORTHER);
        object.setAttr("PNAME", objname);
        object.setAttr("SUBTYPE", "CONSTRAINT");
        object.setAttr("CONTYPE", rs.getString("CONSTRAINT_TYPE"));
        object.setAttr("RNAME", rs.getString("RNAME"));
        database.addDMObject("CONSTRAINT." + name, object);// 特别声明为CONSTRAINT
        logger.debug("找到对象{}的约束:{}", name, name);
      }
    } catch (SQLException e) {
      logger.error(e.getMessage(), e);
    } finally {
      close(rs);
    }
  }

  // 读取约束字段
  private static final String SQL_CON_COL = "SELECT CONSTRAINT_NAME" +
    ",COLUMN_NAME FROM USER_CONS_COLUMNS" +
    " ORDER BY TABLE_NAME,CONSTRAINT_NAME,POSITION";// 索引字段

  private void loadConColumn(Statement st, DMDatabase database) {
    ResultSet rs = null;
    try {
      rs = st.executeQuery(SQL_CON_COL);
      DMColumn column;
      String objname, name;
      while (rs.next()) {
        name = rs.getString("COLUMN_NAME");
        objname = convertObjname(rs.getString("CONSTRAINT_NAME"));
        column = new DMColumn(name);
        database.addObjectColumn("CONSTRAINT." + objname, column);
        logger.debug("找到约束{}的字段:{}", objname, name);// 特别声明为CONSTRAINT
      }
    } catch (SQLException e) {
      logger.error(e.getMessage(), e);
    } finally {
      close(rs);
    }
  }

  private static final String SQL_PRO_COL = "SELECT OBJECT_NAME" +
    ",ARGUMENT_NAME,DATA_TYPE,IN_OUT,NVL(CHAR_LENGTH,0) AS CLEN" +
    ",NVL(DATA_PRECISION,0) AS PLEN,NVL(DATA_SCALE,0) AS SLEN" +
    ",DEFAULT_VALUE FROM USER_ARGUMENTS ORDER BY OBJECT_NAME,POSITION";// 存储过程参数

  private void loadProColumn(Statement st, DMDatabase database) {
    ResultSet rs = null;
    try {
      rs = st.executeQuery(SQL_PRO_COL);
      DMColumn column;
      String objname, name;
      while (rs.next()) {
        name = rs.getString("ARGUMENT_NAME");
        objname = convertObjname(rs.getString("OBJECT_NAME"));
        column = new DMColumn(name);
        convertColumnType(column, rs.getString("DATA_TYPE"), rs.getInt("CLEN"), rs.getInt("PLEN"), rs.getInt("SLEN"));
        column.setAttr("IN_OUT", rs.getString("IN_OUT"));
        column.setAttr("DEF", rs.getString("DEFAULT_VALUE"));
        database.addObjectColumn(objname, column);
        logger.debug("找到存储过程{}的参数:{}", objname, name);
      }
    } catch (SQLException e) {
      logger.error(e.getMessage(), e);
    } finally {
      close(rs);
    }
  }

  private static final String SQL_SEQUENCES = "SELECT SEQUENCE_NAME" +
    ",MIN_VALUE,MAX_VALUE,INCREMENT_BY,CYCLE_FLAG,ORDER_FLAG" +
    ",CACHE_SIZE,LAST_NUMBER FROM USER_SEQUENCES";// 读取seq信息

  private void loadSequence(Statement st, DMDatabase database) {
    ResultSet rs = null;
    try {
      rs = st.executeQuery(SQL_SEQUENCES);
      String name;
      while (rs.next()) {
        name = convertObjname(rs.getString("SEQUENCE_NAME"));
        database.setObjectAttr(name, "MIN_VALUE", rs.getString("MIN_VALUE"));
        database.setObjectAttr(name, "MAX_VALUE", rs.getString("MAX_VALUE"));
        database.setObjectAttr(name, "INCREMENT_BY", rs.getString("INCREMENT_BY"));
        database.setObjectAttr(name, "CYCLE_FLAG", rs.getString("CYCLE_FLAG"));
        database.setObjectAttr(name, "ORDER_FLAG", rs.getString("ORDER_FLAG"));
        database.setObjectAttr(name, "CACHE_SIZE", rs.getString("CACHE_SIZE"));
        database.setObjectAttr(name, "LAST_NUMBER", rs.getString("LAST_NUMBER"));
        logger.debug("找到Sequence{}的参数", name);
      }
    } catch (SQLException e) {
      logger.error(e.getMessage(), e);
    } finally {
      close(rs);
    }
  }

  // 读取DDL定义
  private static final String SQL_DDL = "SELECT DBMS_METADATA.GET_DDL(?,?) AS DDL FROM DUAL";

  private void loadDDL(DMDatasource datasource, DMDatabase database) {
    PreparedStatement ps = null;
    try {
      ps = datasource.getConnection().prepareStatement(SQL_DDL);
      String type, ddl;
      for (DMObject obj : database.loadObjects(null)) {
        if (EnumDMObjectType.ORTHER.equals(obj.getType())) {
          type = String.valueOf(obj.getAttrs().get("SUBTYPE"));
          if (("CONSTRAINT".equals(type)) && "R".equals(obj.getAttrs().get("CONTYPE"))) {
            type = "REF_CONSTRAINT";// 外键比较特殊需要特殊处理
          }
          // 不支持这些类型的ddl生成
          if ("TABLE PARTITION".equals(type)
            || "INDEX PARTITION".equals(type)
            || "TABLE SUBPARTITION".equals(type)
            || "INDEX SUBPARTITION".equals(type)
            || "LOB".equals(type)
            || "LOB PARTITION".equals(type)) {
            continue;
          }
        } else if (EnumDMObjectType.PROCEDURE.equals(obj.getType())) {
          type = String.valueOf(obj.getAttrs().get("SUBTYPE"));
        } else {
          type = obj.getType().name();
        }
        ddl = loadDDL(ps, type, reciveObjname(obj.getName()));
        obj.setAttr("DDL", ddl);
        if (EnumDMObjectType.TABLE.equals(obj.getType()) && obj.getAttrs().containsKey("PARTITION")) {
          logger.debug("解析{}的分区信息", obj.getName());
          obj.setAttr("PARTITION", loadTablePartition(ddl));
        }
      }
    } catch (SQLException e) {
      logger.error(e.getMessage(), e);
    } finally {
      close(ps);
    }
  }

  private String loadDDL(PreparedStatement ps, String type, String name) {
    ResultSet rs = null;
    Reader reader;
    try {
      ps.setString(1, type);
      ps.setString(2, name);
      rs = ps.executeQuery();
      // 只有一条记录
      if (rs.next()) {
        reader = rs.getClob(1).getCharacterStream();
        BufferedReader br = new BufferedReader(reader);
        StringBuilder ddl = new StringBuilder(1024);
        String s = br.readLine();
        while (s != null) {
          ddl.append(s);
          s = br.readLine();
          ddl.append("\r\n");
        }
        return ddl.toString().trim();
      }
    } catch (SQLException e) {
      logger.warn("Oracle系统不支持{}对象{}获取DDL!", type, name);// 读出的内容不是全部都可以生成DDL,这里的错误大多数由此产生,所以这里仅显示一下
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    } finally {
      close(rs);
    }
    return null;
  }

  private String loadTablePartition(String ddl) {
    StringBuilder partition = new StringBuilder();
    char ch;// 读取的字符
    boolean record = false;// 开始记录语句的开关
    int pattern = 0;// 括号匹配
    int num = 0;// 括号匹配次数
    // DDL关键字都是大写的,并且PARTITiON都是在语句中心或末尾
    for (int i = 0; i < ddl.length(); i++) {
      ch = ddl.charAt(i);
      if (record) {
        partition.append(ch);
        if (ch == '(' || ch == ')') {
          if (ch == '(') {
            pattern++;
          } else {
            pattern--;
          }
          // 如果完整匹配一次就需要减少匹配次数
          if (pattern == 0) {
            num--;
            if (num == 0) {
              break;// 2次匹配均已用完,这时候需要退出
            }
          }
        }
        // 读到;肯定要结束
        if (ch == ';') {
          break;
        }
      } else if (
        // 检索后面前后否为 (非字母连接符_')PARTITION(空格)
        ch == 'P'
          && !(ddl.charAt(i - 1) == '_'
          || ddl.charAt(i - 1) == '\''
          || Character.isLetterOrDigit(ddl.charAt(i - 1))
        )
          && ddl.charAt(i + 1) == 'A'
          && ddl.charAt(i + 2) == 'R'
          && ddl.charAt(i + 3) == 'T'
          && ddl.charAt(i + 4) == 'I'
          && ddl.charAt(i + 5) == 'T'
          && ddl.charAt(i + 6) == 'I'
          && ddl.charAt(i + 7) == 'O'
          && ddl.charAt(i + 8) == 'N'
          && ddl.charAt(i + 9) == ' '
        ) {
        partition.append(ch);
        record = true;
        num = 2;// 一个分区需要匹配2次括号
      }
    }
    return partition.toString();
  }
}