<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<table class="list" id="parameter_list">
    <thead>
        <tr>
        <c:forEach items="${column_names}" var="column">
            <th class="col_${column.key}"><c:out value="${column.value}" /></th>
        </c:forEach>
        </tr>
    </thead>
    <tbody>
    <c:forEach items="${rows}" var="cells">
        <tr>
        <c:forEach items="${cells}" var="cell">
            <td class="col_${cell.key}"><c:out value="${cell.value}" /></td>
        </c:forEach>
        </tr>
    </c:forEach>
    </tbody>
</table>
