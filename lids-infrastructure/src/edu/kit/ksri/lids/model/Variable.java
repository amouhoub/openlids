package edu.kit.ksri.lids.model;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author ssp
 */
public class Variable extends Value {

    public Variable(String string) {
    	setName(string);
	}

	public Variable() {
		
	}

	@Override
    public String toString() {
        return "?" + getName();
    }

    @Override
    boolean isMappableTo(Value subject) {
        return true;
    }
}
