/**
 * Copyright 2012 By Teradata China Co.Ltd. All rights reserved
 */
package com.teradata.dmet.service;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * EXCEL导出
 */
public class ExcelTranslator{
  protected Logger logger = LoggerFactory.getLogger(this.getClass());
  protected Workbook workbook;// 解析出来的workbook对象
  protected static final String FMT_VAR;// 模板中变量格式
  protected static final Pattern REG_VER;// 参数表达式类型
  protected static final String FMT_CODE;// 约定的数据格式
  protected static final Pattern REG_CODE;// 参数表达式类型
  protected static final String ERROR_CELL;//数据读出错误的标记

  static {
    FMT_VAR = "\\$(\\w+)";
    REG_VER = Pattern.compile(FMT_VAR);
    FMT_CODE = "\\{\"id\":\"(\\w +)\"\\}";
    REG_CODE = Pattern.compile(FMT_CODE);
    ERROR_CELL = "ERROR";
  }

  public ExcelTranslator(String template) {
    // 读出excel并转化为可以解析对象
    try {
      if (template.toUpperCase().endsWith(".XLS")) {
        this.workbook = new HSSFWorkbook(new POIFSFileSystem(new FileInputStream(new File(template))));
      } else if (template.toUpperCase().endsWith(".XLSX")) {
        this.workbook = new XSSFWorkbook(new FileInputStream(new File(template)));
      }
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e.getMessage());
    }
  }

  public void export(List dataList, int sheetIndex, int dataRow) {
    List<CellConfig> mappings = getMapping(sheetIndex, dataRow);
    Sheet sheet = workbook.getSheetAt(sheetIndex);
    int row = dataRow;
    for (Object data : dataList) {
      copyRows(sheet, row, row + 1);//将模板的数据拷贝出来一份 留作下一次使用
      for (CellConfig cc : mappings) {
        // 根据列配置设置数据
        Cell cell = sheet.getRow(row).getCell(cc.column);
        try {
          setValue(cell, PropertyUtils.getProperty(data, cc.value));// 使用配置中vlaue名称取出WadiList一行的值
        } catch (Exception e) {
          logger.error(e.getMessage(), e);// 仅记录错误,这样可导出数据,从数据文件中看到错误信息
          cell.setCellValue(ERROR_CELL);// 获取value出错设置错误标记
        }
      }
      row++;// 将模板开始行列向下移动
    }
    if (sheet != null) {
      sheet.removeRow(sheet.getRow(row));// 删除模板行
    }
  }

  public void export(Map<String, Object> data, int sheetIndex) {
    Sheet sheet = workbook.getSheetAt(sheetIndex);
    String value;
    for (Row row : sheet) {
      for (Cell cell : row) {
        value = getValue(cell);
        if (value != null && REG_VER.matcher(value).find()) {
          setValue(cell, data.get(value.substring(1)));
        }
      }
    }
  }

  public void write(OutputStream out) {
    try {
      workbook.write(out);// 输出流
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
  }

  public static final String FMT_DATE = "yyyy-MM-dd";
  public static final String FMT_TIME = "yyyy-MM-dd HH:mm:ss";
  public static final String FMT_NUMBER = "##0.##";

  /**
   * 格式化日期
   *
   * @param dateValue 日期值对象
   * @return 格式化后的日期字符串
   */
  private static String fmtDate(Date dateValue) {
    if (dateValue == null) {
      return null;
    }
    String p = FMT_TIME;
    SimpleDateFormat sdf;
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(dateValue);
    if (calendar.get(Calendar.HOUR_OF_DAY) == 0 && calendar.get(Calendar.MINUTE) == 0 && calendar.get(Calendar.SECOND) == 0) {
      p = FMT_DATE;// 如果时分秒都是0,那么认为是纯日期格式
    }
    sdf = new SimpleDateFormat(p);
    return sdf.format(dateValue);
  }

  /**
   * 格式化数字
   *
   * @param numberValue 数字值对象
   * @return 格式化后的数字字符串
   */
  public static String fmtNumber(Number numberValue) {
    if (numberValue == null) {
      return null;
    }
    DecimalFormat nf = new DecimalFormat(FMT_NUMBER);
    return nf.format(numberValue);
  }

  /**
   * 读取cell的值
   *
   * @param cell cell单元格对象，非空
   * @return 读取的cell值, 多空格视为没有填写,返回null
   */
  private String getValue(Cell cell) {
    String value = null;
    if (cell != null) {
      switch (cell.getCellType()) {
        case Cell.CELL_TYPE_STRING:
          value = cell.getStringCellValue();//字符型
          break;
        case Cell.CELL_TYPE_NUMERIC:
          if (DateUtil.isCellDateFormatted(cell)) {
            value = fmtDate(cell.getDateCellValue());//日期
            break;
          } else {
            value = fmtNumber(cell.getNumericCellValue());//数字
            break;
          }
        case Cell.CELL_TYPE_BOOLEAN:
          value = String.valueOf(cell.getBooleanCellValue());//布尔
          break;
      }
    }
    return value != null ? value:null;
  }

  protected void setValue(Cell cell, Object value) {
    if (value == null) {
      cell.setCellValue("");// 设置为空串
    } else if (Number.class.isAssignableFrom(value.getClass())) {
      cell.setCellValue(new Double(value.toString()));// 数字
    } else if (Date.class.isAssignableFrom(value.getClass())) {
      cell.setCellValue(new Date(((Date) value).getTime()));//日期
    } else if (Boolean.class.isAssignableFrom(value.getClass())) {
      cell.setCellValue((Boolean) value);//布尔
    } else {
      cell.setCellValue(value.toString());// 默认都按字符串
    }
  }

  private List<CellConfig> getMapping(int sheet, int datarow) {
    Sheet extractSheet = workbook.getSheetAt(sheet);
    // 分析模板行的数据,提取metas信息
    List<CellConfig> metas = new ArrayList<CellConfig>();
    Row row = extractSheet.getRow(datarow);
    for (int i = 0; row != null && i < row.getLastCellNum(); ++i) {
      Cell cell = row.getCell(i);
      String value = getValue(cell);
      if (value != null && REG_VER.matcher(value).find()) {
        metas.add(new CellConfig(value.substring(1), i));
      }
    }
    return metas;
  }

  /**
   * 同一sheet内复制startRow~endRow行数据给pPosition
   *
   * @param st        sheet对象非空
   * @param row       源行,0开始
   * @param pPosition 目标行位置
   */
  private void copyRows(Sheet st, int row, int pPosition) {
    // 复制merge情况
    CellRangeAddress region;
    for (int i = 0; i < st.getNumMergedRegions(); i++) {
      region = st.getMergedRegion(i);
      if ((region.getFirstRow() == row) && (region.getLastRow() == row)) {
        CellRangeAddress newRegion = region.copy();
        newRegion.setFirstRow(pPosition);
        newRegion.setLastRow(pPosition);
        newRegion.setFirstColumn(region.getFirstColumn());
        newRegion.setLastColumn(region.getLastColumn());
        st.addMergedRegion(newRegion);
      }
    }
    Row sourceRow = st.getRow(row);// 复制一段row至pPositon
    int columnCount = sourceRow.getLastCellNum();
    Row newRow = st.createRow(pPosition);
    newRow.setHeight(sourceRow.getHeight());
    for (int j = 0; j < columnCount; j++) {
      Cell templateCell = sourceRow.getCell(j);
      if (templateCell != null) {
        newRow.createCell(j).setCellStyle(templateCell.getCellStyle());// 仅复制样式
      }
    }
  }

  private class CellConfig{
    public String value;// 存储的值或名称
    public int column;// 列序号

    public CellConfig(String value, int column) {
      this.value = value;
      this.column = column;
    }
  }
}
