/**
 * Copyright 2011 By Teradata China Co.Ltd. All rights reserved
 */
package com.teradata.dmet.service;

import com.teradata.dmet.mobel.DMConfig;
import com.teradata.dmet.mobel.DMDatabase;
import com.teradata.dmet.mobel.DMExportConfig;
import com.teradata.dmet.mobel.DMTypeConfig;

import java.util.Collection;
import java.util.List;

/**
 * 数据源Meta信息管理
 */
public interface DBMetaService{
  static final String BEAN_NAME = "dbmetaService";

  /**
   * 获取支持清单
   *
   * @return 支持清单
   */
  Collection<DBMetaPrivoder> getSupport();

  /**
   * 注册一个数据源
   * <p/>
   * 根据配置,连接并存储这个数据源,连接失败抛出异常
   * 成功返回的key,可以被Manager使用并用来继续其它操作
   *
   * @param config 配置信息对象
   * @return Manager内部对于该数据源的key, 非空, 如连接失败会抛出BussinesExeption
   */
  String register(DMConfig config);

  /**
   * 读取数据源的结构
   *
   * @param dbkey 非空,这个key已经成功注册
   * @return 数据源的库结构, 非空
   */
  DMDatabase loadDatabase(String dbkey ,DMTypeConfig dmTypeConfig);

  /**
   * 导出数据
   *
   * @param config    导出配置
   * @param path
   * @param databases 需要到处的库
   */
  void export(DMExportConfig config, String path, List<DMDatabase> databases);

  /**
   * 释放数据库连接
   *
   * @param dbkey server返回的key
   */
  void release(String dbkey);
}
