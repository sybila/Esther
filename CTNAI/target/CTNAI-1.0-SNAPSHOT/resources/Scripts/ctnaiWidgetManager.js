if(jQuery) (function($)
{
    $.extend($.fn,
    {
        openWelcomeScreen: function()
        {
            $(this).append('<div id="frontpage"><h7>Welcome to the analysis screen!</h7><p>If this is your first time visiting you might want to try our <a href="">Getting Started Tutorial</a>.</p><p>Or you may just select one of the existing files to the left or <a href="javascript:newModel()">create a new model</a> right now!</p></div>');
        }
    });
})(jQuery);

function newModel()
{
    var name;

    if (((name = prompt("Enter model name: ", "")) != null) && (name != ''))
    {
        $.post('File/Create', { name: name, type: 'dbm' });
    }
}

function openWidget(fileRef)
{
    var clss = fileRef.parent().attr('class').split(/\s+/);
    
    for (var i = 0; i < clss.length; i++)
    {
        var cls = clss[i];
        
        if ((cls == 'public') || (cls == 'private') || (cls == 'expanded') || (cls == 'file') || (cls == 'open'))
            continue;
        
        switch (cls)
        {
            case "dbm":
            {
                $('#widget').openInteractionGraph(fileRef.attr('file_id'));
                break;
            }
            case "sqlite":
            {
                $('#widget').openParameterView(fileRef.attr('file_id'));
                break;
            }
            case "filter":
            {
                var source;
                
                source = fileRef.parent().parent().parent().find('A').attr('file_id');
                
                $('#widget').openParameterView(source, fileRef.attr('file_id'));
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