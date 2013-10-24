<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<ul class="estherFileSystem">
    <li id="privateFolder" class="folder private">
        <div class="icon_container">
            <div id="icon"/>
        </div>
        <a href="#">My Files</a>
    </li>
    <li id="publicFolder" class="folder public">
        <div class="icon_container">
            <div id="icon"/>
        </div>
        <a href="#">Public</a>
    </li>
    <li class="file private unexpandable tasklist">
        <a file_id="tasklist" href="#">Task List</a>
    </li>
</ul>
