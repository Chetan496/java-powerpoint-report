package com.hp.autonomy.frontend.reports.powerpoint.dto;

import java.util.ArrayList;

/* Represents a chart category with multiple series values*/

public class Category {
	
	private String categoryName;
	private int numberOfSeries;
	private ArrayList<Double> seriesValues  = null;
	
	
	public Category(String categoryName1, int numberOfSeries1){
		this.categoryName = categoryName1;
		this.numberOfSeries = numberOfSeries1;
		
		seriesValues = new ArrayList<Double>(this.numberOfSeries);
		
	}
	
	
	
}
