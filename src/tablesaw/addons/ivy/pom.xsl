<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"  omit-xml-declaration="no" />
	<xsl:preserve-space elements="project"/>

	<xsl:param name="groupId"/>
	<xsl:param name="artifactId"/>
	<xsl:param name="version"/>
	<xsl:param name="packaging">jar</xsl:param>
	<xsl:param name="name"/>
	<xsl:param name="description"/>
	<xsl:param name="url"/>
	<xsl:param name="scm_url"/>
	<xsl:param name="scm_connection"/>

	<xsl:template match="ivy-module">

<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<groupId><xsl:value-of select="$groupId"/></groupId>
	<artifactId><xsl:value-of select="$artifactId"/></artifactId>
	<version><xsl:value-of select="$version"/></version>
	<packaging><xsl:value-of select="$packaging"/></packaging>

	<name><xsl:value-of select="$name"/></name>
	<description><xsl:value-of select="$description"/></description>
	<url><xsl:value-of select="$url"/></url>

	<scm>
		<url><xsl:value-of select="$scm_url"/></url>
		<connection><xsl:value-of select="$scm_connection"/></connection>
	</scm>

	<dependencies>
		<xsl:for-each select="dependencies/dependency">
		<dependency>
			<groupId><xsl:value-of select="@org"/></groupId>
			<artifactId><xsl:value-of select="@name"/></artifactId>
			<version><xsl:value-of select="@rev"/></version>
			<xsl:if test="exclude or @transitive = 'false'">
			<exclusions>
			<xsl:if test="@transitive = 'false'">
				<exclusion>
					<groupId>*</groupId>
					<artifactId>*</artifactId>
				</exclusion>
			</xsl:if>
			<xsl:for-each select="exclude">
				<exclusion>
					<xsl:choose>
						<xsl:when test="@org">
							<groupId><xsl:value-of select="@org"/></groupId>
						</xsl:when>
						<xsl:otherwise>
							<groupId>*</groupId>
						</xsl:otherwise>
					</xsl:choose>
					<artifactId><xsl:value-of select="@module"/></artifactId>
				</exclusion>
			</xsl:for-each>
			</exclusions>
			</xsl:if>
		</dependency>
		</xsl:for-each>
	</dependencies>

</project>

	</xsl:template>

</xsl:stylesheet>