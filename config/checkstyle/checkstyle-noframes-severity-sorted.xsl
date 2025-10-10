<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" indent="yes"/>
    <xsl:key name="files" match="file" use="@name"/>
    <xsl:template match="checkstyle">
        <html>
            <head>
                <style type="text/css">
                    .bannercell {
                    border: 0;
                    padding: 0;
                    }

                    body {
                    margin-left: 10;
                    margin-right: 10;
                    font: normal 80% arial, helvetica, sans-serif;
                    background-color: #ffffff;
                    color: #000000;
                    }

                    .a td {
                    background: #efefef;
                    }

                    .b td {
                    background: #fff;
                    }

                    th,
                    td {
                    text-align: left;
                    vertical-align: top;
                    }

                    th {
                    font-weight: bold;
                    background: #ccc;
                    color: black;
                    }

                    table,
                    th,
                    td {
                    font-size: 100%;
                    border: none;
                    }

                    h2 {
                    font-weight: bold;
                    font-size: 140%;
                    margin-bottom: 5;
                    }

                    h3 {
                    font-size: 100%;
                    font-weight: bold;
                    background: #525d76;
                    color: white;
                    text-decoration: none;
                    padding: 5px;
                    margin-right: 2px;
                    margin-left: 2px;
                    margin-bottom: 0;
                    }
                </style>
            </head>
            <body>
                <a name="top"/>
                <table border="0" cellpadding="0" cellspacing="0" width="100%">
                    <tr>
                        <td class="bannercell" rowspan="2"/>
                        <td class="text-align:right"><h2>CheckStyle Audit</h2></td>
                    </tr>
                    <tr>
                        <td class="text-align:right">Designed for use with <a href='https://checkstyle.org/'>CheckStyle</a> and <a href='https://jakarta.apache.org'>Ant</a>.</td>
                    </tr>
                </table>
                <hr size="1"/>

                <xsl:apply-templates select="." mode="summary"/>
                <hr size="1" width="100%" align="left"/>

                <xsl:apply-templates select="." mode="filelist"/>
                <hr size="1" width="100%" align="left"/>

                <xsl:apply-templates select="file[@name and generate-id(.) = generate-id(key('files', @name))]"/>

                <hr size="1" width="100%" align="left"/>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="checkstyle" mode="filelist">
        <h3>Files</h3>
        <table class="log" border="0" cellpadding="5" cellspacing="2" width="100%">
            <tr>
                <th>Name</th>
                <th>Errors</th>
                <th>Warnings</th>
                <th>Infos</th>
            </tr>
            <xsl:for-each select="file[@name and generate-id(.) = generate-id(key('files', @name))]">
                <xsl:sort data-type="number" order="descending" select="count(key('files', @name)/error[@severity='error'])"/>
                <xsl:sort data-type="number" order="descending" select="count(key('files', @name)/error[@severity='warning'])"/>
                <xsl:sort data-type="number" order="descending" select="count(key('files', @name)/error[@severity='info'])"/>

                <xsl:variable name="errorCount" select="count(key('files', @name)/error[@severity='error'])"/>
                <xsl:variable name="warningCount" select="count(key('files', @name)/error[@severity='warning'])"/>
                <xsl:variable name="infoCount" select="count(key('files', @name)/error[@severity='info'])"/>

                <tr>
                    <xsl:call-template name="alternated-row"/>
                    <td><a href="#f-{translate(@name,'\\','/')}"><xsl:value-of select="@name"/></a></td>
                    <td><xsl:value-of select="$errorCount"/></td>
                    <td><xsl:value-of select="$warningCount"/></td>
                    <td><xsl:value-of select="$infoCount"/></td>
                </tr>
            </xsl:for-each>
        </table>
    </xsl:template>

    <xsl:template match="file">
        <a name="f-{translate(@name,'\\','/')}"/>
        <h3>File <xsl:value-of select="@name"/></h3>

        <table class="log" border="0" cellpadding="5" cellspacing="2" width="100%">
            <tr>
                <th>Severity</th>
                <th>Error Description</th>
                <th>Line</th>
            </tr>
            <xsl:for-each select="key('files', @name)/error">
                <xsl:sort select="@line"/>
                <tr>
                    <xsl:call-template name="alternated-row"/>
                    <td><xsl:value-of select="@severity"/></td>
                    <td><xsl:value-of select="@message"/></td>
                    <td><xsl:value-of select="@line"/></td>
                </tr>
            </xsl:for-each>
        </table>
        <a href="#top">Back to top</a>
    </xsl:template>

    <xsl:template match="checkstyle" mode="summary">
        <h3>Summary</h3>
        <xsl:variable name="fileCount" select="count(file[@name and generate-id(.) = generate-id(key('files', @name))])"/>
        <xsl:variable name="errorCount" select="count(file/error[@severity='error'])"/>
        <xsl:variable name="warningCount" select="count(file/error[@severity='warning'])"/>
        <xsl:variable name="infoCount" select="count(file/error[@severity='info'])"/>
        <table class="log" border="0" cellpadding="5" cellspacing="2" width="100%">
            <tr>
                <th>Files</th>
                <th>Errors</th>
                <th>Warnings</th>
                <th>Infos</th>
            </tr>
            <tr>
                <xsl:call-template name="alternated-row"/>
                <td><xsl:value-of select="$fileCount"/></td>
                <td><xsl:value-of select="$errorCount"/></td>
                <td><xsl:value-of select="$warningCount"/></td>
                <td><xsl:value-of select="$infoCount"/></td>
            </tr>
        </table>
    </xsl:template>

    <xsl:template name="alternated-row">
        <xsl:attribute name="class">
            <xsl:if test="position() mod 2 = 1">a</xsl:if>
            <xsl:if test="position() mod 2 = 0">b</xsl:if>
        </xsl:attribute>
    </xsl:template>
</xsl:stylesheet>
