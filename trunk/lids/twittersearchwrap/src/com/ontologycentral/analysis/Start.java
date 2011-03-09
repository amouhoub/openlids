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
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class Start {
	public static void main(String[] args) throws Exception {

		List<Hashtag> hashtags = new ArrayList();
		
		// Read all URIs - Saved in textfile in input folder
		File dir = new File(".");
		File file0 = new File(dir.getCanonicalPath() + "/analysis/input/uris.txt" );
	    String uristxt = fileToString(file0);
		String[] uri = uristxt.split(";");
		//
		
		// Create list of file names in analysis/input folder
		File file = new File(dir.getCanonicalPath() + "/analysis/input" );
	    File[] listOfFiles = file.listFiles();
	    
		for(int i = 0; i<uri.length; i++){
			
			// Find documents for URI
			List<File> listOfFilesHashtag = new ArrayList();
			List<File> listOfFilesHashtagExtern = new ArrayList();
			
			for(int j = 0; j<listOfFiles.length;j++){
				if (listOfFiles[j].getAbsolutePath().contains(uri[i])){
					if(listOfFiles[j].getAbsolutePath().contains("extern=true")) {
						listOfFilesHashtagExtern.add(listOfFiles[j]);
						}else{
							listOfFilesHashtag.add(listOfFiles[j]);
					}
				}
			}

			// Split analysis in extern=true and extern=false
			// Analysis for extern=false
			Iterator fileIt = listOfFilesHashtag.iterator();
			ExtractedInformationOfFile[] exFiles = new ExtractedInformationOfFile[listOfFilesHashtag.size()];
			int count = 0;
			while(fileIt.hasNext()){
				try{
				String thisFile = fileToString((File) fileIt.next());
				ExtractedInformationOfFile exFile = new ExtractedInformationOfFile();
		    	// Find all DBPedia links in current document
		    	List<String> entities = new ArrayList<String>();
		    	String[] links = getStringBetweenString(thisFile, "<rdfs:seeAlso xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" rdf:resource=\"", "\"/>");
		    	for(String link:links){
		    		entities.add(link);
		    	}
		    	exFile.setEntities(entities);
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
		    	
		    	// Calculate time stability
		    	if(count==0){
		    		exFile.setTimestability(1);
		    	}else{
		    		// Compare to previous document
		    		double timestability = 0.0;
		    		double helpersum = 0;
		    		HashSet<String> allEntities = new HashSet();
		    		Iterator entityIt =  entities.iterator();
		    		while(entityIt.hasNext()){
		    			String thisnext = (String)entityIt.next();
		    			allEntities.add(thisnext);
		    			for(String element : exFiles[count-1].getEntities()){
		    				if(element.equals(thisnext)){
		    					helpersum = helpersum+1;
		    				}
		    			}
		    		}
		    		Iterator entityItOld =  exFiles[count-1].getEntities().iterator();
		    		while(entityItOld.hasNext()){
		    			allEntities.add((String)entityItOld.next());
		    		}
		    		timestability = helpersum/allEntities.size();
			    	// Delete decimals
			    	int decimalPlace3 = 3;
			    	BigDecimal bd2 = new BigDecimal(timestability);
			    	bd2 = bd2.setScale(decimalPlace3,BigDecimal.ROUND_HALF_UP);
			    	timestability = bd2.doubleValue();
			    	
			    	exFile.setTimestability(timestability);
		    	}
		    	
		    	exFiles[count] = exFile;
		    	count = count+1;
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			// Analysis for extern=true
			Iterator fileItEXT = listOfFilesHashtagExtern.iterator();
			ExtractedInformationOfFile[] exFilesEXT = new ExtractedInformationOfFile[listOfFilesHashtagExtern.size()];
			int countEXT = 0;
			while(fileItEXT.hasNext()){
				try{
				String thisFile = fileToString((File) fileItEXT.next());
				ExtractedInformationOfFile exFileEXT = new ExtractedInformationOfFile();
		    	// Find all DBPedia links in current document
		    	List<String> entitiesEXT = new ArrayList<String>();
		    	String[] links = getStringBetweenString(thisFile, "<rdfs:seeAlso xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" rdf:resource=\"", "\"/>");
		    	for(String link:links){
		    		entitiesEXT.add(link);
		    	}
		    	exFileEXT.setEntities(entitiesEXT);
		    	
		    	// Calculate time stability
		    	if(countEXT==0){
		    		exFileEXT.setTimestability(1);
		    	}else{
		    		// Compare to previous document
		    		double timestability = 0.0;
		    		double helpersum = 0;
		    		HashSet<String> allEntities = new HashSet();
		    		Iterator entityIt =  entitiesEXT.iterator();
		    		while(entityIt.hasNext()){
		    			String thisnext = (String)entityIt.next();
		    			allEntities.add(thisnext);
		    			for(String element : exFilesEXT[countEXT-1].getEntities()){
		    				if(element.equals(thisnext)){
		    					helpersum = helpersum+1;
		    				}
		    			}
		    		}
		    		Iterator entityItOld =  exFilesEXT[countEXT-1].getEntities().iterator();
		    		while(entityItOld.hasNext()){
		    			allEntities.add((String)entityItOld.next());
		    		}
		    		timestability = helpersum/allEntities.size();
			    	// Delete decimals
			    	int decimalPlace3 = 3;
			    	BigDecimal bd2 = new BigDecimal(timestability);
			    	bd2 = bd2.setScale(decimalPlace3,BigDecimal.ROUND_HALF_UP);
			    	timestability = bd2.doubleValue();
			    	
			    	exFileEXT.setTimestability(timestability);
		    	}
		    	
		    	exFilesEXT[countEXT] = exFileEXT;
		    	countEXT = countEXT+1;
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			// Save relevant information in Hashtag object
			Hashtag hashtag = new Hashtag();
			double sumavgtimebetweentweets = 0;
			double squaresumavgtimebetweentweets = 0;
			double sumtimestability = 0;
			double squaresumtimestability = 0;
			List<String> entitiesUsed = new ArrayList();
			List<Entity> entitiesProb = new ArrayList();
			for(ExtractedInformationOfFile item:exFiles){
				sumavgtimebetweentweets = sumavgtimebetweentweets + item.getAvgtimebetweentweets();
				squaresumavgtimebetweentweets = squaresumavgtimebetweentweets + item.getAvgtimebetweentweets()*item.getAvgtimebetweentweets();
				sumtimestability = sumtimestability + item.getTimestability();
				squaresumtimestability = squaresumtimestability + item.getTimestability()*item.getTimestability();
				for(String ent : item.getEntities()){
					if(entitiesUsed!=null){
					if(entitiesUsed.contains(ent)){
						int index = entitiesUsed.indexOf(ent);
						Entity ent1 = entitiesProb.get(index);
						ent1.setProb(ent1.getProb()+(1/Double.valueOf(exFiles.length)));
						entitiesProb.set(index, ent1);
					}else{
						entitiesUsed.add(ent);
						Entity ent2 = new Entity();
						ent2.setName(ent);
						ent2.setProb((1/Double.valueOf(exFiles.length)));
						entitiesProb.add(ent2);
					}}
					else{
						entitiesUsed.add(ent);
						Entity ent3 = new Entity();
						ent3.setProb((1/Double.valueOf(exFiles.length)));
						entitiesProb.add(ent3);
					}
				}
			}
			
			hashtag.setEntities(entitiesProb);
			hashtag.setAvgavgtimebetweentweets(sumavgtimebetweentweets/exFiles.length);
			hashtag.setVaravgtimebetweentweets(squaresumavgtimebetweentweets-(sumavgtimebetweentweets/exFiles.length)*(sumavgtimebetweentweets/exFiles.length));
			hashtag.setAvgtimestability(sumtimestability/exFiles.length);
			hashtag.setVartimestability(squaresumtimestability-(sumtimestability/exFiles.length)*(sumtimestability/exFiles.length));
			double sumtimestabilityEXT = 0;
			double squaresumtimestabilityEXT = 0;
			List<String> entitiesUsedEXT = new ArrayList();
			List<Entity> entitiesProbEXT = new ArrayList();
			for(ExtractedInformationOfFile item:exFilesEXT){
	            sumtimestabilityEXT = sumtimestabilityEXT + item.getTimestability();
				squaresumtimestabilityEXT = squaresumtimestabilityEXT + item.getTimestability()*item.getTimestability();
				for(String ent : item.getEntities()){
					if(entitiesUsedEXT!=null){
					if(entitiesUsedEXT.contains(ent)){
						int index = entitiesUsedEXT.indexOf(ent);
						Entity ent1 = entitiesProbEXT.get(index);
						ent1.setProb(ent1.getProb()+(1/Double.valueOf(exFilesEXT.length)));
						ent1.setName(ent);
						entitiesProbEXT.set(index, ent1);
					}else{
						entitiesUsedEXT.add(ent);
						Entity ent2 = new Entity();
						ent2.setProb((1/Double.valueOf(exFilesEXT.length)));
						ent2.setName(ent);
						entitiesProbEXT.add(ent2);
					}}
					else{
						entitiesUsedEXT.add(ent);
						Entity ent3 = new Entity();
						ent3.setProb((1/Double.valueOf(exFilesEXT.length)));
						ent3.setName(ent);
						entitiesProbEXT.add(ent3);
					}
				}
			}
			hashtag.setEntitiesEXT(entitiesProbEXT);
			hashtag.setAvgtimestabilityEXT(sumtimestabilityEXT/exFilesEXT.length);
			hashtag.setVartimestabilityEXT(squaresumtimestabilityEXT-(sumtimestabilityEXT/exFilesEXT.length)*(sumtimestabilityEXT/exFilesEXT.length));
			hashtag.setName(uri[i]);
			hashtags.add(hashtag);
		}
		StringBuffer csvString = new StringBuffer();
		csvString.append("Hashtag;E(aut);VAR(aut);E(ts);VAR(ts);E(ts)[EXT];VAR(ts)[EXT];");
		csvString.append("Entity 1; sts(Entity 1);Entity 2; sts(Entity 2);Entity 3; sts(Entity 3);Entity 4; sts(Entity 4);Entity 5; sts(Entity 5);");
		csvString.append("Entity 1[EXT]; sts(Entity 1)[EXT];Entity 2[EXT]; sts(Entity 2)[EXT];Entity 3[EXT]; sts(Entity 3)[EXT];Entity 4[EXT]; sts(Entity 4)[EXT];Entity 5[EXT]; sts(Entity 5)[EXT];");
		csvString.append("\n");
		for(Hashtag htag : hashtags){
			csvString.append(htag.getName()+";");
			csvString.append(htag.getAvgavgtimebetweentweets()/1000+";");
			csvString.append(htag.getVaravgtimebetweentweets()/(1000*1000)+";");
			csvString.append(htag.getAvgtimestability()+";");
			csvString.append(htag.getVartimestability()+";");
			csvString.append(htag.getAvgtimestabilityEXT()+";");
			csvString.append(htag.getVartimestabilityEXT()+";");
			// Find Top entities
			List<Entity> init = new ArrayList(htag.getEntities());
			for(int v =0;v<5;v++){
			Entity highest = new Entity();
			highest.setName("0");
			highest.setProb(0);
			List<Entity> init2 = new ArrayList(init);
			for(int x = 0; x<init2.size();x++){
				Entity next = init2.get(x);
				if(next.getProb()>highest.getProb()){
					highest.setName(next.getName());
					highest.setProb(next.getProb());
					init.remove(next);
				}
			}
			if(highest.getProb()>0){
				csvString.append(highest.getName()+";"+highest.getProb()+";");
			}
			}
			// Find Top entities Extern
			List<Entity> initEXT = new ArrayList(htag.getEntitiesEXT());
			for(int v =0;v<5;v++){
			Entity highest = new Entity();
			highest.setName("0");
			highest.setProb(0);
			List<Entity> initEXT2 = new ArrayList(initEXT);
			for(int x = 0; x<initEXT2.size();x++){
				Entity next = initEXT2.get(x);
				if(next.getProb()>highest.getProb()){
					highest.setName(next.getName());
					highest.setProb(next.getProb());
					initEXT.remove(next);
				}
			}
			if(highest.getProb()>0){
				csvString.append(highest.getName()+";"+highest.getProb()+";");
			}
			}
		csvString.replace(csvString.lastIndexOf(";"), csvString.lastIndexOf(";")+1, "");
		csvString.append("\n");
		}

    	BufferedWriter out = new BufferedWriter(new FileWriter(dir.getCanonicalPath() + "/analysis/analysis.csv"));
    	out.write(csvString.toString().replace(".", ","));
    	out.close();
		
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


