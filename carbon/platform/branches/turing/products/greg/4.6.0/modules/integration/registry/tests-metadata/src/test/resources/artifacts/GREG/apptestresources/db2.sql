CREATE TABLE REG_CLUSTER_LOCK(
    REG_LOCK_NAME VARCHAR(20) NOT NULL,
    REG_LOCK_STATUS VARCHAR(20),
    REG_LOCKED_TIME TIMESTAMP,
    REG_TENANT_ID DECIMAL(31,0) DEFAULT 0,
    CONSTRAINT PK_REG_CLUSTER_LO1 PRIMARY KEY(REG_LOCK_NAME)
)/


CREATE TABLE REG_LOG(
    REG_LOG_ID DECIMAL(31,0) NOT NULL,
    REG_PATH VARCHAR(750),
    REG_USER_ID VARCHAR(31) NOT NULL,
    REG_LOGGED_TIME TIMESTAMP NOT NULL,
    REG_ACTION DECIMAL(31,0) NOT NULL,
    REG_ACTION_DATA VARCHAR(500),
    REG_TENANT_ID DECIMAL(31,0) DEFAULT 0 NOT NULL,
    CONSTRAINT PK_REG_LOG PRIMARY KEY(REG_LOG_ID,REG_TENANT_ID)
)/

CREATE SEQUENCE REG_LOG_SEQUENCE AS DECIMAL(27,0)
    INCREMENT BY 1
    START WITH 1
    NO CACHE/

CREATE TRIGGER REG_LOG_TRIGGER NO CASCADE BEFORE INSERT ON REG_LOG
REFERENCING NEW AS NEW FOR EACH ROW MODE DB2SQL 

BEGIN ATOMIC
    
    SET (NEW.REG_LOG_ID)
       = (NEXTVAL FOR REG_LOG_SEQUENCE);

END/


CREATE TABLE REG_PATH(
    REG_PATH_ID DECIMAL(31,0)  NOT NULL,
    REG_PATH_VALUE VARCHAR(750) NOT NULL,
    REG_PATH_PARENT_ID DECIMAL(31,0),
    REG_TENANT_ID DECIMAL(31,0) DEFAULT 0 NOT NULL,
    CONSTRAINT PK_PATH PRIMARY KEY(REG_PATH_ID,REG_TENANT_ID)
)/


-- CREATE INDEX REG_PATH_IND_BY_P1
  --  ON REG_PATH(REG_PATH_VALUE,REG_TENANT_ID)/


CREATE INDEX REG_PATH_IND_BY_P2
    ON REG_PATH(REG_PATH_PARENT_ID,REG_TENANT_ID)/

CREATE SEQUENCE REG_PATH_SEQUENCE AS DECIMAL(27,0)
    INCREMENT BY 1
    START WITH 1
    NO CACHE/

CREATE TRIGGER REG_PATH_TRIGGER NO CASCADE BEFORE INSERT ON REG_PATH
REFERENCING NEW AS NEW FOR EACH ROW MODE DB2SQL 

BEGIN ATOMIC
    
    SET (NEW.REG_PATH_ID)
       = (NEXTVAL FOR REG_PATH_SEQUENCE);

END/


CREATE TABLE REG_CONTENT(
    REG_CONTENT_ID DECIMAL(31,0) NOT NULL,
    REG_CONTENT_DATA BLOB(2G) NOT LOGGED,
    REG_TENANT_ID DECIMAL(31,0) DEFAULT 0 NOT NULL,
    CONSTRAINT PK_REG_CONTENT PRIMARY KEY(REG_CONTENT_ID,REG_TENANT_ID)
)/

CREATE SEQUENCE REG_CONTENT_SEQUENCE AS DECIMAL(27,0)
    INCREMENT BY 1
    START WITH 1
    NO CACHE/


CREATE TRIGGER REG_CONTENT_TRIGG1 NO CASCADE BEFORE INSERT ON REG_CONTENT
REFERENCING NEW AS NEW FOR EACH ROW MODE DB2SQL 

BEGIN ATOMIC
    
    SET (NEW.REG_CONTENT_ID)
       = (NEXTVAL FOR REG_CONTENT_SEQUENCE);

END/


CREATE TABLE REG_CONTENT_HISTORY(
    REG_CONTENT_ID DECIMAL(31,0) NOT NULL,
    REG_CONTENT_DATA BLOB(2G) NOT LOGGED,
    REG_DELETED SMALLINT,
    REG_TENANT_ID DECIMAL(31,0) DEFAULT 0 NOT NULL,
    CONSTRAINT PK_REG_CONTENT_HI1 PRIMARY KEY(REG_CONTENT_ID,REG_TENANT_ID)
)/


CREATE TABLE REG_RESOURCE(
    REG_PATH_ID DECIMAL(31,0) NOT NULL,
    REG_NAME VARCHAR(256),
    REG_VERSION DECIMAL(31,0) NOT NULL,
    REG_MEDIA_TYPE VARCHAR(500),
    REG_CREATOR VARCHAR(31) NOT NULL,
    REG_CREATED_TIME TIMESTAMP NOT NULL,
    REG_LAST_UPDATOR VARCHAR(31),
    REG_LAST_UPDATED_TIME TIMESTAMP NOT NULL,
    REG_DESCRIPTION VARCHAR(1000),
    REG_CONTENT_ID DECIMAL(31,0),
    REG_TENANT_ID DECIMAL(31,0) DEFAULT 0 NOT NULL,
    REG_UUID VARCHAR(100) NOT NULL,
    CONSTRAINT FK_REG_RES_PATH FOREIGN KEY(REG_PATH_ID,REG_TENANT_ID) REFERENCES REG_PATH(REG_PATH_ID,REG_TENANT_ID),
    CONSTRAINT PK_REG_RESOURCE PRIMARY KEY(REG_VERSION,REG_TENANT_ID)
)/

CREATE SEQUENCE REG_RESOURCE_SEQUENCE AS DECIMAL(27,0)
    INCREMENT BY 1
    START WITH 1
    NO CACHE/


CREATE TRIGGER REG_RESOURCE_TRIG1 NO CASCADE BEFORE INSERT ON REG_RESOURCE
REFERENCING NEW AS NEW FOR EACH ROW MODE DB2SQL 

BEGIN ATOMIC
    
    SET (NEW.REG_VERSION)
       = (NEXTVAL FOR REG_RESOURCE_SEQUENCE);

END/


CREATE INDEX REG_RESOURCE_IND_1
    ON REG_RESOURCE(REG_NAME,REG_TENANT_ID)/


CREATE INDEX REG_RESOURCE_IND_2
    ON REG_RESOURCE(REG_PATH_ID,REG_NAME,REG_TENANT_ID)/


CREATE TABLE REG_RESOURCE_HISTORY(
    REG_PATH_ID DECIMAL(31,0) NOT NULL,
    REG_NAME VARCHAR(256),
    REG_VERSION DECIMAL(31,0) NOT NULL,
    REG_MEDIA_TYPE VARCHAR(500),
    REG_CREATOR VARCHAR(31) NOT NULL,
    REG_CREATED_TIME TIMESTAMP NOT NULL,
    REG_LAST_UPDATOR VARCHAR(31),
    REG_LAST_UPDATED_TIME TIMESTAMP NOT NULL,
    REG_DESCRIPTION VARCHAR(1000),
    REG_CONTENT_ID DECIMAL(31,0),
    REG_DELETED SMALLINT,
    REG_TENANT_ID DECIMAL(31,0) DEFAULT 0 NOT NULL,
    REG_UUID VARCHAR(100) NOT NULL,
    FOREIGN KEY(REG_PATH_ID,REG_TENANT_ID) REFERENCES REG_PATH(REG_PATH_ID,REG_TENANT_ID),
    FOREIGN KEY(REG_CONTENT_ID,REG_TENANT_ID) REFERENCES REG_CONTENT_HISTORY(REG_CONTENT_ID,REG_TENANT_ID),
    CONSTRAINT PK_REG_RESOURCE_H1 PRIMARY KEY(REG_VERSION,REG_TENANT_ID)
)/


CREATE INDEX REG_RES_HIST_IND_1
    ON REG_RESOURCE_HISTORY(REG_NAME,REG_TENANT_ID)/


CREATE INDEX REG_RES_HIST_IND_2
    ON REG_RESOURCE_HISTORY(REG_PATH_ID,REG_NAME,REG_TENANT_ID)/


CREATE TABLE REG_COMMENT(
    REG_ID DECIMAL(31,0) NOT NULL,
    REG_COMMENT_TEXT VARCHAR(500) NOT NULL,
    REG_USER_ID VARCHAR(31) NOT NULL,
    REG_COMMENTED_TIME TIMESTAMP NOT NULL,
    REG_TENANT_ID DECIMAL(31,0) DEFAULT 0 NOT NULL,
    CONSTRAINT PK_REG_COMMENT PRIMARY KEY(REG_ID,REG_TENANT_ID)
)/

CREATE SEQUENCE REG_COMMENT_SEQUENCE AS DECIMAL(27,0)
    INCREMENT BY 1
    START WITH 1
    NO CACHE/


CREATE TRIGGER REG_COMMENT_TRIGG1 NO CASCADE BEFORE INSERT ON REG_COMMENT
REFERENCING NEW AS NEW FOR EACH ROW MODE DB2SQL 

BEGIN ATOMIC
    
    SET (NEW.REG_ID)
       = (NEXTVAL FOR REG_COMMENT_SEQUENCE);

END/

CREATE TABLE REG_RESOURCE_COMMENT(
    REG_COMMENT_ID DECIMAL(31,0) NOT NULL,
    REG_VERSION DECIMAL(31,0),
    REG_PATH_ID DECIMAL(31,0),
    REG_RESOURCE_NAME VARCHAR(256),
    REG_TENANT_ID DECIMAL(31,0) DEFAULT 0,
    FOREIGN KEY(REG_PATH_ID,REG_TENANT_ID) REFERENCES REG_PATH(REG_PATH_ID,REG_TENANT_ID),
    FOREIGN KEY(REG_COMMENT_ID,REG_TENANT_ID) REFERENCES REG_COMMENT(REG_ID,REG_TENANT_ID)
)/


CREATE INDEX REG_RES_COMM_BY_P1
    ON REG_RESOURCE_COMMENT(REG_PATH_ID,REG_RESOURCE_NAME,REG_TENANT_ID)/


CREATE INDEX REG_RES_COMM_BY_V1
    ON REG_RESOURCE_COMMENT(REG_VERSION,REG_TENANT_ID)/


CREATE TABLE REG_RATING(
    REG_ID DECIMAL(31,0) NOT NULL,
    REG_RATING DECIMAL(31,0) NOT NULL,
    REG_USER_ID VARCHAR(31) NOT NULL,
    REG_RATED_TIME TIMESTAMP NOT NULL,
    REG_TENANT_ID DECIMAL(31,0) DEFAULT 0 NOT NULL,
    CONSTRAINT PK_REG_RATING PRIMARY KEY(REG_ID,REG_TENANT_ID)
)/

CREATE SEQUENCE REG_RATING_SEQUENCE AS DECIMAL(27,0)
    INCREMENT BY 1
    START WITH 1
    NO CACHE/

CREATE TRIGGER REG_RATING_TRIGGER NO CASCADE BEFORE INSERT ON REG_RATING
REFERENCING NEW AS NEW FOR EACH ROW MODE DB2SQL 

BEGIN ATOMIC
    
    SET (NEW.REG_ID)
       = (NEXTVAL FOR REG_RATING_SEQUENCE);

END/

CREATE TABLE REG_RESOURCE_RATING(
    REG_RATING_ID DECIMAL(31,0) NOT NULL,
    REG_VERSION DECIMAL(31,0),
    REG_PATH_ID DECIMAL(31,0),
    REG_RESOURCE_NAME VARCHAR(256),
    REG_TENANT_ID DECIMAL(31,0) DEFAULT 0,
    FOREIGN KEY(REG_PATH_ID,REG_TENANT_ID) REFERENCES REG_PATH(REG_PATH_ID,REG_TENANT_ID),
    FOREIGN KEY(REG_RATING_ID,REG_TENANT_ID) REFERENCES REG_RATING(REG_ID,REG_TENANT_ID)
)/


CREATE INDEX REG_RATING_IND_BY1
    ON REG_RESOURCE_RATING(REG_PATH_ID,REG_RESOURCE_NAME,REG_TENANT_ID)/


CREATE INDEX REG_RATING_IND_BY2
    ON REG_RESOURCE_RATING(REG_VERSION,REG_TENANT_ID)/


CREATE TABLE REG_TAG(
    REG_ID DECIMAL(31,0) NOT NULL,
    REG_TAG_NAME VARCHAR(500) NOT NULL,
    REG_USER_ID VARCHAR(31) NOT NULL,
    REG_TAGGED_TIME TIMESTAMP NOT NULL,
    REG_TENANT_ID DECIMAL(31,0) DEFAULT 0 NOT NULL,
    CONSTRAINT PK_REG_TAG PRIMARY KEY(REG_ID,REG_TENANT_ID)
)/

CREATE SEQUENCE REG_TAG_SEQUENCE AS DECIMAL(27,0)
    INCREMENT BY 1
    START WITH 1
    NO CACHE/

CREATE TRIGGER REG_TAG_TRIGGER NO CASCADE BEFORE INSERT ON REG_TAG
REFERENCING NEW AS NEW FOR EACH ROW MODE DB2SQL 

BEGIN ATOMIC
    
    SET (NEW.REG_ID)
       = (NEXTVAL FOR REG_TAG_SEQUENCE);

END/

CREATE TABLE REG_RESOURCE_TAG(
    REG_TAG_ID DECIMAL(31,0) NOT NULL,
    REG_VERSION DECIMAL(31,0),
    REG_PATH_ID DECIMAL(31,0),
    REG_RESOURCE_NAME VARCHAR(256),
    REG_TENANT_ID DECIMAL(31,0) DEFAULT 0,
    FOREIGN KEY(REG_PATH_ID,REG_TENANT_ID) REFERENCES REG_PATH(REG_PATH_ID,REG_TENANT_ID),
    FOREIGN KEY(REG_TAG_ID,REG_TENANT_ID) REFERENCES REG_TAG(REG_ID,REG_TENANT_ID)
)/


CREATE INDEX REG_TAG_IND_BY_PA1
    ON REG_RESOURCE_TAG(REG_PATH_ID,REG_RESOURCE_NAME,REG_TENANT_ID)/


CREATE INDEX REG_TAG_IND_BY_VE1
    ON REG_RESOURCE_TAG(REG_VERSION,REG_TENANT_ID)/


CREATE TABLE REG_PROPERTY(
    REG_ID DECIMAL(31,0) NOT NULL,
    REG_NAME VARCHAR(100) NOT NULL,
    REG_VALUE VARCHAR(1000),
    REG_TENANT_ID DECIMAL(31,0) DEFAULT 0 NOT NULL,
    CONSTRAINT PK_REG_PROPERTY PRIMARY KEY(REG_ID,REG_TENANT_ID)
)/

CREATE SEQUENCE REG_PROPERTY_SEQUENCE AS DECIMAL(27,0)
    INCREMENT BY 1
    START WITH 1
    NO CACHE/


CREATE TRIGGER REG_PROPERTY_TRIG1 NO CASCADE BEFORE INSERT ON REG_PROPERTY
REFERENCING NEW AS NEW FOR EACH ROW MODE DB2SQL 

BEGIN ATOMIC
    
    SET (NEW.REG_ID)
       = (NEXTVAL FOR REG_PROPERTY_SEQUENCE);

END/

CREATE TABLE REG_RESOURCE_PROPERTY(
    REG_PROPERTY_ID DECIMAL(31,0) NOT NULL,
    REG_VERSION DECIMAL(31,0),
    REG_PATH_ID DECIMAL(31,0),
    REG_RESOURCE_NAME VARCHAR(256),
    REG_TENANT_ID DECIMAL(31,0) DEFAULT 0,
    FOREIGN KEY(REG_PATH_ID,REG_TENANT_ID) REFERENCES REG_PATH(REG_PATH_ID,REG_TENANT_ID),
    FOREIGN KEY(REG_PROPERTY_ID,REG_TENANT_ID) REFERENCES REG_PROPERTY(REG_ID,REG_TENANT_ID)
)/


CREATE INDEX REG_RESC_PROP_BY_1
    ON REG_RESOURCE_PROPERTY(REG_PROPERTY_ID,REG_VERSION,REG_TENANT_ID)/


CREATE INDEX REG_RESC_PROP_BY_2
    ON REG_RESOURCE_PROPERTY(REG_PROPERTY_ID,REG_PATH_ID,REG_RESOURCE_NAME,REG_TENANT_ID)/


CREATE TABLE REG_ASSOCIATION(
    REG_ASSOCIATION_ID DECIMAL(31,0) NOT NULL,
    REG_SOURCEPATH VARCHAR(750) NOT NULL,
    REG_TARGETPATH VARCHAR(750) NOT NULL,
    REG_ASSOCIATION_TYPE VARCHAR(2000) NOT NULL,
    REG_TENANT_ID DECIMAL(31,0) DEFAULT 0 NOT NULL,
    CONSTRAINT PK_REG_ASSOCIATION PRIMARY KEY(REG_ASSOCIATION_ID,REG_TENANT_ID)
)/

CREATE SEQUENCE REG_ASSOCIATION_SEQUENCE AS DECIMAL(27,0)
    INCREMENT BY 1
    START WITH 1
    NO CACHE/


CREATE TRIGGER REG_ASSOCIATION_T1 NO CASCADE BEFORE INSERT ON REG_ASSOCIATION
REFERENCING NEW AS NEW FOR EACH ROW MODE DB2SQL 

BEGIN ATOMIC
    
    SET (NEW.REG_ASSOCIATION_ID)
       = (NEXTVAL FOR REG_ASSOCIATION_SEQUENCE);

END/


CREATE TABLE REG_SNAPSHOT(
    REG_SNAPSHOT_ID DECIMAL(31,0) NOT NULL,
    REG_PATH_ID DECIMAL(31,0) NOT NULL,
    REG_RESOURCE_NAME VARCHAR(256),
    REG_RESOURCE_VIDS BLOB(2G) NOT LOGGED NOT NULL,
    REG_TENANT_ID DECIMAL(31,0) DEFAULT 0 NOT NULL,
    FOREIGN KEY(REG_PATH_ID,REG_TENANT_ID) REFERENCES REG_PATH(REG_PATH_ID,REG_TENANT_ID),
    CONSTRAINT PK_REG_SNAPSHOT PRIMARY KEY(REG_SNAPSHOT_ID,REG_TENANT_ID)
)/


CREATE INDEX REG_SNAPSHOT_PATH1
    ON REG_SNAPSHOT(REG_PATH_ID,REG_RESOURCE_NAME,REG_TENANT_ID)/

CREATE SEQUENCE REG_SNAPSHOT_SEQUENCE AS DECIMAL(27,0)
    INCREMENT BY 1
    START WITH 1
    NO CACHE/


CREATE TRIGGER REG_SNAPSHOT_TRIG1 NO CASCADE BEFORE INSERT ON REG_SNAPSHOT
REFERENCING NEW AS NEW FOR EACH ROW MODE DB2SQL 

BEGIN ATOMIC
    
    SET (NEW.REG_SNAPSHOT_ID)
       = (NEXTVAL FOR REG_SNAPSHOT_SEQUENCE);

END/


CREATE TABLE UM_TENANT(
    UM_ID DECIMAL(31,0) NOT NULL,
    UM_DOMAIN_NAME VARCHAR(255) NOT NULL,
    UM_EMAIL VARCHAR(255),
    UM_ACTIVE SMALLINT DEFAULT 0,
    UM_CREATED_DATE TIMESTAMP NOT NULL,
    UM_USER_CONFIG BLOB(2G) NOT LOGGED,
    PRIMARY KEY(UM_ID),
    UNIQUE(UM_DOMAIN_NAME)
)/

CREATE SEQUENCE UM_TENANT_SEQUENCE AS DECIMAL(27,0)
    INCREMENT BY 1
    START WITH 1
    NO CACHE/

CREATE TRIGGER UM_TENANT_TRIGGER NO CASCADE BEFORE INSERT ON UM_TENANT
REFERENCING NEW AS NEW FOR EACH ROW MODE DB2SQL 

BEGIN ATOMIC
    
    SET (NEW.UM_ID)
       = (NEXTVAL FOR UM_TENANT_SEQUENCE);

END/


CREATE TABLE UM_USER(
    UM_ID DECIMAL(31,0) NOT NULL,
    UM_USER_NAME VARCHAR(255) NOT NULL,
    UM_USER_PASSWORD VARCHAR(255) NOT NULL,
    UM_SALT_VALUE VARCHAR(31),
    UM_REQUIRE_CHANGE SMALLINT DEFAULT 0,
    UM_CHANGED_TIME TIMESTAMP NOT NULL,
    UM_TENANT_ID DECIMAL(31,0) DEFAULT 0 NOT NULL,
    PRIMARY KEY(UM_ID,UM_TENANT_ID),
    UNIQUE(UM_USER_NAME,UM_TENANT_ID)
)/

CREATE SEQUENCE UM_USER_SEQUENCE AS DECIMAL(27,0)
    INCREMENT BY 1
    START WITH 1
    NO CACHE/

CREATE TRIGGER UM_USER_TRIGGER NO CASCADE BEFORE INSERT ON UM_USER
REFERENCING NEW AS NEW FOR EACH ROW MODE DB2SQL 

BEGIN ATOMIC
    
    SET (NEW.UM_ID)
       = (NEXTVAL FOR UM_USER_SEQUENCE);

END/


CREATE TABLE UM_USER_ATTRIBUTE(
    UM_ID DECIMAL(31,0) NOT NULL,
    UM_ATTR_NAME VARCHAR(255) NOT NULL,
    UM_ATTR_VALUE VARCHAR(255),
    UM_PROFILE_ID VARCHAR(255),
    UM_USER_ID DECIMAL(31,0),
    UM_TENANT_ID DECIMAL(31,0) DEFAULT 0 NOT NULL,
    FOREIGN KEY(UM_USER_ID,UM_TENANT_ID) REFERENCES UM_USER(UM_ID,UM_TENANT_ID) ON DELETE CASCADE ,
    PRIMARY KEY(UM_ID,UM_TENANT_ID)
)/

CREATE SEQUENCE UM_USER_ATTRIBUTE_SEQUENCE AS DECIMAL(27,0)
    INCREMENT BY 1
    START WITH 1
    NO CACHE/


CREATE TRIGGER UM_USER_ATTRIBUTE1 NO CASCADE BEFORE INSERT ON UM_USER_ATTRIBUTE
REFERENCING NEW AS NEW FOR EACH ROW MODE DB2SQL 

BEGIN ATOMIC
    
    SET (NEW.UM_ID)
       = (NEXTVAL FOR UM_USER_ATTRIBUTE_SEQUENCE);

END/


CREATE TABLE UM_ROLE(
    UM_ID DECIMAL(31,0) NOT NULL,
    UM_ROLE_NAME VARCHAR(255) NOT NULL,
    UM_TENANT_ID DECIMAL(31,0) DEFAULT 0 NOT NULL,
    PRIMARY KEY(UM_ID,UM_TENANT_ID),
    UNIQUE(UM_ROLE_NAME,UM_TENANT_ID)
)/

CREATE SEQUENCE UM_ROLE_SEQUENCE AS DECIMAL(27,0)
    INCREMENT BY 1
    START WITH 1
    NO CACHE/

CREATE TRIGGER UM_ROLE_TRIGGER NO CASCADE BEFORE INSERT ON UM_ROLE
REFERENCING NEW AS NEW FOR EACH ROW MODE DB2SQL 

BEGIN ATOMIC
    
    SET (NEW.UM_ID)
       = (NEXTVAL FOR UM_ROLE_SEQUENCE);

END/


CREATE TABLE UM_PERMISSION(
    UM_ID DECIMAL(31,0) NOT NULL,
    UM_RESOURCE_ID VARCHAR(255) NOT NULL,
    UM_ACTION VARCHAR(255) NOT NULL,
    UM_TENANT_ID DECIMAL(31,0) DEFAULT 0 NOT NULL,
    PRIMARY KEY(UM_ID,UM_TENANT_ID)
)/

CREATE SEQUENCE UM_PERMISSION_SEQUENCE AS DECIMAL(27,0)
    INCREMENT BY 1
    START WITH 1
    NO CACHE/


CREATE TRIGGER UM_PERMISSION_TRI1 NO CASCADE BEFORE INSERT ON UM_PERMISSION
REFERENCING NEW AS NEW FOR EACH ROW MODE DB2SQL 

BEGIN ATOMIC
    
    SET (NEW.UM_ID)
       = (NEXTVAL FOR UM_PERMISSION_SEQUENCE);

END/

CREATE INDEX INDEX_UM_PERMISSION_UM_RESOURCE_ID_UM_ACTION
    ON UM_PERMISSION (UM_RESOURCE_ID, UM_ACTION, UM_TENANT_ID)/

CREATE TABLE UM_ROLE_PERMISSION(
    UM_ID DECIMAL(31,0) NOT NULL,
    UM_PERMISSION_ID DECIMAL(31,0) NOT NULL,
    UM_ROLE_NAME VARCHAR(255) NOT NULL,
    UM_IS_ALLOWED SMALLINT NOT NULL,
    UM_TENANT_ID DECIMAL(31,0) DEFAULT 0 NOT NULL,
    UNIQUE(UM_PERMISSION_ID,UM_ROLE_NAME,UM_TENANT_ID),
    FOREIGN KEY(UM_PERMISSION_ID,UM_TENANT_ID) REFERENCES UM_PERMISSION(UM_ID,UM_TENANT_ID) ON DELETE CASCADE ,
    PRIMARY KEY(UM_ID,UM_TENANT_ID)
)/

CREATE SEQUENCE UM_ROLE_PERMISSION_SEQUENCE AS DECIMAL(27,0)
    INCREMENT BY 1
    START WITH 1
    NO CACHE/


CREATE TRIGGER UM_ROLE_PERMISSIO1 NO CASCADE BEFORE INSERT ON UM_ROLE_PERMISSION
REFERENCING NEW AS NEW FOR EACH ROW MODE DB2SQL 

BEGIN ATOMIC
    
    SET (NEW.UM_ID)
       = (NEXTVAL FOR UM_ROLE_PERMISSION_SEQUENCE);

END/


CREATE TABLE UM_USER_PERMISSION(
    UM_ID DECIMAL(31,0) NOT NULL,
    UM_PERMISSION_ID DECIMAL(31,0) NOT NULL,
    UM_USER_NAME VARCHAR(255) NOT NULL,
    UM_IS_ALLOWED SMALLINT NOT NULL,
    UM_TENANT_ID DECIMAL(31,0) DEFAULT 0 NOT NULL,
    UNIQUE(UM_PERMISSION_ID,UM_USER_NAME,UM_TENANT_ID),
    FOREIGN KEY(UM_PERMISSION_ID,UM_TENANT_ID) REFERENCES UM_PERMISSION(UM_ID,UM_TENANT_ID) ON DELETE CASCADE ,
    PRIMARY KEY(UM_ID,UM_TENANT_ID)
)/

CREATE SEQUENCE UM_USER_PERMISSION_SEQUENCE AS DECIMAL(27,0)
    INCREMENT BY 1
    START WITH 1
    NO CACHE/


CREATE TRIGGER UM_USER_PERMISSIO1 NO CASCADE BEFORE INSERT ON UM_USER_PERMISSION
REFERENCING NEW AS NEW FOR EACH ROW MODE DB2SQL 

BEGIN ATOMIC
    
    SET (NEW.UM_ID)
       = (NEXTVAL FOR UM_USER_PERMISSION_SEQUENCE);

END/


CREATE TABLE UM_USER_ROLE(
    UM_ID DECIMAL(31,0) NOT NULL,
    UM_ROLE_ID DECIMAL(31,0) NOT NULL,
    UM_USER_ID DECIMAL(31,0) NOT NULL,
    UM_TENANT_ID DECIMAL(31,0) DEFAULT 0 NOT NULL,
    UNIQUE(UM_USER_ID,UM_ROLE_ID,UM_TENANT_ID),
    FOREIGN KEY(UM_ROLE_ID,UM_TENANT_ID) REFERENCES UM_ROLE(UM_ID,UM_TENANT_ID) ON DELETE CASCADE ,
    FOREIGN KEY(UM_USER_ID,UM_TENANT_ID) REFERENCES UM_USER(UM_ID,UM_TENANT_ID) ON DELETE CASCADE ,
    PRIMARY KEY(UM_ID,UM_TENANT_ID)
)/

CREATE SEQUENCE UM_USER_ROLE_SEQUENCE AS DECIMAL(27,0)
    INCREMENT BY 1
    START WITH 1
    NO CACHE/


CREATE TRIGGER UM_USER_ROLE_TRIG1 NO CASCADE BEFORE INSERT ON UM_USER_ROLE
REFERENCING NEW AS NEW FOR EACH ROW MODE DB2SQL 

BEGIN ATOMIC
    
    SET (NEW.UM_ID)
       = (NEXTVAL FOR UM_USER_ROLE_SEQUENCE);

END/


CREATE TABLE HYBRID_ROLE(
    UM_ID DECIMAL(31,0) NOT NULL,
    UM_ROLE_ID VARCHAR(255) NOT NULL,
    UM_TENANT_ID DECIMAL(31,0) DEFAULT 0 NOT NULL,
    PRIMARY KEY(UM_ID,UM_TENANT_ID),
    UNIQUE(UM_ROLE_ID,UM_TENANT_ID)
)/

CREATE SEQUENCE HYBRID_ROLE_SEQUENCE AS DECIMAL(27,0)
    INCREMENT BY 1
    START WITH 1
    NO CACHE/


CREATE TRIGGER HYBRID_ROLE_TRIGG1 NO CASCADE BEFORE INSERT ON HYBRID_ROLE
REFERENCING NEW AS NEW FOR EACH ROW MODE DB2SQL 

BEGIN ATOMIC
    
    SET (NEW.UM_ID)
       = (NEXTVAL FOR HYBRID_ROLE_SEQUENCE);

END/


CREATE TABLE HYBRID_USER_ROLE(
    UM_ID DECIMAL(31,0) NOT NULL,
    UM_USER_ID VARCHAR(255),
    UM_ROLE_ID VARCHAR(255) NOT NULL,
    UM_TENANT_ID DECIMAL(31,0) DEFAULT 0 NOT NULL,
    PRIMARY KEY(UM_ID,UM_TENANT_ID)
)/

CREATE SEQUENCE HYBRID_USER_ROLE_SEQUENCE AS DECIMAL(27,0)
    INCREMENT BY 1
    START WITH 1
    NO CACHE/


CREATE TRIGGER HYBRID_USER_ROLE_1 NO CASCADE BEFORE INSERT ON HYBRID_USER_ROLE
REFERENCING NEW AS NEW FOR EACH ROW MODE DB2SQL 

BEGIN ATOMIC
    
    SET (NEW.UM_ID)
       = (NEXTVAL FOR HYBRID_USER_ROLE_SEQUENCE);

END/


CREATE TABLE UM_DIALECT(
    UM_ID DECIMAL(31,0) NOT NULL,
    UM_DIALECT_URI VARCHAR(255) NOT NULL,
    UM_TENANT_ID DECIMAL(31,0) DEFAULT 0 NOT NULL,
    UNIQUE(UM_DIALECT_URI,UM_TENANT_ID),
    PRIMARY KEY(UM_ID,UM_TENANT_ID)
)/

CREATE SEQUENCE UM_DIALECT_SEQUENCE AS DECIMAL(27,0)
    INCREMENT BY 1
    START WITH 1
    NO CACHE/

CREATE TRIGGER UM_DIALECT_TRIGGER NO CASCADE BEFORE INSERT ON UM_DIALECT
REFERENCING NEW AS NEW FOR EACH ROW MODE DB2SQL 

BEGIN ATOMIC
    
    SET (NEW.UM_ID)
       = (NEXTVAL FOR UM_DIALECT_SEQUENCE);

END/


CREATE TABLE UM_CLAIM(
    UM_ID DECIMAL(31,0) NOT NULL,
    UM_DIALECT_ID DECIMAL(31,0) NOT NULL,
    UM_CLAIM_URI VARCHAR(255) NOT NULL,
    UM_DISPLAY_TAG VARCHAR(255),
    UM_DESCRIPTION VARCHAR(255),
    UM_MAPPED_ATTRIBUTE VARCHAR(255),
    UM_REG_EX VARCHAR(255),
    UM_SUPPORTED SMALLINT,
    UM_REQUIRED SMALLINT,
    UM_DISPLAY_ORDER DECIMAL(31,0),
    UM_TENANT_ID DECIMAL(31,0) DEFAULT 0 NOT NULL,
    UNIQUE(UM_DIALECT_ID,UM_CLAIM_URI,UM_TENANT_ID),
    FOREIGN KEY(UM_DIALECT_ID,UM_TENANT_ID) REFERENCES UM_DIALECT(UM_ID,UM_TENANT_ID),
    PRIMARY KEY(UM_ID,UM_TENANT_ID)
)/

CREATE SEQUENCE UM_CLAIM_SEQUENCE AS DECIMAL(27,0)
    INCREMENT BY 1
    START WITH 1
    NO CACHE/

CREATE TRIGGER UM_CLAIM_TRIGGER NO CASCADE BEFORE INSERT ON UM_CLAIM
REFERENCING NEW AS NEW FOR EACH ROW MODE DB2SQL 

BEGIN ATOMIC
    
    SET (NEW.UM_ID)
       = (NEXTVAL FOR UM_CLAIM_SEQUENCE);

END/


CREATE TABLE UM_PROFILE_CONFIG(
    UM_ID DECIMAL(31,0) NOT NULL,
    UM_DIALECT_ID DECIMAL(31,0),
    UM_PROFILE_NAME VARCHAR(255),
    UM_TENANT_ID DECIMAL(31,0) DEFAULT 0 NOT NULL,
    FOREIGN KEY(UM_DIALECT_ID,UM_TENANT_ID) REFERENCES UM_DIALECT(UM_ID,UM_TENANT_ID),
    PRIMARY KEY(UM_ID,UM_TENANT_ID)
)/

CREATE SEQUENCE UM_PROFILE_CONFIG_SEQUENCE AS DECIMAL(27,0)
    INCREMENT BY 1
    START WITH 1
    NO CACHE/


CREATE TRIGGER UM_PROFILE_CONFIG1 NO CASCADE BEFORE INSERT ON UM_PROFILE_CONFIG
REFERENCING NEW AS NEW FOR EACH ROW MODE DB2SQL 

BEGIN ATOMIC
    
    SET (NEW.UM_ID)
       = (NEXTVAL FOR UM_PROFILE_CONFIG_SEQUENCE);

END/


CREATE TABLE UM_CLAIM_BEHAVIOR(
    UM_ID DECIMAL(31,0) NOT NULL,
    UM_PROFILE_ID DECIMAL(31,0),
    UM_CLAIM_ID DECIMAL(31,0),
    UM_BEHAVIOUR SMALLINT,
    UM_TENANT_ID DECIMAL(31,0) DEFAULT 0 NOT NULL,
    FOREIGN KEY(UM_PROFILE_ID,UM_TENANT_ID) REFERENCES UM_PROFILE_CONFIG(UM_ID,UM_TENANT_ID),
    FOREIGN KEY(UM_CLAIM_ID,UM_TENANT_ID) REFERENCES UM_CLAIM(UM_ID,UM_TENANT_ID),
    PRIMARY KEY(UM_ID,UM_TENANT_ID)
)/

CREATE SEQUENCE UM_CLAIM_BEHAVIOR_SEQUENCE AS DECIMAL(27,0)
    INCREMENT BY 1
    START WITH 1
    NO CACHE/


CREATE TRIGGER UM_CLAIM_BEHAVIOR1 NO CASCADE BEFORE INSERT ON UM_CLAIM_BEHAVIOR
REFERENCING NEW AS NEW FOR EACH ROW MODE DB2SQL 

BEGIN ATOMIC
    
    SET (NEW.UM_ID)
       = (NEXTVAL FOR UM_CLAIM_BEHAVIOR_SEQUENCE);

END/


CREATE TABLE UM_HYBRID_ROLE(
    UM_ID DECIMAL(31,0) NOT NULL,
    UM_ROLE_NAME VARCHAR(255),
    UM_TENANT_ID DECIMAL(31,0) DEFAULT 0 NOT NULL,
    PRIMARY KEY(UM_ID,UM_TENANT_ID)
)/

CREATE SEQUENCE UM_HYBRID_ROLE_SEQUENCE AS DECIMAL(27,0)
    INCREMENT BY 1
    START WITH 1
    NO CACHE/


CREATE TRIGGER UM_HYBRID_ROLE_TR1 NO CASCADE BEFORE INSERT ON UM_HYBRID_ROLE
REFERENCING NEW AS NEW FOR EACH ROW MODE DB2SQL 

BEGIN ATOMIC
    
    SET (NEW.UM_ID)
       = (NEXTVAL FOR UM_HYBRID_ROLE_SEQUENCE);

END/


CREATE TABLE UM_HYBRID_USER_ROLE(
    UM_ID DECIMAL(31,0) NOT NULL,
    UM_USER_NAME VARCHAR(255) NOT NULL,
    UM_ROLE_ID DECIMAL(31,0) NOT NULL,
    UM_TENANT_ID DECIMAL(31,0) DEFAULT 0 NOT NULL,
    UNIQUE(UM_USER_NAME,UM_ROLE_ID,UM_TENANT_ID),
    FOREIGN KEY(UM_ROLE_ID,UM_TENANT_ID) REFERENCES UM_HYBRID_ROLE(UM_ID,UM_TENANT_ID),
    PRIMARY KEY(UM_ID,UM_TENANT_ID)
)/

CREATE SEQUENCE UM_HYBRID_USER_ROLE_SEQUENCE AS DECIMAL(27,0)
    INCREMENT BY 1
    START WITH 1
    NO CACHE/


CREATE TRIGGER UM_HYBRID_USER_RO1 NO CASCADE BEFORE INSERT ON UM_HYBRID_USER_ROLE
REFERENCING NEW AS NEW FOR EACH ROW MODE DB2SQL 

BEGIN ATOMIC
    
    SET (NEW.UM_ID)
       = (NEXTVAL FOR UM_HYBRID_USER_ROLE_SEQUENCE);

END/


CREATE TABLE UM_HYBRID_REMEMBER_ME (
            UM_ID DECIMAL(31,0) NOT NULL,
			UM_USER_NAME VARCHAR(255) NOT NULL,
			UM_COOKIE_VALUE VARCHAR(1024),
			UM_CREATED_TIME TIMESTAMP,
            UM_TENANT_ID INTEGER DEFAULT 0 NOT NULL,
			PRIMARY KEY (UM_ID, UM_TENANT_ID)
)/

CREATE SEQUENCE UM_HYBRID_REMEMBER_ME_SEQUENCE AS DECIMAL(27,0)
    INCREMENT BY 1
    START WITH 1
    NO CACHE/


CREATE TRIGGER UMHYBRID_REMEMB_ME NO CASCADE BEFORE INSERT ON UM_HYBRID_REMEMBER_ME
REFERENCING NEW AS NEW FOR EACH ROW MODE DB2SQL 

BEGIN ATOMIC
    
    SET (NEW.UM_ID)
       = (NEXTVAL FOR UM_HYBRID_REMEMBER_ME_SEQUENCE);

END/

