<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<div id="cytoscape_web">
    
</div>

<div id="graph_information">
    <table style="border-collapse: separate;">
        <tr>
            <td valign="top" style="border: 0px; margin: 2px;">
                <table id="node_table" style="display: none;">
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
                <table id="edge_table" style="display: none;">
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