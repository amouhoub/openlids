package analysis;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class StartAnalysis {

	public static void main(String[] args) throws IOException{ 
		
		// Create list of file names in analysis/input folder

		File dir1 = new File(".");
		File file = new File(dir1.getCanonicalPath() + "/analysis/input" );

	    File[] listOfFiles = file.listFiles();

	    List<ExtractedInformationOfFile> extractedInformation = new ArrayList();
	    
    	// Import and analyze each existing file step by step, save results in object
	    for (int i = 0; i < listOfFiles.length; i++) {
	    	String thisFile = fileToString(listOfFiles[i]);
	    	ExtractedInformationOfFile exFile = new ExtractedInformationOfFile();
	    	exFile.setId(i);
	    	
	    	// Find all DBPedia links in current document
	    	List<String> dbpedialinks = new ArrayList<String>();
	    	String[] links = getStringBetweenString(thisFile, "<rdfs:seeAlso xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" rdf:resource=\"", "\"/>");
	    	for(String link:links){
	    		dbpedialinks.add(link);
	    	}
	    	exFile.setDbpedialinks(dbpedialinks);
	    	
	    	// Calculate avgTimeBetweenTweets in current document
	    	String[] dates = getStringBetweenString(thisFile, "<dc:date xmlns:dc=\"http://purl.org/dc/elements/1.1/\">", "</dc:date>");
	    	Long[] dateIntervalls = new Long[dates.length-1];
	    	
	        DateFormat rfcDateTimeUTCTZFormat = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'Z'");
    	    	    	
	    	for(int j = 0; j < dates.length-1; j++){
				try {
					Date date1 = rfcDateTimeUTCTZFormat.parse(dates[j]);
		    		Date date2 = rfcDateTimeUTCTZFormat.parse(dates[j+1]);
		    		dateIntervalls[j] = date1.getTime()-date2.getTime();
				} catch (ParseException e) {
					e.printStackTrace();
				}
	    	}
	    	// Calculate the average
	    	double avgTimeBetweenTweets = 0;
	    	double sum = 0;
	    	double n = dateIntervalls.length;
	    	for(Long intervall:dateIntervalls){
	    		sum = sum + intervall.doubleValue();
	    	}
	    	avgTimeBetweenTweets=(sum/n);
	    	// Delete decimals
	    	int decimalPlace = 0;
	    	BigDecimal bd = new BigDecimal(avgTimeBetweenTweets);
	    	bd = bd.setScale(decimalPlace,BigDecimal.ROUND_HALF_UP);
	    	avgTimeBetweenTweets = bd.doubleValue();
	    	
	    	// Save avg time in milli seconds in object
	    	exFile.setAvgtimebetweentweets(avgTimeBetweenTweets);
	    	
	    	extractedInformation.add(exFile);
	    }
	    
	    // All information saved in objects now need to be analyzed
	    // In a first round, generate a list of all existing dbpedia tags
	    // Calculate the avg time between tweets of all files
	    double avgTimeBetweenTweetsAllFiles = 0;
    	double sum = 0;
    	double n = extractedInformation.size();
	    List<String> dbPediaLinksExisting = new ArrayList<String>();
	    for(ExtractedInformationOfFile exFileInfo : extractedInformation){
	    	sum = sum + exFileInfo.getAvgtimebetweentweets();
	    	List<String> dbPediaLinksThisFile = exFileInfo.getDbpedialinks();
	    	for(String dbPediaLink : dbPediaLinksThisFile){
	    		if(!dbPediaLinksExisting.contains(dbPediaLink)){
	    			dbPediaLinksExisting.add(dbPediaLink);
	    		}
	    	}	
	    	
	    }
    	avgTimeBetweenTweetsAllFiles=(sum/n);
    	// Delete decimals
    	int decimalPlace = 0;
    	BigDecimal bd = new BigDecimal(avgTimeBetweenTweetsAllFiles);
    	bd = bd.setScale(decimalPlace,BigDecimal.ROUND_HALF_UP);
    	avgTimeBetweenTweetsAllFiles = bd.doubleValue();
    	// Create string to save in csv file
    	StringBuffer csvString = new StringBuffer();
    	// Create Header
    	csvString.append("DBPediaURI");
    	for(ExtractedInformationOfFile exInfoFile : extractedInformation){
    		csvString.append(";");
    		csvString.append(listOfFiles[exInfoFile.getId()].getName());
    	}
    	csvString.append("\n");
    	// Get DBPedia URI from List of URIs and check if URI exists in document, mark with 1 or 0 in csvString
    	for(String dbPediaURI : dbPediaLinksExisting){
    		csvString.append(dbPediaURI);
        	for(ExtractedInformationOfFile exInfoFile : extractedInformation){
        		csvString.append(";");
        		if(exInfoFile.getDbpedialinks().contains(dbPediaURI)){
        			csvString.append("1");
        		}else{
        			csvString.append("0");
        		}
        	}
    		csvString.append("\n");
    	}
    	
    	// Output generation
    	BufferedWriter out = new BufferedWriter(new FileWriter(dir1.getCanonicalPath() + "/analysis/analysis.csv"));
    	out.write(csvString.toString());
    	out.close();
    	
    	BufferedWriter out2 = new BufferedWriter(new FileWriter(dir1.getCanonicalPath() + "/analysis/analysis_short.csv"));

    	System.out.println("ANALYSIS result:");
    	System.out.println("---");
    	System.out.println("The average time between tweets of tweets in all files [in seconds]:");
    	System.out.println(avgTimeBetweenTweetsAllFiles/1000);
    	System.out.println("---");
    	System.out.println("Probability of occurrences of DBPediaURIs in given data:");
    	String[] csvLines = csvString.toString().split("\n");
    	for(int i=1;i<csvLines.length;i++){
    		String[] lineElements = csvLines[i].split(";");
    		System.out.print(lineElements[0]);
    		out2.write(lineElements[0]);
    		System.out.print(" : ");
    		out2.write(";");
    		double prob = 0;
    		for(int j=1;j<lineElements.length;j++){
    			prob=prob+Double.parseDouble(lineElements[j]);
    		}
    		prob=prob/(lineElements.length-1);
    		prob=prob*100;
    		System.out.println(prob + " %");
    		out2.write(String.valueOf(prob).replace(".", ",") + " %");
    		out2.append("\n");
    	}
    	out2.close();
    	System.out.println("---");
    	System.out.println("A csv-file has been generated describing occurrences of dbpedia links");
	}
	
	public static String fileToString(File file) throws IOException {

		BufferedInputStream bin = null;
		
		FileInputStream fin = new FileInputStream(file);
		
		bin = new BufferedInputStream(fin);

		byte[] contents = new byte[1024];

		int bytesRead = 0;

		StringBuffer strFileContents = new StringBuffer();

		while ((bytesRead = bin.read(contents)) != -1) {
			strFileContents.append(new String(contents, 0, bytesRead));
		}

		if (bin != null)
			bin.close();

		return strFileContents.toString();
	}
	
	public static String[] getStringBetweenString(String thisString, String stringStart, String stringEnd) {
    	String[] helper1 = thisString.split(stringStart);
    	String[] helper2 = new String[helper1.length-1];
    	for(int i=1; i< helper1.length;i++){
    		helper2[i-1]=helper1[i].split(stringEnd,2)[0];
    	}
		return helper2;
	}

}