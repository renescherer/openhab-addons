<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:thing="https://openhab.org/schemas/thing-description/v1.0.0" xmlns="http://www.w3.org/TR/REC-html40">
	<xsl:output method="text" encoding="UTF-8" indent="no" />
	<xsl:template match="channel-group">
		<xsl:apply-templates select="/thing:thing-descriptions/channel-group-type[@id=current()/@typeId]" />
	</xsl:template>
	<xsl:template match="channel-group-type">
|&lt;strong&gt;<xsl:value-of select="label" />&lt;/strong&gt;|||
		<xsl:for-each select="channels/channel" >
		<xsl:variable name="channel" select="@id"/>
		<xsl:variable name="label" select="label"/>
	   <xsl:for-each select="/thing:thing-descriptions/channel-type[@id=current()/@typeId]" >
| <xsl:value-of select="$channel"/>|<xsl:value-of select="item-type"/>|<xsl:choose><xsl:when test="$label != ''"><xsl:value-of select="$label"/></xsl:when><xsl:otherwise><xsl:value-of select="label"/></xsl:otherwise></xsl:choose>&lt;br&gt;<xsl:value-of select="description"/>|<xsl:if test="state/@readOnly='true'" >X</xsl:if>|</xsl:for-each>  
		</xsl:for-each>
		</xsl:template>
	<xsl-template match="channel">
		Channel:<xsl:value-of select="@id" />
		<xsl:apply-tempates select="/thing:thing-descriptions/channel-type[@id=current()/@typeId]" />
	</xsl-template>
	<xsl:template match="channel-type">
		Channel Type:
		<xsl:value-of select="@id" />
	</xsl:template>
	<xsl:template match="text()" />
	<xsl:template match="/">
	| channel         | type          | description                                                     |read only|
|-----------------|---------------|-----------------------------------------------------------------|--------|
	
	<xsl:apply-templates select="/thing:thing-descriptions/channel-group-type"/>
	</xsl:template>
</xsl:stylesheet>