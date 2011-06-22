package com.ontologycentral.twittersearchwrap;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;

import org.mozilla.intl.chardet.HtmlCharsetDetector;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
import org.mozilla.intl.chardet.nsPSMDetector;



public class SubjectExtraction {
	
	//test
	public static void main(String[] args){
		extract("http://www.kit.edu/");
	}
	public static boolean found   = false;
	public static String websiteCharset  = "";
	
	//follows a link to a website and returns subjects describing the website
	public static String[] extract(String website){
		
		StringBuffer websiteSubjects = new StringBuffer();
		
			BufferedReader extContent = null;
			String extResponse = "";
			String encoded = "UTF-8";
			
			try {
				//create the HttpURLConnection
				URL url = new URL(website);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				extContent = null;
				connection.setRequestMethod("GET");
				connection.setReadTimeout(10000);
				connection.connect();

				//read the output from the server
				//get charset
				charsetDetector(website);
				if (!websiteCharset.equals("")){
					encoded = websiteCharset;
				}
				extContent = new BufferedReader(new InputStreamReader(connection.getInputStream(),encoded));
				extResponse = connection.getResponseMessage();
				


			} catch (Exception e) {
				System.out.println("ERROR connecting to " + website);
				e.printStackTrace();
			}
		
			if (extResponse.equals("OK") && extContent != null) {
				System.out.println("EXTERNAL URL : " + website);
				
				String websiteString = "";
				String line          = "";
				
				try {
					while ((line = extContent.readLine()) != null)
					{
						websiteString += line;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				//use img alt descriptions as website subjects
				String[] websiteAlts       = websiteString.split("alt=\"");
				for (int i = 1; i < websiteAlts.length; i++) {
					String[] split = websiteAlts[i].split("\"");
					String subject = split[0];
					if (!subject.equals(" ") && !subject.equals("")) {
						websiteSubjects.append(subject);
						websiteSubjects.append(";;");
					}
				}
				
				
				//use text in links as website subjects
				//remove empty links
				websiteString = websiteString.replace("></a>", "");
				
				//split
				String[] websiteLinks        = websiteString.split("</a>");
				
				for (int i = 0; i < websiteLinks.length - 1; i++) {
					String[] split = websiteLinks[i].split(">");
					String subject = split[split.length - 1];
					if (!subject.equals(" ") && !subject.equals("")) {
						websiteSubjects.append(subject);
						websiteSubjects.append(";;");
					}
				}
			}
			
			
		String websiteSubjectsString = websiteSubjects.toString().replaceAll(";;\\s*;;", "");
		String[] subjects = websiteSubjectsString.split(";;");
		
		System.out.println(websiteSubjectsString);
		
		return subjects;

	}
	
	
	public static void charsetDetector(String website) throws Exception{
    // Initalize the nsDetector() ;
    int lang = nsPSMDetector.ALL ;
    nsDetector det = new nsDetector(lang) ;

    // Set an observer...
    // The Notify() will be called when a matching charset is found.

    det.Init(new nsICharsetDetectionObserver() {
            public void Notify(String charset) {
                SubjectExtraction.found = true ;
                SubjectExtraction.websiteCharset = charset;
            }
    });

    URL url;
	url = new URL(website);
    BufferedInputStream imp;
	imp = new BufferedInputStream(url.openStream());


    byte[] buf = new byte[1024] ;
    int len;
    boolean done = false ;
    boolean isAscii = true ;

    try {
		while( (len=imp.read(buf,0,buf.length)) != -1) {

		        // Check if the stream is only ascii.
		        if (isAscii)
		            isAscii = det.isAscii(buf,len);

		        // DoIt if non-ascii and not done yet.
		        if (!isAscii && !done)
		            done = det.DoIt(buf,len, false);
		}
	} catch (IOException e) {
		e.printStackTrace();
	}
    det.DataEnd();

    if (isAscii) {
       SubjectExtraction.websiteCharset = "ASCII";
       SubjectExtraction.found = true ;
    }
	
	}
	
}
