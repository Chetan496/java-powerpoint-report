package com.hp.autonomy.frontend.reports.powerpoint;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.poi.xslf.usermodel.XMLSlideShow;

import com.hp.autonomy.frontend.reports.powerpoint.dto.BarData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.DategraphData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.PieChartData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.ReportData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.SunburstData;

public class TestPPTGen {

	public static void main(String[] args) throws Exception {

		PieChartData pieChartData = new PieChartData();
		pieChartData.setChartLabel("My Pie Chart");
		pieChartData.setCategories(new String[] { "apples", "oranges", "guavas" });
		pieChartData.setSeries(new double[] { 60.0, 30.0, 10 });
		
		
		PowerPointChartUpdaterService chartUpdaterService = new PowerPointChartUpdaterServiceImpl();
		XMLSlideShow outPPTX = chartUpdaterService.updateChart("E:\\SamplePPT.pptx", 2, pieChartData);
		 
		
		//you can choose to write to the same file.. so that it actually updates it.
		outPPTX.write(new FileOutputStream("E:\\updatedChart.pptx"));
		
	}

	private static void testChartGenerator() throws TemplateLoadException, IOException, FileNotFoundException {
		PowerPointService pptxService = new PowerPointServiceImpl();

		 final DategraphData dategraph = new DategraphData(
		            new long[]{
		                1480690162, 1482394810, 1484099459, 1485804108
		            },
		            Arrays.asList(
		                new DategraphData.Row("#FF0000", "Red Line", false, new double[]{
		                    87, 87, 124, 49
		                }),
		                new DategraphData.Row("#00FF00", "Green Line", true, new double[]{
		                    12, 53, 63, 72
		                })
		            )
		    );
		
		
		PieChartData pieChartData = new PieChartData();
		pieChartData.setChartLabel("My Pie Chart");
		pieChartData.setCategories(new String[] { "apples", "oranges", "guavas" });
		pieChartData.setSeries(new double[] { 60.0, 30.0, 10 });

		BarData barData = new BarData();
		barData.setCategoryLabels(new String[] { "cat1", "cat2", "cat3" });
		barData.setColumnChart(true);
		barData.setSeriesData(new ArrayList<BarData.Series>(
				Arrays.asList(new BarData.Series("Red", "series1", new double[] { 1.0, 2.0 }),
						new BarData.Series("Blue", "series2", new double[] { 1.0, 3.0 }),
						new BarData.Series("Green", "series3", new double[] { 1.0, 2.5 })

				)));

		// catgeories, values, colors , strokecolors, showinlegend, title

		final SunburstData topRightSunburst = new SunburstData(new String[] { "Red", "Green", "Blue" },
				new double[] { 1, 169, 130 }, null, null, null, "RGB Colours");
		
		
		 final SunburstData bottomRightSunburst = new SunburstData(
			        new String[] { "Cyan", "Magenta", "Yellow", "Black"},
			        new double[] { 0.994, 0, 0.231, 0.337 },
			        null,
			        null,
			        null,
			        "CMYK Colours"
			    );

		 final String titleFont = "Times New Roman";
		    final double titleFontSize = 12;
		    final double titleMargin = 5;
		    final double widgetMargins = 3;
		    final ReportData report = new ReportData(new ReportData.Child[] {
		        // Dategraph taking the full left pane
		        new ReportData.Child(0, 0, 0.5, 1, "Left Dategraph", widgetMargins, titleMargin, titleFontSize, titleFont, dategraph),
		        
		        new ReportData.Child(0.5, 0, 0.5, 0.5, "Top Right Sunburst", widgetMargins, titleMargin, titleFontSize, titleFont, topRightSunburst) ,
		        // Another sunburst taking the bottom-right
		        new ReportData.Child(0.5, 0.5, 0.5, 0.5, "Bottom Right PieChart", widgetMargins, titleMargin, titleFontSize, titleFont, pieChartData)
		    });

		 

		final XMLSlideShow pptx = pptxService.report(report, false);

		// final XMLSlideShow pptx = pptxService.sunburst(bottomRightSunburst);
		pptx.write(new FileOutputStream("E:\\genchart.pptx"));
	
	}

}
