<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN""http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" href="<c:url value="/resources/Styles/frontpage.css"/>" type="text/css">
        <title>CTNAI</title>
    </head>
    <body>
        <%@include file="/WEB-INF/jspf/header.jspf"%>
        
        <div id="page">
            <jsp:include page="/WEB-INF/jsp/${requestScope.Page}.jsp" flush="true" />
        </div>
        
        <%@include file="/WEB-INF/jspf/footer.jspf"%>
    </body>
</html>
