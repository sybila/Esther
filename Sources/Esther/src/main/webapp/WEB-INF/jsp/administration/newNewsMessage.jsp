<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <script type="text/javascript" src="<c:url value="/resources/scripts/jquery.js"/>"></script>
        <script type="text/javascript" src="<c:url value="/resources/scripts/estherAdministration.js"/>"></script>
        <title>New News Message</title>
        
        <script type="text/javascript">
            $(document).ready( function() {
                $('#page').initNewsPost();
            });
        </script>
    </head>
    <body>
        <h1 class="section">Add News Post</h1>
        <form name="form" action="<c:url value='Post' />" method="POST">
            <p>
                Title:
                <input type="text" name="title" value="${message.title}"/>
            </p>
            <p>
                Contents:
            </p>
            <textarea spellcheck="true" id="contents"></textarea></br>
            <input type="hidden" name="content" value="" />
            <input type="submit" name="submit" value="Post" />
        </form>
    </body>
</html>
