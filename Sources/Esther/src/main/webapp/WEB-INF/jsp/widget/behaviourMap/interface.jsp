<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<head>
    <link rel="stylesheet" href="<c:url value="/resources/css/widget/behaviourMap.css"/>" type="text/css">
    <script type="text/javascript" src="<c:url value="/resources/scripts/widget/behaviourMap/cytoscapeweb.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/resources/scripts/widget/behaviourMap/interface.js"/>"></script>
</head>

<div id="widget_starter" init_function="initBehaviourMap" file="${file}" />

<h7>Behaviour Map:</h7>

<div class="cytoscape" id="cytoscape_web_${file}" />

<div id="graph_information">
    <table style="border-collapse: separate;">
        <tr>
            <td valign="top" style="border: 0px; margin: 2px;">
                <table class="list" id="node_table" style="display: none;">
                    <thead>
                        <tr>
                            <th>State</th>
                            <th>Measurement</th>
                            <th>Inbound Transitions</th>
                            <th>Outbound Transitions</th>
                        </tr>
                    </thead>
                    <tbody></tbody>
                </table>
            </td>
            <td valign="top" style="border: 0px; margin: 2px;">
                <table class="list" id="edge_table" style="display: none;">
                    <thead>
                        <tr>
                            <th>Source</th>
                            <th>Target</th>
                            <th>Transitions</th>
                        </tr>
                    </thead>
                    <tbody></tbody>
                </table>
            </td>
        </tr>
    </table>
</div>

<a style="float: right; margin: 8px;" href="http://cytoscapeweb.cytoscape.org/">
    <img src="http://cytoscapeweb.cytoscape.org/img/logos/cw_s.png" alt="Cytoscape Web"/>
</a>