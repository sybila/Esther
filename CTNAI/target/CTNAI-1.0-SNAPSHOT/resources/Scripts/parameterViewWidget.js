function initializeParameterView(context, data)
{
    context.append(data);

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

    context.find('#constraint_value').keydown(function(e)
        {
                // backspace, delete, tab, escape, and enter
            if (e.keyCode == 46 || e.keyCode == 8 || e.keyCode == 9 || e.keyCode == 27 || e.keyCode == 13 || 
                // Allow: Ctrl+A
               (e.keyCode == 65 && e.ctrlKey == true) || 
                // Allow: home, end, left, right
               (e.keyCode >= 35 && e.keyCode <= 39))
            {
                // let it happen, don't do anything
                return;
            }
            else
            {
                // Prevent keypress if not number
                if (e.shiftKey || e.altKey || (e.keyCode < 48 || e.keyCode > 57) && (e.keyCode < 96 || e.keyCode > 105 ))
                {
                    e.preventDefault(); 
                }
            }
        });

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
                    '<img class="cancel_constraint" src="/CTNAI/resources/Graphics/x_button.png" /></p>');

            context.find('#constraint_container #' + id + ' .cancel_constraint').click(function()
                {
                    context.find('#constraint_container #' + id).remove();
                });
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
                            var filter_data = encodeConstraints(context.find('#constraint_container p'));

                            $.post('File/Write', { file: data, data: filter_data });
                        });
                }
                else
                {
                    return;
                }
            }
            else
            {
                var data = encodeConstraints(context.find('#constraint_container p'));

                $.post('File/Write', { file: file, data: data });
            }
        });

    context.find('#applyButton').click(function()
        {
            $.get('Parameters/Filter', { source: context.find('#filter_controls').attr('source'),
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
        });
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
        openParameterView: function(file, filter)
        {
            var context = $(this);
            
            context.empty();
            
            var params;
            
            if (typeof filter == 'undefined')
            {
                params = { file: file };
            }
            else
            {
                params = { file: file, filter: filter };
            }
            
            $.get('Parameters/List', params, function(data)
                {
                    initializeParameterView(context, data);
                });
        }
    });
})(jQuery);