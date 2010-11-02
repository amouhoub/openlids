/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.model.parser;

import org.openlids.model.LIDSDescription;

/**
 *
 * @author ssp
 */
public interface LIDSParser {
    public LIDSDescription parseLIDSDescription(String lidsDescriptionStr);
}
