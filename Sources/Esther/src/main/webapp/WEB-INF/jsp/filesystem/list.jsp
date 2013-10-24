<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<ul class="estherFileSystem" style="display: none;">
    <c:forEach items="${files}" var="file">
        <li class="file ${privacy} ${file.type}">
            <div class="icon_container">
                <div id="icon"/>
            </div>
            <a href="#" class="<c:if test="${file.locked}">locked</c:if> <c:if test="${file.published}">public</c:if>"
               file_id="${file.id}" file_type="${file.type}" parent_id="${file.parentId}">${file.name}.${file.type}</a>
        </li>
    </c:forEach>
</ul>