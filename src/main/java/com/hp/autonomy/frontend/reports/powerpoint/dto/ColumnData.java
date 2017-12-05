package com.hp.autonomy.frontend.reports.powerpoint.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO to represent a table of data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColumnData implements ComposableElement {

	private String[] categoryLabels;
	
	/* total values in series array = length(categories) */
	private List<Series> seriesData ;
	
	
	public boolean validateInput() {
		
		if(categoryLabels.length < 1  || seriesData.size() < 1 ) {
			return false;
		}
			
		if(seriesData.size() != categoryLabels.length) {
			return false;
		}
		
		return true;
	}
	
	
	 /**
     * A DTO representing a data series in a clustered column chart
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
	public static class Series {

        /** The colour to render the series data, should be a hexadecimal string e.g. #FF0000 */
        private String color;

        /** The label for the series data. */
        private String label;

        /** List of values; should be the same length as the timestamps. */
        private double[] values;
    }
    
    
	 
}
