var timeoutID = null;

if(jQuery) (function($)
{
    $.extend($.fn,
    {
        setupTabs: function()
        {
            function scrollerOnTop(t)
            {
                t.before('<div id="scrollBar" style="overflow-x: scroll; overflow-y: hidden; width: 100%; font-size: 1px;">' +
                    '<div style="width: ' + t[0].scrollWidth + 'px">&nbsp;</div></div>');
                
                t.parent().find('#scrollBar').scroll(function()
                    {
                        t.scrollLeft($(document).find('#scrollBar').scrollLeft());
                    });
            }
            
            var context = $(this);
            
            context.tabs({
                    activate: function(e, ui)
                        {
                            selectFile(ui.newTab.attr('file'));
                        }
                });
            
            scrollerOnTop(context.find('#tabs'));
            
            context.find('#tabs LI IMG').click(function()
                {
                    closeTab($(this).parent());
                });
        }
    });
    
    $.extend($.fn,
    {
        openTaskList: function()
        {
            function setupTaskList(context)
            {
                context.find('DIV.task').each(function()
                    {
                        var task = $(this);

                        task.find('IMG').unbind('click');
                        task.find('IMG').click(function()
                            {
                                if (task.attr('status').trim() == 'ready')
                                {
                                    if (!confirm('Are you sure you want to cancel this task? You will be unable to access the result once cancelled.'))
                                    {
                                        return;
                                    }
                                }
                                else if (task.attr('status').trim() == 'running')
                                {
                                    if (!confirm('This task is still running. Are you sure you want to cancel it?'))
                                    {
                                        return;
                                    }
                                }

                                $.post('Task/Cancel', { task: task.attr('task_id') });
                                task.remove();
                            });

                        task.find('INPUT').unbind('click');
                        task.find('INPUT').click(function(e)
                            {
                                e.preventDefault();
                                
                                $.post('Task/Save', { task: task.attr('task_id')}, function(data)
                                    {
                                        if (data.split('=')[0] == 'LIMIT_REACHED')
                                        {
                                            rescueFile(task.attr('result_id'), data.split('=')[1]);
                                        }
                                        else if (data.split('=')[0] == 'ERROR')
                                        {
                                            alert('Error: ' + data.split('=')[1]);
                                        }
                                        else
                                        {
                                            appendFileEntries($('UL.estherFileSystem LI#privateFolder'), task.attr('property_id'),
                                                task.attr('result_id'), data, extractExtension(data), 'private', false, false);

                                            context.find('INPUT[name=refresh]').trigger('click');
                                            
                                            openWidget(task.attr('result_id'), data, extractExtension(data), task.attr('property_id'));
                                        }
                                    });
                                    
                                return false;
                            });

                        $(task).unbind('click');
                        $(task).click(function()
                            {
                                if (task.hasClass('expanded'))
                                {
                                    task.animate({ height: 42 }, 640, function()
                                        {
                                            task.removeClass('expanded');
                                        }); 
                                    task.find('P').hide();
                                }
                                else
                                {
                                    task.animate({ height: 200 }, 640, function()
                                        {
                                            task.addClass('expanded');
                                        });
                                    task.find('P').show();
                                }
                            });
                    });
            }
            
            var context = $(this);
            
            $.get('Tasks', function(data)
                {
                    context.append(data);
                    
                    context.find('INPUT[name=refresh]').click(function()
                        {
                            window.clearTimeout(timeoutID);

                            timeoutID = null;
                            
                            context.append('<div id="tmp" style="display: none;" />');
                            
                            $.get('Tasks', function(data)
                                {
                                    context.find('#tmp').append(data);
                            
                                    var updatedTasks = context.find('#tmp DIV.task');
                                    var tasks = context.find('> DIV.viewpoint > DIV.task');

                                    tasks.each(function()
                                        {
                                            var closed = true;

                                            for (var i = 0; i < updatedTasks.length; i++)
                                            {
                                                if ($(updatedTasks[i]).attr('task_id') == $(this).attr('task_id'))
                                                {
                                                    closed = false;
                                                    break;
                                                }
                                            }

                                            if (closed)
                                            {
                                                $(this).remove();
                                            }
                                        });

                                    updatedTasks.each(function()
                                        {
                                            var exists = false;

                                            for (var i = 0; i < tasks.length; i++)
                                            {
                                                if ($(tasks[i]).attr('task_id') == $(this).attr('task_id'))
                                                {
                                                    $(tasks[i]).find('TH').html($(this).find('TH').html());
                                                    $(tasks[i]).find('P').html($(this).find('P').html());
                                                    $(tasks[i]).attr('status', $(this).attr('status'));
                                                    $(tasks[i]).find('INPUT').attr('style', $(this).find('INPUT').attr('style'));

                                                    exists = true;
                                                    break;
                                                }
                                            }

                                            if (!exists)
                                            {
                                                context.find('> DIV.viewpoint').append(this);
                                            }
                                        });

                                    context.find('#tmp').remove();

                                    setupTaskList(context);
                                    startTaskListRefreshTimer();
                                });
                        });
                
                    context.find('INPUT[name=cancelAll]').click(function()
                        {
                            context.find('DIV.task IMG').trigger('click');
                        });
                        
                    setupTaskList(context);
                });
        }
    });
})(jQuery);

function newModel()
{
    var name;

    if (((name = prompt('Enter model name: ', '')) != null) && (name != ''))
    {
        $.post('File/Create', { name: name, type: 'pmf' }, function(data)
            {
                if (data.split('=')[0] == 'ERROR')
                {
                    alert('Error creating new model: ' + data.split('=')[1]);
                }
                else
                {
                    appendFileEntries($('UL.estherFileSystem LI#privateFolder'), null, data, (name + '.pmf'), 'pmf',
                        'private', false, false);
                    
                    openWidget(data, (name + '.pmf'), 'pmf', null);
                }
            });
    }
}

function closeWidget(file_id)
{
    closeTab($('#widget #tabs LI[file=' + file_id + ']'));
}

function openWidget(fileID, fileName, fileType, parent)
{
    if (timeoutID != null)
    {
        window.clearTimeout(timeoutID);
        
        timeoutID = null;
    }
    
    var tabs = $('#widget #tabs').find('LI');
            
    for (var j in tabs)
    {
        for (var k in tabs[j].attributes)
        {
            if ((tabs[j].attributes[k].name == 'file') && (tabs[j].attributes[k].value == fileID))
            {
                $('#widget').tabs('option', 'active', j);
                
                if (fileID == 'tasklist')
                {
                    $('#widget #tabs').find('INPUT[name=refresh]').trigger('click');
                }
                
                return;
            }
        }
    }
    
    var tab = createTab(fileID, fileName);
    
    if ((fileType == null) && (fileID == 'tasklist'))
    {
        $(tab).openTaskList();

        startTaskListRefreshTimer();
        
        return;
    }

    $(tab).empty();
    
    $.get('Widget/Open', { file: fileID, type: fileType, parent: parent }, function(data)
        {
            $(tab).append(data);

            var widget_starter = $(tab).find('div#widget_starter');

            if ((widget_starter != null) && (widget_starter.length > 0))
            {
                $(tab)[widget_starter.attr('init_function')]();
            }
        });
}

function createTab(id, name)
{
    var tabNum = ($('#widget #tabs LI').length + 1);
    
    $('#widget #tabs').append('<li class="tmp" file="' + id + '"><a href="#tab_' + tabNum + '">' + name + '</a>' +
        '<img height="10" style="cursor: pointer;" src="resources/images/x_button.png" /></li>');
    $('#widget').append('<div style="position: relative;" id="tab_' + tabNum + '"></div>');
    
    $('#widget').tabs('refresh');
    
    $('#widget').tabs('option', 'active', -1);
    
    var newTab = $('#widget #tabs LI.tmp');
    newTab.removeClass('tmp');
    newTab.find('IMG').click(function()
        {
            closeTab($(this).parent());
        });
        
    $('#widget').find('#scrollBar DIV').width($('#widget').find('#tabs')[0].scrollWidth);
    
    return ('#tab_' + tabNum);
}

function closeTab(tab)
{
    var tab_area = $('#widget').find('#' + tab.attr('aria-controls'));
    var widget_starter = $(tab_area).find('div#widget_starter');

    if ((widget_starter != null) && (widget_starter.length > 0))
    {
        var terminate_function = widget_starter.attr('terminate_function')
        
        if ((typeof terminate_function != 'undefined') && (terminate_function != ''))
        {
            $(tab_area)[terminate_function]();
        }
    }
    
    $(tab_area).remove();

    tab.remove();

    $('#widget').tabs('refresh');

    $('#widget').find('#scrollBar DIV').width($('#widget').find('#tabs')[0].scrollWidth);
    
    if ($('#widget').find('#tabs li').length == 0)
    {
        deselectFile();
    }
}

function refactorTab(file_id, new_id)
{
    $('#widget #tabs LI[file=' + file_id + ']').attr('file', new_id);
}

function renameTab(file_id, name)
{
    $('#widget #tabs LI[file=' + file_id + '] A').text(name);
    
    $('#widget').find('#scrollBar DIV').width($('#widget').find('#tabs')[0].scrollWidth);
}

function startTaskListRefreshTimer()
{
    timeoutID = window.setTimeout(function()
        {                                
            $(document).find('#widget INPUT[name=refresh]').trigger('click');
        }, 8096);
}