/*
 *  Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.rssmanager.core.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.Base64;
import org.w3c.dom.*;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.DataSourceMetaInfo;
import org.wso2.carbon.ndatasource.core.utils.DataSourceUtils;
import org.wso2.carbon.ndatasource.rdbms.RDBMSConfiguration;
import org.wso2.carbon.ndatasource.rdbms.RDBMSDataSource;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.common.RSSManagerHelper;
import org.wso2.carbon.rssmanager.common.exception.RSSManagerCommonException;
import org.wso2.carbon.rssmanager.core.config.datasource.RDBMSConfig;
import org.wso2.carbon.rssmanager.core.entity.Database;
import org.wso2.carbon.rssmanager.core.entity.DatabaseUser;
import org.wso2.carbon.rssmanager.core.entity.RSSInstance;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.internal.RSSManagerDataHolder;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public final class RSSManagerUtil {

    private static SecretResolver secretResolver;

    /**
     * Retrieves the tenant domain name for a given tenant ID
     *
     * @param tenantId Tenant Id
     * @return Domain name of corresponds to the provided tenant ID
     * @throws RSSManagerException Thrown when there's any error while retrieving the tenant
     *                             domain for the provided tenant ID
     */
    public static String getTenantDomainFromTenantId(int tenantId) throws RSSManagerException {
        try {
            TenantManager tenantMgr = RSSManagerDataHolder.getInstance().getTenantManager();
            return tenantMgr.getDomain(tenantId);
        } catch (Exception e) {
            throw new RSSManagerException("Error occurred while retrieving tenant domain for " +
                    "the given tenant ID");
        }
    }

    /**
     * Returns the fully qualified name of the database to be created. This will append an
     * underscore and the tenant's domain name to the database to make it unique for that particular
     * tenant. It will return the database name as it is, if it is created in Super tenant mode.
     *
     * @param databaseName          Name of the database
     * @return                      Fully qualified name of the database
     * @throws RSSManagerException  Is thrown if the functionality is interrupted
     */
    public static String getFullyQualifiedDatabaseName(
            String databaseName) throws RSSManagerException {
        String tenantDomain;
        try {
            tenantDomain =
                    RSSManagerDataHolder.getInstance().getTenantManager().getDomain(
                            CarbonContext.getCurrentContext().getTenantId());
        } catch (Exception e) {
            throw new RSSManagerException("Error occurred while composing fully qualified name " +
                    "of the database '" + databaseName + "'", e);
        }
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            return databaseName + "_" + RSSManagerHelper.processDomainName(tenantDomain);
        }
        return databaseName;
    }

    /**
     * Returns the fully qualified username of a particular database user. For an ordinary tenant,
     * the tenant domain will be appended to the username together with an underscore and the given
     * username will be returned as it is in the case of super tenant.
     *
     * @param username Username of the database user.
     * @return Fully qualified username of the database user.
     */
    public static String getFullyQualifiedUsername(String username) {
        String tenantDomain = CarbonContext.getCurrentContext().getTenantDomain();
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {

            /* The maximum number of characters allowed for the username in mysql system tables is
             * 16. Thus, to adhere the aforementioned constraint as well as to give the username
             * an unique identification based on the tenant domain, we append a hash value that is
             * created based on the tenant domain */
            byte[] bytes = RSSManagerHelper.intToByteArray(tenantDomain.hashCode());
            return username + "_" + Base64.encode(bytes);
        }
        return username;
    }

    public static DataSource createDataSource(RDBMSConfiguration config) {
        try {
            RDBMSDataSource dataSource = new RDBMSDataSource(config);
            return dataSource.getDataSource();
        } catch (DataSourceException e) {
            throw new RuntimeException("Error in creating data source: " + e.getMessage(), e);
        }
    }

    public static DataSource createDataSource(Properties properties, String dataSourceClassName) {
        RDBMSConfiguration config = new RDBMSConfiguration();
        config.setDataSourceClassName(dataSourceClassName);
        List<RDBMSConfiguration.DataSourceProperty> dsProps = new ArrayList<RDBMSConfiguration.DataSourceProperty>();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            RDBMSConfiguration.DataSourceProperty property =
                    new RDBMSConfiguration.DataSourceProperty();
            property.setName((String) entry.getKey());
            property.setValue((String) entry.getValue());
            dsProps.add(property);
        }
        config.setDataSourceProps(dsProps);
        return createDataSource(config);
    }

    public static String composeDatabaseUrl(RSSInstance rssInstance, String databaseName) {
        return rssInstance.getDataSourceConfig().getUrl() + "/" + databaseName;
    }

    private static DataSourceMetaInfo.DataSourceDefinition createDSXMLDefinition(
            RDBMSConfiguration rdbmsConfiguration) throws RSSManagerException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            createMarshaller().marshal(rdbmsConfiguration, out);
        } catch (JAXBException e) {
            String msg = "Error occurred while marshalling datasource configuration";
            throw new RSSManagerException(msg, e);
        }
        DataSourceMetaInfo.DataSourceDefinition defn =
                new DataSourceMetaInfo.DataSourceDefinition();
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        defn.setType(RSSManagerConstants.RDBMS_DATA_SOURCE_TYPE);
        try {
            defn.setDsXMLConfiguration(DataSourceUtils.convertToDocument(in).getDocumentElement());
        } catch (DataSourceException e) {
            throw new RSSManagerException(e.getMessage(), e);
        }
        return defn;
    }

    public static DataSourceMetaInfo createDSMetaInfo(Database database,
                                                      String username) throws RSSManagerException {
        DataSourceMetaInfo metaInfo = new DataSourceMetaInfo();
        RDBMSConfiguration rdbmsConfiguration = new RDBMSConfiguration();
        String url = database.getUrl();
        String driverClassName = RSSManagerHelper.getDatabaseDriver(url);
        rdbmsConfiguration.setUrl(url);
        rdbmsConfiguration.setDriverClassName(driverClassName);
        rdbmsConfiguration.setUsername(username);

        metaInfo.setDefinition(createDSXMLDefinition(rdbmsConfiguration));
        metaInfo.setName(database.getName());

        return metaInfo;
    }

    private static Marshaller createMarshaller() throws RSSManagerException {
        JAXBContext ctx;
        try {
            ctx = JAXBContext.newInstance(RDBMSConfiguration.class);
            return ctx.createMarshaller();
        } catch (JAXBException e) {
            throw new RSSManagerException("Error creating rdbms data source configuration " +
                    "info marshaller: " + e.getMessage(), e);
        }
    }

    public static Document convertToDocument(File file) throws RSSManagerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            return docBuilder.parse(file);
        } catch (Exception e) {
            throw new RSSManagerException("Error occurred while parsing file, while converting " +
                    "to a org.w3c.dom.Document : " + e.getMessage(), e);
        }
    }

    public static Properties loadDataSourceProperties(RDBMSConfig config) {
        Properties props = new Properties();
        List<RDBMSConfig.DataSourceProperty> dsProps = config.getDataSourceProps();
        for (RDBMSConfig.DataSourceProperty dsProp : dsProps) {
            props.setProperty(dsProp.getName(), dsProp.getValue());
        }
        return props;
    }

    private static synchronized String loadFromSecureVault(String alias) {
        if (secretResolver == null) {
            secretResolver = SecretResolverFactory.create((OMElement) null, false);
            secretResolver.init(RSSManagerDataHolder.getInstance().getSecretCallbackHandlerService().
                    getSecretCallbackHandler());
        }
        return secretResolver.resolve(alias);
    }

    public static void secureResolveDocument(Document doc) throws RSSManagerException {
        Element element = doc.getDocumentElement();
        if (element != null) {
            secureLoadElement(element);
        }
    }

    private static void secureLoadElement(Element element) throws RSSManagerException {
        Attr secureAttr = element
                .getAttributeNodeNS(
                        RSSManagerConstants.SecureValueProperties.SECURE_VAULT_NS,
                        RSSManagerConstants.SecureValueProperties.SECRET_ALIAS_ATTRIBUTE_NAME_WITH_NAMESPACE);
        if (secureAttr != null) {
            element.setTextContent(RSSManagerUtil
                    .loadFromSecureVault(secureAttr.getValue()));
            element.removeAttributeNode(secureAttr);
        }
        NodeList childNodes = element.getChildNodes();
        int count = childNodes.getLength();
        Node tmpNode;
        for (int i = 0; i < count; i++) {
            tmpNode = childNodes.item(i);
            if (tmpNode instanceof Element) {
                secureLoadElement((Element) tmpNode);
            }
        }
    }

    public static synchronized void cleanupResources(ResultSet rs, PreparedStatement stmt,
                                                     Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignore) {
                //ignore
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ignore) {
                //ignore
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignore) {
                //ignore
            }
        }
    }

    public synchronized static int getTenantId() throws RSSManagerException {
        try {
            return RSSManagerDataHolder.getInstance().getTenantId();
        } catch (RSSManagerCommonException e) {
            throw new RSSManagerException("Error occurred while determining the tenant id", e);
        }
    }

    public static synchronized int getTenantId(String tenantDomain) throws RSSManagerCommonException {
        int tenantId = MultitenantConstants.INVALID_TENANT_ID;
        if (null != tenantDomain) {
            try {
                TenantManager tenantManager = RSSManagerDataHolder.getInstance().getTenantManager();
                tenantId = tenantManager.getTenantId(tenantDomain);
            } catch (UserStoreException e) {
                throw new RSSManagerCommonException("Error while retrieving the tenant Id for " +
                        "tenant domain : " + tenantDomain, e);
            }
        }
        return tenantId;
    }

    public static void checkIfParameterSecured(final String st) throws RSSManagerException{
        boolean hasSpaces = true;
        if(!st.trim().contains(" ")){
            hasSpaces = false;
        }
        if(hasSpaces){
            throw new RSSManagerException("Parameter is not secure enough to execute SQL query.");
        }
    }

    public static DataSource lookupDataSource(String dataSourceName, final Hashtable<Object,Object> jndiProperties) {
        try {
            if(jndiProperties == null || jndiProperties.isEmpty()){
                return (DataSource) InitialContext.doLookup(dataSourceName);
            }
            final InitialContext context = new InitialContext(jndiProperties);
            return (DataSource) context.doLookup(dataSourceName);
        } catch (Exception e) {
            throw new RuntimeException("Error in looking up data source: " + e.getMessage(), e);
        }
    }

    public static void validateDatabaseUserInfo(DatabaseUser user) throws RSSManagerException {
        checkIfParameterSecured(user.getName());
        checkIfParameterSecured(user.getPassword());
    }

    public static void validateDatabaseInfo(Database database) throws RSSManagerException {
        checkIfParameterSecured(database.getName());
    }

    public static Map<String, RSSInstance> getRSSInstanceMap(RSSInstance[] rssInstances) {
        Map<String, RSSInstance> rssInstanceMap = new HashMap<String, RSSInstance>();
        for (RSSInstance rssInstance : rssInstances) {
            rssInstanceMap.put(rssInstance.getName(), rssInstance);
        }
        return rssInstanceMap;
    }

    public static boolean isSuperTenantUser() throws RSSManagerException {
        return (RSSManagerUtil.getTenantId() ==
                org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_ID);
    }

}
