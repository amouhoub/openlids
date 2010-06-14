package edu.kit.ksri.lids.model;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author ssp
 */
public class BGP {

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BGP other = (BGP) obj;
        if (this.subject != other.subject && (this.subject == null || !this.subject.equals(other.subject))) {
            return false;
        }
        if (this.predicate != other.predicate && (this.predicate == null || !this.predicate.equals(other.predicate))) {
            return false;
        }
        if (this.object != other.object && (this.object == null || !this.object.equals(other.object))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.subject != null ? this.subject.hashCode() : 0);
        hash = 59 * hash + (this.predicate != null ? this.predicate.hashCode() : 0);
        hash = 59 * hash + (this.object != null ? this.object.hashCode() : 0);
        return hash;
    }
    Value subject;

    public Value getObject() {
        return object;
    }

    public void setObject(Value object) {
        this.object = object;
    }

    public Value getPredicate() {
        return predicate;
    }

    public void setPredicate(Value predicate) {
        this.predicate = predicate;
    }

    public Value getSubject() {
        return subject;
    }

    public void setSubject(Value subject) {
        this.subject = subject;
    }
    Value predicate;
    Value object;

    public BGP(Value subject, Value predicate, Value object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(subject.toString()).append(" ").
                append(predicate.toString()).append(" ").
                append(object.toString());
        return sb.toString();
    }

}
