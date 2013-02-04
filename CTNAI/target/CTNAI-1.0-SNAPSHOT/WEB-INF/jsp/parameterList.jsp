<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<table>
    <tr>
        <c:forEach items="${rows[0]}" var="column">
            <th><c:out value="${column.key}" /></th>
        </c:forEach>
    </tr>
    <c:forEach items="${rows}" var="cells">
        <tr>
            <c:forEach items="${cells}" var="cell">
                <td><c:out value="${cell.value}" /></td>
            </c:forEach>
        </tr>
    </c:forEach>
</table>
