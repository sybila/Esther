if(jQuery) (function($)
{
    $.extend($.fn,
    {
        openInteractionGraph: function(file)
        {
            var context = $(this);
            
            context.empty();
            
            $.get('Model', { file: file }, function(data)
                {
                    context.append(data);
                    
                    context.find('#save_button').click(function()
                        {
                            $.post('File/Write',
                            { file: context.find('#save_button').attr('file_id'),
                                data: context.find('#model').val() });
                        });

                    context.find('#parsybone_button').click(function()
                        {
                            $.post('Parsybone', { file: context.find('#parsybone_button').attr('file_id')}, function(data)
                                {
                                    if (data.split('=')[0] == 'REACHED_LIMIT')
                                    {
                                        alert('You have reached the maximum number of ' + data.split('=')[1] +
                                            ' active tasks.\nPlease wait for the tasks in progress to finish first.');
                                    }
                                    else
                                    {
                                        openWidget('tasklist');
                                        
                                        $('#widget INPUT[name=refresh]').trigger('click');
                                    }
                                });
                        });
                });
        }
    });
})(jQuery);