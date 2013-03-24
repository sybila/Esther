if(jQuery) (function($)
{
    $.extend($.fn,
    {
        loadFileSystem: function()
        {
            $(this).each( function()
            {
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

                function bindFiles(f) {
                    var links = $(f).find('LI A');
                    
                    links.unbind('click');
                    links.unbind('contextmenu');
                    
                    links.click(function(e)
                        {
                            e.preventDefault();
                            
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
                            return false;
                        });
                    links.bind('contextmenu', function(e)
                        {
                            e.preventDefault();
                            if ($(this).parent().hasClass('file'))
                            {
                                $(document).find('BODY').append('<div class="fileMenu" style=" top: ' +
                                    e.pageY + 'px; left: ' + e.pageX + 'px">');
                                
                                var file_id = $(this).attr('file_id');
                                var file_name = $(this).text();
                                var parent = $(this).parent().parent().parent().find('> A').attr('file_id');
                                
                                $.get('File/Menu', { file: file_id }, function(data)
                                    {
                                        $(document).find('BODY DIV.fileMenu').append(data);
                                        
                                        $(document).find('BODY DIV.fileMenu A').click(function (e)
                                            {
                                                //e.precentDefault();
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
                                                                        appendFileEntries(parent, data,
                                                                            (copyname + '.' + extractExtension(file_name)),
                                                                            ('file private ' + extractExtension(file_name)),
                                                                            $('UL.ctnaiFileSystem LI#privateFolder'));
                                                                    });
                                                            }
                                                            break;
                                                        }
                                                    case 'delete':
                                                        {
                                                            if (confirm('The file cannot be restored after deleting. Are you sure you want to proceed?'))
                                                            {
                                                                $.post('File/Delete', { file: file_id }, function()
                                                                    {
                                                                        closeWidget(file_id);
                                                                        removeFileEntries(file_id, $(document));
                                                                    });
                                                            }
                                                            break;
                                                        }
                                                    case 'download':
                                                        {
                                                            var url = window.location.pathname;
                                                            
                                                            url = url.replace('Analysis', ('File/Download?file=' + file_id));
                                                            
                                                            window.open(url);
                                                            
                                                            break;
                                                        }
                                                    case 'privatize':
                                                        {
                                                            $.post('File/Privatize', { file: file_id }, function()
                                                                {
                                                                    removeFileEntries(file_id, $('LI#publicFolder'));
                                                                });
                                                                
                                                            break;
                                                        }
                                                    case 'publish':
                                                        {
                                                            $.post('File/Publish', { file: file_id }, function()
                                                                {
                                                                    appendFileEntries(parent, file_id, file_name,
                                                                        ('file public ' + extractExtension(file_name)), $('UL.ctnaiFileSystem LI#publicFolder'));
                                                                });
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
                                                            break;
                                                        }
                                                    default:
                                                        break;
                                                }
                                                
                                                $("div.fileMenu").remove();
                                            })
                                    });
                            }
                            return false;
                        });
                }
                
                $(document).bind('mousedown', function(e)
                    {
                        if (!$(e.target).parent().hasClass('fileMenu'))
                        {
                            $("div.fileMenu").remove();
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