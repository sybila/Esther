<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<head>
    <link rel="stylesheet" href="<c:url value="/resources/css/widget/parameterView.css"/>" type="text/css">
</head>

<div id="widget_starter" init_function="initParameterView" />

<h2 style="float: left" class="widget">Parameters:</h2>

<p style="text-align: right; padding-right: 64px;">
    <a id="column_options_button" class="expandable closed" href="#">Display Options ▼</a>
</p>

<div id="column_options" style="display: none;">
    <p style="float: left;">Select columns to display:</p>
    <p style="text-align: right; padding-right: 32px">
        <a id="all_columns" href="#">select all</a> / <a id="no_columns" href="#">select none</a>
    </p>
    <div style="width: 98%; margin-left: 4px;" id="column_list" class="container">
        <c:forEach items="${display_settings}" var="column_group">
            <c:if test="${not empty column_group.key}">
                <label class="column_checker">
                    <input column_num="group" type="checkbox" checked="checked" />
                    <c:out value="${column_group.key}" />
                </label></br>
            </c:if>
            <c:forEach items="${column_group.value}" var="column">
                <label class="column_checker" <c:if test="${not empty column_group.key}">style="padding-left: 16px;"</c:if>>
                    <input column_num="${column.key}" group_id="${column_group.key}" type="checkbox" checked="checked" />
                     <c:out value="${column.value}" />
                </label></br>
            </c:forEach>
        </c:forEach>
    </div>
    <input id="column_apply_button" class="button" style="float: left;" type="Submit" value="Apply" name="submit"/>
    <img id="column_options_hide" src="<c:url value="/resources/images/hide_up_button.png" />" />
</div>

<div class="viewpoint">
    <%@ include file="/WEB-INF/jsp/widget/parameterView/list.jsp" %>
</div>

<c:if test="${not empty behaviourMapper && not readonly}">
    <input style="float: left" id="behaviourMap" class="button" type="submit" value="Behaviour Map" name="submit" />
</c:if>
    
<p style="text-align: right; padding-right: 64px;" >
    <a id="filter" class="expandable closed" href="#">Filter ▲</a>
</p>

<div id="filter_controls" source="${file}" filter="${filter}" style="display: none;" >
    <img id="filter_hide" src="<c:url value="/resources/images/hide_button.png" />" />
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
            <img class="cancel_constraint" src="<c:url value="/resources/images/x_button.png" />" />
        </p>
    </c:forEach>
    </div>
    <input id="saveButton" class="button" type="Submit" value="Save" name="submit"/>
    <input id="applyButton" class="button" type="Submit" value="Apply" name="submit"/>
</div>
