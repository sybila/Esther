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
                $('#widget').setupTabs();
            });
        </script>
        
        <title>CTNAI Analysis</title>
    </head>
    <body>
        <h2>Analysis</h2>
        <div id="filesystembrowser">
        </div>
        <div id="widget">
            <ul id="tabs">
                <li file="frontpage">
                    <a href="#tab_1">Start Page</a>
                    <img height="10" style="cursor: pointer;" src="<c:url value="/resources/Graphics/x_button.png" />" />
                </li>
            </ul>
            <div id="tab_1">
                <div id="frontpage">
                    <h7>Welcome to the analysis screen!</h7>
                    <p>
                        If this is your first time visiting you might want to try our
                        <a href="">Getting Started Tutorial</a>.
                    </p>
                    <p>
                        Or you may just select one of the existing files to the left or
                        <a href="javascript:newModel()">create a new model</a> right now!
                    </p>
                </div>
            </div>
        </div>
    </body>
</html>
