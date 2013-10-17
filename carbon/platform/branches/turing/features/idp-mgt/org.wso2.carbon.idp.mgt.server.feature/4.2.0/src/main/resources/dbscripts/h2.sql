CREATE TABLE IF NOT EXISTS IDP_BASE_TABLE (
            NAME VARCHAR (20),
            PRIMARY KEY (NAME)
);

INSERT INTO IDP_BASE_TABLE values ('IdP');

CREATE TABLE IF NOT EXISTS UM_TENANT_IDP (
			UM_ID INTEGER AUTO_INCREMENT,
			UM_TENANT_ID INTEGER,
			UM_TENANT_IDP_NAME VARCHAR(512),
			UM_TENANT_IDP_ISSUER VARCHAR(512),
            		UM_TENANT_IDP_URL VARCHAR(2048),
			UM_TENANT_IDP_THUMBPRINT VARCHAR(2048),
			UM_TENANT_IDP_PRIMARY CHAR(1)  NOT NULL,
			UM_TENANT_IDP_AUDIENCE VARCHAR(2048),
			UM_TENANT_IDP_TOKEN_EP_ALIAS VARCHAR(2048),
			PRIMARY KEY (UM_ID),
			UNIQUE (UM_TENANT_ID, UM_TENANT_IDP_NAME));

CREATE TABLE IF NOT EXISTS UM_TENANT_IDP_ROLES (
			UM_ID INTEGER AUTO_INCREMENT,
			UM_TENANT_IDP_ID INTEGER,
			UM_TENANT_IDP_ROLE VARCHAR(512),
			PRIMARY KEY (UM_ID),
			UNIQUE (UM_TENANT_IDP_ID, UM_TENANT_IDP_ROLE),
			FOREIGN KEY (UM_TENANT_IDP_ID) REFERENCES UM_TENANT_IDP(UM_ID) ON DELETE CASCADE);

CREATE TABLE IF NOT EXISTS UM_TENANT_IDP_ROLE_MAPPINGS (
			UM_ID INTEGER AUTO_INCREMENT,
			UM_TENANT_IDP_ROLE_ID INTEGER,
			UM_TENANT_ID INTEGER,
			UM_TENANT_ROLE VARCHAR(512),	
			PRIMARY KEY (UM_ID),
			UNIQUE (UM_TENANT_IDP_ROLE_ID, UM_TENANT_ID, UM_TENANT_ROLE),
			FOREIGN KEY (UM_TENANT_IDP_ROLE_ID) REFERENCES UM_TENANT_IDP_ROLES(UM_ID) ON DELETE CASCADE);
