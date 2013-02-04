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
                    $.get('Subfiles', { file: f, privacy: p }, function(data)
                        {
                            $(c).find('.start').html('');
                            $(c).append(data);

                            $(c).find('UL:hidden').slideDown({ duration: 420 });

                            bindFiles(c);
                        });
                }

                function bindFiles(f) {
                    $(f).find('LI A').click(function()
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
                                        $.get('MyFiles', function(data)
                                            {
                                                context.find('.start').html('');
                                                context.append(data);

                                                context.find('UL:hidden').slideDown({ duration: 420 });

                                                bindFiles(context);
                                            });
                                    }
                                    else if ($(this).parent().hasClass('public'))
                                    {
                                        $.get('PublicFiles', function(data)
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
                }

                var context = $(this);

                $.get('FileSystemRoot', function(data)
                    {
                        context.append(data);
                        bindFiles(context);
                    });
            });
        }
    });
})(jQuery);