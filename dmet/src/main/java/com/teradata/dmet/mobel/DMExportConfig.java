/**
 * Copyright 2011 By Teradata China Co.Ltd. All rights reserved
 */
package com.teradata.dmet.mobel;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * 导出配置
 */
public class DMExportConfig implements Serializable{
  private String sys;
  private String ver;
  private String filter;// 过滤的对象
  private String reject;// 排除的对象
  private String templateFile;// 导出模板
  private String writeFile;// 导出文件存储路径
  private Pattern filterRegexp = null;
  private Pattern rejectRegexp = null;

  public String getSys() {
    return sys;
  }

  public void setSys(String sys) {
    this.sys = sys;
  }

  public String getVer() {
    return ver;
  }

  public void setVer(String ver) {
    this.ver = ver;
  }

  public String getFilter() {
    return filter;
  }

  public void setFilter(String filter) {
    this.filter = filter;
    filterRegexp = Pattern.compile(filter);
  }

  public String getReject() {
    return reject;
  }

  public void setReject(String reject) {
    this.reject = reject;
    rejectRegexp = Pattern.compile(reject);
  }

  public String getTemplateFile() {
    return templateFile;
  }

  public void setTemplateFile(String templateFile) {
    this.templateFile = templateFile;
  }

  public String getWriteFile() {
    return writeFile;
  }

  public void setWriteFile(String writeFile) {
    this.writeFile = writeFile;
  }

  /**
   * 是否可以导出
   *
   * @param objName 对象名称
   * @return true可以导出
   */
  public boolean export(String objName) {
    boolean export = true;
    if (filterRegexp != null) {
      export = filterRegexp.matcher(objName).find();
    }
    if (rejectRegexp != null) {
      export = !rejectRegexp.matcher(objName).find();
    }
    return export;
  }
}
