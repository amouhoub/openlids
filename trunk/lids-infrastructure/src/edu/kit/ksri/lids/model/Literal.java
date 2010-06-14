package edu.kit.ksri.lids.model;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author ssp
 */
public class Literal extends Value {

    @Override
    public String toString() {
        return "\"" + getName() + "\"";
    }

    @Override
    boolean isMappableTo(Value subject) {
        if(this.equals(subject))
            return true;
        if ((subject instanceof Variable))
            return true;
        return false;
    }
}
