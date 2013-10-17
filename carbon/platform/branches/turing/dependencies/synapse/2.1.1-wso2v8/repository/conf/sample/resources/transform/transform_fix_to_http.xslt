<?xml version="1.0" encoding="ISO-8859-1"?>
<!--
  ~  Licensed to the Apache Software Foundation (ASF) under one
  ~  or more contributor license agreements.  See the NOTICE file
  ~  distributed with this work for additional information
  ~  regarding copyright ownership.  The ASF licenses this file
  ~  to you under the Apache License, Version 2.0 (the
  ~  "License"); you may not use this file except in compliance
  ~  with the License.  You may obtain a copy of the License at
  ~
  ~   http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~  Unless required by applicable law or agreed to in writing,
  ~  software distributed under the License is distributed on an
  ~   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~  KIND, either express or implied.  See the License for the
  ~  specific language governing permissions and limitations
  ~  under the License.
  -->
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:fn="http://www.w3.org/2005/02/xpath-functions">
	<xsl:output method="xml" omit-xml-declaration="yes" indent="yes" />
	<xsl:template match="/">
		<m0:placeOrder xmlns:m0="http://services.samples">
			<m0:order>
				<m0:price><xsl:value-of select="//message/body/field[@id='44']"/></m0:price>
				<m0:quantity><xsl:value-of select="//message/body/field[@id='38']"/></m0:quantity>
				<m0:symbol><xsl:value-of select="//message/body/field[@id='55']"/></m0:symbol>
			</m0:order>
		</m0:placeOrder>
	</xsl:template>
</xsl:stylesheet>

