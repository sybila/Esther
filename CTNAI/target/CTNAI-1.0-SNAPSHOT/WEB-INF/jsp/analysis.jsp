<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN""http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" href="<c:url value="/resources/Styles/widget.css"/>" type="text/css">
        <link rel="stylesheet" href="<c:url value="/resources/Styles/ctnaiFileSystem.css"/>" type="text/css">
        
        <script type="text/javascript" src="<c:url value="/resources/Scripts/jquery.js"/>"></script>
        <script type="text/javascript" src="<c:url value="/resources/Scripts/jquery.ui.js"/>"></script>
        <script type="text/javascript" src="<c:url value="/resources/Scripts/jquery.easing.js"/>"></script>
        <script type="text/javascript" src="<c:url value="/resources/Scripts/jquery.tablesorter.js"/>"></script>
        <script type="text/javascript" src="<c:url value="/resources/Scripts/interactionGraphWidget.js"/>"></script>
        <script type="text/javascript" src="<c:url value="/resources/Scripts/parameterViewWidget.js"/>"></script>
        <script type="text/javascript" src="<c:url value="/resources/Scripts/json2.js"/>"></script>
        <script type="text/javascript" src="<c:url value="/resources/Scripts/AC_OETags.js"/>"></script>
        <script type="text/javascript" src="<c:url value="/resources/Scripts/cytoscapeweb.js"/>"></script>
        <script type="text/javascript" src="<c:url value="/resources/Scripts/behaviourMapWidget.js"/>"></script>
        <script type="text/javascript" src="<c:url value="/resources/Scripts/ctnaiWidgetManager.js"/>"></script>
        <script type="text/javascript" src="<c:url value="/resources/Scripts/ctnaiFileSystem.js"/>"></script>
        <script type="text/javascript">
            $(document).ready( function() {
                $('#filesystembrowser').loadFileSystem();
                $('#widget').openWelcomeScreen();
            });
        </script>
        
        <title>CTNAI Analysis</title>
    </head>
    <body>
        <h2>Analysis</h2>
        <div id="filesystembrowser">
        </div>
        <div id="widget">
        </div>
    </body>
</html>
