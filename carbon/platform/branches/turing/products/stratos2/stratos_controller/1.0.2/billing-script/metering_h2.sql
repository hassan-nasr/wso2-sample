--
-- TABLES
--

CREATE TABLE IF NOT EXISTS USAGE_HOURLY_ANALYTICS (
        ID VARCHAR(200) NOT NULL,
        HOUR_FACT TIMESTAMP,
        SERVER_NAME VARCHAR(100),
        TENANT_ID VARCHAR(50),
        PAYLOAD_TYPE VARCHAR(50),
        PAYLOAD_VALUE  BIGINT,
        PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS USAGE_DAILY_ANALYTICS (
        ID VARCHAR(200) NOT NULL,
        DAY_FACT TIMESTAMP,
        SERVER_NAME VARCHAR(100),
        TENANT_ID VARCHAR(50),
        PAYLOAD_TYPE VARCHAR(50),
        PAYLOAD_VALUE  BIGINT,
        PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS USAGE_MONTHLY_ANALYTICS (
        ID VARCHAR(200) NOT NULL,
        MONTH_FACT TIMESTAMP,
        SERVER_NAME VARCHAR(100),
        TENANT_ID VARCHAR(50),
        PAYLOAD_TYPE VARCHAR(50),
        PAYLOAD_VALUE  BIGINT,
        PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS USAGE_LAST_HOURLY_TS (
	ID VARCHAR(200) NOT NULL,
	TIMESTMP TIMESTAMP,
	PRIMARY KEY (ID)
);
	
CREATE TABLE IF NOT EXISTS USAGE_LAST_DAILY_TS (
	ID VARCHAR(200) NOT NULL,
	TIMESTMP TIMESTAMP,
	PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS USAGE_LAST_MONTHLY_TS (
	ID VARCHAR(200) NOT NULL,
	TIMESTMP TIMESTAMP,
	PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS SERVICE_STATS_HOURLY_ANALYTICS (
	ID VARCHAR(200) NOT NULL,
	HOUR_FACT TIMESTAMP,
	SERVER_NAME VARCHAR(100),
	TENANT_ID VARCHAR(50),
	REQUEST_COUNT BIGINT,
	RESPONSE_COUNT BIGINT,
	FAULT_COUNT BIGINT,
	PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS SERVICE_STATS_DAILY_ANALYTICS (
	ID VARCHAR(200) NOT NULL,
	DAY_FACT TIMESTAMP,
	SERVER_NAME VARCHAR(100),
	TENANT_ID VARCHAR(50),
	REQUEST_COUNT BIGINT,
	RESPONSE_COUNT BIGINT,
	FAULT_COUNT BIGINT,
	PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS SERVICE_STATS_MONTHLY_ANALYTICS (
	ID VARCHAR(200) NOT NULL,
	MONTH_FACT TIMESTAMP,
	SERVER_NAME VARCHAR(100),
	TENANT_ID VARCHAR(50),
	REQUEST_COUNT BIGINT,
	RESPONSE_COUNT BIGINT,
	FAULT_COUNT BIGINT,
	PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS SERVICE_STATS_LAST_HOURLY_TS (
	ID VARCHAR(200) NOT NULL,
	TIMESTMP TIMESTAMP,
	PRIMARY KEY (ID)
);
	
CREATE TABLE IF NOT EXISTS SERVICE_STATS_LAST_DAILY_TS (
	ID VARCHAR(200) NOT NULL,
	TIMESTMP TIMESTAMP,
	PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS SERVICE_STATS_LAST_MONTHLY_TS (
	ID VARCHAR(200) NOT NULL,
	TIMESTMP TIMESTAMP,
	PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS REGISTRY_USAGE_HOURLY_ANALYTICS ( 
	ID VARCHAR(50),
	TENANT_ID VARCHAR(50),	
	HISTORY_USAGE BIGINT,
	CURRENT_USAGE BIGINT,
	PRIMARY KEY (ID)
); 


COMMIT;

