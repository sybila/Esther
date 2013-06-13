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
        <h1 class="section">Edit Profile</h1>
        
        <c:if test="${not empty error}">
            <div id="error">
                ${error}
            </div>
	</c:if>
        
        <form name="form" action="<c:url value='/Profile/Edit/Core' />" method="POST">
            <table>
                <tr>
                    <th>Username: </th>
                    <th><sec:authentication property="principal.username" /></th>
                </tr>
                <tr>
                    <th>E-Mail:</th>
                    <td><input type="email" name="email" value="${email}"/></td>
                </tr>
            </table>
            <p>
                <input class="button" type="Submit" value="Save" name="submit"/>
            </p>
        </form>
    </body>
</html>