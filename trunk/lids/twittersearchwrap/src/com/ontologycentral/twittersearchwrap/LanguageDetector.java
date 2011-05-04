package com.ontologycentral.twittersearchwrap;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

import com.lingway.ld.LangDetector;

public class LanguageDetector {

	private LangDetector langDetector;

	public LanguageDetector(File dir) throws FileNotFoundException, IOException{

			langDetector = new LangDetector();
			for( String filename : dir.list() ){
				if( !filename.endsWith(".bin")){
					continue;
				}
				ObjectInputStream input = new ObjectInputStream(
						new BufferedInputStream(
								new FileInputStream(dir + File.separator + filename)));
				String lang = filename.substring(0,filename.indexOf('_'));
				langDetector.register(lang, input);
			}
	}
	
	public String detectLanguage( String input ){
		return langDetector.detectLang(input, false);
	}

}
