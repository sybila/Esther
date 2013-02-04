<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<ul class="ctnaiFileSystem" style="display: none;">
    <c:forEach items="${files}" var="file">
        <li class="file ${privacy} ${file.type}">
            <a href="#" file_id="${file.id}">${file.name}.${file.type}</a>
        </li>
    </c:forEach>
</ul>