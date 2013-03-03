if(jQuery) (function($)
{
    $.extend($.fn,
    {
        openInteractionGraph: function(file)
        {
            var context = $(this);
            
            context.empty();
            
            $.get('File/Read',
                { file: file }, function(data)
                    {
                        context.append('<h7>Model:</h7>');
                        context.append('<textarea spellcheck=false id="contents">' +
                            data + '</textarea>');
                        context.append('<input class="button" id="savebutton" file_id="' +
                            file + '" type="Submit" value="Save" />');
                        context.append('<input class="button" id="parsybone" file_id="' +
                            file + '" type="Submit" value="Parsybone" />');

                        context.find('#savebutton').click(function()
                            {
                                $.post('File/Write',
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