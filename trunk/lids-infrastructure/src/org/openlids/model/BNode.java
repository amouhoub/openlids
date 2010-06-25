package org.openlids.model;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author ssp
 */
public class BNode extends Value {

	public BNode() {
		super();
	}
    public BNode(String string) {
    	super();
    	setName(string);
	}

	@Override
    public String toString() {
        return "_:" + getName();
    }

    @Override
    boolean isMappableTo(Value subject) {
        if ((subject instanceof Variable) ||
            (subject instanceof BNode) ||
            (subject instanceof IRI)) {
            return true;
        }
        return false;
    }
}
