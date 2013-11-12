var viss = { };

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

            viss[file] = vis;

            context.find('#layout_button').click(function(e)
                {
                    e.preventDefault();
                    
                    vis.layout('ForceDirected');
                    
                    return false;
                });
            
            vis.ready(function()
                {    
                    vis.addListener('select', function(e)
                        {
                            for (var i in e.target)
                            {
                                var values = '<tr id="' + e.target[i].data['id'] + '" >';
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

                                values += '</tr>';
                                context.find(tableId + ' TBODY').append(values);
                            }

                            context.find('#node_table').trigger('update');
                            context.find('#node_table').tablesorter();
                            context.find('#edge_table').trigger('update');
                            context.find('#edge_table').tablesorter();
                            
                            if (!context.find('#node_table').is(':visible') && (context.find('#node_table tbody tr').length > 0))
                            {
                                context.find('#node_table').show();
                            }
                            
                            if (!context.find('#edge_table').is(':visible') && (context.find('#edge_table tbody tr').length > 0))
                            {
                                context.find('#edge_table').show();
                            }

                        }).addListener('deselect', function(e)
                        {
                            for (var i in e.target)
                            {
                                var tableId = '#graph_information TABLE ';

                                switch (e.target[i].group)
                                {
                                    case 'nodes':
                                    {
                                        tableId += '#node_table';

                                        break;
                                    }
                                    case 'edges':
                                    {
                                        tableId += '#edge_table';

                                        break;
                                    }
                                    default: break;
                                }
                                
                                context.find(tableId + ' tr[id=' + e.target[i].data['id'] + ']').remove();
                            }
                            
                            context.find('#node_table').trigger('update');
                            context.find('#node_table').tablesorter();
                            context.find('#edge_table').trigger('update');
                            context.find('#edge_table').tablesorter();
                            
                            if (context.find('#node_table').is(':visible') && (context.find('#node_table tbody tr').length == 0))
                            {
                                context.find('#node_table').hide();
                            }
                            
                            if (context.find('#edge_table').is(':visible') && (context.find('#edge_table tbody tr').length == 0))
                            {
                                context.find('#edge_table').hide();
                            }
                        });
                });

            $.get('File/Read', { file: file }, function(data)
                {
                    vis.draw({ network: data, layout: 'Preset' });
                });
        }
    });
    
    $.extend($.fn,
    {       
        closeBehaviourMap: function()
        {
            var context = $(this);
            var file = context.find('div#widget_starter').attr('file');
            
            var vis = viss[file];
            
            $.post('File/Write', { file: file, data: vis.xgmml() });
        }
    });
})(jQuery);