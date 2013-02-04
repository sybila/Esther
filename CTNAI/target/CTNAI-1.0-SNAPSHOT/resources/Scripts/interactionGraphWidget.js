if(jQuery) (function($)
{
    $.extend($.fn,
    {
        openInteractionGraph: function(file)
        {
            var context = $(this);
            
            context.empty();
            
            $.get('ReadFile',
                { file: file }, function(data)
                    {
                        context.append('<textarea spellcheck=false id="contents">' +
                            data + '</textarea>');
                        context.append('<input id="savebutton" file_id="' +
                            file + '" type="Submit" value="Save" />');
                        context.append('<input id="parsybone" file_id="' +
                            file + '" type="Submit" value="Parsybone" />');

                        context.find('#savebutton').click(function()
                            {
                                $.post('WriteFile',
                                { file: context.find('#savebutton').attr('file_id'),
                                    data: context.find('#contents').val() });
                            });

                        context.find('#parsybone').click(function()
                        {
                            $.post('Parsybone', { file: context.find('#parsybone').attr('file_id')}, function(data)
                                {
                                    data.split('/\s+/');
                                });
                        });
            });
        }
    });
})(jQuery);