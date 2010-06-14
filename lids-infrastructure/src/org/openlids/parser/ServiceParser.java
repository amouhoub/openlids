package org.openlids.parser;

import org.openlids.model.ServiceDescription;

public interface ServiceParser {
	public ServiceDescription parseServiceDescription(String serviceDescriptionTxt);
}
