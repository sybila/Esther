if(jQuery) (function($)
{
    $.extend($.fn,
    {
        loadFileSystem: function()
        {
            $(this).each( function()
            {                
                $(document).bind('mousedown', function(e)
                    {
                        if ($('DIV.fileMenu').find(e.target).length == 0)
                        {
                            $("DIV.fileMenu").remove();
                        }
                    });

                var context = $(this);

                $.get('Files/Root', function(data)
                    {
                        context.append(data);
                        bindFiles(context);
                    });
            });
        }
    });
})(jQuery);

function showFiles(c, f, p)
{
    $(".ctnaiFileSystem.start").remove();
    $.get('Files/Sub', { file: f, privacy: p }, function(data)
        {
            $(c).find('.start').html('');
            $(c).append(data);

            $(c).find('UL:hidden').slideDown({ duration: 420 });

            bindFiles(c);
        });
}

function bindFiles(f)
{
    var links = $(f).find('LI A');

    links.unbind('click');
    links.unbind('contextmenu');

    links.click(function(e)
        {
            e.preventDefault();

            if ($(this).parent().hasClass('unexpandable'))
            {
                openWidget($(this).parent().attr('id'));
            }
            else
            {
                if ($(this).parent().hasClass('expanded'))
                {
                    $(this).parent().find('UL').slideUp({ duration: 420 });
                    $(this).parent().removeClass('expanded');

                    if ($(this).parent().hasClass('open_folder'))
                    {
                        $(this).parent().removeClass('open_folder');
                        $(this).parent().addClass('folder');
                    }
                }
                else
                {
                    $(this).parent().find('UL').remove();
                    $(this).parent().addClass('expanded');

                    if ($(this).parent().hasClass('folder'))
                    {
                        $(this).parent().removeClass('folder');
                        $(this).parent().addClass('open_folder');

                        var context = $(this).parent();

                        $(".ctnaiFileSystem.start").remove();
                        if ($(this).parent().hasClass('private'))
                        {
                            $.get('Files/My', function(data)
                                {
                                    context.find('.start').html('');
                                    context.append(data);

                                    context.find('UL:hidden').slideDown({ duration: 420 });

                                    bindFiles(context);
                                });
                        }
                        else if ($(this).parent().hasClass('public'))
                        {
                            $.get('Files/Public', function(data)
                                {
                                    context.find('.start').html('');
                                    context.append(data);

                                    context.find('UL:hidden').slideDown({ duration: 420 });

                                    bindFiles(context);
                                });
                        }
                    }
                    else if ($(this).parent().hasClass('file'))
                    {
                        if ($(this).parent().hasClass('private'))
                        {
                            showFiles($(this).parent(), $(this).attr('file_id'), 'private');
                        }
                        else if ($(this).parent().hasClass('public'))
                        {
                            showFiles($(this).parent(), $(this).attr('file_id'), 'public');
                        }
                    }
                }

                if ($(this).parent().hasClass('file'))
                {
                    openWidget($(this));
                }
            }
            return false;
        });
    links.bind('contextmenu', function(e)
        {
            e.preventDefault();
            if ($(this).parent().hasClass('file') || ($(this).parent().hasClass('private') &&
                ($(this).parent().hasClass('folder') || $(this).parent().hasClass('open_folder'))))
            {
                $(document).find('BODY').append('<div class="fileMenu" style=" top: ' +
                    e.pageY + 'px; left: ' + e.pageX + 'px">');

                var file_id = $(this).attr('file_id');
                var file_name = $(this).text();
                var parent = $(this).parent().parent().parent().find('> A').attr('file_id');
                
                if (!$(this).parent().hasClass('file'))
                {
                    file_id = null;
                }

                $.get('File/Menu', { file: file_id }, function(data)
                    {
                        $('DIV.fileMenu').append(data);

                        $('DIV.fileMenu FORM#uploadOptions').submit(function()
                            {
                                var fileExt = $('DIV.fileMenu FORM#uploadOptions TR TD INPUT[type=file]').val().split('.');
                                if ($(this).attr('ext') != fileExt[fileExt.length - 1])
                                {
                                    alert('Invalid file specified! Please select a .' + $(this).attr('ext') + ' file.');
                                    return false;
                                }
                                
                                var parentData = { };
                                
                                if (file_id != null)
                                {
                                    parentData = { 'parent': (file_id + '') }
                                }
                                
                                $.ajaxFileUpload({
                                        url:'File/Upload',
                                        secureuri:false,
                                        fileElementId:'fileInput',
                                        data: parentData,
                                        dataType: 'json',
                                        success: function (data, status) 
                                            {
                                                data = (data + '');
                                                if (data.split('=')[0] == 'LIMIT_REACHED')
                                                {
                                                    alert('Cannot upload file. Your ' +
                                                        data.split('=')[1] + ' storage limit has been reached.');
                                                }
                                            
                                                var filePath = $('DIV.fileMenu FORM#uploadOptions TR TD INPUT[type=file]').val().split('[\\/]');
                                                appendFileEntries(file_id, data, filePath[filePath.length - 1],
                                                    ('file private ' + fileExt[fileExt.length - 1]),
                                                    $('UL.ctnaiFileSystem LI#privateFolder'));
                                    
                                                $('DIV.fileMenu').remove();
                                            },
                                        
                                        error: function (data, status, e) {
                                                alert(e);
                                            }
                                    });
                                    
                                return false;
                            });

                        $('DIV.fileMenu A').click(function (e)
                            {
                                e.preventDefault();
                                var func = $(this).attr('func');

                                switch(func)
                                {
                                    case 'copy':
                                        {
                                            var copyname;
                                            if (((copyname = prompt("Enter new file name: ", "")) != null) && (copyname != ''))
                                            {
                                                $.post('File/Copy', { file: file_id, name: copyname }, function(data)
                                                    {
                                                        if (data.split('=')[0] == "LIMIT_REACHED")
                                                        {
                                                            alert('You do not have enough space available to save a copy of this file! Your '
                                                                + data.split('=')[1] + ' limit has been reached.');
                                                        }
                                                        else
                                                        {
                                                            appendFileEntries(parent, data,
                                                                (copyname + '.' + extractExtension(file_name)),
                                                                ('file private ' + extractExtension(file_name)),
                                                                $('UL.ctnaiFileSystem LI#privateFolder'));
                                                        }
                                                    });
                                            }

                                            $('DIV.fileMenu').remove();
                                            
                                            break;
                                        }
                                    case 'delete':
                                        {
                                            if (confirm('The file cannot be restored after deleting. Are you sure you want to proceed?'))
                                            {
                                                deleteFile(file_id);
                                            }
                                            
                                            $('DIV.fileMenu').remove();
                                            
                                            break;
                                        }
                                    case 'download':
                                        {
                                            downloadFile(file_id);

                                            $('DIV.fileMenu').remove();

                                            break;
                                        }
                                    case 'privatize':
                                        {
                                            $.post('File/Privatize', { file: file_id }, function()
                                                {
                                                    removeFileEntries(file_id, $('LI#publicFolder'));
                                                });

                                            $('DIV.fileMenu').remove();

                                            break;
                                        }
                                    case 'publish':
                                        {
                                            $.post('File/Publish', { file: file_id }, function()
                                                {
                                                    appendFileEntries(parent, file_id, file_name,
                                                        ('file public ' + extractExtension(file_name)), $('UL.ctnaiFileSystem LI#publicFolder'));
                                                });

                                            $('DIV.fileMenu').remove();
                                            
                                            break;
                                        }
                                    case 'rename':
                                        {
                                            var newname;
                                            if (((newname = prompt("Enter new name: ", "")) != null) && (newname != ''))
                                            {
                                                $.post('File/Rename', { file: file_id, name: newname }, function()
                                                    {
                                                        renameFileEntries(file_id, (newname + '.' + extractExtension(file_name)), $(document));
                                                        renameTab(file_id, (newname + '.' + extractExtension(file_name)));
                                                    });
                                            }

                                            $('DIV.fileMenu').remove();
                                            
                                            break;
                                        }
                                    case 'upload':
                                        {
                                            if ($('div.fileMenu FORM#uploadOptions TABLE').hasClass('visible'))
                                            {
                                                $('div.fileMenu FORM#uploadOptions TABLE').removeClass('visible');
                                                
                                                $('div.fileMenu FORM#uploadOptions TABLE').slideUp({ duration: 360 });
                                            }
                                            else
                                            {
                                                $('div.fileMenu FORM#uploadOptions TABLE').addClass('visible');
                                                
                                                $('div.fileMenu FORM#uploadOptions TABLE').slideDown({ duration: 360 });
                                            }
                                            break;
                                        }
                                    default:
                                        break;
                                }
                            });
                    });
            }
            return false;
        });
}

function extractExtension(name)
{
    return name.split('.').pop();
}

function removeFileEntries(id, context)
{
    context.find('UL.ctnaiFileSystem LI A[file_id="' + id + '"]').parent().remove();
}

function renameFileEntries(id, name, context)
{
    context.find('UL.ctnaiFileSystem LI A[file_id="' + id + '"]').text(name);
}

function appendFileEntries(parent_id, id, name, cls, context)
{
    var parents = context.find('UL.ctnaiFileSystem LI A[file_id="' + parent_id + '"]');

    if (parents.length == 0)
    {
        parents = context.find('> UL');
    }

    parents.parent().each(function()
        {
            if ($(this).hasClass('expanded'))
            {
                $(this).find('> UL.ctnaiFileSystem').append('<li class="' + cls + '"><a file_id="' + id +
                    '" href="#">' + name + '</a></li>');
                bindFiles(this);
            }
        })
}

function deleteFile(file)
{
    $.post('File/Delete', { file: file }, function()
    {
        closeWidget(file);
        removeFileEntries(file, $(document));
    });
}

function downloadFile(file)
{
    var url = window.location.pathname;

    url = url.replace('Analysis', ('File/Download?file=' + file));

    window.open(url);
}

function resqueFile(file, limit)
{
    if (confirm('You have exceeded your storage space capacity of ' + limit +
            '. Would you like to download the file instead? (You will lose your data otherwise.)'))
    {
        var url = window.location.pathname;

        url = url.replace('Analysis', ('File/Resque?file=' + file));

        window.open(url);

        closeWidget(file);
        removeFileEntries(file);
    }
    else
    {
        deleteFile(file);
    }
}