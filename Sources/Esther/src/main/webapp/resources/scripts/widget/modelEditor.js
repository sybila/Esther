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
                
            context.find('#new_property_button').click(function()
                {
                    var name;
                    var parent_id = context.find('#save_button').attr('file_id');
                    
                    if (((name = prompt('Enter property name: ', '')) != null) && (name != ''))
                    {
                        $.post('File/Create', { name: name, type: 'ppf', parent: parent_id }, function(data)
                            {
                                if (data.split('=')[0] == 'ERROR')
                                {
                                    alert('Error creating new prperty file: ' + data.split('=')[1]);
                                }
                                else
                                {
                                    appendFileEntries($('UL.estherFileSystem LI#privateFolder'), parent_id, data,
                                        (name + '.ppf'), 'ppf', 'private', false, false);
                                    
                                    openWidget(data, (name + '.ppf'), 'ppf', parent_id);
                                }
                            });
                    }
                })

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
                
            context.find('#parsybone_controls form#parsybone_options input[name=compute_witnesses]').click(function()
                {
                    if ($(this).prop('checked'))
                    {
                        context.find('#parsybone_controls form#parsybone_options input[name=negate]').prop('checked', false);
                    }
                });
                
            context.find('#parsybone_controls form#parsybone_options input[name=compute_robustness]').click(function()
                {
                    if ($(this).prop('checked'))
                    {
                        context.find('#parsybone_controls form#parsybone_options input[name=negate]').prop('checked', false);
                    }
                });
                
            context.find('#parsybone_controls form#parsybone_options input[name=minimise_cost]').click(function()
                {
                    if ($(this).prop('checked'))
                    {
                        context.find('#parsybone_controls form#parsybone_options input[name=negate]').prop('checked', false);
                        context.find('#parsybone_controls form#parsybone_options input[name=bound]').prop('checked', false);
                    }
                });
                
            context.find('#parsybone_controls form#parsybone_options input[name=bound]').click(function()
                {
                    if ($(this).prop('checked'))
                    {
                        context.find('#parsybone_controls form#parsybone_options input[name=negate]').prop('checked', false);
                        context.find('#parsybone_controls form#parsybone_options input[name=minimise_cost]').prop('checked', false);
                    }
                });
                
            context.find('#parsybone_controls form#parsybone_options input[name=bound_value]').keyup(function()
                {
                    context.find('#parsybone_controls form#parsybone_options input[name=bound_value]')
                        .val(context.find('#parsybone_controls form#parsybone_options input[name=bound_value]')
                        .val().replace(/[^0-9]/g,''));
                });
                
            context.find('#parsybone_controls form#parsybone_options input[name=negate]').click(function()
                {
                    if ($(this).prop('checked'))
                    {
                        context.find('#parsybone_controls form#parsybone_options input[name=compute_witnesses]').prop('checked', false);
                        context.find('#parsybone_controls form#parsybone_options input[name=compute_robustness]').prop('checked', false);
                        context.find('#parsybone_controls form#parsybone_options input[name=minimise_cost]').prop('checked', false);
                        context.find('#parsybone_controls form#parsybone_options input[name=bound]').prop('checked', false);
                    }
                });
                
            context.find('#parsybone_controls #parsybone_filters').droppable({
                hoverClass: 'drag_n_drop_hover',
                accept: 'li.file',
                drop: function(e, ui)
                    {
                        if (ui.draggable.find('a').attr('file_type') != 'sqlite')
                        {
                            return false;
                        }
                        
                        var entry = context.find('#parsybone_filters #' + ui.draggable.find('> a').attr('file_id'));
                        
                        if (entry.length > 0)
                        {
                            return false;
                        }
                        
                        $(this).append('<p id="' + ui.draggable.find('> a').attr('file_id') + '">' +
                            ui.draggable.find('> a').text() +
                            '<img class="cancel_filter" src="/resources/images/x_button.png" /></p>');

                        context.find('#parsybone_filters #' + ui.draggable.find('> a').attr('file_id') + ' img.cancel_filter').click(function()
                            {
                                context.find('#parsybone_filters #' + ui.draggable.find('> a').attr('file_id')).remove();
                            });

                        return false;
                    }
            });
                
            context.find('#parsybone_controls form#parsybone_options').submit(function()
                {
                    var filters = [];
                    
                    var i = 0;
                    context.find('#parsybone_controls #parsybone_filters p').each(function()
                        {
                            filters[i] = $(this).attr('id');
                            i++;
                        });
                    
                    $('#parsybone_controls FORM#parsybone_options').ajaxSubmit({
                        data:
                        { 
                            model: context.find('#save_button').attr('model_id'),
                            property: context.find('#save_button').attr('file_id'),
                            filters: filters
                        },
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
                                openWidget('tasklist', 'Task List', null, null);

                                $('#widget INPUT[name=refresh]').trigger('click');
                            }
                        }
                    });
                    
                    return false;
                });
        }
    });
})(jQuery);