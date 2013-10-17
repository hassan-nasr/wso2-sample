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

create table ODE_SCHEMA_VERSION (VERSION integer);
insert into ODE_SCHEMA_VERSION values (6);
-- Apache ODE - SimpleScheduler Database Schema
--
-- Apache Derby scripts by Maciej Szefler.
--
--

CREATE TABLE ode_job (
  jobid CHAR(64)  NOT NULL DEFAULT '',
  ts BIGINT  NOT NULL DEFAULT 0,
  nodeid char(64),
  scheduled int  NOT NULL DEFAULT 0,
  transacted int  NOT NULL DEFAULT 0,

  instanceId BIGINT,
  mexId varchar(255),
  processId varchar(255),
  type varchar(255),
  channel varchar(255),
  correlatorId varchar(255),
  correlationKeySet varchar(255),
  retryCount int,
  inMem int,
  detailsExt blob(4096),

  PRIMARY KEY(jobid));

CREATE INDEX IDX_ODE_JOB_TS ON ode_job(ts);
CREATE INDEX IDX_ODE_JOB_NODEID ON ode_job(nodeid);

CREATE TABLE TASK_ATTACHMENT -- AttachmentDAOImpl
    (ATTACHMENT_ID BIGINT NOT NULL, MESSAGE_EXCHANGE_ID VARCHAR(255), PRIMARY KEY (ATTACHMENT_ID));
CREATE TABLE ODE_ACTIVITY_RECOVERY -- ActivityRecoveryDAOImpl
    (ID BIGINT NOT NULL, ACTIONS VARCHAR(255), ACTIVITY_ID BIGINT, CHANNEL VARCHAR(255), DATE_TIME TIMESTAMP, DETAILS CLOB, INSTANCE_ID BIGINT, REASON VARCHAR(255), RETRIES INTEGER, PRIMARY KEY (ID));
CREATE TABLE ODE_CORRELATION_SET -- CorrelationSetDAOImpl
    (CORRELATION_SET_ID BIGINT NOT NULL, CORRELATION_KEY VARCHAR(255), NAME VARCHAR(255), SCOPE_ID BIGINT, PRIMARY KEY (CORRELATION_SET_ID));
CREATE TABLE ODE_CORRELATOR -- CorrelatorDAOImpl
    (CORRELATOR_ID BIGINT NOT NULL, CORRELATOR_KEY VARCHAR(255), PROC_ID BIGINT, PRIMARY KEY (CORRELATOR_ID));
CREATE TABLE ODE_CORSET_PROP -- CorrSetProperty
    (ID BIGINT NOT NULL, CORRSET_ID BIGINT, PROP_KEY VARCHAR(255), PROP_VALUE VARCHAR(255), PRIMARY KEY (ID));
CREATE TABLE ODE_EVENT -- EventDAOImpl
    (EVENT_ID BIGINT NOT NULL, DETAIL VARCHAR(255), DATA BLOB, SCOPE_ID BIGINT, TSTAMP TIMESTAMP, TYPE VARCHAR(255), INSTANCE_ID BIGINT, PROCESS_ID BIGINT, PRIMARY KEY (EVENT_ID));
CREATE TABLE ODE_FAULT -- FaultDAOImpl
    (FAULT_ID BIGINT NOT NULL, ACTIVITY_ID INTEGER, DATA CLOB, MESSAGE VARCHAR(4000), LINE_NUMBER INTEGER, NAME VARCHAR(255), PRIMARY KEY (FAULT_ID));
CREATE TABLE ODE_MESSAGE -- MessageDAOImpl
    (MESSAGE_ID BIGINT NOT NULL, DATA CLOB, HEADER CLOB, TYPE VARCHAR(255), MESSAGE_EXCHANGE_ID VARCHAR(255), PRIMARY KEY (MESSAGE_ID));
CREATE TABLE ODE_MESSAGE_EXCHANGE -- MessageExchangeDAOImpl
    (MESSAGE_EXCHANGE_ID VARCHAR(255) NOT NULL, CALLEE VARCHAR(255), CHANNEL VARCHAR(255), CORRELATION_ID VARCHAR(255), CORRELATION_KEYS VARCHAR(255), CORRELATION_STATUS VARCHAR(255), CREATE_TIME TIMESTAMP, DIRECTION INTEGER, EPR CLOB, FAULT VARCHAR(255), FAULT_EXPLANATION VARCHAR(255), OPERATION VARCHAR(255), PARTNER_LINK_MODEL_ID INTEGER, PATTERN VARCHAR(255), PIPED_ID VARCHAR(255), PORT_TYPE VARCHAR(255), PROPAGATE_TRANS SMALLINT, STATUS VARCHAR(255), SUBSCRIBER_COUNT INTEGER, CORR_ID BIGINT, PARTNER_LINK_ID BIGINT, PROCESS_ID BIGINT, PROCESS_INSTANCE_ID BIGINT, REQUEST_MESSAGE_ID BIGINT, RESPONSE_MESSAGE_ID BIGINT, PRIMARY KEY (MESSAGE_EXCHANGE_ID));
CREATE TABLE ODE_MESSAGE_ROUTE -- MessageRouteDAOImpl
    (MESSAGE_ROUTE_ID BIGINT NOT NULL, CORRELATION_KEY VARCHAR(255), GROUP_ID VARCHAR(255), ROUTE_INDEX INTEGER, PROCESS_INSTANCE_ID INTEGER, ROUTE_POLICY VARCHAR(16), CORR_ID BIGINT, PRIMARY KEY (MESSAGE_ROUTE_ID));
CREATE TABLE ODE_MEX_PROP -- MexProperty
    (ID BIGINT NOT NULL, MEX_ID VARCHAR(255), PROP_KEY VARCHAR(255), PROP_VALUE VARCHAR(2000), PRIMARY KEY (ID));
CREATE TABLE ODE_PARTNER_LINK -- PartnerLinkDAOImpl
    (PARTNER_LINK_ID BIGINT NOT NULL, MY_EPR CLOB, MY_ROLE_NAME VARCHAR(255), MY_ROLE_SERVICE_NAME VARCHAR(255), MY_SESSION_ID VARCHAR(255), PARTNER_EPR CLOB, PARTNER_LINK_MODEL_ID INTEGER, PARTNER_LINK_NAME VARCHAR(255), PARTNER_ROLE_NAME VARCHAR(255), PARTNER_SESSION_ID VARCHAR(255), SCOPE_ID BIGINT, PRIMARY KEY (PARTNER_LINK_ID));
CREATE TABLE ODE_PROCESS -- ProcessDAOImpl
    (ID BIGINT NOT NULL, GUID VARCHAR(255), PROCESS_ID VARCHAR(255), PROCESS_TYPE VARCHAR(255), VERSION BIGINT, PRIMARY KEY (ID));
CREATE TABLE ODE_PROCESS_INSTANCE -- ProcessInstanceDAOImpl
    (ID BIGINT NOT NULL, DATE_CREATED TIMESTAMP, EXECUTION_STATE BLOB, FAULT_ID BIGINT, LAST_ACTIVE_TIME TIMESTAMP, LAST_RECOVERY_DATE TIMESTAMP, PREVIOUS_STATE SMALLINT, SEQUENCE BIGINT, INSTANCE_STATE SMALLINT, INSTANTIATING_CORRELATOR_ID BIGINT, PROCESS_ID BIGINT, ROOT_SCOPE_ID BIGINT, PRIMARY KEY (ID));
CREATE TABLE ODE_SCOPE -- ScopeDAOImpl
    (SCOPE_ID BIGINT NOT NULL, MODEL_ID INTEGER, SCOPE_NAME VARCHAR(255), SCOPE_STATE VARCHAR(255), PROCESS_INSTANCE_ID BIGINT, PARENT_SCOPE_ID BIGINT, PRIMARY KEY (SCOPE_ID));
CREATE TABLE ODE_XML_DATA -- XmlDataDAOImpl
    (XML_DATA_ID BIGINT NOT NULL, DATA CLOB, IS_SIMPLE_TYPE SMALLINT, NAME VARCHAR(255), SCOPE_ID BIGINT, PRIMARY KEY (XML_DATA_ID));
CREATE TABLE ODE_XML_DATA_PROP -- XmlDataProperty
    (ID BIGINT NOT NULL, XML_DATA_ID BIGINT, PROP_KEY VARCHAR(255), PROP_VALUE VARCHAR(255), PRIMARY KEY (ID));
CREATE TABLE OPENJPA_SEQUENCE_TABLE (ID SMALLINT NOT NULL, SEQUENCE_VALUE BIGINT, PRIMARY KEY (ID));
CREATE TABLE STORE_DU -- DeploymentUnitDaoImpl
    (NAME VARCHAR(255) NOT NULL, DEPLOYDT TIMESTAMP, DEPLOYER VARCHAR(255), DIR VARCHAR(255), PRIMARY KEY (NAME));
CREATE TABLE STORE_PROCESS -- ProcessConfDaoImpl
    (PID VARCHAR(255) NOT NULL, STATE VARCHAR(255), TYPE VARCHAR(255), VERSION BIGINT, DU VARCHAR(255), PRIMARY KEY (PID));
CREATE TABLE STORE_PROCESS_PROP -- ProcessConfPropertyDaoImpl
    (id BIGINT NOT NULL, -- datastore id
    PROP_KEY VARCHAR(255), PROP_VAL VARCHAR(255), PRIMARY KEY (id));
CREATE TABLE STORE_PROC_TO_PROP (PROCESSCONFDAOIMPL_PID VARCHAR(255), ELEMENT_ID BIGINT);
CREATE TABLE STORE_VERSIONS -- VersionTrackerDAOImpl
    (id BIGINT NOT NULL, -- datastore id
    VERSION BIGINT, PRIMARY KEY (id));
CREATE INDEX I_D_TASK_ATTACMENT ON TASK_ATTACHMENT (MESSAGE_EXCHANGE_ID);
CREATE INDEX I_D_CTVRY_INSTANCE ON ODE_ACTIVITY_RECOVERY (INSTANCE_ID);
CREATE INDEX I_D_CR_ST_SCOPE ON ODE_CORRELATION_SET (SCOPE_ID);
CREATE INDEX I_D_CRLTR_PROCESS ON ODE_CORRELATOR (PROC_ID);
CREATE INDEX I_D_CRPRP_CORRSET ON ODE_CORSET_PROP (CORRSET_ID);
CREATE INDEX I_OD_VENT_INSTANCE ON ODE_EVENT (INSTANCE_ID);
CREATE INDEX I_OD_VENT_PROCESS ON ODE_EVENT (PROCESS_ID);
CREATE INDEX I_OD_MSSG_MESSAGEEXCHANGE ON ODE_MESSAGE (MESSAGE_EXCHANGE_ID);
CREATE INDEX I_D_MSHNG_CORRELATOR ON ODE_MESSAGE_EXCHANGE (CORR_ID);
CREATE INDEX I_D_MSHNG_PARTNERLINK ON ODE_MESSAGE_EXCHANGE (PARTNER_LINK_ID);
CREATE INDEX I_D_MSHNG_PROCESS ON ODE_MESSAGE_EXCHANGE (PROCESS_ID);
CREATE INDEX I_D_MSHNG_PROCESSINST ON ODE_MESSAGE_EXCHANGE (PROCESS_INSTANCE_ID);
CREATE INDEX I_D_MSHNG_REQUEST ON ODE_MESSAGE_EXCHANGE (REQUEST_MESSAGE_ID);
CREATE INDEX I_D_MSHNG_RESPONSE ON ODE_MESSAGE_EXCHANGE (RESPONSE_MESSAGE_ID);
CREATE INDEX I_D_MS_RT_CORRELATOR ON ODE_MESSAGE_ROUTE (CORR_ID);
CREATE INDEX I_D_MS_RT_PROCESSINST ON ODE_MESSAGE_ROUTE (PROCESS_INSTANCE_ID);
CREATE INDEX I_D_MXPRP_MEX ON ODE_MEX_PROP (MEX_ID);
CREATE INDEX I_D_PRLNK_SCOPE ON ODE_PARTNER_LINK (SCOPE_ID);
CREATE INDEX I_D_PRTNC_FAULT ON ODE_PROCESS_INSTANCE (FAULT_ID);
CREATE INDEX I_D_PRTNC_INSTANTIATINGCORRELATOR ON ODE_PROCESS_INSTANCE (INSTANTIATING_CORRELATOR_ID);
CREATE INDEX I_D_PRTNC_PROCESS ON ODE_PROCESS_INSTANCE (PROCESS_ID);
CREATE INDEX I_D_PRTNC_ROOTSCOPE ON ODE_PROCESS_INSTANCE (ROOT_SCOPE_ID);
CREATE INDEX I_OD_SCOP_PARENTSCOPE ON ODE_SCOPE (PARENT_SCOPE_ID);
CREATE INDEX I_OD_SCOP_PROCESSINSTANCE ON ODE_SCOPE (PROCESS_INSTANCE_ID);
CREATE INDEX I_D_XM_DT_SCOPE ON ODE_XML_DATA (SCOPE_ID);
CREATE INDEX I_D_XMPRP_XMLDATA ON ODE_XML_DATA_PROP (XML_DATA_ID);
CREATE INDEX I_STR_CSS_DU ON STORE_PROCESS (DU);
CREATE INDEX I_STR_PRP_ELEMENT ON STORE_PROC_TO_PROP (ELEMENT_ID);
CREATE INDEX I_STR_PRP_PROCESSCONFDAOIMPL_PID ON STORE_PROC_TO_PROP (PROCESSCONFDAOIMPL_PID);


--
-- Human Task Related SQL Scripts
--

CREATE TABLE HT_DEADLINE -- Deadline
    (id BIGINT NOT NULL, DEADLINE_DATE TIMESTAMP NOT NULL, DEADLINE_NAME VARCHAR(255) NOT NULL, STATUS_TOBE_ACHIEVED VARCHAR(255) NOT NULL, TASK_ID BIGINT, PRIMARY KEY (id));
CREATE TABLE HT_EVENT -- Event
    (id BIGINT NOT NULL, EVENT_DETAILS VARCHAR(255), NEW_STATE VARCHAR(255), OLD_STATE VARCHAR(255), EVENT_TIMESTAMP TIMESTAMP NOT NULL, EVENT_TYPE VARCHAR(255) NOT NULL, EVENT_USER VARCHAR(255) NOT NULL, TASK_ID BIGINT, PRIMARY KEY (id));
CREATE TABLE HT_GENERIC_HUMAN_ROLE -- GenericHumanRole
    (GHR_ID BIGINT NOT NULL, GHR_TYPE VARCHAR(255), TASK_ID BIGINT, PRIMARY KEY (GHR_ID));
CREATE TABLE HT_HUMANROLE_ORGENTITY (HUMANROLE_ID BIGINT, ORGENTITY_ID BIGINT);
CREATE TABLE HT_JOB -- HumanTaskJob
    (id BIGINT NOT NULL, JOB_DETAILS VARCHAR(4000), JOB_NAME VARCHAR(255), NODEID VARCHAR(255), SCHEDULED VARCHAR(1) NOT NULL, TASKID BIGINT NOT NULL, JOB_TIME BIGINT NOT NULL, TRANSACTED VARCHAR(1) NOT NULL, JOB_TYPE VARCHAR(255) NOT NULL, PRIMARY KEY (id));
CREATE TABLE HT_MESSAGE -- Message
    (MESSAGE_ID BIGINT NOT NULL, MESSAGE_DATA CLOB, MESSAGE_HEADER CLOB, MESSAGE_TYPE VARCHAR(255), MESSAGE_NAME VARCHAR(512), TASK_ID BIGINT, PRIMARY KEY (MESSAGE_ID));
CREATE TABLE HT_ORG_ENTITY -- OrganizationalEntity
    (ORG_ENTITY_ID BIGINT NOT NULL, ORG_ENTITY_NAME VARCHAR(255), ORG_ENTITY_TYPE VARCHAR(255), PRIMARY KEY (ORG_ENTITY_ID));
CREATE TABLE HT_PRESENTATION_ELEMENT -- PresentationElement
    (id BIGINT NOT NULL, PE_CONTENT VARCHAR(2000), XML_LANG VARCHAR(255), PE_TYPE VARCHAR(31), CONTENT_TYPE VARCHAR(255), TASK_ID BIGINT, PRIMARY KEY (id));
CREATE TABLE HT_PRESENTATION_PARAM -- PresentationParameter
    (id BIGINT NOT NULL, PARAM_NAME VARCHAR(255), PARAM_TYPE VARCHAR(255), PARAM_VALUE VARCHAR(2000), TASK_ID BIGINT, PRIMARY KEY (id));
CREATE TABLE HT_TASK -- Task
    (id BIGINT NOT NULL, ACTIVATION_TIME TIMESTAMP, COMPLETE_BY_TIME TIMESTAMP, CREATED_ON TIMESTAMP, ESCALATED VARCHAR(1) NOT NULL, EXPIRATION_TIME TIMESTAMP, TASK_NAME VARCHAR(255) NOT NULL, PRIORITY INTEGER NOT NULL, SKIPABLE VARCHAR(1) NOT NULL, START_BY_TIME TIMESTAMP, STATUS VARCHAR(255) NOT NULL, STATUS_BEFORE_SUSPENSION VARCHAR(255), TENANT_ID INTEGER NOT NULL, TASK_TYPE VARCHAR(255) NOT NULL, UPDATED_ON TIMESTAMP, FAILURE_MESSAGE BIGINT, INPUT_MESSAGE BIGINT, OUTPUT_MESSAGE BIGINT, PARENTTASK_ID BIGINT, PRIMARY KEY (id));
CREATE TABLE HT_TASK_ATTACHMENT -- Attachment
    (id BIGINT NOT NULL, ACCESS_TYPE VARCHAR(255), ATTACHED_AT TIMESTAMP, CONTENT_TYPE VARCHAR(255), ATTACHMENT_NAME VARCHAR(255), ATTACHMENT_VALUE VARCHAR(255), TASK_ID BIGINT, ATTACHED_BY BIGINT, PRIMARY KEY (id));
CREATE TABLE HT_TASK_COMMENT -- Comment
    (id BIGINT NOT NULL, COMMENT_TEXT VARCHAR(4000), COMMENTED_BY VARCHAR(100), COMMENTED_ON TIMESTAMP, MODIFIED_BY VARCHAR(100), MODIFIED_ON TIMESTAMP, TASK_ID BIGINT, PRIMARY KEY (id));
CREATE INDEX I_HT_DDLN_TASK ON HT_DEADLINE (TASK_ID);
CREATE INDEX I_HT_VENT_TASK ON HT_EVENT (TASK_ID);
CREATE INDEX I_HT_G_RL_TASK ON HT_GENERIC_HUMAN_ROLE (TASK_ID);
CREATE INDEX I_HT_HTTY_ELEMENT ON HT_HUMANROLE_ORGENTITY (ORGENTITY_ID);
CREATE INDEX I_HT_HTTY_HUMANROLE_ID ON HT_HUMANROLE_ORGENTITY (HUMANROLE_ID);
CREATE INDEX I_HT_MSSG_TASK ON HT_MESSAGE (TASK_ID);
CREATE INDEX I_HT_PMNT_DTYPE ON HT_PRESENTATION_ELEMENT (PE_TYPE);
CREATE INDEX I_HT_PMNT_TASK ON HT_PRESENTATION_ELEMENT (TASK_ID);
CREATE INDEX I_HT_PPRM_TASK ON HT_PRESENTATION_PARAM (TASK_ID);
CREATE INDEX I_HT_TASK_FAILUREMESSAGE ON HT_TASK (FAILURE_MESSAGE);
CREATE INDEX I_HT_TASK_INPUTMESSAGE ON HT_TASK (INPUT_MESSAGE);
CREATE INDEX I_HT_TASK_OUTPUTMESSAGE ON HT_TASK (OUTPUT_MESSAGE);
CREATE INDEX I_HT_TASK_PARENTTASK ON HT_TASK (PARENTTASK_ID);
CREATE INDEX I_HT_TMNT_ATTACHEDBY ON HT_TASK_ATTACHMENT (ATTACHED_BY);
CREATE INDEX I_HT_TMNT_TASK ON HT_TASK_ATTACHMENT (TASK_ID);
CREATE INDEX I_HT_TMNT_TASK1 ON HT_TASK_COMMENT (TASK_ID);

--
-- Attachment Management Related SQL Scripts
--
CREATE TABLE ATTACHMENT (
    id BIGINT NOT NULL,
    ATTACHMENT_CONTENT BLOB,
    CONTENT_TYPE VARCHAR(255) NOT NULL,
    CREATED_BY VARCHAR(255) NOT NULL,
    CREATED_TIME Timestamp NOT NULL WITH DEFAULT CURRENT_TIMESTAMP,
    ATTACHMENT_NAME VARCHAR(255) NOT NULL,
    ATTACHMENT_URL VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

--
-- B4P Related SQL Scripts
--
CREATE TABLE HT_COORDINATION_DATA -- HTProtocolHandler
    (MESSAGE_ID VARCHAR(255) NOT NULL, PROCESS_INSTANCE_ID VARCHAR(255), PROTOCOL_HANDlER_URL VARCHAR(255) NOT NULL, TASK_ID VARCHAR(255), PRIMARY KEY (MESSAGE_ID));
