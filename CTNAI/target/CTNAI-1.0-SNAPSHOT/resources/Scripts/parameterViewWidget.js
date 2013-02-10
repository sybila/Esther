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
                    
                    context.find('#filter').click(function(e)
                        {
                            e.preventDefault();
                            
                            if ($(this).hasClass('open'))
                            {
                                $(this).text('Filter \u25b2');
                                $(this).removeClass('open');
                                $(this).addClass('closed');
                                
                                $('#filter_controls').hide('slide', { direction: 'down' }, 640);
                            }
                            else if ($(this).hasClass('closed'))
                            {
                                $(this).text('Filter \u25bc');
                                $(this).removeClass('closed');
                                $(this).addClass('open');
                                
                                $('#filter_controls').show('slide', { direction: 'down' }, 640);
                            }
                        });
                });
        }
    });
})(jQuery);