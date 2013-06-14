<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN""http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Esther Home</title>
    </head>
    <body>
        <h1 class="section">Home</h1>
        <p class="par">
            Welcome to the Esther web-service.
        </p>
        <p class="par">
            Esther is an interface to a suite of tools for discrete simulation a reverse-engineering of gene regulatory and signal transduction networks. 
            For modeling we employ the framework of asynchronous boolean or multi-valued (commonly referred to as Thomas or Qualitative) networks.
            Currently Esther allows for conduction of the following tasks:
        <ul>
            <li style="pointer-events: auto;">Creating a regulation or signal transduction model.</li>
            <li style="pointer-events: auto;">Partial / full specification of kinetic parameters.</li>
            <li style="pointer-events: auto;">Automated parameter filtering by edge constraints and/or LTL formulae.</li>
            <li style="pointer-events: auto;">Comparison of parametrized models.</li>
            <li style="pointer-events: auto;">Manual parameter filtering.</li>
            <li style="pointer-events: auto;">Visualization of the dynamics.</li>
            <li style="pointer-events: auto;">File storage and management, file transfer.</li>            
        </ul>
    </p>
    <p class="par">
        For a more thorough description of the platform please refer to the <a href="/Guide">Guide page</a>. 
        The theoretical background of the computation is described in <a href="https://is.muni.cz/th/325017/fi_m/thesis_streck.pdf">this pdf</a>. 
    </p>
    <sec:authorize access="isAnonymous()">
        <p class="par">
            To access the tool-set and also obtain a server-side storage and computational capacity, 
            please <a href="/Registration">register</a>. 
            An e-mail with an activation link for your account will be sent to you within 24 hours.
        </p>
    </sec:authorize>
    <p class="par">
        Esther is being developed in collaboration of members of the <a href="http://sybila.fi.muni.cz">Systems Biology Laboratory</a> of Masaryk University and the <a href="http://www.mi.fu-berlin.de/en/math/groups/dibimath/index.html">Discrete Biomathematics group</a> of Freie Universität of Berlin.
    </p>
    <p class="par">
        The source code is distributed under <a href="http://www.gnu.org/licenses/gpl.html">GNU general public license v3</a> and is available on <a href="https://github.com/sybila/Esther.git">GitHub</a>.
    </p>
</body>
</html>
