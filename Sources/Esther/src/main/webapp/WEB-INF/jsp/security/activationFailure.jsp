<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN""http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Esther registration</title>
    </head>
    <body>
        <h1 class="section">Activation failed</h1>
        
        <p>
            Oops! Something went wrong. </br>
            Please make sure you copied the activation link from your e-mail properly.
        </p>
        <p>
            Is your account already activated? It's not possible to activate an account twice.
            If you're unsure you can try <a href="/Login">logging in</a> right now.
        </p>
        <p>
            Is the link we sent you broken? If so, you can have the confirmation e-mail resent by clicking
            <a href="/ResendToken?user=${user_id}">here</a>.
        </p>
    </body>
</html>
