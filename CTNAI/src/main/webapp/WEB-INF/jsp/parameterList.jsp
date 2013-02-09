<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<div id="viewpoint">
    <table id="parameter_list">
        <thead>
            <tr>
            <c:forEach items="${rows[0]}" var="column">
                <th><c:out value="${column.key}" /></th>
            </c:forEach>
            </tr>
        </thead>
        <tbody>
        <c:forEach items="${rows}" var="cells">
            <tr>
            <c:forEach items="${cells}" var="cell">
                <td><c:out value="${cell.value}" /></td>
            </c:forEach>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
