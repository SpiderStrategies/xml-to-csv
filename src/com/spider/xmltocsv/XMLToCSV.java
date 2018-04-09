package com.spider.xmltocsv;

import com.google.common.base.Joiner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.JDOMException;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.XSLTransformer;
import org.jdom2.transform.XSLTransformException;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

public class XMLToCSV {
	
	public static void main(String[] args) throws IOException, XSLTransformException, JDOMException {
		Document xmlDoc = getXMLDocument(args);
		String xsltString = Utils.readTextFromFile(args[0]);
		Document newDoc = getTransformedDocument(xmlDoc, xsltString);
		//outputTransformedXML(newDoc);
		
		XPathExpression<Element> rowsXpath = XPathFactory.instance().compile("table/row", Filters.element());
		List<Map<String,String>> dataSet = new ArrayList<>();
		
		for(Element row: rowsXpath.evaluate(newDoc)) {
			XPathExpression<Element> columnsXpath = XPathFactory.instance().compile("column", Filters.element());
			Map<String,String> mapRow = new HashMap<>();
			for(Element column: columnsXpath.evaluate(row)) {
				String label = column.getAttribute("name").getValue();
				String value = column.getText();
				mapRow.put(label, "\"" + value + "\"");
			}
			dataSet.add(mapRow);
		}
		
		Set<String> colNames = dataSet.get(0).keySet();
		System.out.println(Joiner.on(",").join(colNames));
		
		for(Iterator<Map<String,String>> dataRowIter = dataSet.iterator(); dataRowIter.hasNext();) {
			Map<String,String> dataRow = dataRowIter.next();
			String comma = "";
			for(String colName: colNames) {
				System.out.print(comma + dataRow.get(colName));
				comma = ",";
			}
			System.out.println("");
		}
	}
	
	private static Document getTransformedDocument(Document doc, String xsltString) throws XSLTransformException {		
		XSLTransformer xslt = new XSLTransformer(new ByteArrayInputStream(xsltString.getBytes()));
		return xslt.transform(doc);
	}
	
	private static Document getXMLDocument(String[] args) throws IOException, JDOMException {
		if(args.length > 1) {
			String xmlContent = Utils.readTextFromFile(args[1]);
			InputStream stream = new ByteArrayInputStream(xmlContent.getBytes("UTF-8"));
			return new SAXBuilder().build(stream);
		}
		else {
			return new SAXBuilder().build(System.in);
		}
	}
	
	private static void outputTransformedXML(Document doc) throws IOException{
		Format format = Format.getPrettyFormat();
		XMLOutputter outputter = new XMLOutputter(format);
		PrintWriter writer = new PrintWriter(System.out);
		outputter.output(doc, writer);
	}

}
