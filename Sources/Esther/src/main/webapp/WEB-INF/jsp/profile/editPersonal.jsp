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
        
        <c:if test="${not empty error}">
            <div id="error">
                Unsuccessful registration attempt.<br/>
                ${error}
            </div>
	</c:if>
        
        <form name="form" action="<c:url value='/Profile/Edit/Personal' />" method="POST">
            <table>
                <tr>
                    <th>Country:</th>
                    <td><%@ include file="/WEB-INF/jspf/countrySelector.jspf" %></td>
                </tr>
                <tr>
                    <th>Institution:</th>
                    <td><input type="text" name="organization" value="${information.organization}"/></td>
                </tr>
            </table>
            <p>
                <input class="button" type="Submit" value="Save" name="submit"/>
            </p>
        </form>
    </body>
</html>