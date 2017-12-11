package com.hp.autonomy.frontend.reports.powerpoint;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFChart;
import org.apache.poi.xslf.usermodel.XSLFGraphicFrame;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumData;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumVal;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPieChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPieSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTSerTx;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrData;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrVal;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGraphicalObjectFrame;

import com.hp.autonomy.frontend.reports.powerpoint.dto.ComposableElement;
import com.hp.autonomy.frontend.reports.powerpoint.dto.PieChartData;

public class PowerPointChartUpdaterServiceImpl implements PowerPointChartUpdaterService {
	
	private static final String RELATION_NAMESPACE = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";
	

	@Override
	public XMLSlideShow updateChart(final String filePath, final int slideNumber, final ComposableElement composableElement) {
		
		if(composableElement instanceof PieChartData) {
			return updatePieChart(filePath,  slideNumber, (PieChartData) composableElement);
		}
		
		return null;
		
	}

	
	//we should be returning back the new XMLSlideShow, so that the caller can choose to write it wherever they want to write it
	private XMLSlideShow updatePieChart(final String filePath, final int slideNumber, final PieChartData pieChartData) {

		try {
			validateInput(filePath, slideNumber, pieChartData); // take care of all bad input.
			
			FileInputStream fileInputStream = new FileInputStream(new File(filePath));
			XMLSlideShow slideShow = new XMLSlideShow(fileInputStream);

			// get the mentioned slide
			XSLFSlide slide = slideShow.getSlides().get(slideNumber);

			// get the chart from this slide
			final ImmutablePair<XSLFChart, CTGraphicalObjectFrame> chartArtifact= getChart(slide);
			
			//check if the chart is really a pie chart or some other chart
			if (ArrayUtils.isEmpty(chartArtifact.getLeft().getCTChart().getPlotArea().getPieChartArray())) {
                throw new IllegalArgumentException("The slide has the wrong chart type, expected pie chart");
            }
			
			//get the Excel workbook for this chart
			final XSSFWorkbook workbook = new XSSFWorkbook() ;
			final XSSFSheet sheet = workbook.createSheet("Data for Pie Chart");
			
			
			//unwrap the data from the piechartdata sent
			final String chartLabel = pieChartData.getChartLabel();
			final double[] seriesValues = pieChartData.getSeries();
			
			
			final CTPieChart ctPieChart =
					chartArtifact.getLeft().getCTChart().getPlotArea().getPieChartArray(0);
			final CTPieSer ctPieSer = ctPieChart.getSerArray(0);
			
			
			// update text for this series
			CTSerTx tx = ctPieSer.getTx();
			tx.getStrRef().getStrCache().getPtArray(0).setV(chartLabel);
			sheet.createRow(0).createCell(1).setCellValue(chartLabel);
			String seriesRef = new CellReference(sheet.getSheetName(), 0,  1, true, true)
					.formatAsString();
			tx.getStrRef().setF(seriesRef);
			
			// category axis data
			CTAxDataSource cat = ctPieSer.getCat();
			CTStrData catData = cat.getStrRef().getStrCache();

			// Values
			CTNumDataSource val = ctPieSer.getVal();
			CTNumData numData = val.getNumRef().getNumCache();
			
			
			catData.setPtArray(null); // unset old axis text
			numData.setPtArray(null); // unset old values
			
			int idx = 0;
			int rownum = 1;

			for (double seriesVal : seriesValues) {

				CTNumVal numVal = numData.addNewPt();
				numVal.setIdx(idx);
				numVal.setV(new Double(seriesVal).toString());

				String categoryLabel = pieChartData.getCategories()[idx];
				CTStrVal sVal = catData.addNewPt();
				sVal.setIdx(idx);
				sVal.setV(categoryLabel);

				XSSFRow row = createOrGetRow(sheet, rownum);
				row.createCell(0).setCellValue(categoryLabel); //the cell may already exists
				row.createCell(1).setCellValue(seriesVal);

				idx++;
				rownum++;

			}
			
			numData.getPtCount().setVal(idx);
			catData.getPtCount().setVal(idx);
			
			String numDataRange = new CellRangeAddress(1, rownum - 1, 1, 1).formatAsString(sheet.getSheetName(), true);
			val.getNumRef().setF(numDataRange);

			String axisDataRange = new CellRangeAddress(1, rownum - 1, 0, 0).formatAsString(sheet.getSheetName(), true);
			cat.getStrRef().setF(axisDataRange);
			
			//we need to save this in chartPart of the XMLSlideShow
			//the Excel workbook needs to be written back
			XSLFChart chart = chartArtifact.getLeft();
			
			for (final POIXMLDocumentPart.RelationPart part : chart.getRelationParts()) {
				final String contentType = part.getDocumentPart().getPackagePart().getContentType();
				
				if ("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType)) {
					 workbook.write(part.getDocumentPart().getPackagePart().getOutputStream()) ;
					 break;
				}
				
			}
			
			

			// we are done updating the chart
			workbook.close();
			fileInputStream.close();
			
			return slideShow;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SlideHasNoChartException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return null;

	}

	
	private static XSSFRow createOrGetRow(XSSFSheet sheet, int rownum) {

		if (sheet.getRow(rownum) == null) {
			return sheet.createRow(rownum);
		}

		return sheet.getRow(rownum);
	}
	
	
	private void validateInput(final String filePath, final int slideNumber, final PieChartData pieChartData) throws IOException {
		
		if(filePath == null || filePath.trim().equals("") ) {
			throw new IllegalArgumentException("FilePath cannot be null/empty");
		}
		
		
		if(pieChartData == null) {
			throw new IllegalArgumentException("The pie chart data is null");
		}
		
		FileInputStream fileInputStream = new FileInputStream(new File(filePath));
		XMLSlideShow slideShow = new XMLSlideShow(fileInputStream);
		
		if(  slideNumber < 0 ) {
			slideShow.close();
			fileInputStream.close();
			throw new IllegalArgumentException("slide number cannot be less than 0");
		}
		
		if ( slideNumber + 1 > slideShow.getSlides().size() ) {
			slideShow.close();
			fileInputStream.close();
			throw new IllegalArgumentException("slide number cannot be greater than number of slides");
		}
		
		
		if(!pieChartData.validateInput()) {
			slideShow.close();
			fileInputStream.close();
			throw new IllegalStateException();
		}
		
		slideShow.close();
		fileInputStream.close();

	}

	
	
	private ImmutablePair<XSLFChart, CTGraphicalObjectFrame> getChart(final XSLFSlide slide) throws SlideHasNoChartException {
		for (POIXMLDocumentPart.RelationPart part : slide.getRelationParts()) {
			if (part.getDocumentPart() instanceof XSLFChart) {
				final String relId = part.getRelationship().getId();

				for (XSLFShape shape : slide.getShapes()) {
					if (shape instanceof XSLFGraphicFrame) {
						final CTGraphicalObjectFrame frameXML = (CTGraphicalObjectFrame) shape.getXmlObject();
						final XmlObject[] children = frameXML.getGraphic().getGraphicData()
								.selectChildren(new QName(XSSFRelation.NS_CHART, "chart"));

						for (final XmlObject child : children) {
							final String imageRel = child.getDomNode().getAttributes()
									.getNamedItemNS(RELATION_NAMESPACE, "id").getNodeValue();

							if (relId.equals(imageRel)) {
								return new ImmutablePair<>(part.getDocumentPart(), frameXML);
							}
						}
					}
				}
			}
		}
		
		throw new SlideHasNoChartException("The Slide does not have the chart");

	}
	
	
	
	private  XSSFWorkbook getWorkBookOfChart(final XSLFChart chart) throws ChartHasNoWorkBookException {
		
		for (final POIXMLDocumentPart.RelationPart part : chart.getRelationParts()) {
			
			final String contentType = part.getDocumentPart().getPackagePart().getContentType();
			
			if(  "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType) ) {
				
				try {
					InputStream inputStream = part.getDocumentPart().getPackagePart().getInputStream();
					XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
					
					
					//we need to set all rows and column to null for this workbook.
					return workbook;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		}
		

		throw new ChartHasNoWorkBookException("This chart has no associated excel workbook");		
		
	}
	
	
	private void printRowsFromFirstSheet(XSSFWorkbook workbook) {
		
		
		Iterator<Row> rows = workbook.getSheetAt(0).rowIterator();
		while (rows.hasNext()) {
		    XSSFRow row = (XSSFRow) rows.next();

		    Iterator<Cell> cells = row.cellIterator();
		    while (cells.hasNext()) {
		        XSSFCell cell = (XSSFCell) cells.next();

		        //Must do this, you need to get value based on the cell type
		        switch (cell.getCellType()) {
		            case XSSFCell.CELL_TYPE_NUMERIC:
		                System.out.println(cell.getNumericCellValue());
		            break;
		            case XSSFCell.CELL_TYPE_STRING:
		                System.out.println(cell.getStringCellValue());
		            break;
		            default: break;
		        }
		    }
		}	
		
	}
	
	
	
	
	
	
	
	
	

}
