if(jQuery) (function($)
{
    $.extend($.fn,
    {
        loadFileSystem: function()
        {
            $(this).each( function()
            {
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
                                $.get('File/Menu', { file: file_id }, function(data)
                                    {
                                        $(document).find('BODY DIV.fileMenu').append(data);
                                        
                                        $(document).find('BODY DIV.fileMenu A').click(function ()
                                            {
                                                var func = $(this).attr('func');
                                                
                                                switch(func)
                                                {
                                                    case 'copy':
                                                        {
                                                            var copyname;
                                                            if (((copyname = prompt("Enter new file name: ", "")) != null) && (copyname != ''))
                                                            {
                                                                $.post('File/Copy', { file: file_id, name: copyname });
                                                            }
                                                            break;
                                                        }
                                                    case 'delete':
                                                        {
                                                            if (confirm('The file cannot be restored after deleting. Are you sure you want to proceed?'))
                                                            {
                                                                $.post('File/Delete', { file: file_id });
                                                                closeWidget(file_id);
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
                                                            $.post('File/Privatize', { file: file_id });
                                                            break;
                                                        }
                                                    case 'publish':
                                                        {
                                                            $.post('File/Publish', { file: file_id });
                                                            break;
                                                        }
                                                    case 'rename':
                                                        {
                                                            var newname;
                                                            if (((newname = prompt("Enter new name: ", "")) != null) && (newname != ''))
                                                            {
                                                                $.post('File/Rename', { file: file_id, name: newname });
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