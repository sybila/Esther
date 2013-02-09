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
                    links.click(function()
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

                                    openWidget($(this));
                                }
                            }
                            return false;
                        });
                    links.bind('contextmenu', function(e)
                        {
                            e.preventDefault();
                            if ($(this).parent().hasClass('file'))
                            {
                                $(document).find('BODY').append('<div class="fileMenu" style=" top: ' +
                                    e.pageY + 'px; left: ' + e.pageX + 'px">' +
                                    '<a class="delete" href="#">Delete</a><br/>' +
                                    '<a class="rename" href="#">Rename</a></div>');
                                
                                var context = $(this);
                                $(document).find('BODY DIV.fileMenu A.delete').click(function()
                                    {
                                        if (confirm('After deleting the file and all of it\'s subfiles will become unavailable. Are you sure you want to continue?'))
                                        {
                                            $.post('File/Delete', { file: context.attr('file_id') });
                                        }
                                    });
                                    
                                $(document).find('BODY DIV.fileMenu A.rename').click(function()
                                    {
                                        var name;
                                        if (((name = prompt("Enter new name: ", "")) != null) && (name != ''))
                                        {
                                            $.post('File/Rename', { file: context.attr('file_id'), name: name });
                                        }
                                    });
                            }
                            return false;
                        });
                }
                
                $(document).bind('mousedown', function(e)
                    {
                        if (!$(e.target).parent().hasClass('fileMenu'))
                        {
                            $("div.fileMenu").hide();
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