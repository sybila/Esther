<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN""http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Esther registration</title>
    </head>
    <body>
        <h2>Registration Successful</h2>
        
        <p>
            Use the confirmation link we sent to ${email} to activate your account.
        </p>
        <p>
            You did not receive our e-mail? <a href="/ResendToken?user=${user_id}">Click here</a> to have it resend.
        </p>
    </body>
</html>
