<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN""http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" href="<c:url value="/resources/css/estherWidget.css"/>" type="text/css">
        <link rel="stylesheet" href="<c:url value="/resources/css/estherFileSystem.css"/>" type="text/css">
        
        <script type="text/javascript" src="<c:url value="/resources/scripts/jquery.js"/>"></script>
        <script type="text/javascript" src="<c:url value="/resources/scripts/jquery.ui.js"/>"></script>
        <script type="text/javascript" src="<c:url value="/resources/scripts/jquery.easing.js"/>"></script>
        <script type="text/javascript" src="<c:url value="/resources/scripts/jquery.tablesorter.js"/>"></script>
        <script type="text/javascript" src="<c:url value="/resources/scripts/jquery.form.js"/>"></script>
        <script type="text/javascript" src="<c:url value="/resources/scripts/json2.js"/>"></script>
        <script type="text/javascript" src="<c:url value="/resources/scripts/AC_OETags.js"/>"></script>
        <script type="text/javascript" src="<c:url value="/resources/scripts/estherWidgetManager.js"/>"></script>
        <script type="text/javascript" src="<c:url value="/resources/scripts/estherFileSystem.js"/>"></script>
	<c:forEach items="${global_js}" var="script">
		<script type="text/javascript" src="<c:url value="/resources/scripts/widget/${script}.js"/>"></script>
	</c:forEach>
        <script type="text/javascript">
            $(document).ready( function() {
                $('#filesystembrowser').loadFileSystem();
                $('#widget').setupTabs();
                openWidget('startpage', 'Start Page', null, null);
            });
        </script>
        
        <title>Esther Analysis</title>
    </head>
    <body>
        <h1 class="section">Analysis</h1>
        <div id="filesystembrowser">
        </div>
        <div id="widget">
            <ul id="tabs">
            </ul>
        </div>
    </body>
</html>
