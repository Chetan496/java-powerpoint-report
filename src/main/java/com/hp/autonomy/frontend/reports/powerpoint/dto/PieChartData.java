package com.hp.autonomy.frontend.reports.powerpoint.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PieChartData implements ComposableElement {

	
	private double[] series;
	private String[] categories;
	
	public boolean validateInput() {
		
		if(series.length == 0 || categories.length == 0) {
			return false;
		}
		
		if(series.length != categories.length) {
			return false;
		}
		
		return true;
		
	}
	
	
	
}
