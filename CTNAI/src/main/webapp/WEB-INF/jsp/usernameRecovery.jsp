<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN""http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>CTNAI username recovery</title>
    </head>
    <body>
        <h2>Username Recovery</h2>
        
        <c:if test="${not empty error}">
            <div id="error">
                ${error}
            </div>
	</c:if>
        
        <p>
            Please enter your e-mail into the box below:
        </p>
        
        <form name="form" action="<c:url value='Username' />" method="POST">
            <input type="email" name="email" value=""/>
            <p>
                <input class="button" type="Submit" value="Submit" name="submit"/>
            </p>
        </form>
            
        <c:if test="${not empty registerPrompt}">
            <p>
                If you don't have an account yet <a href="/CTNAI/Registration">register here</a>.
            </p>
        </c:if>
    </body>
</html>