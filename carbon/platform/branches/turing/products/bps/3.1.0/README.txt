WSO2 Business Process Server v@product.version@
-------------------------------------------

Welcome to the WSO2 BPS @product.version@ release

WSO2 Business Process Server (BPS) is an easy-to-use open source
business process server that executes business processes written using
the WS-BPEL standard. It is powered by Apache ODE and provides a
complete Web-based graphical console to deploy, manage and view
processes in addition to managing and viewing process instances.

WSO2 BPS is developed on top of the WSO2 Carbon platform and all the
existing capabilities of Enterprise Service Bus(ESB) and Application 
Server(AS) can be applied to business processes. For example securing 
business processes and throttling requests to business processes.
 

Key Features
------------
* Deploying Business Processes written in compliance with WS-BPEL 2.0 Standard and BPEL4WS 1.1 standard.
* Support for Human Interactions in BPEL Processes with WS-Human Task and BPEL4People.
* Managing BPEL packages, processes and process instances.
* BPEL Extensions and XPath extensions support.
* Instance recovery (Only supports 'Invoke' activity) support through management console.
* OpenJPA based Data Access Layer For BPEL and Human Tasks.
* WS-Security support for external services.
* WS-Security support for business processes.
* Support for HumanTask Coordination.
* Clustering support.
* BPEL Package hot update which facilitate Versioning of BPEL Packages.
* BPEL deployment descriptor editor.
* E4X based data manipulation support for BPEL assignments.
* Ability to configure external data base system as the BPEL engine's persistence storage.
* Caching support for business processes.
* Throttling support for business processes.
* Transport management.
* Internationalized web based management console.
* System monitoring.
* Try-it for business processes.
* SOAP Message Tracing.
* End-point configuration mechanism based on WSO2 Unified Endpoints.
* Customizable server - You can customize the BPS to fit into your
  exact requirements, by removing certain features or by adding new
  optional features.
* Performance improvements in XPath evaluations.
* Improved BPS home page.
* Process Monitoring support with WSO2 Business Activity Monitor.

New Features In This Release
----------------------------

* WS- Human Task support.
* WS- BPEL4People support.
* BPEL deployment descriptor editor.
* Improved BPS management console.
* Process Monitoring support with WSO2 Business Activity Monitor

Issues Fixed In This Release
----------------------------

* WSO2 BPS related components of the WSO2 Carbon Platform -
       https://wso2.org/jira/browse/BPS/fixforversion/10967


Known Issues
-----------

* WS-Human Task implementation does not support sub tasks and lean tasks.
        BPEL4People only supports remote tasks and remote notification creation.
* For a complete list of features to be implemented please refer the list of known issues -
       https://wso2.org/jira/secure/Dashboard.jspa?selectPageId=10403


Installation & Running
----------------------
1. Extract the downloaded zip file
2. Run the wso2server.sh or wso2server.bat file in the bin directory
3. Once the server starts, point your Web browser to
   https://localhost:9443/carbon/
4. Use the following username and password to login
    username : admin
    password : admin

For more details, see the Installation Guide


System Requirements
-------------------

1. Minimum memory - 1GB
2. Processor      - Pentium 800MHz or equivalent at minimum
3. The Management Console requires full Javascript enablement of the Web browser
   NOTE:
     On Windows Server 2003, it is not allowed to go below the medium security
     level in Internet Explorer 6.x.

 

WSO2 BPS @product.version@ distribution directory structure
=============================================

	CARBON_HOME
		|- bin <folder>
		|- dbscripts <folder>
		|- lib <folder>
		|- repository <folder>
			|-- logs <folder>
			|-- database <folder>
			|-- samples <folder>
		|--- conf <folder>
		|- resources <folder>
		|- samples <folder>
		|- webapp-mode <folder>
		|- tmp <folder>
		|- LICENSE.txt <file>
		|- README.txt <file>
		|- INSTALL.txt <file>
		|- release-notes.html <file>

    - bin
	  Contains various scripts .sh & .bat scripts

	- conf
	  Contains configuration files

	- database
      Contains the database

    - dbscripts
      Contains all the database scripts

    - lib
	  Contains the basic set of libraries required to startup BPS
	  in standalone mode

	- repository
	  The repository where services and modules deployed in WSO2 BPS
	  are stored. In addition to this the components directory inside the
	  repository directory contains the carbon runtime and the user added
	  jar files including mediators, third party libraries and so on.

	- logs
	  Contains all log files created during execution

	- resources
	  Contains additional resources that may be required, including sample
	  configuration and sample resources

	- samples
	  Contains sample axis2 server and client files to execute some of the
	  samples shipped with WSO2 BPS

	- tmp
	  Used for storing temporary files, and is pointed to by the
	  java.io.tmpdir System property

	- LICENSE.txt
	  Apache License 2.0 and the relevant other licenses under which
	  WSO2 BPS is distributed.

	- README.txt
	  This document.

    - INSTALL.txt
      This document will contain information on installing WSO2 BPS

	- release-notes.html
	  Release information for WSO2 BPS @product.version@

Support
==================================

WSO2 Inc. offers a variety of development and production support
programs, ranging from Web-based support up through normal business
hours, to premium 24x7 phone support.

For additional support information please refer to http://wso2.com/support/

For more information on WSO2 BPS, visit the WSO2 Oxygen Tank (http://wso2.org)

Issue Tracker
==================================

  https://wso2.org/jira/browse/BPS
  https://wso2.org/jira/browse/CARBON

Crypto Notice
==================================

   This distribution includes cryptographic software.  The country in
   which you currently reside may have restrictions on the import,
   possession, use, and/or re-export to another country, of
   encryption software.  BEFORE using any encryption software, please
   check your country's laws, regulations and policies concerning the
   import, possession, or use, and re-export of encryption software, to
   see if this is permitted.  See <http://www.wassenaar.org/> for more
   information.

   The U.S. Government Department of Commerce, Bureau of Industry and
   Security (BIS), has classified this software as Export Commodity
   Control Number (ECCN) 5D002.C.1, which includes information security
   software using or performing cryptographic functions with asymmetric
   algorithms.  The form and manner of this Apache Software Foundation
   distribution makes it eligible for export under the License Exception
   ENC Technology Software Unrestricted (TSU) exception (see the BIS
   Export Administration Regulations, Section 740.13) for both object
   code and source code.

   The following provides more details on the included cryptographic
   software:

   Apache Rampart   : http://ws.apache.org/rampart/
   Apache WSS4J     : http://ws.apache.org/wss4j/
   Apache Santuario : http://santuario.apache.org/
   Bouncycastle     : http://www.bouncycastle.org/
   Apache ODE       : http://ode.apache.org/

--------------------------------------------------------------------------------
(c) Copyright 2012 WSO2 Inc.


