package com.teradata.dmet.mobel;

import java.io.Serializable;

public class DMTypeConfig implements Serializable{
	
	private String table;
	private String tableInclude;
	private String view;
	private String viewInclude;
	private String index;
	private String indexInclude;
	
	public DMTypeConfig(){}
	
	public DMTypeConfig(String table ,String tableInclude , String view , String viewInclude , String index , String indexIncude){
		this.table = table ;
		this.tableInclude = tableInclude ; 
		this.view = view ; 
		this.viewInclude = viewInclude;
		this.index = index;
		this.indexInclude = indexIncude;
	}
	
	public String getTable() {
		return table;
	}
	public void setTable(String table) {
		this.table = table;
	}
	public String getTableInclude() {
		return tableInclude;
	}
	public void setTableInclude(String tableInclude) {
		this.tableInclude = tableInclude;
	}
	public String getView() {
		return view;
	}
	public void setView(String view) {
		this.view = view;
	}
	public String getViewInclude() {
		return viewInclude;
	}
	public void setViewInclude(String viewInclude) {
		this.viewInclude = viewInclude;
	}
	public String getIndex() {
		return index;
	}
	public void setIndex(String index) {
		this.index = index;
	}
	public String getIndexInclude() {
		return indexInclude;
	}
	public void setIndexInclude(String indexInclude) {
		this.indexInclude = indexInclude;
	}

}
