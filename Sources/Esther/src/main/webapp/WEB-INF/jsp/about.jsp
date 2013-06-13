<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN""http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Esther About</title>
    </head>
    <body>
        <h1 class="section">About</h1>
        <ul class="page_navigation">
            <h1 class="toc">  Page navigation </h1>
            <li>
                <a href="#Esther">What's Esther?</a>
            </li>
            <li>
                <a href="#ParamEval">Parameter evaluation</a>
            </li>
            <li>
                <a href="#Parsybone">Parsybone</a>
            </li>
            <li>
                <a href="#Tutorial">Getting started tutorial</a>
            </li>
        </ul>
        
        <h2 class="subsection">What's Esther?</h2>
        <a id="Esther"></a>
        <p>
            Esther is an application for developing and sharing gene regulatory models. 
            Esther provides a common user interface for creating models, 
            running <a href="#ParamEval">parameter evaluation</a> analyses and browsing the results.
        </p>
        
        <p>
            Esther was developed to allow easy access to tools for gene regulatory network inference. 
            If the topic interests you, feel free to <a href="/Esther/Registration">register</a> right now! No payment required!
        </p>
        
        <p>
            Esther is available under the <a href="http://www.gnu.org/licenses/gpl.html">GNU general public license</a>.
            You can access all the source codes and documentation on <a href="https://github.com/sybila/Esther.git">GitHub</a>.
        </p>
        
        <h2 class="subsection">Parameter evaluation</h2>
        <a id="ParamEval"></a>
        <p>
            The process of parameter evaluation is sequential analysis of the input model. 
            To specify the dynamics of a gene regulatory model, 
            it is necessary to define the properties of the interactions between the species. 
            The possible assignment of those properties, or parameters, is called a parametrization.
        </p>
        
        <p>
            The task of Esther is to give you the tools necessary to find the optimal parametrization of your network.
            The first step is the identification of the parametrization that satisfy the input criteria,
            (the tested hypothesis) you put into your model as an LTL formula. 
            This is done with the use of a powerful coloured model checker <a href="#Parsybone">Parsybone</a>.
        </p>
        
        <p>
            Once you obtain the acceptable parametrizations you can filter them by the desired criteria by the parameter filter. 
            And finally, it's said that one picture stands for thousand words, so Esther allows you to visualise the results. 
            The behaviour map graph can greatly help in identifying the workings behind the behaviour of your model.
        </p>
        
        <p>
            To learn more about the procedure of parameter evaluation, 
            refer to the published <a href="http://link.springer.com/chapter/10.1007%2F978-3-642-33636-2_13">article</a>
        </p>
        
        <h2 class="subsection">Parsybone</h2>
        <a id="Parsybone"></a>
        <p>
            Parsybone is a complex tool for parameter identification Esther uses. 
            The tool utilizes the technique of coloured model checking and is 
            easily distributable creating great speedups for complex tasks.
        </p>
        
        <p>
            Parsybone was as well developed at <a href="http://sybila.fi.muni.cz">SyBiLa</a> 
            and is available on <a href="https://github.com/sybila/Parsybone.git">GitHub</a> under <a href="http://www.gnu.org/licenses/gpl.html">GNU GPL</a>.
        </p>
        
        <h2 class="subsection">Getting started tutorial</h2>
        <a id="Tutorial"></a>
        <p>
            This is a brief tutorial to get you started with Esther.
        </p>
        
        <h3 class="subsubsection">1. Creating a model:</h3>
        <p>
            To create a model open the start page on the analysis screen and click the "create a new model" link. 
            This will bring up a new model file opened in the model editor. 
            Editing models with Esther is simple - all you have to do is write the model definition in XML format. 
            See the modelling section of <a href="/Esther/resources/pdf/parsyboneManual.pdf">this manual</a> 
            to discover all the possibilities the model file allows you.
        </p>
        
        <h3 class="subsubsection">2. Parameter identification</h3>
        <p>
            Running the parameter identification is simple! 
            Just click the Parsybone button at the bottom of the model editor and a new Task will start up. 
            You can keep track of the task in your Task List available from the file system browser. 
            After the task finishes you will be able to save the result in form of an sqlite 
            file you can find under your model file.
        </p>
        
        <h3 class="subsubsection">3. Filtering parameters</h3>
        <p>
            Upon opening the sqlite file you will see a table of possible parametrizations. 
            To save you the trouble only 128 of the parameters is displayed. 
            To view the other ones that are interesting for you apply a parameter filter.
            You can place multiple constraints into one filter and you can put 
            a constraint on many different values available from the dropdown list.
        </p>
        
        <h3 class="subsubsection">4. Behaviour map</h3>
        <p>
            To visualise the parameters chosen by the filter (all the whole parameter set obtained) 
            just click the Behaviour map button at the bottom of the parameter list screen. 
            (Please note that for visualisation all parametrizations not just the visible 128 are used. 
            Also note that you have to save your filter if you want to use it for filtering visualised data.)
            The behaviour map will be created in form of an XGMML graph file under your filter/parameter set file.
            When you open an behaviour map the graph of all possible behaviours of the 
            model will be displayed in a transition graph. 
            The nodes of the graph are states the model visits and edges are the transitions it uses. 
            The nodes coloured in shades of cyan are measurements of the time series data 
            whilst the gray ones are additional steps required by the model.
            You can select individual nodes and edges to display additional information. 
            (Drag and drop allows to select multiple elements at once.)
        </p>
        
    </body>
</html>
