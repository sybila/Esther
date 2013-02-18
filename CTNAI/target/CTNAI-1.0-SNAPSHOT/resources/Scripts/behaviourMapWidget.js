if(jQuery) (function($)
{
    $.extend($.fn,
    {        
        openBehaviourMap: function(file)
        {
            var context = $(this);
            
            context.empty();
            
            context.append('<div id="cytoscape_web" style="height: 564px; width: 712px; margin: 8px;" />' +
                '<a style="float: right; margin: 8px;" href="http://cytoscapeweb.cytoscape.org/">' + 
                '<img src="http://cytoscapeweb.cytoscape.org/img/logos/cw_s.png" alt="Cytoscape Web"/></a>');
            
            var vis = new org.cytoscapeweb.Visualization('cytoscape_web', { swfPath: "resources/Flash/CytoscapeWeb",
                    flashInstallerPath: "resources/Flash/playerProductInstall" });
                
            $.get('File/Read', { file: file }, function(data)
                {
                    vis.draw({ network: data });
                });
        }
    });
})(jQuery);