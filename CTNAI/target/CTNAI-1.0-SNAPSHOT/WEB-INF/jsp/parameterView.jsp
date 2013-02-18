<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<div class="viewpoint">
    <%@ include file="/WEB-INF/jsp/parameterList.jsp" %>
</div>

<input style="float: left" id="behaviourMap" class="button" type="submit" value="Behaviour Map" name="submit" />

<p style="text-align: right; padding-right: 64px;" >
    <a id="filter" class="expandable closed" href="#">Filter ▲</a>
</p>

<div id="filter_controls" source="${file}" filter="${filter}" style="display: none;" >
    <img id="filter_hide" src="<c:url value="/resources/Graphics/hide_button.png" />" />
    <p style="padding-top: 32px;">
        Constraints:
    </p>
    <p id="new_constraint">
        <select id="constraint_variable" class="drop_down">
            <option value="cost">Cost</option>
            <option value="robustness">Robustness</option>
            <c:forEach items="${context_masks}" var="context_mask">
                <option value="${context_mask.key}"><c:out value="${context_mask.value}" /></option>
            </c:forEach>
        </select>
        
        <select id="constraint_type" class="drop_down">
            <option value="eq">equal to</option>
            <option value="gt">greater than</option>
            <option value="lt">less than</option>
        </select>
        
        <input id="constraint_value" type="number" value="0" />
        
        <c id="suffix">.</c>
        
        <a id="add_constraint" href="#">Add</a>
    </p>
    <div id="constraint_container" class="container">
    <c:forEach items="${filter_properties}" var="constraint">
        <p id="${constraint.key}" variable="${constraint.value[0]}" type="${constraint.value[2]}" value="${constraint.value[4]}">
            ${constraint.value[1]} ${constraint.value[3]} ${constraint.value[4]}${constraint.value[5]}
            <img class="cancel_constraint" src="<c:url value="/resources/Graphics/x_button.png" />" />
        </p>
    </c:forEach>
    </div>
    <input id="saveButton" class="button" type="Submit" value="Save" name="submit"/>
    <input id="applyButton" class="button" type="Submit" value="Apply" name="submit"/>
</div>
