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
    var tabs = $('#widget #tabs').find('LI');
            
    for (var j in tabs)
    {
        for (var k in tabs[j].attributes)
        {
            if ((tabs[j].attributes[k].name == 'file') && (tabs[j].attributes[k].value == fileRef.attr('file_id')))
            {
                $('#widget').tabs('option', 'active', j);
                return;
            }
        }
    }
    
    var clss = fileRef.parent().attr('class').split(/\s+/);
    
    for (var i = 0; i < clss.length; i++)
    {
        var cls = clss[i];
        
        if ((cls == 'public') || (cls == 'private') || (cls == 'expanded') || (cls == 'file'))
            continue;
        
        var tab = createTab(fileRef.attr('file_id'), fileRef.html());

        switch (cls)
        {
            case "dbm":
            {
                $(tab).openInteractionGraph(fileRef.attr('file_id'));
                break;
            }
            case "sqlite":
            {
                $(tab).openParameterView(fileRef.attr('file_id'));
                break;
            }
            case "filter":
            {
                var source;

                source = fileRef.parent().parent().parent().find('A').attr('file_id');

                $(tab).openParameterView(source, fileRef.attr('file_id'));
                break;
            }
            case "xgmml":
            {
                $(tab).openBehaviourMap(fileRef.attr('file_id'));
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