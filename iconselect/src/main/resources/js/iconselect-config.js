(function () {
    AJS.$(function () {

        var el = AJS.$("#cf-config-icon-options");
        if (el.length == 0) {
            return;
        }
        var fieldConfig = WRM.data.claim("com.codebarrel.iconselect:fieldConfig");
        var customField = WRM.data.claim("com.codebarrel.iconselect:customField");
        var defaultAvatar = WRM.data.claim("com.codebarrel.iconselect:defaultAvatar");

        var iconUrl = AJS.contextPath() + "/rest/iconselectoptions/1.0/option/context/" + fieldConfig;

        var viewRow = AJS.RestfulTable.Row.extend({
            initialize: function () {
                var instance = this;

                // call super
                AJS.RestfulTable.Row.prototype.initialize.apply(this, arguments);

                this.bind(this._event.RENDER, function (el, data) {

                    var $el = $(this.el);

                    $el.find(".aui-restfultable-operations").append(
                        com.codebarrel.iconselect.config.restfulltable.operations()
                    );
                    if (data.disabled) {
                        $el.addClass("iconselect-options-config-disabled");
                        $el.addClass("disabled");
                    } else {
                        $el.addClass("iconselect-options-config-enabled");
                    }
                    $el.find(".icon-select-disable").click(function (e) {
                        JIRA.SmartAjax.makeRequest({
                            url: iconUrl + "/" + data.id + "/disable",
                            type: "POST",
                            dataType: "json",
                            complete: function (xhr, status, smartAjaxResponse) {

                                var smartAjaxResponseData = smartAjaxResponse.data;

                                if (typeof smartAjaxResponse.data === "string") {
                                    smartAjaxResponseData = JSON.parse(smartAjaxResponse.data);
                                }

                                var isValidationError = !(xhr.status === 400 && smartAjaxResponseData && smartAjaxResponseData.errors);

                                if (smartAjaxResponse.successful) {
                                    $el.addClass("iconselect-options-config-disabled");
                                    $el.addClass("disabled");
                                    $el.removeClass("iconselect-options-config-enabled");
                                } else if (isValidationError) {
                                    alert("Error while disabling option. Reload and try again. If it pesists contact your administrator")
                                }

                            }
                        });
                    });
                    $el.find(".icon-select-enable").click(function (e) {
                        JIRA.SmartAjax.makeRequest({
                            url: iconUrl + "/"+ data.id + "/enable",
                            type: "POST",
                            dataType: "json",
                            complete: function (xhr, status, smartAjaxResponse) {

                                var smartAjaxResponseData = smartAjaxResponse.data;

                                if (typeof smartAjaxResponse.data === "string") {
                                    smartAjaxResponseData = JSON.parse(smartAjaxResponse.data);
                                }

                                var isValidationError = !(xhr.status === 400 && smartAjaxResponseData && smartAjaxResponseData.errors);

                                if (smartAjaxResponse.successful) {
                                    $el.addClass("iconselect-options-config-enabled");
                                    $el.removeClass("iconselect-options-config-disabled");
                                    $el.removeClass("disabled");
                                } else if(isValidationError) {
                                    alert("Error while enabling option. Reload and try again. If it pesists contact your administrator")
                                }

                            }
                        });
                    });
                });
            }

        });

        //TODO Workaround. Look comments TLASSIANDEV-120
        (function(open) {
            XMLHttpRequest.prototype.open = function() {
                if (arguments[1].includes('/rest/api/latest/avatar/project/system')) {
                    arguments[1] = arguments[1].replace('/rest/api/latest/avatar/project/system', '/rest/api/latest/avatar/iconselectlist/system');
                }
                open.apply(this, arguments);
            };
        })(XMLHttpRequest.prototype.open);

        var iconEditView = AJS.RestfulTable.CustomEditView.extend({
            render: function (self) {
                var val = self.value == null ? defaultAvatar : self.value;
                var editView = AJS.$(com.codebarrel.iconselect.config.restfulltable.iconEditView({name: self.name, value: val, contextPath: AJS.contextPath()}));

                JIRA.createUniversalAvatarPickerDialog({
                    trigger: editView,
                    title: AJS.I18n.getText("icon.option.select.icon.title"),
                    projectId: customField,
                    defaultAvatarId: defaultAvatar,
                    initialSelection: val,
                    avatarSize: JIRA.Avatar.getSizeObjectFromName("xsmall"),
                    avatarType: "iconselectlist",
                    select: function (avatar, src) {
                        editView.find("img").attr("src", src);
                        editView.find("input").val(avatar.getId());
                    }
                });

                return editView;
            }
        });
        var iconReadView = AJS.RestfulTable.CustomReadView.extend({
            render: function (self) {
                return com.codebarrel.iconselect.config.restfulltable.iconReadView({value: self.value, contextPath: AJS.contextPath()});
            }
        });

        var conifgTable = new AJS.RestfulTable({
            autoFocus: true,
            el: el,
            allowReorder: true,
            addPosition: "bottom",
            noEntriesMsg: AJS.I18n.getText("icon.option.configure.no.options"),
            resources: {
                all: iconUrl,
                self: iconUrl
            },
            columns: [
                {
                    id: "avatarId",
                    name: "avatarId",
                    header: AJS.I18n.getText("icon.option.icon"),
                    styleClass: "iconselect-options-config-avatar",
                    readView: iconReadView,
                    editView: iconEditView
                },
                {
                    id: "label",
                    name: "label",
                    styleClass: "iconselect-options-config-label",
                    header: AJS.I18n.getText("icon.option.label")
                }
            ],
            views: {
                row: viewRow
            }
        });
    });
})();
