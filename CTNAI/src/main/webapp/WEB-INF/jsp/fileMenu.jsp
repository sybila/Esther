<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<c:forEach items="${links}" var="link">
    <a func="${link.key}" href="#">${link.value}</a><br/>
    <c:if test="${not empty ext and link.key == 'upload'}">
        <form id="uploadOptions" ext="${ext}" method="POST" action="<c:url value="File/Upload" />" enctype="multipart/form-data">
            <table style="display: none;">
                <tr><td><input id="fileInput" type="file" size="32" name="file" /></td></tr>
                <tr><td><input type="submit" value="Upload" /></td></tr>
            </table>
        </form>
    </c:if>
</c:forEach>