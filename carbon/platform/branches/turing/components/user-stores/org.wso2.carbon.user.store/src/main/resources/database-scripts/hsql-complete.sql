
CREATE TABLE REG_CONTENT (
                    CONTENT_ID VARCHAR (50),
                    CONTENT_DATA BINARY,
                    PRIMARY KEY (CONTENT_ID));

CREATE TABLE REG_RESOURCE (
                    RID VARCHAR (50),
                    PATH VARCHAR (2000) NOT NULL,
                    MEDIA_TYPE VARCHAR (500),
                    COLLECTION INTEGER NOT NULL,
                    CREATOR VARCHAR (500),
                    CREATED_TIME TIMESTAMP,
                    LAST_UPDATOR VARCHAR (500),
                    LAST_UPDATED_TIME TIMESTAMP,
                    DESCRIPTION VARCHAR (10000),
                    CONTENT_ID VARCHAR (50),
                    EQUIVALENT_VERSION INTEGER NOT NULL,
                    ASSOCIATED_SNAPSHOT_ID INTEGER NOT NULL,
                    PRIMARY KEY (RID),
                    FOREIGN KEY (CONTENT_ID) REFERENCES REG_CONTENT (CONTENT_ID),
                    UNIQUE(PATH));

CREATE TABLE REG_DEPENDENCY (
                    DEPENDENCY_ID INTEGER GENERATED BY DEFAULT AS IDENTITY,
                    PARENT_RID VARCHAR (50) NOT NULL,
                    CHILD_RID VARCHAR (50) NOT NULL,
                    PRIMARY KEY (DEPENDENCY_ID),
                    UNIQUE (PARENT_RID, CHILD_RID),
                    FOREIGN KEY (PARENT_RID) REFERENCES REG_RESOURCE (RID) ON DELETE CASCADE,
                    FOREIGN KEY (CHILD_RID) REFERENCES REG_RESOURCE (RID) ON DELETE CASCADE);

CREATE TABLE REG_PROPERTY (
                    PROPERTY_ID INTEGER GENERATED BY DEFAULT AS IDENTITY,
                    RID VARCHAR (50) NOT NULL,
                    NAME VARCHAR (100) NOT NULL,
                    PROPERTY_VALUE VARCHAR (500),
                    PRIMARY KEY (PROPERTY_ID),
                    FOREIGN KEY (RID) REFERENCES REG_RESOURCE (RID) ON DELETE CASCADE);

CREATE TABLE REG_ASSOCIATION (
                    ASSOCIATION_ID INTEGER GENERATED BY DEFAULT AS IDENTITY,
                    SOURCEPATH VARCHAR (2000) NOT NULL,
                    TARGETPATH VARCHAR (2000) NOT NULL,
                    ASSOCIATION_TYPE VARCHAR (2000) NOT NULL,
                    PRIMARY KEY (ASSOCIATION_ID),
                    UNIQUE (SOURCEPATH, TARGETPATH, ASSOCIATION_TYPE));

CREATE TABLE REG_TAG (
                    TAG_ID INTEGER GENERATED BY DEFAULT AS IDENTITY,
                    TAG_NAME VARCHAR (500) NOT NULL,
                    RID VARCHAR (50) NOT NULL,
                    USER_ID VARCHAR (20) NOT NULL,
                    TAGGED_TIME TIMESTAMP NOT NULL,
                    PRIMARY KEY (TAG_ID),
                    UNIQUE (TAG_NAME, RID, USER_ID));

CREATE TABLE REG_COMMENT (
                    COMMENT_ID INTEGER GENERATED BY DEFAULT AS IDENTITY,
                    RID VARCHAR (50) NOT NULL,
                    USER_ID VARCHAR (20) NOT NULL,
                    COMMENT_TEXT VARCHAR (500) NOT NULL,
                    COMMENTED_TIME TIMESTAMP NOT NULL,
                    PRIMARY KEY (COMMENT_ID));

CREATE TABLE REG_RATING (
                    RATING_ID INTEGER GENERATED BY DEFAULT AS IDENTITY,
                    RID VARCHAR (50) NOT NULL,
                    USER_ID VARCHAR (20) NOT NULL,
                    RATING INTEGER NOT NULL,
                    RATED_TIME TIMESTAMP NOT NULL,
                    PRIMARY KEY (RATING_ID));

CREATE TABLE REG_LOG (
                    LOG_ID INTEGER GENERATED BY DEFAULT AS IDENTITY,
                    PATH VARCHAR (2000),
                    USER_ID VARCHAR (20) NOT NULL,
                    LOGGED_TIME TIMESTAMP NOT NULL,
                    ACTION INTEGER NOT NULL,
                    ACTION_DATA VARCHAR (500),
                    PRIMARY KEY (LOG_ID));

CREATE TABLE REG_CONTENT_VERSION (
                    CONTENT_VERSION_ID VARCHAR (50),
                    CONTENT_DATA BINARY,
                    PRIMARY KEY (CONTENT_VERSION_ID));

CREATE TABLE REG_RESOURCE_VERSION (
                    RESOURCE_VERSION_ID INTEGER GENERATED BY DEFAULT AS IDENTITY,
                    RID VARCHAR (50) NOT NULL,
                    VERSION INTEGER NOT NULL,
                    PATH VARCHAR (2000) NOT NULL,
                    MEDIA_TYPE VARCHAR (500),
                    COLLECTION INTEGER NOT NULL,
                    CREATOR VARCHAR (500),
                    CREATED_TIME TIMESTAMP,
                    LAST_UPDATOR VARCHAR (500),
                    LAST_UPDATED_TIME TIMESTAMP,
                    DESCRIPTION VARCHAR (10000),
                    CONTENT_ID VARCHAR (50),
                    ASSOCIATED_SNAPSHOT_ID INTEGER NOT NULL,
                    FOREIGN KEY (CONTENT_ID) REFERENCES REG_CONTENT_VERSION (CONTENT_VERSION_ID),
                    PRIMARY KEY (RESOURCE_VERSION_ID),
                    UNIQUE(RID, VERSION));

CREATE TABLE REG_DEPENDENCY_VERSION (
                    DEPENDENCY_VERSION_ID INTEGER GENERATED BY DEFAULT AS IDENTITY,
                    PARENT_RID VARCHAR (50) NOT NULL,
                    PARENT_VERSION INTEGER NOT NULL,
                    CHILD_RID VARCHAR (50) NOT NULL,
                    PRIMARY KEY (DEPENDENCY_VERSION_ID),
                    UNIQUE (PARENT_RID, PARENT_VERSION, CHILD_RID));

CREATE TABLE REG_PROPERTY_VERSION (
                    PROPERTY_VERSION_ID INTEGER GENERATED BY DEFAULT AS IDENTITY,
                    RID VARCHAR (50) NOT NULL,
                    VERSION INTEGER NOT NULL,
                    NAME VARCHAR (100) NOT NULL,
                    PROPERTY_VALUE VARCHAR (500),
                    PRIMARY KEY (PROPERTY_VERSION_ID),
                    FOREIGN KEY (RID, VERSION) REFERENCES REG_RESOURCE_VERSION (RID, VERSION));

CREATE TABLE REG_SNAPSHOT (
                    SNAPSHOT_ID INTEGER GENERATED BY DEFAULT AS IDENTITY,
                    ROOT_ID VARCHAR (50) NOT NULL,
                    PRIMARY KEY (SNAPSHOT_ID),
                    UNIQUE (SNAPSHOT_ID, ROOT_ID));

CREATE INDEX INDEX_SNAPSHOT_ROOT_ID ON REG_SNAPSHOT (ROOT_ID);

CREATE TABLE REG_SNAPSHOT_RESOURCE_VERSION (
                    SRV_ID INTEGER GENERATED BY DEFAULT AS IDENTITY,
                    SNAPSHOT_ID INTEGER NOT NULL,
                    RID VARCHAR (50) NOT NULL,
                    VERSION INTEGER NOT NULL,
                    PRIMARY KEY (SRV_ID),
                    UNIQUE (SNAPSHOT_ID, RID, VERSION));

CREATE TABLE UM_USERS (
			ID INTEGER GENERATED BY DEFAULT AS IDENTITY,
			USER_NAME VARCHAR(255) NOT NULL,
			USER_PASSWORD VARCHAR(255) NOT NULL,
			PRIMARY KEY (ID),
			UNIQUE(USER_NAME));

CREATE TABLE UM_USER_ATTRIBUTES (
			ID INTEGER GENERATED BY DEFAULT AS IDENTITY,
			ATTR_NAME VARCHAR(255) NOT NULL,
			ATTR_VALUE VARCHAR(255),
			USER_ID INTEGER,
			FOREIGN KEY (USER_ID) REFERENCES UM_USERS(ID) ON DELETE CASCADE,
			PRIMARY KEY (ID));

CREATE TABLE UM_ROLES (
			ID INTEGER GENERATED BY DEFAULT AS IDENTITY,
			ROLE_NAME VARCHAR(255) NOT NULL,
			PRIMARY KEY (ID),
			UNIQUE(ROLE_NAME));

CREATE TABLE UM_ROLE_ATTRIBUTES (
			ID INTEGER GENERATED BY DEFAULT AS IDENTITY,
			ATTR_NAME VARCHAR(255) NOT NULL,
			ATTR_VALUE VARCHAR(255),
			ROLE_ID INTEGER,
			FOREIGN KEY (ROLE_ID) REFERENCES UM_ROLES(ID) ON DELETE CASCADE,
			PRIMARY KEY (ID));

CREATE TABLE UM_PERMISSIONS (
			ID INTEGER GENERATED BY DEFAULT AS IDENTITY,
			RESOURCE_ID VARCHAR(255) NOT NULL,
			ACTION VARCHAR(255) NOT NULL,
			PRIMARY KEY (ID));

CREATE INDEX INDEX_UM_PERMISSIONS_RESOURCE_ID_ACTION ON UM_PERMISSIONS (RESOURCE_ID, ACTION);			

CREATE TABLE UM_ROLE_PERMISSIONS (
			ID INTEGER GENERATED BY DEFAULT AS IDENTITY,
			PERMISSION_ID INTEGER NOT NULL,
			ROLE_ID INTEGER NOT NULL,
			IS_ALLOWED SMALLINT NOT NULL,
			UNIQUE (PERMISSION_ID, ROLE_ID),
			FOREIGN KEY (PERMISSION_ID) REFERENCES UM_PERMISSIONS(ID) ON DELETE  CASCADE,
			FOREIGN KEY (ROLE_ID) REFERENCES UM_ROLES(ID) ON DELETE CASCADE,
			PRIMARY KEY (ID));

CREATE TABLE UM_USER_PERMISSIONS (
			ID INTEGER GENERATED BY DEFAULT AS IDENTITY,
			PERMISSION_ID INTEGER NOT NULL,
			USER_ID INTEGER NOT NULL,
			IS_ALLOWED SMALLINT NOT NULL,
			UNIQUE (PERMISSION_ID, USER_ID),
			FOREIGN KEY (PERMISSION_ID) REFERENCES UM_PERMISSIONS(ID) ON DELETE CASCADE,
			FOREIGN KEY (USER_ID) REFERENCES UM_USERS(ID) ON DELETE CASCADE,
			PRIMARY KEY (ID));

CREATE TABLE UM_USER_ROLES (
			ID INTEGER GENERATED BY DEFAULT AS IDENTITY,
			ROLE_ID INTEGER NOT NULL,
			USER_ID INTEGER NOT NULL,
			UNIQUE (USER_ID, ROLE_ID),
			FOREIGN KEY (ROLE_ID) REFERENCES UM_ROLES(ID) ON DELETE CASCADE,
			FOREIGN KEY (USER_ID) REFERENCES UM_USERS(ID) ON DELETE CASCADE,
			PRIMARY KEY (ID));
