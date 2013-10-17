/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.connector.twilio.conference;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.core.util.ConnectorUtils;
import org.wso2.carbon.connector.twilio.util.TwilioUtil;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.resource.instance.Conference;
import com.twilio.sdk.resource.instance.Participant;

/*
 * Class mediator for kick a participant from a given conference.
 * For more information, seehttp://www.twilio.com/docs/api/rest/participant
 */
public class RemoveParticipant extends AbstractConnector {

	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);
		log.auditLog("Start: remove participant");

		String conferenceSid =
		                       (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                   TwilioUtil.PARAM_CONFERENCE_SID);
		String callSidOfParticipant =
		                              (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                          TwilioUtil.PARAM_CALL_SID);

		try {
			TwilioRestClient twilioRestClient = TwilioUtil.getTwilioRestClient(messageContext);

			// Get the conference
			Conference conference = twilioRestClient.getAccount().getConference(conferenceSid);

			// Get the participant by his call sid.
			// Refer https://www.twilio.com/docs/api/rest/participant
			Participant participant = conference.getParticipant(callSidOfParticipant);
			// kick the participant
			OMElement omResponse = null;
			if (participant.kick()) {
				omResponse = TwilioUtil.parseResponse("participant.delete.success");
			} else {
				omResponse = TwilioUtil.parseResponse("participant.delete.fail");
			}
			TwilioUtil.addElement(omResponse, TwilioUtil.PARAM_CONFERENCE_SID, conference.getSid());
			TwilioUtil.addElement(omResponse, TwilioUtil.PARAM_CALL_SID, participant.getCallSid());
			TwilioUtil.preparePayload(messageContext, omResponse);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new SynapseException(e);
		}
		log.auditLog("End: remove participant");
	}
}