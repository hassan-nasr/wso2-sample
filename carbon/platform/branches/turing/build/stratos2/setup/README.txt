WSO2 Stratos version 2.0.0 Beta 2
---------------------------------

22 March 2013

Welcome to the WSO2 Stratos 2.0.0 Beta 2 release

How to install Stratos2 in a single node
----------------------------------------
Set environment variable JAVA_HOME to point to your java runtime. Copy mysql
connector jar to this folder.

Edit conf/setup.conf according to your environment. There you need to give
information about your IaaS environment.

run setup.sh as root to install.
eg.
sudo JAVA_HOME=/opt/jdk1.6.0_24 ./setup.sh -uwso2 -p"all"

You need to configure IaaS as well. To do that fill the IaaS specific settings
in conf/setup.conf file and execute IaaS specific configure script.

eg. Openstack
configure_openstack.sh

eg. EC2
configure_ec2.sh

Please refer to the Documentation shipped along with this release packs.

---------------------------------------------------------------------------
(c) Copyright 2013 WSO2 Inc.
