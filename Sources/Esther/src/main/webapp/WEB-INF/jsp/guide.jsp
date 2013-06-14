<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN""http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Esther Guide</title>
    </head>
    <body>
        <h1 class="section">Guide</h1>
        <ul class="page_navigation">
            <h1 class="toc">  Page navigation </h1>
            <li>
                <a href="#GeneralUsage">General usage</a>
            </li>
            <li>
                <a href="#CreatingAModel">Creating a model</a>
            </li>
            <li>
                <a href="#UsingTheTools">Using the tools</a>
            </li>
            <li>
                <a href="#UsingTool">File management</a>
            </li>
            <li>
                <a href="#Questions">Questions</a>
            </li>
        </ul>

        <h2 class="subsection">General usage</h2> <a id="GeneralUsage"></a>
        <p class="par">
            To use Esther it is mandatory to first register and log in. 
            After this procedure is completed, one can access the analyze page of Esther which serves as an interface to our tool-set for parameter identification.
            The interface consists of a data-tree, providing access to datafiles owned by the respective user or public to all, 
            and a widget pane, where the content of individual files may be managed and the computation may be started.
            Opening a datafile spawns a window displaying its contents in the form of an appropriate widget.
        </p>
        <p class="par">  
            Each user is provided with 500 MB of data storage and 2 server-side processes for computation.
        </p>

        <h2 class="subsection">Creating a model</h2>
        <a id="CreatingAModel"></a>
        <p class="par">
            To create a model, either copy one of the publicly available one that are stored in the public folder of the interface or create a new empty file using the button on the Start screen.
            A model is a multi-valued, multigraph described using our XML-based language DBM (a visual environment is currently being developed).
        </p>
        <p class="par">
            If you are not familiar with the framework of discrete regulatory models, you can get a quick idea in our <a href src="/Esther/resources/pdf/" >introductory PDF</a> to the topic. 
            For a more thorough description we can recommend for example the article <a href="http://www.irccyn.fr/franck/bib/ps-pdf-files/bioconcur-entcs-07.pdf">Semantics of Biological Regulatory Networks</a>.
        </p>
        <p class="par">  
            DBM is built over XML, so if you are familiar with XML, you should be able to get the core ideas very quickly.
            For a swift overview of how the model works, you can see the <i>example_model.dbm</i> model in the Public folder in your file tree. 
            The model is commented and provides all the necessary knowledge for understanding of how to create a model on your own. 
            In case of deeper interest, we also provide a complete and formal description of the modeling language in <a href="/Esther/resources/pdf/DBM_manual.pdf">this PDF manual</a>.
        </p>

        <p>
            Once a model is finished, it must be saved before starting the simulation.
        </p>

        <h2 class="subsection">Using the tools</h2>
        <a id="UsingTools"></a>
        <p class="par">
            Our tools operate on the server and provide quite strong computational capabilities.
            To use a tool it is usually sufficient to click an appropriate button in the current window.
            Clicking a button will start the respective program using the currently visible file and store its results to a new file of appropriate sort.
            The result file is usually created as a child of the source file, but in some cases it must be explicitly saved first.
        </p>

        <h3 class="subsubsection">Parsybone</h3>
        <p class="par">
            Upon opening the sqlite file you will see a table of possible parametrizations. 
            To save you the trouble only 128 of the parameters is displayed. 
            To view the other ones that are interesting for you apply a parameter filter.
            You can place multiple constraints into one filter and you can put 
            a constraint on many different values available from the dropdown list.
        </p>
        
        
        <h3 class="subsubsection">Filter</h3>
        <p class="par">
            Upon opening the sqlite file you will see a table of possible parametrizations. 
            To save you the trouble only 128 of the parameters is displayed. 
            To view the other ones that are interesting for you apply a parameter filter.
            You can place multiple constraints into one filter and you can put 
            a constraint on many different values available from the dropdown list.
        </p>

        <h3 class="subsubsection">Behaviour map</h3>
        <p class="par">
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

        <h2 class="subsection">File management</h2>
        <a id="FileManagement"></a>
        <p>
            Each user is provided a 500 MB space for storing their files.
            Even though this may seem like a lot, the results of computation are usually quite spacious.
        </p>

        <h2 class="subsection">Questions</h2>
        <a id="Questions"></a>
        <p>
            Each user is provided a 500 MB space for storing their files.
            Even though this may seem like a lot, the results of computation are usually quite spacious.
        </p>

    </body>
</html>
