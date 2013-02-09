<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<c:forEach items="${links}" var="link">
    <a func="${link.key}" href="#">${link.value}</a><br/>
</c:forEach>