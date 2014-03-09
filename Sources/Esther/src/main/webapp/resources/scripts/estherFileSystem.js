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

function showFiles(context, request_data)
{
    $(".estherFileSystem.start").remove();
    $.get('Files/List', request_data, function(data)
        {
            $(context).find('.start').html('');
            $(context).append(data);

            $(context).find('UL:hidden').slideDown({ duration: 420 });
    
            selectFile($('#widget #tabs').find('li.ui-tabs-active').attr('file'));

            bindFiles(context);
        });
}

function expandSubtree(file)
{
    if ($(file).hasClass('unexpandable'))
    {
        return false;
    }
    
    if ($(file).hasClass('expanded'))
    {
        $(file).find('ul').slideUp({ duration: 420 });
        $(file).removeClass('expanded');
        
        if ($(file).hasClass('open_folder'))
        {
            $(file).removeClass('open_folder');
            $(file).addClass('folder');
        }
    }
    else
    {
        $(file).find('ul').remove();
        $(file).addClass('expanded');
        
        var data = {};
        
        if ($(file).hasClass('folder'))
        {
            $(file).removeClass('folder');
            $(file).addClass('open_folder');
            
            data.file = null;
        }
        else if ($(file).hasClass('file'))
        {
            data.file = $(file).find('a').attr('file_id');
        }
        
        if ($(file).hasClass('private'))
        {
            data.privacy = 'private';
        }
        else if ($(file).hasClass('public'))
        {
            data.privacy = 'public';
        }
        
        showFiles(file, data);
    }
    
    return false;
}

function openFileMenu(operation, x, y, file_ref, file_id, file_name, parent_id, source_id, source_name)
{
    $(document).find('BODY').append('<div class="fileMenu" style=" top: ' + y + 'px; left: ' + x + 'px">');
    
    var params = { file: file_id };
    
    if (typeof source_id != 'undefined' && (source_id != null))
    {
        if (source_id == file_id)
        {
            return;
        }
        
        params['source'] = source_id;
    }
    
    $.get(('File/' + operation), params, function(data)
        {
            if (data.replace(/[\t\r\n ]/g, '') == '')
            {
                $('DIV.fileMenu').remove();
                return;
            }
            
            $('DIV.fileMenu').append(data);

            $('DIV.fileMenu FORM#uploadOptions').submit(function()
                {
                    var fileExt = $('DIV.fileMenu FORM#uploadOptions TR TD INPUT[type=file]').val().match(/\.([^\.]+)$/)[0].substring(1);

                    var parentData = { };

                    if (file_id != null)
                    {
                        parentData = { parent: file_id };
                    }

                    $('DIV.fileMenu FORM#uploadOptions').ajaxSubmit({ data: parentData, success: function(data)
                        {
                            if (data.split('=')[0] == 'LIMIT_REACHED')
                            {
                                alert('Cannot upload file. Your ' +
                                    data.split('=')[1] + ' storage limit has been reached.');
                            }
                            else if (data.split('=')[0] == 'ERROR')
                            {
                                alert('Error: ' + data.split('=')[1]);
                            }
                            else
                            {
                                var fileName = $('DIV.fileMenu FORM#uploadOptions TR TD INPUT[type=file]')[0].files[0].name;

                                appendFileEntries($('UL.estherFileSystem LI#privateFolder'), file_id, data,
                                    fileName, fileExt, 'private', false, false);
                                    
                                openWidget(data, fileName, fileExt, file_id);
                            }

                            $('DIV.fileMenu').remove();
                        }});

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
                                if (((copyname = prompt("Enter new file name: ", file_name.split('\.')[0])) != null) && (copyname != ''))
                                {
                                    $.post('File/Copy', { file: file_id, name: copyname }, function(data)
                                        {
                                            if (data.split('=')[0] == "LIMIT_REACHED")
                                            {
                                                alert('You do not have enough space available to save a copy of this file! Your '
                                                    + data.split('=')[1] + ' limit has been reached.');
                                            }
                                            else if (data.split('=')[0] == "ERROR")
                                            {
                                                alert('Error: ' + data.split('=')[1]);
                                            }
                                            else
                                            {
                                                appendFileEntries($('UL.estherFileSystem LI#privateFolder'), parent_id,
                                                    data, (copyname + '.' + extractExtension(file_name)), extractExtension(file_name),
                                                    'private', false, false);

                                                openWidget(data, (copyname + '.' + extractExtension(file_name)),
                                                    extractExtension(file_name), parent_id);
                                            }
                                        });
                                }

                                $('DIV.fileMenu').remove();

                                break;
                            }
                        case 'copy_as_child':
                            {
                                $.post('File/Copy', { file: source_id, name: source_name.split('.')[0], parent: file_id }, function(data)
                                    {
                                        if (data.split('=')[0] == "LIMIT_REACHED")
                                        {
                                            alert('You do not have enough space available to save a copy of this file! Your '
                                                + data.split('=')[1] + ' limit has been reached.');
                                        }
                                        else if (data.split('=')[0] == "ERROR")
                                        {
                                            alert('Error: ' + data.split('=')[1]);
                                        }
                                        else
                                        {
                                            appendFileEntries($('UL.estherFileSystem LI#privateFolder'), file_id,
                                                data, source_name, extractExtension(source_name),
                                                'private', false, false);

                                            openWidget(data, source_name, extractExtension(source_name), file_id);
                                        }
                                    });
                                
                                $('DIV.fileMenu').remove();
                                
                                break;
                            }
                        case 'delete':
                            {
                                if (confirm('The file will be deleted with all of it\'s subfiles. Are you sure you want to proceed?'))
                                {
                                    var subfiles = $(file_ref).parent().find('ul a');

                                    subfiles.each(function()
                                        {
                                            closeWidget($(this).attr('file_id'));
                                        });

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
                        case 'move_as_child':
                            {
                                $.post('File/Move', { file: source_id, parent: file_id }, function(data)
                                    {
                                        if (data.split('=')[0] == "ERROR")
                                        {
                                            alert('Error: ' + data.split('=')[1]);
                                        }
                                        else
                                        {
                                            removeFileEntries(source_id, $('ul.estherFileSystem'));
                                            closeWidget(source_id);
                                            
                                            appendFileEntries($('UL.estherFileSystem LI#privateFolder'), file_id,
                                                source_id, source_name, extractExtension(source_name),
                                                'private', false, false);

                                            openWidget(source_id, source_name, extractExtension(source_name), file_id);
                                        }
                                    });
                                
                                $('DIV.fileMenu').remove();
                                
                                break;
                            }
                        case 'new_model':
                            {
                                newModel();

                                $('DIV.fileMenu').remove();

                                break;
                            }
                        case 'privatize':
                            {
                                $.post('File/Privatize', { file: file_id }, function()
                                    {
                                        $('ul.estherFileSystem a[file_id=' + file_id + ']').removeClass('public');

                                        removeFromPublic(file_id, parent_id);
                                    });

                                $('DIV.fileMenu').remove();

                                break;
                            }
                        case 'publish':
                            {
                                $.post('File/Publish', { file: file_id }, function()
                                    {
                                        $('ul.estherFileSystem a[file_id=' + file_id + ']').addClass('public');

                                        addToPublic(file_id, parent_id, file_name, true, false);
                                    });

                                $('DIV.fileMenu').remove();

                                break;
                            }
                        case 'rename':
                            {
                                var newname;
                                if (((newname = prompt("Enter new name: ", file_name.split('\.')[0])) != null) && (newname != ''))
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

                                    $('div.fileMenu FORM#uploadOptions TABLE').hide();

                                    $('DIV.fileMenu A').each(function()
                                        {
                                            if ($(this).attr('func') != 'upload')
                                            {
                                                $(this).show();
                                            }
                                        });
                                }
                                else
                                {
                                    $('div.fileMenu FORM#uploadOptions TABLE').addClass('visible');

                                    $('div.fileMenu FORM#uploadOptions TABLE').show();

                                    $('DIV.fileMenu A').each(function()
                                        {
                                            if ($(this).attr('func') != 'upload')
                                            {
                                                $(this).hide();
                                            }
                                        });
                                }
                                break;
                            }
                        default:
                            break;
                    }
                });
        });
}

function bindFiles(context)
{
    $(context).find('li.file > a').droppable(
        {
            hoverClass: 'drag_n_drop_hover',
            accept: 'li.file',
            drop: function(e, ui)
                {
                    if ($(this).parent().hasClass('unexpandable') || ui.draggable.hasClass('unexpandable'))
                    {
                        return false;
                    }
                    
                    $(document).find('BODY').append('<div class="fileMenu" style=" top: ' +
                        e.pageY + 'px; left: ' + e.pageX + 'px">');
                    
                    openFileMenu('DragMenu', e.pageX, e.pageY, $(this).parent(), $(this).attr('file_id'),
                        $(this).text(), $(this).attr('parent_id'), ui.draggable.find('> a').attr('file_id'), ui.draggable.find('> a').text());
                    
                    return false;
                }
        });
        
    $(context).find('li.file').draggable(
        {
            appendTo: $('body'),
            opacity: true,
            helper: 'clone'
        });
    
    var icons = $(context).find('LI div.icon_container');
    
    icons.unbind('click');
    
    icons.click(function(e)
        {
            e.preventDefault();

            expandSubtree($(this).parent());
            
            return false;
        });
    
    var links = $(context).find('LI A');

    links.unbind('click');
    links.unbind('contextmenu');

    links.click(function(e)
        {
            e.preventDefault();

            if ($(this).hasClass('locked') || $(this).parent().hasClass('folder') || $(this).parent().hasClass('open_folder'))
            {
                $(this).parent().find("div.icon_container").trigger('click');
                return false;
            }
            
            openWidget($(this).attr('file_id'), $(this).html(), $(this).attr('file_type'), $(this).attr('parent_id'));
            
            return false;
        });
    links.bind('contextmenu', function(e)
        {
            e.preventDefault();
            if (($(this).parent().hasClass('file') && !$(this).parent().hasClass('unexpandable') && !$(this).hasClass('locked'))
                    || ($(this).parent().hasClass('private') && ($(this).parent().hasClass('folder')
                        || $(this).parent().hasClass('open_folder'))))
            {
                openFileMenu('Menu', e.pageX, e.pageY, $(this).parent(),
                    ($(this).parent().hasClass('file') ? $(this).attr('file_id') : null), $(this).text(),
                    $(this).attr('parent_id'), null, null);
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
    context.find('UL.estherFileSystem LI A[file_id="' + id + '"]').parent().remove();
}

function renameFileEntries(id, name, context)
{
    context.find('UL.estherFileSystem LI A[file_id="' + id + '"]').text(name);
}

function appendFileEntries(context, parent_id, id, name, type, tree, published, locked)
{
    var parents = context.find('UL.estherFileSystem LI A[file_id="' + parent_id + '"]');

    if (parents.length == 0)
    {
        parents = context.find('> UL');
    }

    parents.parent().each(function()
        {
            if ($(this).hasClass('expanded'))
            {
                $(this).find('> UL.estherFileSystem').append('<li class="file ' + tree + ' ' + type +
                    '"><div class="icon_container"><div id="icon"></div></div><a class="' +
                    (published ? 'public ' : ' ') + (locked ? 'locked' : '') + '" file_id="' + id +
                    '" file_type="' + type + '" parent_id="' + parent_id + '" href="#">' + name + '</a></li>');
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

function rescueFile(file, limit)
{
    if (confirm('You have exceeded your storage space capacity of ' + limit +
            '. Would you like to download the file instead? (You will lose your data otherwise.)'))
    {
        var url = window.location.pathname;

        url = url.replace('Analysis', ('File/Rescue?file=' + file));

        window.open(url);

        closeWidget(file);
        removeFileEntries(file);
    }
    else
    {
        deleteFile(file);
    }
}

function removeFromPublic(file, parent_id)
{
    removeFileEntries(file, $('LI#publicFolder'));
    
    if ((parent_id != null) && (parent_id != ''))
    {
        var parent = $('LI#publicFolder a[file_id=' + parent_id + ']');
       
        if (parent.hasClass('locked') && ($(parent).parent().find('ul.estherFileSystem li').length == 0))
        {
            removeFromPublic($(parent).attr('file_id'), $(parent).attr('parent_id'));
        }
    }
}

function addToPublic(file, parent_id, file_name, published, locked)
{
    if ((parent_id != null) && (parent_id != ''))
    {
        if ($('#publicFolder').find('a[file_id=' + parent_id + ']').length == 0)
        {
            var parent = $('LI#privateFolder  a[file_id=' + parent_id + ']');
            
            addToPublic(parent_id, $(parent).attr('parent_id'), $(parent).text(),
                $(parent).hasClass('public'), !$(parent).hasClass('public'));
                
            var publicParent = $('#publicFolder').find('a[file_id=' + parent_id + ']').parent();
            $(publicParent).addClass('expanded');
            $(publicParent).append('<ul class="estherFileSystem" />');
        }
    }
    
    appendFileEntries($('UL.estherFileSystem LI#publicFolder'), parent_id, file,
        file_name, extractExtension(file_name), 'public', published, locked);
}

function deselectFile()
{
    $('ul.estherFileSystem').find('a').parent().removeClass('selected');
}

function selectFile(file)
{
    deselectFile();
    
    var items = $('ul.estherFileSystem').find('a[file_id=' + file + ']').parent();
    
    items.each(function()
        {
            if (!$(this).is(':visible'))
            {
                showTree($(this).parent());
            }
        });
    
    items.addClass('selected');
}

function showTree(tree)
{
    $(tree).each(function()
        {
            if (!$(this).is(':visible'))
            {
                if (!$(this).parent().parent().is(':visible'))
                {
                    showTree($(this).parent().parent())
                }
                
                $(tree).find('li').removeClass('expanded');
                $(tree).parent().addClass('expanded');
                
                if ($(tree).parent().hasClass('folder'))
                {
                    $(tree).parent().removeClass('folder');
                    $(tree).parent().addClass('open_folder');
                }
                
                $(tree).show();
            }
        });
}