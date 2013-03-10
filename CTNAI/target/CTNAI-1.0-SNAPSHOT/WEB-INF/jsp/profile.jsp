<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN""http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>CTNAI Profile</title>
    </head>
    <body>
        <h2><sec:authentication property="principal.username" />'s Profile</h2>
        <table class="profile_chapter">
            <tr><th>Core information<a href="Profile/Edit/Core">edit</a></th></tr>
            <tr><td>Username: <sec:authentication property="principal.username" /></td></tr>
            <tr><td>E-mail: ${email}</td></tr>
            <tr><td>Password: <a href="Profile/Edit/Password">change</a></td></tr>
        </table>
        <table class="profile_chapter">
            <tr><th>Preferences <a href="Profile/Edit/Preferences">edit</a></th></tr>
            <tr>
                <td>
                    <c:if test="${hide_public_owned}">
                        Owned files hidden in public folder.
                    </c:if>
                    <c:if test="${not hide_public_owned}">
                        Owned files displayed also in public folder.
                    </c:if>
                </td>
            </tr>
        </table>
    </body>
</html>