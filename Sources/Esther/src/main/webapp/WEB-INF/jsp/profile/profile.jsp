<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN""http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Esther Profile</title>
    </head>
    <body>
        <h1 class="section">${user.username}'s Profile</h1>
        <table class="profile_chapter">
            <tr><th>Core information<c:if test="${edit}"><a href="/Profile/Edit/Core">edit</a></c:if></th></tr>
            <tr><td>Username: ${user.username}</td></tr>
            <tr><td>E-mail: 
                <c:if test="${edit}">${user.email}
                    <c:if test="${information.showEmail}">  (e-mail is public)</c:if>
                    <c:if test="${not information.showEmail}">  (e-mail is private)</c:if>
                </c:if>
                <c:if test="${not edit}">
                    <c:if test="${information.showEmail}">${user.email}</c:if>
                    <c:if test="${not information.showEmail}">private</c:if>
                </c:if></td></tr>
            <c:if test="${edit}"><tr><td>Password: <a href="/Profile/Edit/Password">change</a></td></tr></c:if>
        </table>
        <table class="profile_chapter">
            <tr><th>Personal Information <c:if test="${edit}"><a href="/Profile/Edit/Personal">edit</a></c:if></th></tr>
            <tr><td>Country: ${information.country}</td></tr>
            <tr><td>Organisation: ${information.organization}</td></tr>
        </table>
        <table class="profile_chapter">
            <tr><th>Preferences <c:if test="${edit}"><a href="/Profile/Edit/Preferences">edit</a></c:if></th></tr>
            <tr>
                <td>
                    <c:if test="${information.hidePublicOwned}">
                        Owned files hidden in public folder.
                    </c:if>
                    <c:if test="${not information.hidePublicOwned}">
                        Owned files displayed also in public folder.
                    </c:if>
                </td>
            </tr>
        </table>
    </body>
</html>