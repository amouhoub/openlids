package org.openlids.model;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;



public class ServiceDescription {
	// Set<Variable> exposedVars;
	Variable exposedVar;
    Set<BGP> input = new HashSet<BGP>();
    Set<BGP> output = new HashSet<BGP>();
    Set<Variable> requiredVars = new HashSet<Variable>();
    Set<Variable> variables = new HashSet<Variable>();
    Map<Variable,List<Variable>> boundBy = new HashMap<Variable,List<Variable>>();
    Map<Variable,List<BGP>> provides = new HashMap<Variable,List<BGP>>();
    IRI endpoint;
    
    boolean analyzed = false;
	private Map<Variable, List<BGP>> boundByExp = new HashMap<Variable,List<BGP>>();
    
    public void addInputBGP(BGP bgp) {
    	input.add(bgp);
    }
    
    public void addOutputBGP(BGP bgp) {
    	output.add(bgp);
    }
    
    public Set<BGP> getOutputBGP() {
    	return output;
    }
    
    private void analyze() {
    	if(analyzed) return;
    
    	Set<Variable> inputVars = new HashSet<Variable>();
    	
    	for(BGP bgpI : input) {
    		
    		if(bgpI.getObject() instanceof Variable) {
    			inputVars.add((Variable) bgpI.getObject());
    			requiredVars.add((Variable) bgpI.getObject());
    		}
    	}
    	for(BGP bgpI : input) {
    		if(bgpI.getSubject() instanceof Variable) {
    			inputVars.add((Variable) bgpI.getSubject());
    			requiredVars.remove((Variable) bgpI.getSubject());
    		}
    		
    	}
    	variables.addAll(inputVars);
    	for(BGP bgpO : output) {
    		if(bgpO.getSubject() instanceof Variable) {
    			// variables.add((Variable) bgpO.getSubject());
    			if(inputVars.contains((Variable) bgpO.getSubject())) exposedVar = (Variable) bgpO.getSubject();
    		}
    		
    		if(bgpO.getObject() instanceof Variable) {
    			// variables.add((Variable) bgpO.getObject());	
    			if(inputVars.contains((Variable) bgpO.getObject())) exposedVar = (Variable) bgpO.getObject();
    		}
    	}
    	
    	for(Variable v : variables) {
    		List<Variable> bindingList = new LinkedList<Variable>();
    		boundBy.put(v, bindingList);
    		List<BGP> providingList = new LinkedList<BGP>();
    		provides.put(v, providingList);
    		List<BGP> bindingExpList = new LinkedList<BGP>();
    		boundByExp.put(v, bindingExpList);
    		for(BGP bgp : input) {
    			if(bgp.getObject().equals(v)) {
    				if(bgp.getSubject() instanceof Variable) {
    					bindingList.add((Variable) bgp.getSubject());
    					bindingExpList.add(bgp);
    				}
    			}
    			if(bgp.getSubject().equals(v)) {
    				providingList.add(bgp);
    			}
    		}
    	}
    	
    	
    	analyzed = true;
    }
    
    
    public Variable getExposedVar() {
    	analyze();
    	return exposedVar;
    }
    
    public Set<Variable> getVariables() {
    	analyze();
    	return variables;
    }
    
    public Map<Variable,List<Variable>> getBoundBy() {
    	analyze();
    	return boundBy;
    }
    
    public List<Variable> getBoundBy(Variable v) {
    	analyze();
    	return boundBy.get(v);
    }
    
    public List<BGP> getBoundByExpressions(Variable exposedVar2) {
    	analyze();
    	return boundByExp .get(exposedVar2);
	}
    
    public Set<Variable> getRequiredVars() {
    	analyze();
    	return requiredVars;
    }
    
    public Map<Variable,List<BGP>> getProvides() {
    	analyze();
    	return provides;
    }
    
    public List<BGP> getProvides(Variable v) {
    	analyze();
    	return provides.get(v);
    }

	public void setEndpoint(IRI endpoint2) {
		this.endpoint = endpoint2;
	}
	
	public IRI getEndpoint() {
		return endpoint;
	}

	public Set<BGP> getInput() {
		return input;
	}

	public String makeURI(Map<Variable,Value> bindings) {
		if(!bindings.keySet().containsAll(getRequiredVars())) {
			return null;
			// not enough vars bound
		}
		String uri = getEndpoint().getName();
		try {
		if(getRequiredVars().size() == 1) {
			uri += "/" + URLEncoder.encode(bindings.get(getRequiredVars().iterator().next()).getName(),"UTF-8");
		} else {
			uri += "?";
			for(Variable v : getRequiredVars()) {
				uri += URLEncoder.encode(v.getName(),"UTF-8") + "=" + URLEncoder.encode(bindings.get(v).getName(),"UTF-8") + "&";
			}
			uri = uri.substring(0,uri.length()-1);
		}
		} catch(UnsupportedEncodingException e) {
			
		}
		uri += "#" + getExposedVar().getName();
		return uri;
	}
	
}
