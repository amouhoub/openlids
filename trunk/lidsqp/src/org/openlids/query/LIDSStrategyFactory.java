package org.openlids.query;

import java.util.Set;

import org.openlids.model.ServiceDescription;

public abstract class LIDSStrategyFactory {

	public abstract LIDSStrategy createLIDSStrategy(QueryObj q, QueryExecutor qe, Set<ServiceDescription> services);
	
}
