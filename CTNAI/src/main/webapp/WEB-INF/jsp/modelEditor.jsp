<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<h7>Model:</h7>

<textarea spellcheck="false" id="model">${model}</textarea>

<input class="button" id="save_button" file_id="${file}" type="Submit" value="Save" />
<input class="button" id="parsybone_button" file_id="${file}" type="Submit" value="Parsybone" />

<div id="parsybone_output" style="display: none;" >
    <p style="padding-top: 32px;">
        Parsybone is running...
    </p>
</div>
