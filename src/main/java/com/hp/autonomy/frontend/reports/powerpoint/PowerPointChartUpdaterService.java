package com.hp.autonomy.frontend.reports.powerpoint;

import com.hp.autonomy.frontend.reports.powerpoint.dto.ComposableElement;


public interface PowerPointChartUpdaterService {

	/**
	 * 
	 * @param filePath - the file path of the PPT to update in-place
	 * @param slideNumber - the slideNumber which points to the slide to update
	 * @param composableElement - the new data to use for the chart. could be PieChartData, BarChartData etc.
	 */
	public void updateChart( final String filePath, final int slideNumber, final ComposableElement composableElement ) ;
	
}
