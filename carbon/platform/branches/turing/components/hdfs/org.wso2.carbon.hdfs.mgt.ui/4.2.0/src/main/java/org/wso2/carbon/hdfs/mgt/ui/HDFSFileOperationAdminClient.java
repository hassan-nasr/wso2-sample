package org.wso2.carbon.hdfs.mgt.ui;

import java.rmi.RemoteException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.hdfs.mgt.stub.fs.HDFSFileOperationAdminHDFSServerManagementException;
import org.wso2.carbon.hdfs.mgt.stub.fs.HDFSFileOperationAdminStub;
import org.wso2.carbon.hdfs.mgt.stub.fs.xsd.HDFSFileContent;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

public class HDFSFileOperationAdminClient {

    private HDFSFileOperationAdminStub HdfsFileOperationStub;

    public HDFSFileOperationAdminClient(ConfigurationContext ctx, String serverURL, String cookie)
            throws AxisFault {
        init(ctx, serverURL, cookie);
    }

    public HDFSFileOperationAdminClient(javax.servlet.ServletContext servletContext,
                           javax.servlet.http.HttpSession httpSession) throws Exception {
        ConfigurationContext ctx =
                (ConfigurationContext) servletContext.getAttribute(
                        CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) httpSession.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String serverURL = CarbonUIUtil.getServerURL(servletContext, httpSession);
        init(ctx, serverURL, cookie);
    }

    private void init(ConfigurationContext ctx,
                      String serverURL,
                      String cookie) throws AxisFault {
        String serviceURL = serverURL + "HdfsFileUploadDownloader";
        HdfsFileOperationStub = new HDFSFileOperationAdminStub(ctx, serviceURL);
        ServiceClient client = HdfsFileOperationStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
        options.setTimeOutInMilliSeconds(10000);
    }
    

    public boolean createFile(String filePath, byte [] fileContent)
            throws HDFSFileOperationAdminHDFSServerManagementException, RemoteException {
        DataSource dataSource = (DataSource) new ByteArrayDataSource(fileContent,"application/octet-stream");
        DataHandler dataHandler = new DataHandler(dataSource);
        return  HdfsFileOperationStub.createFile(filePath, dataHandler);
    }
    
    public HDFSFileContent downloadFile(String srcFolder)
            throws HDFSFileOperationAdminHDFSServerManagementException, RemoteException {
        return HdfsFileOperationStub.downloadFile(srcFolder);
    }


}
