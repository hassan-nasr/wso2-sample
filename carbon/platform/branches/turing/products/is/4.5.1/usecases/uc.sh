#!/bin/sh
rm -rf wso2is-4.5.1
unzip modules/distribution/target/wso2is-4.5.1.zip
cp usecases/common/drivers/informix/*.jar  wso2is-4.5.1/repository/components/lib/
cp usecases/common/drivers/mysql/*.jar  wso2is-4.5.1/repository/components/lib/
cp usecases/common/client-truststore.jks wso2is-4.5.1/repository/resources/security/
cp usecases/common/informix-um/*.jar wso2is-4.5.1/repository/components/lib/
cp usecases/$1/master-datasources.xml wso2is-4.5.1/repository/conf/datasources/
cp usecases/$1/user-mgt.xml wso2is-4.5.1/repository/conf/
cat usecases/$1/readme
