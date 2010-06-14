package org.openlids.model;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author ssp
 */
public class IRI extends Value {
    public IRI(String string) {
    	this.setName(string);
	}
    
    public IRI() {
    	
    }

	@Override
    public String toString() {
        return "<" + getName() + ">";
    }

    @Override
    boolean isMappableTo(Value subject) {
        if(this.equals(subject))
            return true;
        if ((subject instanceof Variable)
                ||
            (subject instanceof BNode)) {
            return true;
        }
        return false;
    }
}
