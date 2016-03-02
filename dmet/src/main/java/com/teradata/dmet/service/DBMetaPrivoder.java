/**
 * Copyright 2011 By Teradata China Co.Ltd. All rights reserved
 */
package com.teradata.dmet.service;

import com.teradata.dmet.mobel.*;

import java.util.Collection;
import java.util.List;

/**
 * 数据库meta信息价值提供接口
 */
public interface DBMetaPrivoder{
  /**
   * 获得提供者名称
   *
   * @return 非空
   */
  String getName();

  /**
   * 获取提供者支持的驱动类型
   *
   * @return 驱动字符串, 非空
   */
  String getDriver();

  /**
   * 获得连接串模板
   *
   * @return 连接串
   */
  String getURLTemplate();

  /**
   * 获得附加配置
   *
   * @return 附加配置的清单
   */
  Collection<DMParam> getAddition();


  /**
   * 根据配置生成特有的连接串
   *
   * @param config 数据库通用配置
   * @return 连接串, 非空
   */
  String createUrl(DMConfig config);

  /**
   * 读取数据源的库信息
   *
   * @param datasoure 内部用数据源对象
   * @return 数据源的库结构, 非空
   */
  DMDatabase loadDatabase(DMDatasource datasoure ,DMTypeConfig dmTypeConfig);
}
