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
                });
        }
    });
})(jQuery);