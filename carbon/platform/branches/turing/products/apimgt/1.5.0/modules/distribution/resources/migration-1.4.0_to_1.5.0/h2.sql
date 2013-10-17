ALTER TABLE AM_API_URL_MAPPING ADD THROTTLING_TIER varchar(512) DEFAULT 'Unlimited';

ALTER TABLE AM_APPLICATION ADD DESCRIPTION VARCHAR(512);

ALTER TABLE IDN_OAUTH_CONSUMER_APPS ADD COLUMN GRANT_TYPES VARCHAR (1024);

ALTER TABLE IDN_OAUTH_CONSUMER_APPS ADD COLUMN LOGIN_PAGE_URL VARCHAR (1024);

ALTER TABLE IDN_OAUTH_CONSUMER_APPS ADD COLUMN ERROR_PAGE_URL VARCHAR (1024);

ALTER TABLE IDN_OAUTH_CONSUMER_APPS ADD COLUMN CONSENT_PAGE_URL VARCHAR (1024);

ALTER TABLE IDN_OAUTH2_AUTHORIZATION_CODE ADD COLUMN CALLBACK_URL VARCHAR(1024);

CREATE TABLE IF NOT EXISTS AM_EXTERNAL_STORES(
    APISTORE_ID INTEGER AUTO_INCREMENT,
    API_ID INTEGER,
    STORE_ID VARCHAR(255) NOT NULL,
    STORE_DISPLAY_NAME VARCHAR(255) NOT NULL,
    STORE_ENDPOINT VARCHAR(255) NOT NULL,
    STORE_TYPE VARCHAR(255) NOT NULL,
    FOREIGN KEY(API_ID) REFERENCES AM_API(API_ID) ON UPDATE CASCADE ON DELETE RESTRICT,
    PRIMARY KEY (APISTORE_ID)
);

CREATE TABLE IF NOT EXISTS AM_TIER_PERMISSIONS (
    TIER_PERMISSIONS_ID INTEGER AUTO_INCREMENT,
    TIER VARCHAR(50) NOT NULL,
    PERMISSIONS_TYPE VARCHAR(50) NOT NULL,
    ROLES VARCHAR(512) NOT NULL,
    TENANT_ID INTEGER NOT NULL,
    PRIMARY KEY(TIER_PERMISSIONS_ID)
);

CREATE TABLE IF NOT EXISTS IDN_THRIFT_SESSION (
       SESSION_ID VARCHAR(255) NOT NULL,
       USER_NAME VARCHAR(255) NOT NULL,
       CREATED_TIME VARCHAR(255) NOT NULL,
       LAST_MODIFIED_TIME VARCHAR(255) NOT NULL,
       PRIMARY KEY (SESSION_ID)
);
