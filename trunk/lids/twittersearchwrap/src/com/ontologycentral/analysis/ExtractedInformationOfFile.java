package analysis;

import java.util.List;

public class ExtractedInformationOfFile {
int id;
List<String> entities;
double avgtimebetweentweets;
double timestability;

public int getId() {
	return id;
}
public void setId(int id) {
	this.id = id;
}
public double getAvgtimebetweentweets() {
	return avgtimebetweentweets;
}
public void setAvgtimebetweentweets(double avgtimebetweentweets) {
	this.avgtimebetweentweets = avgtimebetweentweets;
}
public double getTimestability() {
	return timestability;
}
public void setTimestability(double timestability) {
	this.timestability = timestability;
}
public List<String> getEntities() {
	return entities;
}
public void setEntities(List<String> entities) {
	this.entities = entities;
}

}
