<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<head>
    <link rel="stylesheet" href="<c:url value="/resources/css/widget/modelEditor.css"/>" type="text/css">
</head>

<div id="widget_starter" init_function="initModelEditor" /> 

<h2 class="widget">Model:</h2> <br />

<textarea spellcheck="false" id="model">${model}</textarea> <br />

<input class="button" id="save_button" file_id="${file}" type="Submit" value="Save" />
<c:if test="${not empty parsybone}">
    <input class="button" id="parsybone_button" file_id="${file}" type="Submit" value="Parsybone" />
</c:if>