/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rahas;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;

public class TempIssuer implements TokenIssuer {

    /* (non-Javadoc)
     * @see org.apache.rahas.TokenIssuer#setConfigurationFile(java.lang.String)
     */
    public void setConfigurationFile(String configFile) {
    }

    /* (non-Javadoc)
     * @see org.apache.rahas.TokenIssuer#setConfigurationElement(org.apache.axiom.om.OMElement)
     */
    public void setConfigurationElement(OMElement configElement) {
    }

    /* (non-Javadoc)
     * @see org.apache.rahas.TokenIssuer#setConfigurationParamName(java.lang.String)
     */
    public void setConfigurationParamName(String configParamName) {
    }

    /* (non-Javadoc)
     * @see org.apache.rahas.TokenIssuer#issue(org.apache.rahas.RahasData)
     */
    public SOAPEnvelope issue(RahasData data) throws TrustException {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    /* (non-Javadoc)
     * @see org.apache.rahas.TokenIssuer#getResponseAction(org.apache.rahas.RahasData)
     */
    public String getResponseAction(RahasData data) throws TrustException {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

}
