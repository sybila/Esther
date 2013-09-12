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
                                rescueFile(context.find('#save_button').attr('file_id'), data.split('=')[1]);
                            }
                        });
                });

            context.find('#parsybone_button').click(function(e)
                {
                    e.preventDefault();

                    if ($(this).hasClass('open'))
                    {
                        $(this).text('Parsybone \u25b2');
                        $(this).removeClass('open');
                        $(this).addClass('closed');

                        context.find('#parsybone_controls').hide('slide', { direction: 'down' }, 640);
                    }
                    else if ($(this).hasClass('closed'))
                    {
                        $(this).text('Parsybone \u25bc');
                        $(this).removeClass('closed');
                        $(this).addClass('open');

                        context.find('#parsybone_controls').show('slide', { direction: 'down' }, 640);
                    }
                });

            context.find('#parsybone_hide').click(function()
                {
                    $(document).find('#widget #parsybone_button').trigger('click');
                });

            context.find('#parsybone_controls FORM#parsybone_options').submit(function()
                {
                    $('#parsybone_controls FORM#parsybone_options').ajaxSubmit({
                        data: { file: context.find('#save_button').attr('file_id') },
                        success: function(data)
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
                        }
                    });
                    
                    return false;
                });
        }
    });
})(jQuery);