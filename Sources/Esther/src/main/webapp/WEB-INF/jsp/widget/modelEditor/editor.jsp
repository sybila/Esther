<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<head>
    <link rel="stylesheet" href="<c:url value="/resources/css/widget/modelEditor.css"/>" type="text/css">
</head>

<div id="widget_starter" init_function="initModelEditor" /> 

<h2 class="widget">Model:</h2> <br />

<textarea spellcheck="false" id="model">${model}</textarea> <br />

<input class="button" id="save_button" file_id="${file}" type="Submit" value="Save" style="float: left;" />
<c:if test="${not empty parsybone}">
    <p style="text-align: right; padding-right: 64px;" >
        <a id="parsybone_button" class="expandable closed" href="#">Parsybone ▲</a>
    </p>
    
    <div id="parsybone_controls" style="display: none;" >
        <img id="parsybone_hide" src="<c:url value="/resources/images/hide_button.png" />" />
        <form id="parsybone_options" name="form" action="<c:url value='Widget/Parsybone' />" method="POST">
            <p>
                <input type="checkbox" name="compute_robustness" checked="checked"/>
            </p>
            <p>
                <input type="checkbox" name="compute_witnesses" checked="checked"/>
            </p>
            <p>
                <input class="button" type="Submit" value="Parsybone" name="submit"/>
            </p>
        </form>
    </div>
</c:if>