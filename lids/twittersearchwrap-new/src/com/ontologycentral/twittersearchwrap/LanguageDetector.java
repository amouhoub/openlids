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

	public static final String[] langFileNames = new String[]{"en_tree.bin","de_tree.bin","es_tree.bin","fr_tree.bin"};

	public LanguageDetector(File[] langFiles) throws FileNotFoundException, IOException{

			langDetector = new LangDetector();
			for( File langFile : langFiles ){
				ObjectInputStream input = new ObjectInputStream(
						new BufferedInputStream(
								new FileInputStream(langFile)));
				String filename = langFile.getName();
				String lang = filename.substring(0,filename.indexOf('_'));
				langDetector.register(lang, input);
			}
	}
	
	public String detectLanguage( String input ){
		return langDetector.detectLang(input, false);
	}

}
