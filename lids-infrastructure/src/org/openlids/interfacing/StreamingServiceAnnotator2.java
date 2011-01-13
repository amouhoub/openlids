package org.openlids.interfacing;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;


import org.openlids.model.BGP;
import org.openlids.model.BNode;
import org.openlids.model.IRI;
import org.openlids.model.Literal;
import org.openlids.model.ServiceDescription;
import org.openlids.model.Value;
import org.openlids.model.Variable;
import org.openlids.parser.ServiceParser;
import org.openlids.parser.ServiceParserJena;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.parser.ParseException;
import org.semanticweb.yars2.rdfxml.RDFXMLParser;



public class StreamingServiceAnnotator2 implements Callback {
	
	List<ServiceDescription> descs = new LinkedList<ServiceDescription>();
	Map<String,Set<ServiceDescription>> fireDescs = new HashMap<String,Set<ServiceDescription>>();
	Map<String,Map<Value,Set<Value>>> bindings = new HashMap<String,Map<Value,Set<Value>>>();
	
//	Map<ServiceDescription,Map<Variable,Value>> sameAsEs = new HashMap<ServiceDescription,Map<Variable,Value>>();
	Set<Map<Variable,Value>> sameAsEs = new HashSet<Map<Variable,Value>>();
	private Map<ServiceDescription, List<BGP>> queryBGP = new HashMap<ServiceDescription,List<BGP>>();
	Map<Value,String> source = new HashMap<Value,String>();
    private long nrStmts = 0;
    private Set<String[]> sames = new HashSet<String[]>();

    public Set<String[]> getSames() {
        return sames;
    }
	
	public Set<Map<Variable,Value>> getSameAsEs() {
		return sameAsEs;
	}
	
	public StreamingServiceAnnotator2() {
	}
	
	public void addDescription(ServiceDescription desc) {
		// compile ...
		// check if there can be a input match beginning at exposedVar 
		Map<Variable,List<BGP>> provides = desc.getProvides();
		
		Set<BGP> treatedBGP = new HashSet<BGP>();
		Set<Variable> treatedVars = new HashSet<Variable>();
		
		Variable exposedVar = desc.getExposedVar();
		Queue<BGP> lookAt = new LinkedList<BGP>();
		lookAt.addAll(provides.get(exposedVar));
		while(!lookAt.isEmpty()) {
			BGP bgp = lookAt.poll();
			if(treatedBGP.contains(bgp))
				continue;
			treatedBGP.add(bgp);
			if(bgp.getObject() instanceof Variable) {
				treatedVars.add((Variable) bgp.getObject());
				lookAt.addAll(provides.get((Variable) bgp.getObject()));
			}
		}
		if(!treatedVars.containsAll(desc.getRequiredVars())) {
			System.err.println("Desc is not streamable: " + desc.getEndpoint());
			return;
		}
		// treated BGPs will be collected and fire to desc
		List<BGP> queryBGPs = new LinkedList<BGP>();
		for(BGP bgp : treatedBGP) {
			queryBGPs.add(bgp);
			Set<ServiceDescription> fires = fireDescs.get(bgp);
			if(fires == null) {
				fires = new HashSet<ServiceDescription>();
				fireDescs.put(bgp.getPredicate().toString(), fires);
			}
			fires.add(desc);
			
			Map<Value,Set<Value>> binding = bindings.get(bgp);
			if(binding == null) {
				binding = new HashMap<Value,Set<Value>>();
				bindings.put(bgp.getPredicate().toString(), binding);
			}
		}
		queryBGP.put(desc,queryBGPs);
		// sameAsEs.put(desc, new HashMap<Variable,Value>());
	}
	
	
	public void annotate(InputStream in, String baseURI, String lang) throws IOException {
		try {
			Iterator<Node[]> parser;
			if(lang.equals("RDF/XML")) {
				parser = new RDFXMLParser(in,false,true,baseURI,this);
			} else {
				parser = new NxParser(in,this);
			}
		} catch(ParseException pe) {
			throw new IOException(pe.toString());
		}
		
	}
	
	public void startDocument() {
	}
	
	public void endDocument() {

	}
	
	public void processStatement(Node[] nx) {
		nrStmts ++;
		if(nrStmts % 10000 == 0) {
			//System.err.println("Processed " + nrStmts + " Statements.\n");
			//System.err.println("Cleaning out the caches.");
			
			Map<String,Map<Value,Set<Value>>> newBindings = new HashMap<String,Map<Value,Set<Value>>>();
			for(String pred : bindings.keySet()) {
				newBindings.put(pred, new HashMap<Value,Set<Value>>());
			}
			bindings = newBindings;
			source = new HashMap<Value,String>();
			System.gc();
		}
                if(nx.length == 3) {
                    nx = new Node[] { null, nx[0], nx[1], nx[2] };
                }
		if(nx[1] instanceof org.semanticweb.yars.nx.Resource) {
			String pred = nx[1].toN3();
			Map<Value,Set<Value>> binding = bindings.get(pred);
			if(binding != null) {
				Value subj = node2Value(nx[0]);
				if(subj != null) {
					source.put(subj, nx[3].toN3());
				}
				Set<Value> vals = binding.get(subj);
				if(vals == null) {
					vals = new HashSet<Value>();
					binding.put(subj, vals);
				}
				vals.add(node2Value(nx[2]));
			} else {
				return;
			}
			Set<ServiceDescription> fires = fireDescs.get(pred);
			if(fires != null) {
				for(ServiceDescription desc : fires) {
					fire(desc);
				}
			}
		}
		
	}
	
	private Value node2Value(Node node) {
		if(node instanceof org.semanticweb.yars.nx.Resource) {
			return new IRI(node.toString());
		} else if(node instanceof org.semanticweb.yars.nx.BNode) {
			return new BNode(node.toString());
		} else if(node instanceof org.semanticweb.yars.nx.Literal) {
			return new Literal(node.toString());
		} else if(node instanceof org.semanticweb.yars.nx.Variable) {
			return new Variable(node.toString());
		}
		return null;
	}

	public void fire(ServiceDescription desc) {
		Variable exposedV = desc.getExposedVar();
	
		// find Bindings
		Variable[] headVars = new Variable[desc.getRequiredVars().size() + 1];
		headVars[0] = exposedV;
		Iterator<Variable> reqVars = desc.getRequiredVars().iterator();
		for(int i=1;i<headVars.length;i++) {
			headVars[i] = reqVars.next();
		}
		Collection<Value[]> bindings = execQuery(headVars,queryBGP.get(desc));
		for(Value[] res : bindings) {
			Map<Variable,Value> b = new HashMap<Variable,Value>();
			for(int i=0;i<headVars.length;i++) {
				b.put(headVars[i],res[i]);
			}
			// check if already available
			if(sameAsEs.contains(b)) {
				continue;
			}
			sameAsEs.add(b);
			// no ... so generate link

                        sames.add(new String[] { res[0].getName(), desc.makeURI(b) } );
			// System.out.println(res[0] + " <http://www.w3.org/2002/07/owl#sameAs> <" + desc.makeURI(b) + "> " + source.get(res[0]) + " ." );
		}
		
	}
	
	public Collection<Value[]> execQuery(Variable[] headVars, List<BGP> bgps) {
		return execQuery(headVars,bgps,new HashMap<Variable,Value>());
	}
	public Collection<Value[]> execQuery(Variable[] headVars, List<BGP> bgps, Map<Variable,Value> qbindings) {
		List<Value[]> ret = new LinkedList<Value[]>();
		
		if(bgps.size() == 0) {
			// return result
			Value[] result = new Value[headVars.length];
			int i = 0;
			for(Variable headV : headVars) {
				result[i] = qbindings.get(headV);
				if(result[i] == null) {
					break;
				}
				i++;
			}
			if(i == headVars.length) {
				ret.add(result);
			}
		} else {
    		// Take one bgp, evaluate, for each binding do the rest
			List<BGP> restBGPs = new LinkedList<BGP>();
			Iterator<BGP> it = bgps.iterator();
			BGP thisBGP = it.next();
			while(it.hasNext()) {
				restBGPs.add(it.next());
			}
			
			
			Value subj = thisBGP.getSubject();
			if(qbindings.containsKey(subj))
				subj = qbindings.get(subj);
			
			Value pred = thisBGP.getPredicate();
			if(qbindings.containsKey(pred))
				pred = qbindings.get(pred);
			Value obj = thisBGP.getObject();
			if(qbindings.containsKey(obj))
				obj = qbindings.get(obj);
			
			
			boolean sVar = (subj instanceof Variable);
			boolean pVar = (pred instanceof Variable);
			boolean oVar = (obj  instanceof Variable);
			
		
			Map<Value,Set<Value>> binding = bindings.get(pred.toString());
			if(binding == null)
				return ret;

			if(subj instanceof Variable) {
				for(Value newSubj : binding.keySet()) {
					for(Value newObj : binding.get(newSubj)) {
						Map<Variable,Value> newBindings = new HashMap<Variable,Value>();
						newBindings.putAll(qbindings);
						if(oVar) {
							newBindings.put((Variable) obj, newObj);
						}
						newBindings.put((Variable) subj, newSubj);
						Collection<Value[]> intermed = execQuery(headVars,restBGPs,newBindings);
						ret.addAll(intermed);
					}
				}
			} else {
				Set<Value> objs = binding.get(subj);
				if(objs == null) {
					return ret;
				}
			
				for(Value newObj : objs) {
					Map<Variable,Value> newBindings = new HashMap<Variable,Value>();
					newBindings.putAll(qbindings);
					if(oVar) {
						newBindings.put((Variable) obj, newObj);
					}
					Collection<Value[]> intermed = execQuery(headVars,restBGPs,newBindings);
					ret.addAll(intermed);
				}
			}
		}
		return ret;
	}

	public static void main(String args[]) {
		
		//args = new String[] { "/Users/ssp/No Time Machine/btc2010/btc-2010-chunk-000.nt", "/Users/ssp/No Time Machine/btc2010/geofindnearby.rq"};

		
		if(args.length < 2) {
			System.out.println("Usage: java -jar interlinker.jar inputfile servicedesc1 servicedesc2 ...");
			System.out.println("  inputfile:   source data set in ntriples format");
			System.out.println("  servicedesc: one or more LIDS descriptions, basically SPARQL files, that are used to interlink the input data with the described LIDS");
			System.exit(-1);
		}
		try {
			FileInputStream fin = new FileInputStream(args[0]);
						
			String sparqls[] = new String[args.length - 1];
			for(int i=0;i<sparqls.length;i++) {
				FileReader reader = new FileReader(args[i+1]);
				char buf[] = new char[(int) new File(args[i+1]).length()];
				int off = 0;
				int l = 0;
				while((l = reader.read(buf, off, buf.length - off)) > -1 && off < buf.length) {
					off += l;
				}
				sparqls[i] = new String(buf);
			}
			
			ServiceParser sp = new ServiceParserJena();
								
			StreamingServiceAnnotator2 sa = new StreamingServiceAnnotator2();
			// StreamingServiceAnnotator sas[] = new StreamingServiceAnnotator[sparqls.length];
			for(int i=0;i<sparqls.length;i++) {
				sa.addDescription(sp.parseServiceDescription(sparqls[i]));
				//sas[i] = new StreamingServiceAnnotator(sp.parseServiceDescription(sparqls[i]));
			}
			
//			Model m = ModelFactory.createDefaultModel();
//			m.read(fin, args[0], "N3");
			try {
				sa.annotate(new FileInputStream(args[0]), args[0], "N3");
			} catch(Exception e) {
				System.err.println("That went wrong!\n");
				e.printStackTrace();
			}
			
			
		} catch (FileNotFoundException e) {
			System.err.println("Could not read file: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

			
}
