<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN""http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Esther log in</title>
    </head>
    <body onload="document.form.j_username.focus();">
        <h1 class="section">Log in</h1>

        <c:if test="${not empty error}">
            <div id="error">
                Unsuccessful login attempt.<br/>
                ${sessionScope["SPRING_SECURITY_LAST_EXCEPTION"].message}
            </div>
	</c:if>
        
        <form name="form" action="<c:url value='j_spring_security_check' />" method="POST">
            <table>
                <tr>
                    <th>Username:</th>
                    <td><input type="text" name="j_username"/></td>
                </tr>
                <tr>
                    <th>Password:</th>
                    <td><input type="password" name="j_password"/></td>
                </tr>
            </table>
            <p>
                <input class="button" type="Submit" value="Sign In" name="submit"/>
            </p>
        </form>
        <p>
            Don't have an account? <a href="Registration">Register now!</a>
        </p>
        <p>
            Forgot your password? Proceed with <a href="Recover/Password">password recovery</a>.
        </p>
        <p>
            Can't remember your username? Try the <a href="Recover/Username">username recovery</a>.
        </p>
    </body>
</html>