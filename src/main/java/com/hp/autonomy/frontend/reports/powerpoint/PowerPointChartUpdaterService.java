package com.hp.autonomy.frontend.reports.powerpoint;

import org.apache.poi.xslf.usermodel.XMLSlideShow;

import com.hp.autonomy.frontend.reports.powerpoint.dto.ComposableElement;


public interface PowerPointChartUpdaterService {

	/**
	 * 
	 * @param filePath - the file path of the PPT to update in-place
	 * @param slideNumber - the slideNumber which points to the slide to update
	 * @param composableElement - the new data to use for the chart. could be PieChartData, BarChartData etc.
	 * @return XMLSlideShow  - the modified PPT, you can choose to overwrite the existine one, or write to a new file.
	 */
	public XMLSlideShow updateChart( final String filePath, final int slideNumber, final ComposableElement composableElement ) ;
	
}
