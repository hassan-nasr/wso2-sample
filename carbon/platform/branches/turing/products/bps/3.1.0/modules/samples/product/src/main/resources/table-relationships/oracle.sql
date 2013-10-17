--
-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements.  See the NOTICE file
-- distributed with this work for additional information
-- regarding copyright ownership.  The ASF licenses this file
-- to you under the Apache License, Version 2.0 (the
-- "License"); you may not use this file except in compliance
-- with the License.  You may obtain a copy of the License at
--
--    http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
-- KIND, either express or implied.  See the License for the
-- specific language governing permissions and limitations
-- under the License.
--

--
-- BPEL Related SQL Scripts
--


CREATE TABLE ODE_SCHEMA_VERSION (VERSION integer) 
/



-- Apache ODE - SimpleScheduler Database Schema
--
-- Apache Derby scripts by Maciej Szefler.
--
--

-- DROP TABLE ode_job;


CREATE TABLE ode_job (
  jobid VARCHAR(64)  NOT NULL,
  ts number(37)  NOT NULL,
  nodeid varchar(64),
  scheduled int  NOT NULL,
  transacted int  NOT NULL,
  instanceId number(37),
  mexId varchar(255),
  processId varchar(255),
  type varchar(255),
  channel varchar(255),
  correlatorId varchar(255),
  correlationKeySet varchar(255),
  retryCount int,
  inMem int,
  detailsExt blob,
  PRIMARY KEY(jobid)) 
/

CREATE INDEX IDX_ODE_JOB_TS ON ode_job(ts) 
/
CREATE INDEX IDX_ODE_JOB_NODEID ON ode_job(nodeid) 

/
CREATE TABLE TASK_ATTACHMENT (ATTACHMENT_ID NUMBER NOT NULL, MESSAGE_EXCHANGE_ID VARCHAR2(255), PRIMARY KEY (ATTACHMENT_ID)) 
/
CREATE TABLE ODE_PROCESS_INSTANCE (ID NUMBER NOT NULL, DATE_CREATED TIMESTAMP, EXECUTION_STATE BLOB, FAULT_ID NUMBER, LAST_ACTIVE_TIME TIMESTAMP, LAST_RECOVERY_DATE TIMESTAMP, PREVIOUS_STATE NUMBER, SEQUENCE NUMBER, INSTANCE_STATE NUMBER, INSTANTIATING_CORRELATOR_ID NUMBER, PROCESS_ID NUMBER, ROOT_SCOPE_ID NUMBER, PRIMARY KEY (ID)) 

/
CREATE TABLE ODE_SCOPE (SCOPE_ID NUMBER NOT NULL, MODEL_ID NUMBER, SCOPE_NAME VARCHAR2(255), SCOPE_STATE VARCHAR2(255), PROCESS_INSTANCE_ID NUMBER, PARENT_SCOPE_ID NUMBER, PRIMARY KEY (SCOPE_ID), FOREIGN KEY (PROCESS_INSTANCE_ID) REFERENCES ODE_PROCESS_INSTANCE(ID) ON DELETE CASCADE) 

/
CREATE TABLE ODE_PARTNER_LINK (PARTNER_LINK_ID NUMBER NOT NULL, MY_EPR CLOB, MY_ROLE_NAME VARCHAR2(255), MY_ROLE_SERVICE_NAME VARCHAR2(255), MY_SESSION_ID VARCHAR2(255), PARTNER_EPR CLOB, PARTNER_LINK_MODEL_ID NUMBER, PARTNER_LINK_NAME VARCHAR2(255), PARTNER_ROLE_NAME VARCHAR2(255), PARTNER_SESSION_ID VARCHAR2(255), SCOPE_ID NUMBER, PRIMARY KEY (PARTNER_LINK_ID), FOREIGN KEY (SCOPE_ID) REFERENCES ODE_SCOPE (SCOPE_ID) ON DELETE CASCADE) 

/
CREATE TABLE ODE_PROCESS (ID NUMBER NOT NULL, GUID VARCHAR2(255), PROCESS_ID VARCHAR2(255), PROCESS_TYPE VARCHAR2(255), VERSION NUMBER, PRIMARY KEY (ID)) 

/
CREATE TABLE ODE_CORRELATOR (CORRELATOR_ID NUMBER NOT NULL, CORRELATOR_KEY VARCHAR2(255), PROC_ID NUMBER, PRIMARY KEY (CORRELATOR_ID), FOREIGN KEY (PROC_ID) REFERENCES ODE_PROCESS (ID) ON DELETE CASCADE) 

/
CREATE TABLE ODE_MESSAGE_EXCHANGE (MESSAGE_EXCHANGE_ID VARCHAR2(255) NOT NULL, CALLEE VARCHAR2(255), CHANNEL VARCHAR2(255), CORRELATION_ID VARCHAR2(255), CORRELATION_KEYS VARCHAR2(255), CORRELATION_STATUS VARCHAR2(255), CREATE_TIME TIMESTAMP, DIRECTION NUMBER, EPR CLOB, FAULT VARCHAR2(255), FAULT_EXPLANATION VARCHAR2(255), OPERATION VARCHAR2(255), PARTNER_LINK_MODEL_ID NUMBER, PATTERN VARCHAR2(255), PIPED_ID VARCHAR2(255), PORT_TYPE VARCHAR2(255), PROPAGATE_TRANS NUMBER, STATUS VARCHAR2(255), SUBSCRIBER_COUNT NUMBER, CORR_ID NUMBER, PARTNER_LINK_ID NUMBER, PROCESS_ID NUMBER, PROCESS_INSTANCE_ID NUMBER, REQUEST_MESSAGE_ID NUMBER, RESPONSE_MESSAGE_ID NUMBER, PRIMARY KEY (MESSAGE_EXCHANGE_ID), FOREIGN KEY (PROCESS_INSTANCE_ID) REFERENCES ODE_PROCESS_INSTANCE(ID) ON DELETE CASCADE, FOREIGN KEY (PARTNER_LINK_ID) REFERENCES ODE_PARTNER_LINK (PARTNER_LINK_ID) ON DELETE CASCADE, FOREIGN KEY (PROCESS_ID) REFERENCES ODE_PROCESS (ID) ON DELETE CASCADE, FOREIGN KEY (CORR_ID) REFERENCES ODE_CORRELATOR (CORRELATOR_ID) ON DELETE CASCADE) 

/
CREATE TABLE ODE_MESSAGE (MESSAGE_ID NUMBER NOT NULL, DATA CLOB, HEADER CLOB, TYPE VARCHAR2(255), MESSAGE_EXCHANGE_ID VARCHAR2(255), PRIMARY KEY (MESSAGE_ID), FOREIGN KEY (MESSAGE_EXCHANGE_ID) REFERENCES ODE_MESSAGE_EXCHANGE (MESSAGE_EXCHANGE_ID) ON DELETE CASCADE) 

/
CREATE TABLE ODE_ACTIVITY_RECOVERY (ID NUMBER NOT NULL, ACTIONS VARCHAR2(255), ACTIVITY_ID NUMBER, CHANNEL VARCHAR2(255), DATE_TIME TIMESTAMP, DETAILS CLOB, INSTANCE_ID NUMBER, REASON VARCHAR2(255), RETRIES NUMBER, PRIMARY KEY (ID), FOREIGN KEY (INSTANCE_ID) REFERENCES ODE_PROCESS_INSTANCE(ID) ON DELETE CASCADE) 

/
CREATE TABLE ODE_CORRELATION_SET (CORRELATION_SET_ID NUMBER NOT NULL, CORRELATION_KEY VARCHAR2(255), NAME VARCHAR2(255), SCOPE_ID NUMBER, PRIMARY KEY (CORRELATION_SET_ID), FOREIGN KEY (SCOPE_ID) REFERENCES ODE_SCOPE (SCOPE_ID) ON DELETE CASCADE) 

/
CREATE TABLE ODE_CORSET_PROP (ID NUMBER NOT NULL, CORRSET_ID NUMBER, PROP_KEY VARCHAR2(255), PROP_VALUE VARCHAR2(255), PRIMARY KEY (ID), FOREIGN KEY (CORRSET_ID) REFERENCES ODE_CORRELATION_SET (CORRELATION_SET_ID) ON DELETE CASCADE) 

/
CREATE TABLE ODE_EVENT (EVENT_ID NUMBER NOT NULL, DETAIL VARCHAR2(255), DATA BLOB, SCOPE_ID NUMBER, TSTAMP TIMESTAMP, TYPE VARCHAR2(255), INSTANCE_ID NUMBER, PROCESS_ID NUMBER, PRIMARY KEY (EVENT_ID), FOREIGN KEY (INSTANCE_ID) REFERENCES ODE_PROCESS_INSTANCE(ID) ON DELETE CASCADE, FOREIGN KEY (SCOPE_ID) REFERENCES ODE_SCOPE (SCOPE_ID) ON DELETE CASCADE, FOREIGN KEY (PROCESS_ID) REFERENCES ODE_PROCESS (ID) ON DELETE CASCADE, FOREIGN KEY (INSTANCE_ID) REFERENCES ODE_PROCESS_INSTANCE(ID) ON DELETE CASCADE) 



/
CREATE TABLE ODE_FAULT (FAULT_ID NUMBER NOT NULL, ACTIVITY_ID NUMBER, DATA CLOB, MESSAGE VARCHAR2(4000), LINE_NUMBER NUMBER, NAME VARCHAR2(255), PRIMARY KEY (FAULT_ID)) 

/
CREATE TABLE ODE_MESSAGE_ROUTE (MESSAGE_ROUTE_ID NUMBER NOT NULL, CORRELATION_KEY VARCHAR2(255), GROUP_ID VARCHAR2(255), ROUTE_INDEX NUMBER, PROCESS_INSTANCE_ID NUMBER, ROUTE_POLICY VARCHAR2(16), CORR_ID NUMBER, PRIMARY KEY (MESSAGE_ROUTE_ID), FOREIGN KEY (PROCESS_INSTANCE_ID) REFERENCES ODE_PROCESS_INSTANCE(ID) ON DELETE CASCADE) 
/
CREATE TABLE ODE_MEX_PROP (ID NUMBER NOT NULL, MEX_ID VARCHAR2(255), PROP_KEY VARCHAR2(255), PROP_VALUE VARCHAR2(2000), PRIMARY KEY (ID)) 


/
CREATE TABLE ODE_XML_DATA (XML_DATA_ID NUMBER NOT NULL, DATA CLOB, IS_SIMPLE_TYPE NUMBER, NAME VARCHAR2(255), SCOPE_ID NUMBER, PRIMARY KEY (XML_DATA_ID)) 
/
CREATE TABLE ODE_XML_DATA_PROP (ID NUMBER NOT NULL, XML_DATA_ID NUMBER, PROP_KEY VARCHAR2(255), PROP_VALUE VARCHAR2(255), PRIMARY KEY (ID)) 
/
CREATE TABLE OPENJPA_SEQUENCE_TABLE (ID NUMBER NOT NULL, SEQUENCE_VALUE NUMBER, PRIMARY KEY (ID)) 
/
CREATE TABLE STORE_DU (NAME VARCHAR2(255) NOT NULL, DEPLOYDT TIMESTAMP, DEPLOYER VARCHAR2(255), DIR VARCHAR2(255), PRIMARY KEY (NAME)) 
/
CREATE TABLE STORE_PROCESS (PID VARCHAR2(255) NOT NULL, STATE VARCHAR2(255), TYPE VARCHAR2(255), VERSION NUMBER, DU VARCHAR2(255), PRIMARY KEY (PID)) 
/
CREATE TABLE STORE_PROCESS_PROP (id NUMBER NOT NULL, PROP_KEY VARCHAR2(255), PROP_VAL VARCHAR2(255), PRIMARY KEY (id)) 
/
CREATE TABLE STORE_PROC_TO_PROP (PROCESSCONFDAOIMPL_PID VARCHAR2(255), ELEMENT_ID NUMBER) 
/
CREATE TABLE STORE_VERSIONS (id NUMBER NOT NULL, VERSION NUMBER, PRIMARY KEY (id)) 
/
CREATE INDEX I_D_TASK_ATTACMENT ON TASK_ATTACHMENT (MESSAGE_EXCHANGE_ID) 
/
CREATE INDEX I_D_CTVRY_INSTANCE ON ODE_ACTIVITY_RECOVERY (INSTANCE_ID) 
/
CREATE INDEX I_D_CR_ST_SCOPE ON ODE_CORRELATION_SET (SCOPE_ID) 
/
CREATE INDEX I_D_CRLTR_PROCESS ON ODE_CORRELATOR (PROC_ID) 
/
CREATE INDEX I_D_CRPRP_CORRSET ON ODE_CORSET_PROP (CORRSET_ID) 
/
CREATE INDEX I_OD_VENT_INSTANCE ON ODE_EVENT (INSTANCE_ID) 
/
CREATE INDEX I_OD_VENT_PROCESS ON ODE_EVENT (PROCESS_ID) 
/
CREATE INDEX I_OD_MSSG_MESSAGEEXCHANGE ON ODE_MESSAGE (MESSAGE_EXCHANGE_ID) 
/
CREATE INDEX I_D_MSHNG_CORRELATOR ON ODE_MESSAGE_EXCHANGE (CORR_ID) 
/
CREATE INDEX I_D_MSHNG_PARTNERLINK ON ODE_MESSAGE_EXCHANGE (PARTNER_LINK_ID) 
/
CREATE INDEX I_D_MSHNG_PROCESS ON ODE_MESSAGE_EXCHANGE (PROCESS_ID) 
/
CREATE INDEX I_D_MSHNG_PROCESSINST ON ODE_MESSAGE_EXCHANGE (PROCESS_INSTANCE_ID) 
/
CREATE INDEX I_D_MSHNG_REQUEST ON ODE_MESSAGE_EXCHANGE (REQUEST_MESSAGE_ID) 
/
CREATE INDEX I_D_MSHNG_RESPONSE ON ODE_MESSAGE_EXCHANGE (RESPONSE_MESSAGE_ID) 
/
CREATE INDEX I_D_MS_RT_CORRELATOR ON ODE_MESSAGE_ROUTE (CORR_ID) 
/
CREATE INDEX I_D_MS_RT_PROCESSINST ON ODE_MESSAGE_ROUTE (PROCESS_INSTANCE_ID) 
/
CREATE INDEX I_D_MXPRP_MEX ON ODE_MEX_PROP (MEX_ID) 
/
CREATE INDEX I_D_PRLNK_SCOPE ON ODE_PARTNER_LINK (SCOPE_ID) 
/
CREATE INDEX I_D_PRTNC_FAULT ON ODE_PROCESS_INSTANCE (FAULT_ID) 
/
CREATE INDEX I_D_PRTNC_INSTANTIATINGCORRELA ON ODE_PROCESS_INSTANCE (INSTANTIATING_CORRELATOR_ID) 
/
CREATE INDEX I_D_PRTNC_PROCESS ON ODE_PROCESS_INSTANCE (PROCESS_ID) 
/
CREATE INDEX I_D_PRTNC_ROOTSCOPE ON ODE_PROCESS_INSTANCE (ROOT_SCOPE_ID) 
/
CREATE INDEX I_OD_SCOP_PARENTSCOPE ON ODE_SCOPE (PARENT_SCOPE_ID) 
/
CREATE INDEX I_OD_SCOP_PROCESSINSTANCE ON ODE_SCOPE (PROCESS_INSTANCE_ID) 
/
CREATE INDEX I_D_XM_DT_SCOPE ON ODE_XML_DATA (SCOPE_ID) 
/
CREATE INDEX I_D_XMPRP_XMLDATA ON ODE_XML_DATA_PROP (XML_DATA_ID) 
/
CREATE INDEX I_STR_CSS_DU ON STORE_PROCESS (DU) 
/
CREATE INDEX I_STR_PRP_ELEMENT ON STORE_PROC_TO_PROP (ELEMENT_ID) 
/
CREATE INDEX I_STR_PRP_PROCESSCONFDAOIMPL_P ON STORE_PROC_TO_PROP (PROCESSCONFDAOIMPL_PID) 

--
-- Human Task Related SQL Scripts
--

/
CREATE TABLE HT_DEADLINE (id NUMBER NOT NULL, DEADLINE_DATE TIMESTAMP NOT NULL, DEADLINE_NAME VARCHAR2(255) NOT NULL, STATUS_TOBE_ACHIEVED VARCHAR2(255) NOT NULL, TASK_ID NUMBER, PRIMARY KEY (id)) 
/
CREATE TABLE HT_EVENT (id NUMBER NOT NULL, EVENT_DETAILS VARCHAR2(255), NEW_STATE VARCHAR2(255), OLD_STATE VARCHAR2(255), EVENT_TIMESTAMP TIMESTAMP NOT NULL, EVENT_TYPE VARCHAR2(255) NOT NULL, EVENT_USER VARCHAR2(255) NOT NULL, TASK_ID NUMBER, PRIMARY KEY (id)) 
/
CREATE TABLE HT_GENERIC_HUMAN_ROLE (GHR_ID NUMBER NOT NULL, GHR_TYPE VARCHAR2(255), TASK_ID NUMBER, PRIMARY KEY (GHR_ID)) 
/
CREATE TABLE HT_HUMANROLE_ORGENTITY (HUMANROLE_ID NUMBER, ORGENTITY_ID NUMBER) 
/
CREATE TABLE HT_JOB (id NUMBER NOT NULL, JOB_DETAILS VARCHAR2(4000), JOB_NAME VARCHAR2(255), NODEID VARCHAR2(255), SCHEDULED VARCHAR(1) NOT NULL, TASKID NUMBER NOT NULL, JOB_TIME NUMBER NOT NULL, TRANSACTED VARCHAR(1) NOT NULL, JOB_TYPE VARCHAR2(255) NOT NULL, PRIMARY KEY (id)) 
/
CREATE TABLE HT_MESSAGE (MESSAGE_ID NUMBER NOT NULL, MESSAGE_DATA CLOB, MESSAGE_HEADER CLOB, MESSAGE_TYPE VARCHAR2(255), MESSAGE_NAME VARCHAR2(512), TASK_ID NUMBER, PRIMARY KEY (MESSAGE_ID)) 
/
CREATE TABLE HT_ORG_ENTITY (ORG_ENTITY_ID NUMBER NOT NULL, ORG_ENTITY_NAME VARCHAR2(255), ORG_ENTITY_TYPE VARCHAR2(255), PRIMARY KEY (ORG_ENTITY_ID)) 
/
CREATE TABLE HT_PRESENTATION_ELEMENT (id NUMBER NOT NULL, PE_CONTENT VARCHAR2(2000), XML_LANG VARCHAR2(255), PE_TYPE VARCHAR2(31), CONTENT_TYPE VARCHAR2(255), TASK_ID NUMBER, PRIMARY KEY (id)) 
/
CREATE TABLE HT_PRESENTATION_PARAM (id NUMBER NOT NULL, PARAM_NAME VARCHAR2(255), PARAM_TYPE VARCHAR2(255), PARAM_VALUE VARCHAR2(2000), TASK_ID NUMBER, PRIMARY KEY (id)) 
/
CREATE TABLE HT_TASK (id NUMBER NOT NULL, ACTIVATION_TIME TIMESTAMP, COMPLETE_BY_TIME TIMESTAMP, CREATED_ON TIMESTAMP, ESCALATED VARCHAR(1) NOT NULL, EXPIRATION_TIME TIMESTAMP, TASK_NAME VARCHAR2(255) NOT NULL, PRIORITY NUMBER NOT NULL, SKIPABLE VARCHAR(1) NOT NULL, START_BY_TIME TIMESTAMP, STATUS VARCHAR2(255) NOT NULL, STATUS_BEFORE_SUSPENSION VARCHAR2(255), TENANT_ID NUMBER NOT NULL, TASK_TYPE VARCHAR2(255) NOT NULL, UPDATED_ON TIMESTAMP, FAILURE_MESSAGE NUMBER, INPUT_MESSAGE NUMBER, OUTPUT_MESSAGE NUMBER, PARENTTASK_ID NUMBER, PRIMARY KEY (id)) 
/
CREATE TABLE HT_TASK_ATTACHMENT (id NUMBER NOT NULL, ACCESS_TYPE VARCHAR2(255), ATTACHED_AT TIMESTAMP, CONTENT_TYPE VARCHAR2(255), ATTACHMENT_NAME VARCHAR2(255), ATTACHMENT_VALUE VARCHAR2(255), TASK_ID NUMBER, ATTACHED_BY NUMBER, PRIMARY KEY (id)) 
/
CREATE TABLE HT_TASK_COMMENT (id NUMBER NOT NULL, COMMENT_TEXT VARCHAR2(4000), COMMENTED_BY VARCHAR2(100), COMMENTED_ON TIMESTAMP, MODIFIED_BY VARCHAR2(100), MODIFIED_ON TIMESTAMP, TASK_ID NUMBER, PRIMARY KEY (id)) 
/
/
CREATE INDEX I_HT_DDLN_TASK ON HT_DEADLINE (TASK_ID) 
/
CREATE INDEX I_HT_VENT_TASK ON HT_EVENT (TASK_ID) 
/
CREATE INDEX I_HT_G_RL_TASK ON HT_GENERIC_HUMAN_ROLE (TASK_ID) 
/
CREATE INDEX I_HT_HTTY_ELEMENT ON HT_HUMANROLE_ORGENTITY (ORGENTITY_ID) 
/
CREATE INDEX I_HT_HTTY_HUMANROLE_ID ON HT_HUMANROLE_ORGENTITY (HUMANROLE_ID) 
/
CREATE INDEX I_HT_MSSG_TASK ON HT_MESSAGE (TASK_ID) 
/
CREATE INDEX I_HT_PMNT_DTYPE ON HT_PRESENTATION_ELEMENT (PE_TYPE) 
/
CREATE INDEX I_HT_PMNT_TASK ON HT_PRESENTATION_ELEMENT (TASK_ID) 
/
CREATE INDEX I_HT_PPRM_TASK ON HT_PRESENTATION_PARAM (TASK_ID) 
/
CREATE INDEX I_HT_TASK_FAILUREMESSAGE ON HT_TASK (FAILURE_MESSAGE) 
/
CREATE INDEX I_HT_TASK_INPUTMESSAGE ON HT_TASK (INPUT_MESSAGE) 
/
CREATE INDEX I_HT_TASK_OUTPUTMESSAGE ON HT_TASK (OUTPUT_MESSAGE) 
/
CREATE INDEX I_HT_TASK_PARENTTASK ON HT_TASK (PARENTTASK_ID) 
/
CREATE INDEX I_HT_TMNT_ATTACHEDBY ON HT_TASK_ATTACHMENT (ATTACHED_BY) 
/
CREATE INDEX I_HT_TMNT_TASK ON HT_TASK_ATTACHMENT (TASK_ID) 
/
CREATE INDEX I_HT_TMNT_TASK1 ON HT_TASK_COMMENT (TASK_ID) 
/
--
-- Attachment Management Related SQL Scripts
--

CREATE TABLE ATTACHMENT (
    id NUMBER NOT NULL,
    ATTACHMENT_CONTENT BLOB,
    CONTENT_TYPE VARCHAR2(255) NOT NULL,
    CREATED_BY VARCHAR2(255) NOT NULL,
    CREATED_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ATTACHMENT_NAME VARCHAR2(255) NOT NULL,
    ATTACHMENT_URL VARCHAR2(2048) NOT NULL,
    PRIMARY KEY (id)
) 
/
CREATE INDEX I_ATTACHMENT_URL ON ATTACHMENT (ATTACHMENT_URL) 
/
INSERT INTO ODE_SCHEMA_VERSION values (6)

CREATE TABLE HT_COORDINATION_DATA (MESSAGE_ID VARCHAR2(255) NOT NULL, PROCESS_INSTANCE_ID VARCHAR2(255), PROTOCOL_HANDlER_URL VARCHAR2(255) NOT NULL, TASK_ID VARCHAR2(255), PRIMARY KEY (MESSAGE_ID)) 
/
