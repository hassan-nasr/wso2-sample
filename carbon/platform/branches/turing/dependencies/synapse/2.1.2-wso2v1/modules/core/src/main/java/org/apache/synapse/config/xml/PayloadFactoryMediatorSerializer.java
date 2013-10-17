/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.config.xml;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.synapse.Mediator;
import org.apache.synapse.mediators.transform.Argument;
import org.apache.synapse.mediators.transform.PayloadFactoryMediator;

import javax.xml.stream.XMLStreamException;
import java.util.List;

public class PayloadFactoryMediatorSerializer extends AbstractMediatorSerializer {

    private static final String PAYLOAD_FACTORY = "payloadFactory";
    private static final String FORMAT = "format";
    private static final String ARGS = "args";
    private static final String ARG = "arg";
    private static final String VALUE = "value";
    private static final String EXPRESSION = "expression";
    private static final String EVALUATOR = "evaluator";
    private final String JSON_TYPE="json";
    private final String MEDIA_TYPE="media-type";



    public OMElement serializeSpecificMediator(Mediator m) {

        if (!(m instanceof PayloadFactoryMediator)) {
            handleException("Unsupported mediator passed in for serialization : " + m.getType());
            return null;
        }

        PayloadFactoryMediator mediator = (PayloadFactoryMediator) m;

        OMElement payloadFactoryElem = fac.createOMElement(PAYLOAD_FACTORY, synNS);

        if(mediator.getType()!=null){


            payloadFactoryElem.addAttribute(fac.createOMAttribute(MEDIA_TYPE,null,mediator.getType()));

        }



        saveTracingState(payloadFactoryElem, mediator);
       /* if (mediator.isDynamic()) {
            payloadFactoryElem.addAttribute(fac.createOMAttribute(
                    "key", nullNS, mediator.getRegistryKey()));
        } else {    */
        if(!mediator.isFormatDynamic()){
            if (mediator.getFormat() != null) {

                try {
                    OMElement formatElem = fac.createOMElement(FORMAT, synNS);
                String type = mediator.getType();
                if(type!=null && type.contains(JSON_TYPE)) {
                     formatElem.setText(mediator.getFormat());
                } else{
                    formatElem.addChild(AXIOMUtil.stringToOM(mediator.getFormat()));
                }
                    payloadFactoryElem.addChild(formatElem);
                } catch (XMLStreamException e) {
                    handleException("Error while serializing payloadFactory mediator", e);
                }
            } else {
                handleException("Invalid payloadFactory mediator, format is required");
            }
        }else{

                // Serialize Value using ValueSerializer
            OMElement formatElem = fac.createOMElement(FORMAT, synNS);
            formatElem.addAttribute(fac.createOMAttribute(
                    "key", nullNS, mediator.getFormatKey().getKeyValue()));
                ValueSerializer keySerializer = new ValueSerializer();
                keySerializer.serializeValue(mediator.getFormatKey(), XMLConfigConstants.KEY, formatElem);
             payloadFactoryElem.addChild(formatElem);
        }

            /*List<Argument> argList = mediator.getArgumentList();

            if (argList != null && argList.size() > 0) {

                OMElement argumentsElem = fac.createOMElement(ARGS, synNS);

                for (Argument arg : argList) {

                    OMElement argElem = fac.createOMElement(ARG, synNS);

                    if (arg.getValue() != null) {
                        argElem.addAttribute(fac.createOMAttribute(VALUE, nullNS, arg.getValue()));
                    } else if (arg.getExpression() != null) {
                        SynapseXPathSerializer.serializeXPath(arg.getExpression(), argElem, EXPRESSION);
                    } else if (arg.getJsonPath()!=null){
                        argElem.addAttribute(fac.createOMAttribute(EXPRESSION, nullNS, arg.getJsonPath().getJsonPathExpression()));
                    }

                    if(arg.getEvaluator()!=null){
                        argElem.addAttribute(fac.createOMAttribute(EVALUATOR, nullNS, arg.getEvaluator()));
                    }

                    argumentsElem.addChild(argElem);
                }
                payloadFactoryElem.addChild(argumentsElem);
            }*/
        OMElement argumentsElem = fac.createOMElement(ARGS, synNS);
        List<Argument> xPathArgList = mediator.getXPathArgumentList();

        if (xPathArgList != null && xPathArgList.size() > 0) {

            for (Argument arg : xPathArgList) {
                OMElement argElem = fac.createOMElement(ARG, synNS);
                if(arg.getEvaluator()!=null){
                    argElem.addAttribute(fac.createOMAttribute(EVALUATOR, nullNS, arg.getEvaluator()));
                }
                if (arg.getValue() != null) {
                    argElem.addAttribute(fac.createOMAttribute(VALUE, nullNS, arg.getValue()));
                } else if (arg.getExpression() != null) {
                    SynapseXPathSerializer.serializeXPath(arg.getExpression(), argElem, EXPRESSION);
                }
                argumentsElem.addChild(argElem);
            }
        }

        List<Argument> jsonPathArgList = mediator.getJsonPathArgumentList();

        if (jsonPathArgList != null && jsonPathArgList.size() > 0) {

            for (Argument arg : jsonPathArgList) {
                OMElement argElem = fac.createOMElement(ARG, synNS);
                if(arg.getEvaluator()!=null){
                    argElem.addAttribute(fac.createOMAttribute(EVALUATOR, nullNS, arg.getEvaluator()));
                }
                if (arg.getJsonPath()!=null){
                    argElem.addAttribute(fac.createOMAttribute(EXPRESSION, nullNS, arg.getJsonPath().getJsonPathExpression()));
                    argumentsElem.addChild(argElem);
                }
            }
        }
        payloadFactoryElem.addChild(argumentsElem);
        return payloadFactoryElem;
    }

    public String getMediatorClassName() {
        return PayloadFactoryMediator.class.getName();
    }

}
