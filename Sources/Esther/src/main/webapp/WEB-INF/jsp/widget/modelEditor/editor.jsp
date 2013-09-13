<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<head>
    <link rel="stylesheet" href="<c:url value="/resources/css/widget/modelEditor.css"/>" type="text/css">
</head>

<div id="widget_starter" init_function="initModelEditor" /> 

<h2 class="widget">
    <c:if test="${content eq 'model'}">Model:</c:if>
    <c:if test="${content eq 'property'}">Property:</c:if>
</h2> <br />

<textarea spellcheck="false" id="model">${data}</textarea> <br />

<input class="button" id="save_button" file_id="${file}" <c:if test="${content eq 'property'}">model_id="${model}"</c:if> type="Submit" value="Save" style="float: left;" />
    
<c:if test="${content eq 'model'}">
    <p style="text-align: right; padding-right: 64px;" >
        <a id="new_property_button" href="#">New Property</a>
    </p>
</c:if>

<c:if test="${(not empty parsybone) and (content eq 'property')}">
    <p style="text-align: right; padding-right: 64px;" >
        <a id="parsybone_button" class="expandable closed" href="#">Parsybone ▲</a>
    </p>
    
    <div id="parsybone_controls" style="display: none;" >
        <img id="parsybone_hide" src="<c:url value="/resources/images/hide_button.png" />" />
        <form id="parsybone_options" action="<c:url value='Widget/Parsybone' />" method="POST">
            <p>
                <label><input type="checkbox" name="compute_robustness" checked="checked"/> Calculate Robustness</label>
            </p>
            <p>
                <label><input type="checkbox" name="compute_witnesses" checked="checked"/> Compute Witnesses</label>
            </p>
            <p>
                <input class="button" type="submit" value="Parsybone" />
            </p>
        </form>
    </div>
</c:if>