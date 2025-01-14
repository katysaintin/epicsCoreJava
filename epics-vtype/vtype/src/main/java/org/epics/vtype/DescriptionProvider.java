/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

public interface DescriptionProvider {
    
    /**
     * Human-readable description of the underlying data, e.g. the DESC field of an EPICS record.
     * @return description, or <code>null</code> if not set.
     */
    public String getDescription();

}
