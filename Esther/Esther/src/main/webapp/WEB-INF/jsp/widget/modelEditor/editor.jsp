<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<head>
    <link rel="stylesheet" href="<c:url value="/resources/css/widget/modelEditor.css"/>" type="text/css">
    <script type="text/javascript" src="<c:url value="/resources/scripts/widget/modelEditor.js"/>"></script>
</head>

<div id="widget_starter" init_function="initModelEditor" />

<h7>Model:</h7>

<textarea spellcheck="false" id="model">${model}</textarea>

<input class="button" id="save_button" file_id="${file}" type="Submit" value="Save" />
<c:if test="${not empty parsybone}">
    <input class="button" id="parsybone_button" file_id="${file}" type="Submit" value="Parsybone" />
</c:if>