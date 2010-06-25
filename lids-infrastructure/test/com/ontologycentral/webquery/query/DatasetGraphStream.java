package com.ontologycentral.webquery.query;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.util.iterator.NullIterator;

public class DatasetGraphStream implements DatasetGraph {
	Graph _g;
	
	public DatasetGraphStream(GraphStream g) {
		_g = g;
	}

	public void close() {
		_g.close();	
	}

	public boolean containsGraph(Node graphNode) {
		return false;
	}

	public Graph getDefaultGraph() {
		return _g;
	}

	public Graph getGraph(Node graphNode) {
		return null;
	}

	public Lock getLock() {
		return null;
	}

	public Iterator<Node> listGraphNodes() {
		return NullIterator.instance ();
	}

	public long size() {
		return 0;
	}

	@Override
	public void add(Quad arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addGraph(Node arg0, Graph arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean contains(Quad arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(Node arg0, Node arg1, Node arg2, Node arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void delete(Quad arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteAny(Node arg0, Node arg1, Node arg2, Node arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Iterator<Quad> find(Quad arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Quad> find(Node arg0, Node arg1, Node arg2, Node arg3) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Context getContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeGraph(Node arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDefaultGraph(Graph arg0) {
		// TODO Auto-generated method stub
		
	}

	/*
	public void add(Quad arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addGraph(Node arg0, Graph arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean contains(Quad arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(Node arg0, Node arg1, Node arg2, Node arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void delete(Quad arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteAny(Node arg0, Node arg1, Node arg2, Node arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Iterator<Quad> find(Quad arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Quad> find(Node arg0, Node arg1, Node arg2, Node arg3) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Context getContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeGraph(Node arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDefaultGraph(Graph arg0) {
		// TODO Auto-generated method stub
		
	}
	*/
}
