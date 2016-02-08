<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.List"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
    <h1>Results for query <%=(String)request.getParameter("q")%> in 
    directory <%=(String)request.getAttribute("dataDir")%></h1>
    
    <p>
    
    <c:forEach items="${res}" var="r" varStatus="loop">
        <div style="margin:15px;">
            <div><b>${loop.index + 1}   ${r.key}</b></div>
            <div style="margin-left:10px;"><span>${r.value}</span></div>
        </div>        
    </c:forEach>
    
    </p>
    
</body>
</html>