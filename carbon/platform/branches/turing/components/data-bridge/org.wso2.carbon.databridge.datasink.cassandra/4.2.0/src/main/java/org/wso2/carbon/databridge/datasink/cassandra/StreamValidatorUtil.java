/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.databridge.datasink.cassandra;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.core.internal.utils.DataBridgeConstants;
import org.wso2.carbon.databridge.datasink.cassandra.internal.util.DataSinkConstants;
import org.wso2.carbon.utils.ServerConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.ArrayList;

public class StreamValidatorUtil {

    private static final Log log = LogFactory.getLog(StreamValidatorUtil.class);

    private static OMElement configOmElement = null;

    public boolean isStreamValid(String streamName, String streamVersion) throws DataBridgeException {
        //OMElement configOmElement = loadConfigXML();
        setConfiguration();
        String[] expressions;
        String streamExpression = streamName + ":" + streamVersion ;
        boolean allStreamsAllowed = false;
        ArrayList<String> allowedStreams = new ArrayList<String>();
        ArrayList<String> forbiddenStreams = new ArrayList<String>();
        String validationString = getCassandraStreamValidationString(configOmElement);
        if(validationString != null && !"".equals(validationString)) {
            expressions = validationString.split(";");
            if(expressions.length != 0) {
                for (String expression : expressions) {
                    if("*".equals(expression.trim())) {
                        allStreamsAllowed = true;
                    } else if (expression.trim().startsWith("!")) {
                        forbiddenStreams.add(expression.trim().substring(1));
                    } else {
                        allowedStreams.add(expression.trim());
                    }
                }
            } else {
                return false;
            }
            if(allStreamsAllowed) {
                for (String forbiddenStream : forbiddenStreams) {
                    if(this.streamMatchesExpression(streamExpression, forbiddenStream)) {
                        return false;
                    }
                }
                return true;
            } else {
                for (String allowedStream : allowedStreams) {
                    if(this.streamMatchesExpression(streamExpression, allowedStream)) {
                        return true;
                    }
                }
                return false;
            }
        } else {
            return false;
        }
    }
    
    private boolean streamMatchesExpression (String streamExpression, String stream) {
        if(streamExpression != null && stream != null && streamExpression.split(":") != null && stream.split(":") != null) {
            if( stream.split(":").length == 2 ) { // stream name and version both exists
                return streamExpression.equals(stream);
            } else if (stream.split(":").length == 1 ) { // only stream name exists
                return streamExpression.split(":")[0].equals(stream.split(":")[0]);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static String getCassandraStreamValidationString(OMElement configOmElement) throws DataBridgeException{
        /*OMElement cassandraDataSinkConfig = configOmElement.getFirstChildWithName(
                new QName(DataSinkConstants.DATA_SINK_NAMESPACE, DataSinkConstants.DATA_SINK_VALID_STREAMS));
        if (cassandraDataSinkConfig != null) {*/
            try {
                OMElement validStreams = configOmElement.getFirstChildWithName(
                        new QName(DataSinkConstants.DATA_SINK_NAMESPACE, DataSinkConstants.DATA_SINK_VALID_STREAMS));
                if(validStreams != null) {
                    return validStreams.getText().trim();
                } else {
                    return null;
                }
            } catch (NumberFormatException ignored) {
                String errorMsg = "Error while getting Cassandra Data Sink Configuration:\n" + ignored.getMessage();
                log.error(errorMsg, ignored);
                throw new DataBridgeException(errorMsg, ignored);
            }
        /*} else {
            return null;
        }*/
    }

    private synchronized static void setConfiguration() throws DataBridgeException{
        if(configOmElement == null) {
            configOmElement = loadConfigXML();
        }
    }

    private static OMElement loadConfigXML() throws DataBridgeException {
        String carbonHome = System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH);
        String path = carbonHome + File.separator + DataBridgeConstants.DATA_BRIDGE_DIR +
                File.separator + DataSinkConstants.DATA_SINK_CONFIG_XML;
        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(new File(path)));
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            OMElement omElement = builder.getDocumentElement();
            omElement.build();
            return omElement;
        } catch (FileNotFoundException e) {
            String errorMessage = DataSinkConstants.DATA_SINK_CONFIG_XML
                    + "cannot be found in the path : " + path;
            log.error(errorMessage, e);
            throw new DataBridgeException(errorMessage, e);
        } catch (XMLStreamException e) {
            String errorMessage = "Invalid XML for " + DataSinkConstants.DATA_SINK_CONFIG_XML
                    + " located in the path : " + path;
            log.error(errorMessage, e);
            throw new DataBridgeException(errorMessage, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                String errorMessage = "Can not close the input stream";
                log.error(errorMessage, e);
            }
        }
    }

}
