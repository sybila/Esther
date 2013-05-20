if(jQuery) (function($)
{
    $.extend($.fn,
    {        
        initBehaviourMap: function()
        {
            var context = $(this);
            var file = context.find('div#widget_starter').attr('file');

            context.find('#node_table').tablesorter();
            context.find('#edge_table').tablesorter();

            var vis = new org.cytoscapeweb.Visualization(('cytoscape_web_' + file), { swfPath: "resources/flash/CytoscapeWeb",
                    flashInstallerPath: "resources/flash/playerProductInstall" });

            vis.ready(function()
                {    
                    vis.addListener('select', function(e)
                        {
                            context.find('#graph_information TABLE TABLE TBODY').empty();

                            for (var i in e.target)
                            {
                                var values = '<tr>';
                                var tableId = '#graph_information TABLE ';

                                switch (e.target[i].group)
                                {
                                    case 'nodes':
                                    {
                                        tableId += '#node_table';

                                        values += '<td>' + e.target[i].data['State'] + '</td>';
                                        values += '<td>' + e.target[i].data['Measurement'] + '</td>';
                                        values += '<td>' + e.target[i].data['Inbound Transitions'] + '</td>';
                                        values += '<td>' + e.target[i].data['Outbound Transitions'] + '</td>';

                                        break;
                                    }
                                    case 'edges':
                                    {
                                        tableId += '#edge_table';

                                        values += '<td>' + e.target[i].data['Source'] + '</td>';
                                        values += '<td>' + e.target[i].data['Target'] + '</td>';
                                        values += '<td>' + e.target[i].data['Transitions'] + '</td>';

                                        break;
                                    }
                                    default: break;
                                }

                                context.find(tableId).show();

                                values += '</tr>';
                                context.find(tableId + ' TBODY').append(values);
                            }

                            context.find('#node_table').trigger('update');
                            context.find('#node_table').tablesorter();
                            context.find('#edge_table').trigger('update');
                            context.find('#edge_table').tablesorter();

                        }).addListener('click', function()
                        {
                            context.find('#graph_information TABLE TABLE').hide();
                        });
                });

            $.get('File/Read', { file: file }, function(data)
                {
                    vis.draw({ network: data });
                });
        }
    });
})(jQuery);