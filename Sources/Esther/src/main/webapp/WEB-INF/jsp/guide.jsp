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
            If you are not familiar with the framework of discrete regulatory models, you can get a quick idea in our <a href="/resources/pdf/Regul_intro.pdf" >introductory PDF</a> to the topic. 
            For a more thorough description we can recommend for example the article <a href="http://www.irccyn.fr/franck/bib/ps-pdf-files/bioconcur-entcs-07.pdf">Semantics of Biological Regulatory Networks</a>.
        </p>
        <p class="par">  
            DBM is built over XML, so if you are familiar with XML, you should be able to get the core ideas very quickly.
            For a swift overview of how the model works, you can see the <i>example_model.dbm</i> model in the Public folder in your file tree. 
            The model is commented and provides all the necessary knowledge for understanding of how to create a model on your own. 
            In case of deeper interest, we also provide a complete and formal description of the modeling language in <a href="/resources/pdf/DBM_manual.pdf">this PDF manual</a>.
        </p>

        <p class="par">
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
            Parsybone reads the model and enumerates all the parametrizations that satisfy static constraints imposed on the model. 
            After enumeration, all the parametrizations are verified against a property specified in the model file.
            Those that satisfy the property are saved within an <i>sqlite</i> database file together with other relevant data that were gathered during the computation.
        </p>
        <p class="par">
            The result of the computation is not being stored automatically.
            Starting Parsybone creates an independent task that can be observed in the task list.
            To save the file, wait until the computation is finished and then click the <b>save</b> button.
        </p>
        
        <h3 class="subsubsection">Filter</h3>
        <p class="par">
            Upon opening an sqlite file, a list of parametrizations of the model together with other relevant data is displayed.
            The size of the output is up to 128 members, if there are more, they are not being displayed.
            The current list can be filtered based on various criteria.
            For filtering, click the <b>filter</b> button on the bottom of the screen.
            This brings up an interface for constructing constraints.
            Each constraint is numerical comparison between the values in the database and the one specified by the constraint.
            To apply the constrain, use <b>add</b> for making it appear in the list and then <b>apply</b> to start the process itself.
        </p>
        <p class="par">
            Constrains can be saved in a <i>filter</i> file for later use.
        </p>

        <h3 class="subsubsection">Behaviour map</h3>
        <p class="par">
            To visualise the dynamics of the simulation process, click the <b>Behaviour map</b> button at the bottom of the parameter list screen. 
            Please note that for visualisation all parametrizations not just the visible 128 are used. 
            The behaviour map will be created in form of an <i>XGMML</i> graph file under your filter/parameter set file.
            Also note that you have to save your filter if you want to use it for filtering visualised data.
        </p> 
        <p class="par">   
            The simulation is depicted using nodes to describe states of the simulation process and edges as transitions between them.
            The nodes coloured in shades of cyan represent measurements of the time series data whilst the gray ones depict steps in between the measurements.
            You can select individual nodes and edges to display additional information. 
            (Drag and drop allows to select multiple elements at once.)
        </p>

        <h2 class="subsection">File management</h2>
        <a id="FileManagement"></a>
        <p class="par">
            We provide the file management system to allow easy manipulation with the data. 
            To invoke file-related tools, right-click the respective file - this will create a floating list of options in that place.
            Whereas the functionality of most of the options are quite obvious, there are two cases that deserve closer attention.
        </p>
        <p class="par">
            To simplify collaboration we allow each user to publish his files.
            This is done by clicking the <b>make public</b> button. 
            After this, the file can be read (not overwritten) by any registered user of Esther.
        </p>
        <p class="par" >
            Each user is provided a 500 MB space for storing their files.
            Even though this may seem like a lot, the results of computation are usually quite spacious and it may quickly overrun the capacity.
            To preserve your files over the limit, you may download them using the respective button.
            The file may be later uploaded, if requested for further use.
        <h2 class="subsection">Questions</h2>
        <a id="Questions"></a>
        <p>
            In case of further interest, please send your questions to adam.streck [at] fu-berlin.de .
        </p>

    </body>
</html>
