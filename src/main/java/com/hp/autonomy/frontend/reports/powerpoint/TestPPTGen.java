package com.hp.autonomy.frontend.reports.powerpoint;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFChart;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBarChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBarDir;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBarSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumData;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumVal;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPieChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPieSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTSerTx;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrData;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrVal;
import org.openxmlformats.schemas.drawingml.x2006.chart.impl.CTBarDirImpl;

public class TestPPTGen {

	private static void piechartusage() {
		System.out.println("Usage: PieChartDemo <pie-chart-template.pptx> <pie-chart-data.txt>");
		System.out.println("    pie-chart-template.pptx     template with a pie chart");
		System.out.println("    pie-chart-data.txt          the model to set. First line is chart title, "
				+ "then go pairs {axis-label value}");
	}

	private static void barchartusage() {
		System.out.println("Usage: BarChartDemo <bar-chart-template.pptx> <bar-chart-data.txt>");
		System.out.println("    bar-chart-template.pptx     template with a bar chart");
		System.out.println("    bar-chart-data.txt          the model to set. First line is chart title, "
				+ "then go pairs {axis-label value}");
	}

	private static void threecolumnchartusage() {
		System.out.println("Usage: ColumnChartDemo <column-chart-template.pptx> <column-chart-data.txt>");
		System.out.println("    column-chart-template.pptx     template with a column chart");
		System.out.println("    column-chart-data.txt          the model to set. First line is chart title, "
				+ "then go pairs {axis-label value}");
	}

	private static void TestThreeSeriesColumnChartGen(String[] args) throws IOException {

		if (args.length < 2) {
			threecolumnchartusage();
			return;
		}

		BufferedReader modelReader = new BufferedReader(new FileReader(args[1]));
		XMLSlideShow pptx = null;

		String chartTitle = modelReader.readLine(); // first line is chart title

		pptx = new XMLSlideShow(new FileInputStream(args[0]));

		XSLFSlide slide = pptx.getSlides().get(0);
		XSLFChart templateChart = null;
		for (POIXMLDocumentPart part : slide.getRelations()) {
			if (part instanceof XSLFChart) {
				templateChart = (XSLFChart) part;
				break;
			}
		}

		String seriesLabelsLine = modelReader.readLine();
		String[] seriesVals = seriesLabelsLine.split("\\s+");

		if (templateChart == null)
			throw new IllegalStateException("chart not found in the template");

		// embedded Excel workbook that holds the chart data
		POIXMLDocumentPart xlsPart = templateChart.getRelations().get(0);
		XSSFWorkbook wb = new XSSFWorkbook(); // this one will hold the data that we pass i.e read from the model.

		XSSFSheet sheet = wb.createSheet();

		CTChart ctChart = templateChart.getCTChart();
		CTPlotArea plotArea = ctChart.getPlotArea();

		CTBarChart ctBarChart = plotArea.getBarChartArray(0);

		int currIndex = 0;
		for (CTBarSer ctBarSer:ctBarChart.getSerArray()) {

			

			// text for this series
			CTSerTx tx = ctBarSer.getTx();
			tx.getStrRef().getStrCache().getPtArray(0).setV(seriesVals[currIndex]);
			sheet.createRow(0).createCell(currIndex + 1).setCellValue(seriesVals[currIndex]);
			String seriesRef = new CellReference(sheet.getSheetName(), 0, currIndex + 1, true, true).formatAsString();
			tx.getStrRef().setF(seriesRef);

			// category axis data
			CTAxDataSource cat = ctBarSer.getCat();
			CTStrData catData = cat.getStrRef().getStrCache();

			// Values
			CTNumDataSource val = ctBarSer.getVal();
			CTNumData numData = val.getNumRef().getNumCache();

			catData.setPtArray(null); // unset old axis text
			numData.setPtArray(null); // unset old values

			int idx = 0;
			int rownum = 1;
			String ln;
			
			modelReader.mark(1000);
			
			// for series 0, currIndex = 0
			while ((ln = modelReader.readLine()) != null) {

				String[] lineVals = ln.split("\\s+");

				CTNumVal numVal = numData.addNewPt();
				numVal.setIdx(idx);
				numVal.setV(lineVals[currIndex + 1]);

				CTStrVal sVal = catData.addNewPt();
				sVal.setIdx(idx);
				sVal.setV(lineVals[0]);

				idx++;

				XSSFRow row = createOrGetRow(sheet, rownum);
				row.createCell(0).setCellValue(lineVals[0]);
				row.createCell(currIndex + 1).setCellValue(Double.valueOf(lineVals[currIndex + 1]));

				rownum++;

			}

			numData.getPtCount().setVal(idx);
			catData.getPtCount().setVal(idx);

			String numDataRange = new CellRangeAddress(1, rownum - 1, currIndex + 1, currIndex + 1)
					.formatAsString(sheet.getSheetName(), true);
			val.getNumRef().setF(numDataRange);

			String axisDataRange = new CellRangeAddress(1, rownum - 1, 0, 0).formatAsString(sheet.getSheetName(), true);
			cat.getStrRef().setF(axisDataRange);
			
			modelReader.reset();
			
			currIndex++;

		} //loop ends

		OutputStream xlsOut = xlsPart.getPackagePart().getOutputStream();
		try {
			wb.write(xlsOut);
		} finally {
			xlsOut.close();
		}

		// save the result
		OutputStream out = new FileOutputStream("E:\\column-chart-demo-output.pptx");
		try {
			pptx.write(out);
		} finally {
			out.close();
		}

		wb.close();

		if (pptx != null)
			pptx.close();

		modelReader.close();

	}

	private static XSSFRow createOrGetRow(XSSFSheet sheet, int rownum) {

		if (sheet.getRow(rownum) == null) {
			return sheet.createRow(rownum);
		}

		return sheet.getRow(rownum);
	}

	private static void TestBarChartGen(String[] args) throws IOException {
		if (args.length < 2) {
			barchartusage();
			return;
		}

		// the data reader for our chart.
		BufferedReader modelReader = new BufferedReader(new FileReader(args[1]));
		XMLSlideShow pptx = null;

		String chartTitle = modelReader.readLine(); // first line is chart title

		pptx = new XMLSlideShow(new FileInputStream(args[0]));

		// this slide is read from template in-memory and is being modified by the code
		// here
		XSLFSlide slide = pptx.getSlides().get(0);

		// find chart in the slide
		XSLFChart templateChart = null;
		for (POIXMLDocumentPart part : slide.getRelations()) {
			if (part instanceof XSLFChart) {
				templateChart = (XSLFChart) part;
				break;
			}
		}

		if (templateChart == null)
			throw new IllegalStateException("chart not found in the template");

		// embedded Excel workbook that holds the chart data
		POIXMLDocumentPart xlsPart = templateChart.getRelations().get(0);
		XSSFWorkbook wb = new XSSFWorkbook(); // this one will hold the data that we pass i.e read from the model.

		XSSFSheet sheet = wb.createSheet();

		CTChart ctChart = templateChart.getCTChart();
		CTPlotArea plotArea = ctChart.getPlotArea();

		CTBarChart ctBarChart = plotArea.getBarChartArray(0);

		// we have got the in-memory XML representation of the chart object from
		// template.
		// we are now going to set its values based on the data from the model.

		CTBarSer ctBarSer = ctBarChart.getSerArray(0); // we are getting the first series (assuming that there is just
														// one series)

		// text of this series
		CTSerTx tx = ctBarSer.getTx();
		tx.getStrRef().getStrCache().getPtArray(0).setV(chartTitle);
		sheet.createRow(0).createCell(1).setCellValue(chartTitle);
		String titleRef = new CellReference(sheet.getSheetName(), 0, 1, true, true).formatAsString();
		tx.getStrRef().setF(titleRef); // we set the cell reference for the title.

		// category axis data
		CTAxDataSource cat = ctBarSer.getCat();
		CTStrData strData = cat.getStrRef().getStrCache(); // this will hold the labels for the categories.

		// Values
		CTNumDataSource val = ctBarSer.getVal();
		CTNumData numData = val.getNumRef().getNumCache();

		// old points inside are being set to null -- since we copied in-memory data
		// from template, we also picked the values from template
		strData.setPtArray(null); // unset old axis text
		numData.setPtArray(null); // unset old values

		// set model
		int idx = 0;
		int rownum = 1;
		String ln;
		while ((ln = modelReader.readLine()) != null) {
			String[] vals = ln.split("\\s+");
			CTNumVal numVal = numData.addNewPt();
			numVal.setIdx(idx);
			numVal.setV(vals[1]);

			CTStrVal sVal = strData.addNewPt();
			sVal.setIdx(idx);
			sVal.setV(vals[0]);

			idx++;
			XSSFRow row = sheet.createRow(rownum++);
			row.createCell(0).setCellValue(vals[0]); // category label
			row.createCell(1).setCellValue(Double.valueOf(vals[1])); // value for this category
		}
		numData.getPtCount().setVal(idx);
		strData.getPtCount().setVal(idx);

		// update the cell reference for category and values.
		String numDataRange = new CellRangeAddress(1, rownum - 1, 1, 1).formatAsString(sheet.getSheetName(), true);
		val.getNumRef().setF(numDataRange);
		String axisDataRange = new CellRangeAddress(1, rownum - 1, 0, 0).formatAsString(sheet.getSheetName(), true);
		cat.getStrRef().setF(axisDataRange);

		OutputStream xlsOut = xlsPart.getPackagePart().getOutputStream();
		try {
			wb.write(xlsOut);
		} finally {
			xlsOut.close();
		}

		// save the result
		OutputStream out = new FileOutputStream("E:\\bar-chart-demo-output.pptx");
		try {
			pptx.write(out);
		} finally {
			out.close();
		}

		wb.close();

		if (pptx != null)
			pptx.close();
		modelReader.close();

	}

	private static void TestPieChartGen(String[] args) throws IOException {
		if (args.length < 2) {
			piechartusage();
			return;
		}

		BufferedReader modelReader = new BufferedReader(new FileReader(args[1]));
		XMLSlideShow pptx = null;
		try {
			String chartTitle = modelReader.readLine(); // first line is chart title

			pptx = new XMLSlideShow(new FileInputStream(args[0]));
			XSLFSlide slide = pptx.getSlides().get(0);

			// find chart in the slide
			XSLFChart chart = null;
			for (POIXMLDocumentPart part : slide.getRelations()) {
				if (part instanceof XSLFChart) {
					chart = (XSLFChart) part;
					break;
				}
			}

			if (chart == null)
				throw new IllegalStateException("chart not found in the template");

			// embedded Excel workbook that holds the chart data
			POIXMLDocumentPart xlsPart = chart.getRelations().get(0);
			XSSFWorkbook wb = new XSSFWorkbook();
			try {
				XSSFSheet sheet = wb.createSheet();

				CTChart ctChart = chart.getCTChart();
				CTPlotArea plotArea = ctChart.getPlotArea();

				CTPieChart pieChart = plotArea.getPieChartArray(0);
				// Pie Chart Series
				CTPieSer ser = pieChart.getSerArray(0);

				// Series Text
				CTSerTx tx = ser.getTx();
				tx.getStrRef().getStrCache().getPtArray(0).setV(chartTitle);
				sheet.createRow(0).createCell(1).setCellValue(chartTitle);
				String titleRef = new CellReference(sheet.getSheetName(), 0, 1, true, true).formatAsString();
				tx.getStrRef().setF(titleRef);

				// Category Axis Data
				CTAxDataSource cat = ser.getCat();
				CTStrData strData = cat.getStrRef().getStrCache();

				// Values
				CTNumDataSource val = ser.getVal();
				CTNumData numData = val.getNumRef().getNumCache();

				strData.setPtArray(null); // unset old axis text
				numData.setPtArray(null); // unset old values

				// set model
				int idx = 0;
				int rownum = 1;
				String ln;
				while ((ln = modelReader.readLine()) != null) {
					String[] vals = ln.split("\\s+");
					CTNumVal numVal = numData.addNewPt();
					numVal.setIdx(idx);
					numVal.setV(vals[1]);

					CTStrVal sVal = strData.addNewPt();
					sVal.setIdx(idx);
					sVal.setV(vals[0]);

					idx++;
					XSSFRow row = sheet.createRow(rownum++);
					row.createCell(0).setCellValue(vals[0]);
					row.createCell(1).setCellValue(Double.valueOf(vals[1]));
				}
				numData.getPtCount().setVal(idx);
				strData.getPtCount().setVal(idx);

				String numDataRange = new CellRangeAddress(1, rownum - 1, 1, 1).formatAsString(sheet.getSheetName(),
						true);
				val.getNumRef().setF(numDataRange);
				String axisDataRange = new CellRangeAddress(1, rownum - 1, 0, 0).formatAsString(sheet.getSheetName(),
						true);
				cat.getStrRef().setF(axisDataRange);

				// updated the embedded workbook with the data
				OutputStream xlsOut = xlsPart.getPackagePart().getOutputStream();
				try {
					wb.write(xlsOut);
				} finally {
					xlsOut.close();
				}

				// save the result
				OutputStream out = new FileOutputStream("E:\\pie-chart-demo-output.pptx");
				try {
					pptx.write(out);
				} finally {
					out.close();
				}
			} finally {
				wb.close();
			}
		} finally {
			if (pptx != null)
				pptx.close();
			modelReader.close();
		}

	}

	public static void main(String[] args) throws Exception {

		// TestPieChartGen(args);

		// TestBarChartGen(args);

		TestThreeSeriesColumnChartGen(args);

	}

}
