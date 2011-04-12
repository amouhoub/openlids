package com.ontologycentral.analysis;

import java.util.List;

public class Hashtag {
	String name;
	List<ExtractedInformationOfFile> files;
	double avgavgtimebetweentweets;
	double varavgtimebetweentweets;
	double avgtimestability;
	double avgtimestabilityEXT;
	double vartimestability;
	double vartimestabilityEXT;
	int n;
	int nEXT;
	List<Entity> entities;
	List<Entity> entitiesEXT;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<ExtractedInformationOfFile> getFiles() {
		return files;
	}
	public void setFiles(List<ExtractedInformationOfFile> files) {
		this.files = files;
	}
	public double getAvgavgtimebetweentweets() {
		return avgavgtimebetweentweets;
	}
	public void setAvgavgtimebetweentweets(double avgavgtimebetweentweets) {
		this.avgavgtimebetweentweets = avgavgtimebetweentweets;
	}
	public double getAvgtimestability() {
		return avgtimestability;
	}
	public void setAvgtimestability(double avgtimestability) {
		this.avgtimestability = avgtimestability;
	}
	public List<Entity> getEntities() {
		return entities;
	}
	public void setEntities(List<Entity> entities) {
		this.entities = entities;
	}
	public double getAvgtimestabilityEXT() {
		return avgtimestabilityEXT;
	}
	public void setAvgtimestabilityEXT(double avgtimestabilityEXT) {
		this.avgtimestabilityEXT = avgtimestabilityEXT;
	}
	public List<Entity> getEntitiesEXT() {
		return entitiesEXT;
	}
	public void setEntitiesEXT(List<Entity> entitiesEXT) {
		this.entitiesEXT = entitiesEXT;
	}
	public double getVartimestability() {
		return vartimestability;
	}
	public void setVartimestability(double vartimestability) {
		this.vartimestability = vartimestability;
	}
	public double getVartimestabilityEXT() {
		return vartimestabilityEXT;
	}
	public void setVartimestabilityEXT(double vartimestabilityEXT) {
		this.vartimestabilityEXT = vartimestabilityEXT;
	}
	public double getVaravgtimebetweentweets() {
		return varavgtimebetweentweets;
	}
	public void setVaravgtimebetweentweets(double varavgtimebetweentweets) {
		this.varavgtimebetweentweets = varavgtimebetweentweets;
	}
	public int getN() {
		return n;
	}
	public void setN(int n) {
		this.n = n;
	}
	public int getnEXT() {
		return nEXT;
	}
	public void setnEXT(int nEXT) {
		this.nEXT = nEXT;
	}
}
