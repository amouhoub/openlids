package org.openlids.query.arq;

import java.util.Iterator;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.sparql.core.DatasetGraph;

public class DatasetWeb implements Dataset {
	DatasetGraph _dg;
	
	public DatasetWeb(DatasetGraph dg) {
		_dg = dg;
	}
	
	public DatasetGraph asDatasetGraph() {
		return _dg;
	}

	public void close() {
		_dg.close();
	}

	public boolean containsNamedModel(String uri) {
		return false;
	}

	public Model getDefaultModel() {
		return null;
	}

	public Lock getLock() {
		return null;
	}

	public Model getNamedModel(String uri) {
		return null;
	}

	public Iterator<String> listNames() {
		return null;
	}

}
