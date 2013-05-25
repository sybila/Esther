function initializeParameterView(context, data)
{
    
}

function encodeConstraints(constraints)
{
    var data = '';

    for (var i = 0; i < constraints.length; i++)
    {
        if (i > 0)
        {
            data = data + '\n';
        }

        data = data + $(constraints[i]).attr('variable') + ';' +
            $(constraints[i]).attr('type') + ';' +
            $(constraints[i]).attr('value');
    }

    return data;
}

if(jQuery) (function($)
{
    $.extend($.fn,
    {        
        initParameterView: function()
        {
            var context = $(this);
            
            context.find('#parameter_list').tablesorter();

            context.find('#constraint_variable').change(function()
                {
                    switch(context.find('#constraint_variable option:selected').val())
                    {
                        case 'robustness':
                        {
                            context.find('#new_constraint #suffix').text('%.');
                            break;
                        }
                        default:
                        {
                            context.find('#new_constraint #suffix').text('.');
                            break;
                        }
                    }
                });

            context.find('#constraint_value').keyup(function()
                {
                    context.find('#constraint_value').val(context.find('#constraint_value').val().replace(/[^0-9]/g,''));
                });

            context.find('#filter').click(function(e)
                {
                    e.preventDefault();

                    if ($(this).hasClass('open'))
                    {
                        $(this).text('Filter \u25b2');
                        $(this).removeClass('open');
                        $(this).addClass('closed');

                        context.find('#filter_controls').hide('slide', { direction: 'down' }, 640);
                    }
                    else if ($(this).hasClass('closed'))
                    {
                        $(this).text('Filter \u25bc');
                        $(this).removeClass('closed');
                        $(this).addClass('open');

                        context.find('#filter_controls').show('slide', { direction: 'down' }, 640);
                    }
                });

            context.find('#filter_hide').click(function()
                {
                    $(document).find('#widget #filter').trigger('click');
                });

            context.find('#add_constraint').click(function(e)
                {
                    e.preventDefault();

                    var id = (context.find('#constraint_container p').length + 1);

                    context.find('#constraint_container')
                        .append('<p id="' + id + '" variable="' + context.find('#constraint_variable').val() +
                            '" type="' + context.find('#constraint_type').val() +
                            '" value="' + context.find('#constraint_value').val() + '">' +
                            context.find('#constraint_variable option:selected').text() + ' ' +
                            context.find('#constraint_type option:selected').text() + ' ' +
                            context.find('#constraint_value').val() +
                            context.find('#new_constraint #suffix').text() +
                            '<img class="cancel_constraint" src="/Esther/resources/images/x_button.png" /></p>');

                    context.find('#constraint_container #' + id + ' .cancel_constraint').click(function()
                        {
                            context.find('#constraint_container #' + id).remove();
                            context.find('#constraint_container').addClass('unsaved');
                        });

                    context.find('#constraint_container').addClass('unsaved');
                });

            context.find('#saveButton').click(function()
                {
                    var file = context.find('#filter_controls').attr('filter');

                    if ((typeof file == 'undefined') || (file == null) || (file == ''))
                    {
                        var name;

                        if (((name = prompt('Save as:')) != null) && (name != ''))
                        {
                            $.post('File/Create', { name: name, type: 'filter',
                                    parents: [ context.find('#filter_controls').attr('source') ] },
                                function(data)
                                {
                                    if (data.split('=')[0] == 'ERROR')
                                    {
                                        alert('Error: ' + data.split('=')[1]);
                                        return;
                                    }
                                    else
                                    {
                                        context.find('#filter_controls').attr('filter', data);
                                        file = data;
                                    }
                                });
                        }
                        else
                        {
                            return;
                        }
                    }
                    
                    var data = encodeConstraints(context.find('#constraint_container p'));

                    $.post('File/Write', { file: file, data: data }, function(data)
                        {
                            if (data.split('=')[0] == 'LIMIT_REACHED')
                            {
                                rescueFile(file, data.split('=')[1]);
                            }
                            else if (data.split('=')[0] == 'ERROR')
                            {
                                alert('Error: ' + data.split('=')[1]);
                            }
                            else
                            {
                                context.find('#constraint_container').removeClass('unsaved');
                            }
                        });
                });

            context.find('#applyButton').click(function()
                {
                    $.get('Widget/Parameters/Filter', { source: context.find('#filter_controls').attr('source'),
                            filter: encodeConstraints(context.find('#constraint_container p')) },
                        function(data)
                        {
                            context.find('.viewpoint').empty().append(data);

                            context.find('#parameter_list').tablesorter();
                        });

                    $(document).find('#widget #filter').trigger('click');
                });

            context.find('#constraint_container p .cancel_constraint').click(function()
                {
                    $(this).parent().remove();
                    context.find('#constraint_container').addClass('unsaved');
                });

            context.find('#behaviourMap').click(function()
                {
                    if (context.find('#constraint_container').hasClass('unsaved'))
                    {
                        if (!confirm('Warning: Any unsaved changes to the filter will have no effect on the behaviour map.'))
                        {
                            return;
                        }
                    }

                    var filter = context.find('#filter_controls').attr("filter");
                    var params;

                    if ((typeof filter == 'undefined') || (filter == null) || (filter == ''))
                    {
                        params = { file: context.find('#filter_controls').attr("source") };
                    }
                    else
                    {
                        params = { file: context.find('#filter_controls').attr("source"), filter: filter };
                    }

                    $.post('Widget/BehaviourMap', params, function(data)
                        {
                            if (data.split('=')[0] == 'LIMIT_REACHED')
                            {
                                rescueFile(data.split('=')[2], data.split('=')[1]);
                            }
                        });
                });
        }
    });
})(jQuery);