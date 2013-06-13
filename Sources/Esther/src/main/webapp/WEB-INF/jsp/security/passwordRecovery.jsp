<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN""http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Esther password recovery</title>
    </head>
    <body>
        <h1 class="section">Password Recovery</h1>
        
        <c:if test="${not empty error}">
            <div id="error">
                ${error}
            </div>
	</c:if>
        
        <p>
            Please fill in your account information:
        </p>
        
        <form name="form" action="<c:url value='Password' />" method="POST">
            <table>
                <tr>
                    <th>Username:</th>
                    <td><input type="text" name="username" value=""/></td>
                </tr>
                <tr>
                    <th>E-Mail:</th>
                    <td><input type="email" name="email" value=""/></td>
                </tr>
            </table>
            <p>
                <input class="button" type="Submit" value="Submit" name="submit"/>
            </p>
        </form>
            
        <c:if test="${not empty registerPrompt}">
            <p>
                If you don't have an account yet <a href="/Registration">register here</a>.
            </p>
        </c:if>
    </body>
</html>