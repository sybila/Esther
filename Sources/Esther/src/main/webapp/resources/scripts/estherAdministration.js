if(jQuery) (function($)
{
    $.extend($.fn,
    {
        initNewsFeed: function()
        {
            $('DIV.news_feed DIV.news_post').each(function()
            {
                var context = $(this);
                
                context.find('P.signature A#remove').click(function(e)
                {
                    e.preventDefault();
                    
                    if (confirm('Are you sure you want to remove the selected post?'))
                    {
                        $.post('News/Remove', { id: context.attr('post_id') }, function(data)
                        {
                            if (data.split('=')[0] === "ERROR")
                            {
                                alert('Error: ' + data.split('=')[1]);
                            }
                            else
                            {
                                $('DIV.news_feed').find('DIV.news_post[post_id=' + data + ']').remove();
                            }
                        });
                    }
                });
            });
        }
    });
    
    $.extend($.fn,
    {
        initNewsPost: function()
        {
            $('FORM[name=form]').submit(function()
                {                    
                    //$(this).ajaxSubmit({ data: { content: $(this).find('TEXTAREA#contents')[0].value }});
                    
                    $(this).find('INPUT[name=content]').attr('value', $(this).find('TEXTAREA#contents')[0].value.replace(/\n/g, '<BR/>'));
                    
                    return true;
                });
        }
    });
})(jQuery);

