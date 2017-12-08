package com.hp.autonomy.frontend.reports.powerpoint;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFChart;
import org.apache.poi.xslf.usermodel.XSLFGraphicFrame;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGraphicalObjectFrame;

import com.hp.autonomy.frontend.reports.powerpoint.dto.ComposableElement;
import com.hp.autonomy.frontend.reports.powerpoint.dto.PieChartData;

public class PowerPointChartUpdaterServiceImpl implements PowerPointChartUpdaterService {
	
	private static final String RELATION_NAMESPACE = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";
	

	@Override
	public void updateChart(final String filePath, final int slideNumber, final ComposableElement composableElement) {
		
		if(composableElement instanceof PieChartData) {
			updatePieChart(filePath,  slideNumber, (PieChartData) composableElement);
		}
		
		
		
	}

	private void updatePieChart(final String filePath, final int slideNumber, final PieChartData pieChartData) {

		try {
			validateInput(filePath, slideNumber, pieChartData); // take care of all bad input.
			
			FileInputStream fileInputStream = new FileInputStream(new File(filePath));
			XMLSlideShow slideShow = new XMLSlideShow(fileInputStream);

			// get the mentioned slide
			XSLFSlide slide = slideShow.getSlides().get(slideNumber);

			// get the chart from this slide
			final ImmutablePair<XSLFChart, CTGraphicalObjectFrame> chartArtifact= getChart(slide);
			
			//get the Excel workbook for this chart
			final XSSFWorkbook workbook = getWorkBookOfChart(  chartArtifact.getLeft() ) ;
			
			//now you can write the workbook with the new data and update cell references, then write back the workbook and the chart
					
			
			

			// we are done updating the chart
			workbook.close();
			slideShow.close();
			fileInputStream.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SlideHasNoChartException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ChartHasNoWorkBookException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
					
					return workbook;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		}
		

		throw new ChartHasNoWorkBookException("This chart has no associated excel workbook");		
		
	}
	
	
	
	
	
	
	

}
