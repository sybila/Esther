<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN""http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Esther registration</title>
    </head>
    <body>
        <h2>New User</h2>
        
        <c:if test="${not empty error}">
            <div id="error">
                Unsuccessful registration attempt.<br/>
                ${error}
            </div>
	</c:if>
        
        <form name="form" action="<c:url value='Register' />" method="POST">
            <table>
                <tr>
                    <th>Username:</th>
                    <td><input type="text" name="username" value="${user.username}"/></td>
                </tr>
                <tr>
                    <th>E-Mail:</th>
                    <td><input type="email" name="email" value="${user.email}"/></td>
                </tr>
                <tr>
                    <th>Password:</th>
                    <td><input type="password" name="password" value="${user.password}"/></td>
                </tr>
                <tr>
                    <th>Confirm password:</th>
                    <td><input type="password" name="cPassword" value="${user.cPassword}"/></td>
                </tr>
                <tr>
                    <th>Country of residence:</th>
                    <td><%@ include file="/WEB-INF/jspf/countrySelector.jspf" %></td>
                </tr>
                <tr>
                    <th>Institution:</th>
                    <td><input type="text" name="organization" value="${information.organization}"/></td>
                </tr>
            </table>
            <p>
                <input class="button" type="Submit" value="Register" name="submit"/>
            </p>
        </form>
        <p>
            Already a member? <a href="/Login">Log in here.</a>
        </p>
        <c:if test="${not empty passRecPrompt}">
            <p>
                Forgot your password? Proceed with <a href="/Recover/Password">password recovery</a>.
            </p>
        </c:if>
        <c:if test="${not empty nameRecPrompt}">
            <p>
                Can't remember your username? Try the <a href="/Recover/Username">username recovery</a>.
            </p>
        </c:if>
    </body>
</html>