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
        <p>
            Welcome to Esther, the parameter estimator for discrete gene regulatory networks.
        </p>
        <p>
            Do you have a beautiful gene regulatory network but no parameters for the interactions?</br>
            Then Esther is for you!
        </p>
        <p>
            Do you have none? Worry not!</br>
            Esther contains a model editor that makes it all too easy to create a network of your own!
        </p>
        <p>
            So what is it that Esther does?</br>
            Well, basically Esther provides you with a set of tools, including a powerful
            coloured model checker <a href="/About#Parsybone">Parsybone</a>,
            that you can use to analyse your model and determine the optimal parametrizations.
        </p>
        <p>
            How is this done?</br>
            First, you upload your model to Esther or create a new model using our model editor.</br>
            Next step is to specify the behaviour you are looking for.
            I mean, you usually make a model to observe some behaviour in it.
            (Just looking at the interaction graph doesn't make it all that fun, eh?)</br>
            Well, with a model and an hypothesis to check you can generate all the acceptable parametrizations.
            And what's more! The parametrizations come ranked by several criteria.
            All that's left is to filter them or even visualise them and voila!
            You have just reverse engineered parameters for you model!</br>
            If you want to learn more about parameter evaluation visit our <a href="/About">About</a> page.
        </p>
        <sec:authorize access="isAnonymous()">
            <p>
                You don't have an account yet?</br>
                Don't let your models wait and <a href="/Registration">register now!</a> Completely free of charge!</br>
                We even give you a free server-side storage space for your models and analyses.
            </p>
        </sec:authorize>
        <p>
            Esther was developed at Systems Biology Laboratory (<a href="http://sybila.fi.muni.cz">SyBiLa</a>)
            of Masaryk University, Brno, Czech Republic.</br>
            Esther is distributed under <a href="http://www.gnu.org/licenses/gpl.html">GNU general public license</a>
            and all the source codes and documentation are available at
            <a href="https://github.com/sybila/Esther.git">GitHub</a>.
        </p>
    </body>
</html>
