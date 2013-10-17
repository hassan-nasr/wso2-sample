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

create table ODE_SCHEMA_VERSION(VERSION integer);
insert into ODE_SCHEMA_VERSION values (6);

-- Table structure for table  ATTACHMENT ( Attachment management module )
--

CREATE TABLE  ATTACHMENT  (
   id  bigint(20) NOT NULL AUTO_INCREMENT,
   CREATED_TIME  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
   ATTACHMENT_NAME  varchar(255) NOT NULL,
   CREATED_BY  varchar(255) NOT NULL,
   CONTENT_TYPE  varchar(255) NOT NULL,
   ATTACHMENT_URL  varchar(2048) NOT NULL,
   ATTACHMENT_CONTENT  blob,
  PRIMARY KEY ( id ),
  KEY  I_ATTACHMENT_URL  ( ATTACHMENT_URL (1000))
) ENGINE=innodb DEFAULT CHARSET=latin1;

-- ODE Tables for hibernate
--


CREATE TABLE TASK_ATTACHMENT (
  ID bigint(20) NOT NULL AUTO_INCREMENT,
  MLOCK int(11) NOT NULL,
  INSERT_TIME timestamp DEFAULT CURRENT_TIMESTAMP,
  ATTACHMENT_ID BIGINT NOT NULL, 
  MESSAGE_EXCHANGE_ID VARCHAR(255), 
  PRIMARY KEY (ID)
) ENGINE=innodb;

--
-- Table structure for table  BPEL_ACTIVITY_RECOVERY 
--

CREATE TABLE  BPEL_ACTIVITY_RECOVERY  (
   ID  bigint(20) NOT NULL AUTO_INCREMENT,
   PIID  bigint(20) DEFAULT NULL,
   AID  bigint(20) DEFAULT NULL,
   CHANNEL  varchar(255) DEFAULT NULL,
   REASON  varchar(255) DEFAULT NULL,
   DATE_TIME  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   DETAILS  blob,
   ACTIONS  varchar(255) DEFAULT NULL,
   RETRIES  int(11) DEFAULT NULL,
   INSERT_TIME  timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
   MLOCK  int(11) NOT NULL,
  PRIMARY KEY ( ID )
) ENGINE=innodb DEFAULT CHARSET=latin1;

--
-- Table structure for table  BPEL_CORRELATION_PROP 
--

CREATE TABLE  BPEL_CORRELATION_PROP  (
   ID  bigint(20) NOT NULL,
   NAME  varchar(255) DEFAULT NULL,
   NAMESPACE  varchar(255) DEFAULT NULL,
   VALUE  varchar(255) DEFAULT NULL,
   CORR_SET_ID  bigint(20) DEFAULT NULL,
   INSERT_TIME  datetime DEFAULT NULL,
   MLOCK  int(11) NOT NULL,
  PRIMARY KEY ( ID )
) ENGINE=innodb DEFAULT CHARSET=latin1;

--
-- Table structure for table  BPEL_CORRELATION_SET 

CREATE TABLE  BPEL_CORRELATION_SET  (
   ID  bigint(20) NOT NULL,
   VALUE  varchar(255) DEFAULT NULL,
   CORR_SET_NAME  varchar(255) DEFAULT NULL,
   SCOPE_ID  bigint(20) DEFAULT NULL,
   PIID  bigint(20) DEFAULT NULL,
   PROCESS_ID  bigint(20) DEFAULT NULL,
   INSERT_TIME  datetime DEFAULT NULL,
   MLOCK  int(11) NOT NULL,
  PRIMARY KEY ( ID ),
  KEY  IDX_CORR_SET_NAME  ( CORR_SET_NAME ),
  KEY  IDX_CORR_SET_SCOPE_ID  ( SCOPE_ID )
) ENGINE=innodb DEFAULT CHARSET=latin1;

--
-- Table structure for table  BPEL_CORRELATOR 
--

CREATE TABLE  BPEL_CORRELATOR  (
   ID  bigint(20) NOT NULL,
   CID  varchar(255) DEFAULT NULL,
   PROCESS_ID  bigint(20) DEFAULT NULL,
   INSERT_TIME  datetime DEFAULT NULL,
   MLOCK  int(11) NOT NULL,
  PRIMARY KEY ( ID ),
  KEY  IDX_CORRELATOR_CID  ( CID ),
  KEY  IDX_BPEL_CORRELATOR_PROCESS_ID  ( PROCESS_ID )
) ENGINE=innodb DEFAULT CHARSET=latin1;

--
-- Table structure for table  BPEL_CORRELATOR_MESSAGE_CKEY 
--
CREATE TABLE  BPEL_CORRELATOR_MESSAGE_CKEY  (
   ID  bigint(20) NOT NULL,
   CKEY  varchar(255) DEFAULT NULL,
   CORRELATOR_MESSAGE_ID  bigint(20) DEFAULT NULL,
   INSERT_TIME  datetime DEFAULT NULL,
   MLOCK  int(11) NOT NULL,
  PRIMARY KEY ( ID ),
  KEY  IDX_BPEL_CORRELATOR_MESSAGE_CKEY  ( CKEY )
) ENGINE=innodb DEFAULT CHARSET=latin1;

--
-- Table structure for table  BPEL_EVENT 
--

CREATE TABLE  BPEL_EVENT  (
   ID  bigint(20) NOT NULL AUTO_INCREMENT,
   IID  bigint(20) DEFAULT NULL,
   PID  bigint(20) DEFAULT NULL,
   TSTAMP  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   TYPE  varchar(255) DEFAULT NULL,
   DETAIL  longtext,
   DATA  blob,
   SID  bigint(20) DEFAULT NULL,
   INSERT_TIME  timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
   MLOCK  int(11) NOT NULL,
  PRIMARY KEY ( ID ),
  KEY  IDX_EVENT_IID  ( IID ),
  KEY  IDX_EVENT_PID  ( PID )
) ENGINE=innodb AUTO_INCREMENT=2739 DEFAULT CHARSET=latin1;

--
-- Table structure for table  BPEL_FAULT 
--

CREATE TABLE  BPEL_FAULT  (
   ID  bigint(20) NOT NULL AUTO_INCREMENT,
   FAULTNAME  varchar(255) DEFAULT NULL,
   DATA  blob,
   EXPLANATION  varchar(4000) DEFAULT NULL,
   LINE_NUM  int(11) DEFAULT NULL,
   AID  int(11) DEFAULT NULL,
   INSERT_TIME  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   MLOCK  int(11) NOT NULL,
  PRIMARY KEY ( ID )
) ENGINE=innodb DEFAULT CHARSET=latin1;

--
-- Table structure for table  BPEL_INSTANCE 
--

CREATE TABLE  BPEL_INSTANCE  (
   ID  bigint(20) NOT NULL AUTO_INCREMENT,
   INSTANTIATING_CORRELATOR  bigint(20) DEFAULT NULL,
   FAULT  bigint(20) DEFAULT NULL,
   JACOB_STATE_DATA  blob,
   PREVIOUS_STATE  smallint(6) DEFAULT NULL,
   PROCESS_ID  bigint(20) DEFAULT NULL,
   STATE  smallint(6) DEFAULT NULL,
   LAST_ACTIVE_DT  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   SEQUENCE  bigint(20) DEFAULT NULL,
   FAILURE_COUNT  int(11) DEFAULT NULL,
   FAILURE_DT  timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
   INSERT_TIME  timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
   MLOCK  int(11) NOT NULL,
  PRIMARY KEY ( ID ),
  KEY  IDX_BPEL_INSTANCE_PROCESS_ID  ( PROCESS_ID ),
  KEY  IDX_BPEL_INSTANCE_STATE  ( STATE )
) ENGINE=innodb AUTO_INCREMENT=1805 DEFAULT CHARSET=latin1;

--
-- Table structure for table  BPEL_MESSAGE 
--

CREATE TABLE  BPEL_MESSAGE  (
   ID  bigint(20) NOT NULL AUTO_INCREMENT,
   MEX  bigint(20) DEFAULT NULL,
   TYPE  varchar(255) DEFAULT NULL,
   MESSAGE_DATA  blob,
   MESSAGE_HEADER  blob,
   INSERT_TIME  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   MLOCK  int(11) NOT NULL,
  PRIMARY KEY ( ID ),
  KEY  IDX_MESSAGE_MEX  ( MEX )
) ENGINE=innodb AUTO_INCREMENT=1710 DEFAULT CHARSET=latin1;

--
-- Table structure for table  BPEL_MESSAGE_EXCHANGE 
--

CREATE TABLE  BPEL_MESSAGE_EXCHANGE  (
   ID  bigint(20) NOT NULL AUTO_INCREMENT,
   PORT_TYPE  varchar(255) DEFAULT NULL,
   CHANNEL_NAME  varchar(255) DEFAULT NULL,
   CLIENTKEY  varchar(255) DEFAULT NULL,
   ENDPOINT  blob,
   CALLBACK_ENDPOINT  blob,
   REQUEST  bigint(20) DEFAULT NULL,
   RESPONSE  bigint(20) DEFAULT NULL,
   INSERT_DT  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   OPERATION  varchar(255) DEFAULT NULL,
   STATE  varchar(255) DEFAULT NULL,
   PROCESS  bigint(20) DEFAULT NULL,
   PIID  bigint(20) DEFAULT NULL,
   DIR  char(255) DEFAULT NULL,
   PLINK_MODELID  int(11) DEFAULT NULL,
   PATTERN  varchar(255) DEFAULT NULL,
   CORR_STATUS  varchar(255) DEFAULT NULL,
   FAULT_TYPE  varchar(255) DEFAULT NULL,
   FAULT_EXPL  varchar(255) DEFAULT NULL,
   CALLEE  varchar(255) DEFAULT NULL,
   PARTNERLINK  bigint(20) DEFAULT NULL,
   PIPED_ID  varchar(255) DEFAULT NULL,
   SUBSCRIBER_COUNT  int(11) DEFAULT NULL,
   INSERT_TIME  timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
   MLOCK  int(11) NOT NULL,
  PRIMARY KEY ( ID ),
  KEY  IDX_MESSAGE_EXCHANGE_PIID  ( PIID )
) ENGINE=innodb AUTO_INCREMENT=1608 DEFAULT CHARSET=latin1;


--
-- Table structure for table  BPEL_MEX_PROPS 
--

CREATE TABLE  BPEL_MEX_PROPS  (
   MEX  bigint(20) NOT NULL,
   VALUE  varchar(8000) DEFAULT NULL,
   NAME  varchar(255) NOT NULL,
  PRIMARY KEY ( MEX , NAME )
) ENGINE=innodb DEFAULT CHARSET=latin1;

--
-- Table structure for table  BPEL_PLINK_VAL 
--
CREATE TABLE  BPEL_PLINK_VAL  (
   ID  bigint(20) NOT NULL AUTO_INCREMENT,
   PARTNER_LINK  varchar(100) NOT NULL,
   PARTNERROLE  varchar(100) DEFAULT NULL,
   MYROLE_EPR_DATA  blob,
   PARTNERROLE_EPR_DATA  blob,
   PROCESS  bigint(20) DEFAULT NULL,
   SCOPE  bigint(20) DEFAULT NULL,
   SVCNAME  varchar(255) DEFAULT NULL,
   MYROLE  varchar(100) DEFAULT NULL,
   MODELID  int(11) DEFAULT NULL,
   MYSESSIONID  varchar(255) DEFAULT NULL,
   PARTNERSESSIONID  varchar(255) DEFAULT NULL,
   INSERT_TIME  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   MLOCK  int(11) NOT NULL,
  PRIMARY KEY ( ID ),
  KEY  IDX_PLINK_VAL_PROCESS_IDX  ( PROCESS ),
  KEY  IDX_PLINK_VAL_SCOPE  ( SCOPE ),
  KEY  IDX_PLINK_VAL_MODELID  ( MODELID )
) ENGINE=innodb AUTO_INCREMENT=2108 DEFAULT CHARSET=latin1;

--
-- Table structure for table  BPEL_PROCESS 
--

CREATE TABLE  BPEL_PROCESS  (
   ID  bigint(20) NOT NULL,
   PROCID  varchar(255) NOT NULL,
   deployer  varchar(255) DEFAULT NULL,
   deploydate  datetime DEFAULT NULL,
   type_name  varchar(255) DEFAULT NULL,
   type_ns  varchar(255) DEFAULT NULL,
   version  bigint(20) DEFAULT NULL,
   ACTIVE_  bit(1) DEFAULT NULL,
   guid  varchar(255) DEFAULT NULL,
   INSERT_TIME  datetime DEFAULT NULL,
   MLOCK  int(11) NOT NULL,
  PRIMARY KEY ( ID ),
  UNIQUE KEY  PROCID  ( PROCID ),
  KEY  IDX_BPEL_PROCESS_TYPE_NAME  ( type_name ),
  KEY  IDX_BPEL_PROCESS_TYPE_NS  ( type_ns )
) ENGINE=innodb DEFAULT CHARSET=latin1;

--
-- Table structure for table  BPEL_SCOPE 
--


CREATE TABLE  BPEL_SCOPE  (
   ID  bigint(20) NOT NULL,
   PIID  bigint(20) DEFAULT NULL,
   PARENT_SCOPE_ID  bigint(20) DEFAULT NULL,
   STATE  varchar(255) NOT NULL,
   NAME  varchar(255) NOT NULL,
   MODELID  int(11) DEFAULT NULL,
   INSERT_TIME  datetime DEFAULT NULL,
   MLOCK  int(11) NOT NULL,
  PRIMARY KEY ( ID ),
  KEY  IDX_SCOPE_PIID  ( PIID )
) ENGINE=innodb DEFAULT CHARSET=latin1;

--
-- Table structure for table  BPEL_SELECTORS 
--


CREATE TABLE  BPEL_SELECTORS  (
   ID  bigint(20) NOT NULL,
   PIID  bigint(20) NOT NULL,
   SELGRPID  varchar(255) NOT NULL,
   IDX  int(11) NOT NULL,
   CORRELATION_KEY  varchar(255) NOT NULL,
   PROC_TYPE  varchar(255) NOT NULL,
   ROUTE_POLICY  varchar(255) DEFAULT NULL,
   CORRELATOR  bigint(20) NOT NULL,
   INSERT_TIME  datetime DEFAULT NULL,
   MLOCK  int(11) NOT NULL,
  PRIMARY KEY ( ID ),
  UNIQUE KEY  CORRELATION_KEY  ( CORRELATION_KEY , CORRELATOR ),
  KEY  IDX_SELECTOR_CORRELATOR  ( CORRELATOR ),
  KEY  IDX_SELECTOR_SELGRPID  ( SELGRPID ),
  KEY  IDX_SELECTOR_CKEY  ( CORRELATION_KEY ),
  KEY  IDX_SELECTOR_INSTANCE  ( PIID ),
  KEY  IDX_BPEL_SELECTORS_PROC_TYPE  ( PROC_TYPE ),
  KEY  IDX_BPEL_SELECTORS_SELGRPID  ( SELGRPID )
) ENGINE=innodb DEFAULT CHARSET=latin1;

--
-- Table structure for table  BPEL_UNMATCHED 
--

CREATE TABLE  BPEL_UNMATCHED  (
   ID  bigint(20) NOT NULL,
   MEX  bigint(20) DEFAULT NULL,
   CORRELATION_KEY  varchar(255) DEFAULT NULL,
   CORRELATOR  bigint(20) NOT NULL,
   INSERT_TIME  datetime DEFAULT NULL,
   MLOCK  int(11) NOT NULL,
  PRIMARY KEY ( ID ),
  KEY  IDX_UNMATCHED_CORRELATOR  ( CORRELATOR ),
  KEY  IDX_UNMATCHED_CKEY  ( CORRELATION_KEY ),
  KEY  IDX_UNMATCHED_CORRELATOR_CKEY  ( CORRELATOR , CORRELATION_KEY ),
  KEY  IDX_UNMATCHED_MEX  ( MEX )
) ENGINE=innodb DEFAULT CHARSET=latin1;

--
-- Table structure for table  BPEL_XML_DATA 
--

CREATE TABLE  BPEL_XML_DATA  (
   ID  bigint(20) NOT NULL AUTO_INCREMENT,
   DATA  blob,
   NAME  varchar(255) NOT NULL,
   SIMPLE_VALUE  varchar(255) DEFAULT NULL,
   SCOPE_ID  bigint(20) DEFAULT NULL,
   PIID  bigint(20) DEFAULT NULL,
   IS_SIMPLE_TYPE  bit(1) DEFAULT NULL,
   INSERT_TIME  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   MLOCK  int(11) NOT NULL,
  PRIMARY KEY ( ID ),
  KEY  IDX_XMLDATA_IID  ( PIID ),
  KEY  IDX_XMLDATA_SID  ( SCOPE_ID ),
  KEY  IDX_XMLDATA_NAME  ( NAME ),
  KEY  IDX_XMLDATA_NAME_SID  ( NAME , SCOPE_ID )
) ENGINE=innodb AUTO_INCREMENT=2210 DEFAULT CHARSET=latin1;


--
-- Table structure for table  ODE_JOB 
--

CREATE TABLE  ODE_JOB  (
   jobid  char(64) NOT NULL DEFAULT '',
   ts  bigint(20) NOT NULL DEFAULT '0',
   nodeid  char(64) DEFAULT NULL,
   scheduled  int(11) NOT NULL DEFAULT '0',
   transacted  int(11) NOT NULL DEFAULT '0',
   instanceId  bigint(20) DEFAULT NULL,
   mexId  varchar(255) DEFAULT NULL,
   processId  varchar(255) DEFAULT NULL,
   type  varchar(255) DEFAULT NULL,
   channel  varchar(255) DEFAULT NULL,
   correlatorId  varchar(255) DEFAULT NULL,
   correlationKeySet  varchar(255) DEFAULT NULL,
   retryCount  int(11) DEFAULT NULL,
   inMem  int(11) DEFAULT NULL,
   detailsExt  blob,
  PRIMARY KEY ( jobid ),
  KEY  IDX_ODE_JOB_TS  ( ts ),
  KEY  IDX_ODE_JOB_NODEID  ( nodeid )
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table  STORE_DU 
--

CREATE TABLE  STORE_DU  (
   NAME  varchar(255) NOT NULL,
   DEPLOYDT  datetime DEFAULT NULL,
   DEPLOYER  varchar(255) DEFAULT NULL,
   DIR  varchar(255) DEFAULT NULL,
  PRIMARY KEY ( NAME )
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table  STORE_PROCESS 
--

CREATE TABLE  STORE_PROCESS  (
   PID  varchar(255) NOT NULL,
   STATE  varchar(255) DEFAULT NULL,
   TYPE  varchar(255) DEFAULT NULL,
   VERSION  bigint(20) DEFAULT NULL,
   DU  varchar(255) DEFAULT NULL,
  PRIMARY KEY ( PID )
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table  STORE_PROCESS_PROP 
--
CREATE TABLE  STORE_PROCESS_PROP  (
   id  bigint(20) NOT NULL,
   PROP_KEY  varchar(255) DEFAULT NULL,
   PROP_VAL  varchar(255) DEFAULT NULL,
  PRIMARY KEY ( id )
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table  STORE_PROC_TO_PROP 
--
CREATE TABLE  STORE_PROC_TO_PROP  (
   PROCESSCONFDAOIMPL_PID  varchar(255) DEFAULT NULL,
   ELEMENT_ID  bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table  STORE_VERSIONS 
--
CREATE TABLE  STORE_VERSIONS  (
   id  bigint(20) NOT NULL,
   VERSION  bigint(20) DEFAULT NULL,
  PRIMARY KEY ( id )
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table  VAR_PROPERTY 
--

CREATE TABLE  VAR_PROPERTY  (
   ID  bigint(20) NOT NULL AUTO_INCREMENT,
   XML_DATA_ID  bigint(20) DEFAULT NULL,
   PROP_VALUE  varchar(255) DEFAULT NULL,
   PROP_NAME  varchar(255) NOT NULL,
   INSERT_TIME  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   MLOCK  int(11) NOT NULL,
  PRIMARY KEY ( ID ),
  KEY  IDX_VARPROP_XMLDATA  ( XML_DATA_ID ),
  KEY  IDX_VARPROP_NAME  ( PROP_NAME ),
  KEY  IDX_VARPROP_VALUE  ( PROP_VALUE )
) ENGINE=innodb AUTO_INCREMENT=2302 DEFAULT CHARSET=latin1;


--
-- Table structure for table  hibernate_unique_key 
--
CREATE TABLE  hibernate_unique_key  (
   next_hi  int(11) DEFAULT NULL
) ENGINE=innodb DEFAULT CHARSET=latin1;

insert into hibernate_unique_key values (1);


CREATE INDEX I_D_TASK_ATTACMENT ON TASK_ATTACHMENT (MESSAGE_EXCHANGE_ID);


-- Tables for HumanTask Module
-- Table structure for table  HT_DEADLINE 
--

CREATE TABLE  HT_DEADLINE  (
   id  bigint(20) NOT NULL,
   DEADLINE_DATE  datetime NOT NULL,
   DEADLINE_NAME  varchar(255) NOT NULL,
   STATUS_TOBE_ACHIEVED  varchar(255) NOT NULL,
   TASK_ID  bigint(20) DEFAULT NULL,
  PRIMARY KEY ( id ),
  KEY  I_HT_DDLN_TASK  ( TASK_ID )
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table  HT_EVENT 
--

CREATE TABLE  HT_EVENT  (
   id  bigint(20) NOT NULL,
   EVENT_DETAILS  varchar(255) DEFAULT NULL,
   NEW_STATE  varchar(255) DEFAULT NULL,
   OLD_STATE  varchar(255) DEFAULT NULL,
   EVENT_TIMESTAMP  datetime NOT NULL,
   EVENT_TYPE  varchar(255) NOT NULL,
   EVENT_USER  varchar(255) NOT NULL,
   TASK_ID  bigint(20) DEFAULT NULL,
  PRIMARY KEY ( id ),
  KEY  I_HT_VENT_TASK  ( TASK_ID )
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table  HT_GENERIC_HUMAN_ROLE 
--

CREATE TABLE  HT_GENERIC_HUMAN_ROLE  (
   GHR_ID  bigint(20) NOT NULL,
   GHR_TYPE  varchar(255) DEFAULT NULL,
   TASK_ID  bigint(20) DEFAULT NULL,
  PRIMARY KEY ( GHR_ID ),
  KEY  I_HT_G_RL_TASK  ( TASK_ID )
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table  HT_HUMANROLE_ORGENTITY 
--

CREATE TABLE  HT_HUMANROLE_ORGENTITY  (
   HUMANROLE_ID  bigint(20) DEFAULT NULL,
   ORGENTITY_ID  bigint(20) DEFAULT NULL,
  KEY  I_HT_HTTY_ELEMENT  ( ORGENTITY_ID ),
  KEY  I_HT_HTTY_HUMANROLE_ID  ( HUMANROLE_ID )
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table  HT_JOB 
--

CREATE TABLE  HT_JOB  (
   id  bigint(20) NOT NULL,
   JOB_DETAILS  varchar(4000) DEFAULT NULL,
   JOB_NAME  varchar(255) DEFAULT NULL,
   NODEID  varchar(255) DEFAULT NULL,
   SCHEDULED  varchar(1) NOT NULL,
   TASKID  bigint(20) NOT NULL,
   JOB_TIME  bigint(20) NOT NULL,
   TRANSACTED  varchar(1) NOT NULL,
   JOB_TYPE  varchar(255) NOT NULL,
  PRIMARY KEY ( id )
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table  HT_MESSAGE 
--

CREATE TABLE  HT_MESSAGE  (
   MESSAGE_ID  bigint(20) NOT NULL,
   MESSAGE_DATA  longtext,
   MESSAGE_HEADER  longtext,
   MESSAGE_TYPE  varchar(255) DEFAULT NULL,
   MESSAGE_NAME  varchar(512) DEFAULT NULL,
   TASK_ID  bigint(20) DEFAULT NULL,
  PRIMARY KEY ( MESSAGE_ID ),
  KEY  I_HT_MSSG_TASK  ( TASK_ID )
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table  HT_ORG_ENTITY 
--

CREATE TABLE  HT_ORG_ENTITY  (
   ORG_ENTITY_ID  bigint(20) NOT NULL,
   ORG_ENTITY_NAME  varchar(255) DEFAULT NULL,
   ORG_ENTITY_TYPE  varchar(255) DEFAULT NULL,
  PRIMARY KEY ( ORG_ENTITY_ID )
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table  HT_PRESENTATION_ELEMENT 
--

CREATE TABLE  HT_PRESENTATION_ELEMENT  (
   id  bigint(20) NOT NULL,
   PE_CONTENT  varchar(2000) DEFAULT NULL,
   XML_LANG  varchar(255) DEFAULT NULL,
   PE_TYPE  varchar(31) DEFAULT NULL,
   CONTENT_TYPE  varchar(255) DEFAULT NULL,
   TASK_ID  bigint(20) DEFAULT NULL,
  PRIMARY KEY ( id ),
  KEY  I_HT_PMNT_DTYPE  ( PE_TYPE ),
  KEY  I_HT_PMNT_TASK  ( TASK_ID )
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table  HT_PRESENTATION_PARAM 
--

CREATE TABLE  HT_PRESENTATION_PARAM  (
   id  bigint(20) NOT NULL,
   PARAM_NAME  varchar(255) DEFAULT NULL,
   PARAM_TYPE  varchar(255) DEFAULT NULL,
   PARAM_VALUE  varchar(2000) DEFAULT NULL,
   TASK_ID  bigint(20) DEFAULT NULL,
  PRIMARY KEY ( id ),
  KEY  I_HT_PPRM_TASK  ( TASK_ID )
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table  HT_TASK 
--

CREATE TABLE  HT_TASK  (
   id  bigint(20) NOT NULL,
   ACTIVATION_TIME  datetime DEFAULT NULL,
   COMPLETE_BY_TIME  datetime DEFAULT NULL,
   CREATED_ON  datetime DEFAULT NULL,
   ESCALATED  varchar(1) NOT NULL,
   EXPIRATION_TIME  datetime DEFAULT NULL,
   TASK_NAME  varchar(255) NOT NULL,
   PRIORITY  int(11) NOT NULL,
   SKIPABLE  varchar(1) NOT NULL,
   START_BY_TIME  datetime DEFAULT NULL,
   STATUS  varchar(255) NOT NULL,
   STATUS_BEFORE_SUSPENSION  varchar(255) DEFAULT NULL,
   TENANT_ID  int(11) NOT NULL,
   TASK_TYPE  varchar(255) NOT NULL,
   UPDATED_ON  datetime DEFAULT NULL,
   FAILURE_MESSAGE  bigint(20) DEFAULT NULL,
   INPUT_MESSAGE  bigint(20) DEFAULT NULL,
   OUTPUT_MESSAGE  bigint(20) DEFAULT NULL,
   PARENTTASK_ID  bigint(20) DEFAULT NULL,
  PRIMARY KEY ( id ),
  KEY  I_HT_TASK_FAILUREMESSAGE  ( FAILURE_MESSAGE ),
  KEY  I_HT_TASK_INPUTMESSAGE  ( INPUT_MESSAGE ),
  KEY  I_HT_TASK_OUTPUTMESSAGE  ( OUTPUT_MESSAGE ),
  KEY  I_HT_TASK_PARENTTASK  ( PARENTTASK_ID )
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table  HT_TASK_ATTACHMENT 
--

CREATE TABLE  HT_TASK_ATTACHMENT  (
   id  bigint(20) NOT NULL,
   ACCESS_TYPE  varchar(255) DEFAULT NULL,
   ATTACHED_AT  datetime DEFAULT NULL,
   CONTENT_TYPE  varchar(255) DEFAULT NULL,
   ATTACHMENT_NAME  varchar(255) DEFAULT NULL,
   ATTACHMENT_VALUE  varchar(255) DEFAULT NULL,
   TASK_ID  bigint(20) DEFAULT NULL,
   ATTACHED_BY  bigint(20) DEFAULT NULL,
  PRIMARY KEY ( id ),
  KEY  I_HT_TMNT_ATTACHEDBY  ( ATTACHED_BY ),
  KEY  I_HT_TMNT_TASK  ( TASK_ID )
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table  HT_TASK_COMMENT 
--

CREATE TABLE  HT_TASK_COMMENT  (
   id  bigint(20) NOT NULL,
   COMMENT_TEXT  varchar(4000) DEFAULT NULL,
   COMMENTED_BY  varchar(100) DEFAULT NULL,
   COMMENTED_ON  datetime DEFAULT NULL,
   MODIFIED_BY  varchar(100) DEFAULT NULL,
   MODIFIED_ON  datetime DEFAULT NULL,
   TASK_ID  bigint(20) DEFAULT NULL,
  PRIMARY KEY ( id ),
  KEY  I_HT_TMNT_TASK1  ( TASK_ID )
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


