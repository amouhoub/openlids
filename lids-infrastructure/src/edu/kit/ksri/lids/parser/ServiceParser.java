package edu.kit.ksri.lids.parser;

import edu.kit.ksri.lids.model.ServiceDescription;

public interface ServiceParser {
	public ServiceDescription parseServiceDescription(String serviceDescriptionTxt);
}
