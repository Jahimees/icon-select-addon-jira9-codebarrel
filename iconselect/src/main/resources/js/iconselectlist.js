(function ($) {

    function createSingleIconSelectList(ctx) {

        $(".iconselectlist-singleselect", ctx).each(function () {
            var $this = $(this);
            if ($this.data("aui-ss")) return;

            new AJS.SingleSelect({
                element: $this
            });
        });
    }

    JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context, reason) {
        if (reason !== JIRA.CONTENT_ADDED_REASON.panelRefreshed) {
            createSingleIconSelectList(context);
        }
    });

})(AJS.$);
