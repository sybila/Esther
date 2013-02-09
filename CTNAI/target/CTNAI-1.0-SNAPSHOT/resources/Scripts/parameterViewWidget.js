if(jQuery) (function($)
{
    $.extend($.fn,
    {
        openParameterView: function(file)
        {
            var context = $(this);
            
            context.empty();
            
            $.get('ListParameters', { file: file }, function(data)
                {
                    context.append(data);
                    
                    context.find('#parameter_list').tablesorter();
                    
                    //TODO - slide up filter controls
                });
        }
    });
})(jQuery);