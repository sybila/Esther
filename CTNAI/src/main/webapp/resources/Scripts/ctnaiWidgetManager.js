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
                    '<div style="width: ' + t[0].scrollWidth + 'px">\xA0</div></div>');
                
                t.parent().find('#scrollBar').scroll(function()
                    {
                        t.scrollLeft($(document).find('#scrollBar').scrollLeft());
                    });
            }
            
            var context = $(this);
            
            context.tabs();
            
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
                                if (task.attr('status') == 'ready')
                                {
                                    if (!confirm('Are you sure you want to cancel this task? You will be unable to acess the result once cancelled.'))
                                    {
                                        return;
                                    }
                                }
                                else if (task.attr('status') == 'running')
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
                        task.find('INPUT').click(function()
                            {
                                $.post('Task/Save', { task: task.attr('task_id')}, function(data)
                                    {
                                        appendFileEntries(task.attr('file_id'), task.attr('result_id'),
                                            data, ('file private ' + data.split('.')[1]), $(document));

                                        context.find('INPUT[name=refresh]').trigger('click');
                                    });
                            });

                        task.find('TH').unbind('click');
                        task.find('TH').click(function()
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

                                            for (var i in updatedTasks)
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
        $.post('File/Create', { name: name, type: 'dbm' });
    }
}

function closeWidget(file_id)
{
    closeTab($('#widget #tabs LI[file=' + file_id + ']'));
}

function openWidget(fileRef)
{
    if (timeoutID != null)
    {
        window.clearTimeout(timeoutID);
        
        timeoutID = null;
    }
    
    var fileID;
    var tabName;
    
    var clss = null;
    
    if (fileRef == 'startpage')
    {
        fileID = 'frontpage';
        tabName = 'Frontpage';
    }
    else if (fileRef == 'tasklist')
    {
        fileID = 'tasklist';
        tabName = 'My Tasks';
    }
    else
    {
        fileID = fileRef.attr('file_id');
        tabName = fileRef.html();
        
        clss = fileRef.parent().attr('class').split(/\s+/);
    }
    
    var tabs = $('#widget #tabs').find('LI');
            
    for (var j in tabs)
    {
        for (var k in tabs[j].attributes)
        {
            if ((tabs[j].attributes[k].name == 'file') && (tabs[j].attributes[k].value == fileID))
            {
                $('#widget').tabs('option', 'active', j);
                return;
            }
        }
    }
        
    var tab = createTab(fileID, tabName);
    
    if (clss == null)
    {
        if (fileID == 'tasklist')
        {
            $(tab).openTaskList();
            
            startTaskListRefreshTimer();
        }
        
        return;
    }
    
    for (var i = 0; i < clss.length; i++)
    {
        var cls = clss[i];
        
        if ((cls == 'public') || (cls == 'private') || (cls == 'expanded') || (cls == 'file'))
            continue;

        switch (cls)
        {
            case "dbm":
            {
                $(tab).openInteractionGraph(fileID);
                break;
            }
            case "sqlite":
            {
                $(tab).openParameterView(fileID);
                break;
            }
            case "filter":
            {
                var source;

                source = fileRef.parent().parent().parent().find('A').attr('file_id');

                $(tab).openParameterView(source, fileID);
                break;
            }
            case "xgmml":
            {
                $(tab).openBehaviourMap(fileID);
                break;
            }
            default:
            {
                alert('No widget found for file of type: ' + cls);
                break;
            }
        }
    }
}

function createTab(id, name)
{
    var tabNum = ($('#widget #tabs LI').length + 1);
    
    $('#widget #tabs').append('<li class="tmp" file="' + id + '"><a href="#tab_' + tabNum + '">' + name + '</a>' +
        '<img height="10" style="cursor: pointer;" src="/CTNAI/resources/Graphics/x_button.png" /></li>');
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
    $('#widget').find(tab.attr('aria-controls')).remove();

    tab.remove();

    $('#widget').tabs('refresh');

    $('#widget').find('#scrollBar DIV').width($('#widget').find('#tabs')[0].scrollWidth);
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