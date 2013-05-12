if(jQuery) (function($)
{
    $.extend($.fn,
    {
        initModelEditor: function()
        {
            var context = $(this);
            
            context.find('#save_button').click(function()
                {
                    $.post('File/Write', { file: context.find('#save_button').attr('file_id'),
                            data: context.find('#model').val() }, function(data)
                        {
                            if (data.split('=')[0] == 'LIMIT_REACHED')
                            {
                                resqueFile(context.find('#save_button').attr('file_id'), data.split('=')[1]);
                            }
                        });
                });

            context.find('#parsybone_button').click(function()
                {
                    $.post('Widget/Parsybone', { file: context.find('#parsybone_button').attr('file_id')}, function(data)
                        {
                            if (data.split('=')[0] == 'LIMIT_REACHED')
                            {
                                alert('You have reached the maximum number of ' + data.split('=')[1] +
                                    ' active tasks.\nPlease wait for the tasks in progress to finish first.');
                            }
                            else if (data.split('=')[0] == 'ERROR')
                            {
                                alert('Error: ' + data.split('=')[1]);
                            }
                            else
                            {
                                openWidget('tasklist');

                                $('#widget INPUT[name=refresh]').trigger('click');
                            }
                        });
                });
        }
    });
})(jQuery);