<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<h2 class="widget">My Tasks</h2>
<br/>
<input style="float: right;" class="button" type="Submit" value="Refresh" name="refresh"/>
<input style="float: right;" class="button" type="Submit" value="Cancel All" name="cancelAll"/>

<div class="viewpoint">
<c:forEach items="${tasks}" var="task">
    <div class="task" status="<c:choose>
             <c:when test="${not empty task.error}">errored</c:when>
             <c:when test="${task.finished && not task.active}">completed</c:when>
             <c:when test="${task.finished && task.active}">ready</c:when>
             <c:when test="${not task.finished && task.active}">running</c:when>
         </c:choose>" task_id="${task.id}" model_id="${task.model}" property_id="${task.property}"
         database_id="<c:forEach items="${task.databases}" var="database">${database}_</c:forEach>"
         filter_id="<c:forEach items="${task.filters}" var="filter">${filter}_</c:forEach>" result_id="${task.result}">
        <table>
            <tr>
                <th>${task.date}, ${task.text}: ${task.progress}</th>
                <td style="width: 64px;"><input class="button" style="<c:if test="${not task.finished || not task.successful || not task.active}">display: none;</c:if>" type="Submit" value="Save Result" name="save"/></td>               
                <td style="width: 16px;"><img height="12px" style="padding: 2px;" src="<c:url value="/resources/images/x_button.png" />" /></td>
            </tr>
        </table>
        <p style="height: 72%; width: 86%; overflow: auto; display: none;">
            <sec:authorize access="hasRole('administrator')">
                Command: ${task.command}</br>
            </sec:authorize>${task.information}
        </p>
    </div>
</c:forEach>
</div>