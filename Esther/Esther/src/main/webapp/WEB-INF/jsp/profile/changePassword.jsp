<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN""http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Esther password change</title>
    </head>
    <body>
        <h2>Change Password</h2>
        
        <c:if test="${not empty error}">
            <div id="error">
                ${error}
            </div>
	</c:if>
        
        <form name="form" action="<c:url value='/Profile/Edit/Password' />" method="POST">
            <table>
                <tr>
                    <th>Current Password:</th>
                    <td><input type="password" name="oldPassword" value=""/></td>
                </tr>
                <tr>
                    <th>New Password:</th>
                    <td><input type="password" name="password" value=""/></td>
                </tr>
                <tr>
                    <th>Confirm New Password:</th>
                    <td><input type="password" name="cPassword" value=""/></td>
                </tr>
            </table>
            <p>
                <input class="button" type="Submit" value="Change" name="submit"/>
            </p>
        </form>
    </body>
</html>