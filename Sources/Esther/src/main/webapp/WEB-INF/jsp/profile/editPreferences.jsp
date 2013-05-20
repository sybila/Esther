<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN""http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Esther profile edit</title>
    </head>
    <body>
        <h2>Edit Profile</h2>
        
        <form name="form" action="<c:url value='/Profile/Edit/Preferences' />" method="POST">
            <table>
                <tr>
                    <th>Hide my files in public folder:</th>
                    <td><input type="checkbox" name="hide_public_owned" <c:if test="${hide_public_owned}">checked="checked"</c:if> /></td>
                </tr>
            </table>
            <p>
                <input class="button" type="Submit" value="Save" name="submit"/>
            </p>
        </form>
    </body>
</html>