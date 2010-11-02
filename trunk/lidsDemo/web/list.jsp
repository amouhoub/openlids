<%-- 
    Document   : list
    Created on : Oct 28, 2010, 4:58:26 PM
    Author     : ssp
--%>

<%@page contentType="text/html" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="su" uri="http://localhost:8080/shortenuri.tld" %>
 
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <style type="text/css">
            h1 {
                font-size: 48px;
                text-align: center;
            }

            table {
                border-collapse: collapse;
                font-family: monospace;
            }

            td {
                border: 1px solid black;
                padding: 5px;
                padding-bottom: 15px;
            }

            td {
                vertical-align: top;
            }
        </style>
        <title>LIDS</title>
    </head>
    <body>
        <h1>List of LIDS</h1>
        <table>
            <tr>
                <th>Endpoint</th>
                <th>Input</th>
                <th>Output</th>
            </tr>
            <c:forEach items="${lidsList}" var="lids">
                <tr>
                    <td>${fn:escapeXml(lids.endpoint)}</td>
                    <td><c:forEach items="${lids.inputBGP}" var ="bgp">
                            ${fn:escapeXml(su:shortenURIBGP(bgp))}<br />
                        </c:forEach>
                    </td>
                    <td><c:forEach items="${lids.outputBGP}" var="bgp">
                            ${fn:escapeXml(su:shortenURIBGP(bgp))}<br />
                        </c:forEach>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </body>
</html>
