<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<h7>My Tasks</h7>
<br/>
<input style="float: right;" class="button" type="Submit" value="Refresh" name="refresh"/>
<input style="float: right;" class="button" type="Submit" value="Cancel All" name="cancelAll"/>

<div class="viewpoint">
<c:forEach items="${tasks}" var="task">
    <div class="task" status="<c:choose><c:when test="${!empty task.error}">errored</c:when><c:when test="${task.finished && !task.active}">completed</c:when><c:when test="${task.finished && task.active}">ready</c:when><c:when test="${!task.finished && task.active}">running</c:when></c:choose>" task_id="${task.id}" file_id="${task.file}" result_id="${task.result}">
        <table>
            <tr>
                <th>${task.type} : ${task.progress}</th>
                <c:if test="${task.finished && empty task.error && task.active}">
                    <td style="width: 64px;"><input class="button" type="Submit" value="Save Result" name="save"/></td>
                </c:if>
                <td style="width: 16px;"><img height="12px" style="padding: 2px;" src="<c:url value="/resources/Graphics/x_button.png" />" /></td>
            </tr>
        </table>
        <p style="height: 72%; width: 86%; overflow: auto; display: none;">${task.information}</p>
    </div>
</c:forEach>
</div>