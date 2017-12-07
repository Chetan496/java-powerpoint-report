package com.hp.autonomy.frontend.reports.powerpoint;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.poi.xslf.usermodel.XMLSlideShow;

import com.hp.autonomy.frontend.reports.powerpoint.dto.BarData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.PieChartData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.SunburstData;

public class TestPPTGen {

	public static void main(String[] args) throws Exception {

		PowerPointService pptxService = new PowerPointServiceImpl();

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

		final XMLSlideShow pptx = pptxService.bar(barData);

		// final XMLSlideShow pptx = pptxService.sunburst(topRightSunburst);
		pptx.write(new FileOutputStream("E:\\genchart.pptx"));

	}

}
