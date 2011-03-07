package analysis;

import java.util.List;

public class ExtractedInformationOfFile {
int id;
List<String> dbpedialinks;
double avgtimebetweentweets;

public int getId() {
	return id;
}
public void setId(int id) {
	this.id = id;
}
public List<String> getDbpedialinks() {
	return dbpedialinks;
}
public void setDbpedialinks(List<String> dbpedialinks) {
	this.dbpedialinks = dbpedialinks;
}
public double getAvgtimebetweentweets() {
	return avgtimebetweentweets;
}
public void setAvgtimebetweentweets(double avgtimebetweentweets) {
	this.avgtimebetweentweets = avgtimebetweentweets;
}

}
