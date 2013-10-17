function removeExportedStream(link) {
    var rowToRemove = link.parentNode.parentNode;
    rowToRemove.parentNode.removeChild(rowToRemove);
    CARBON.showInfoDialog("Exported stream removed successfully!!");
    return;
}


function removeImportedStreamDefinition(link) {
    var rowToRemove = link.parentNode.parentNode;
    rowToRemove.parentNode.removeChild(rowToRemove);
    CARBON.showInfoDialog("Imported stream removed successfully!!");
    return;
}

function togglePassThrough(element) {
    var currentVal = element.value;
    if (currentVal == "true") {
        element.value = "false";
    } else {
        element.value = "true";
    }
}

function promptEventBuilderCreation(element) {
    var selectedVal = element.options[element.selectedIndex].value;
    if (selectedVal == 'createEventBuilder') {
        CARBON.showConfirmationDialog("Do you want to create a new custom event builder?", createEventBuilder(element), function () {
        });
    }
}

function createEventBuilder(streamSelectId) {
    var streamNameWithVersion = document.getElementById(streamSelectId).value;
    if(streamNameWithVersion == 'createStreamDef') {
        CARBON.showErrorDialog("No stream definition selected.");
        return;
    }
    new Ajax.Request('../eventbuilder/popup_create_event_builder_ajaxprocessor.jsp', {
        method: 'post',
        asynchronous: false,
        parameters: {streamNameWithVersion: streamNameWithVersion},
        onSuccess: function (data) {
            showCustomPopupDialog(data.responseText, "Create Event Builder", "80%", "", onSuccessCreateEventBuilder, "90%");
        }
    });
}

function promptEventFormatterCreation(element) {
    var selectedVal = element.options[element.selectedIndex].value;
    if (selectedVal == 'createEventFormatter') {
        CARBON.showConfirmationDialog("Do you want to create a new custom event formatter?", createEventFormatter(element), function () {
        });
    }
}

function createEventFormatter(element) {
    var selectedVal = element.options[element.selectedIndex].value;
    var streamId = document.getElementById("exportedStreamId")[document.getElementById("exportedStreamId").selectedIndex].text;
    if (selectedVal == 'createEventFormatter') {
        var streamNameWithVersion = document.getElementById("exportedStreamId").value;
        new Ajax.Request('../eventformatter/popup_create_event_formatter_ajaxprocessor.jsp', {
            method: 'post',
            parameters: {streamId: streamId},
            asynchronous: false,
            onSuccess: function (data) {
                showCustomPopupDialog(data.responseText, "Create Event Formatter", "80%", "", onSuccessCreateEventFormatter, "90%");
            }
        });
    }
}

function builderSelectClick(element) {
    if (element.length <= 1) {
        createEventBuilder(element);
    }
}

function formatterSelectClick(element) {
    if (element.length <= 1) {
        createEventFormatter(element);
    }
}


/**
 * Display the Info Message inside a jQuery UI's dialog widget.
 * @method showPopupDialog
 * @param {String} message to display
 * @return {Boolean}
 */
function showCustomPopupDialog(message, title, windowHight, okButton, callback, windowWidth) {
    var strDialog = "<div id='dialog' title='" + title + "'><div id='popupDialog'></div>" + message + "</div>";
    var requiredWidth = 750;
    if (windowWidth) {
        requiredWidth = windowWidth;
    }
    var func = function () {
        jQuery("#dcontainer").hide();
        jQuery("#dcontainer").html(strDialog);
        if (okButton) {
            jQuery("#dialog").dialog({
                close: function () {
                    jQuery(this).dialog('destroy').remove();
                    jQuery("#dcontainer").empty();
                    return false;
                },
                buttons: {
                    "OK": function () {
                        if (callback && typeof callback == "function")
                            callback();
                        jQuery(this).dialog("destroy").remove();
                        jQuery("#dcontainer").empty();
                        return false;
                    }
                },
                autoOpen: false,
                height: windowHight,
                width: requiredWidth,
                minHeight: windowHight,
                minWidth: requiredWidth,
                modal: true
            });
        } else {
            jQuery("#dialog").dialog({
                close: function () {
                    if (callback && typeof callback == "function")
                        callback();
                    jQuery(this).dialog('destroy').remove();
                    jQuery("#dcontainer").empty();
                    return false;
                },
                autoOpen: false,
                height: windowHight,
                width: requiredWidth,
                minHeight: windowHight,
                minWidth: requiredWidth,
                modal: true
            });
        }

        jQuery('.ui-dialog-titlebar-close').click(function () {
            jQuery('#dialog').dialog("destroy").remove();
            jQuery("#dcontainer").empty();
            jQuery("#dcontainer").html('');
        });
        jQuery("#dcontainer").show();
        jQuery("#dialog").dialog("open");
    };
    if (!pageLoaded) {
        jQuery(document).ready(func);
    } else {
        func();
    }

};


function onSuccessCreateEventFormatter() {
    refreshEventFormatterInfo("eventFormatter");
}

function onSuccessCreateEventBuilder() {
    refreshEventBuilderInfo("eventBuilder");
}

function onSuccessCreateStreamDefinition() {
    refreshStreamDefInfo("importedStreamId");
    refreshStreamDefInfo("exportedStreamId");
}

function refreshEventFormatterInfo(eventFormatterSelectId) {
    var eventFormatterSelect = document.getElementById(eventFormatterSelectId);
    new Ajax.Request('../eventformatter/get_active_event_formatters_ajaxprocessor.jsp', {
        method: 'post',
        asynchronous: false,
        onSuccess: function (event) {
            eventFormatterSelect.length = 0;
            // for each property, add a text and input field in a row
            var jsonArrEventFormatterNames = JSON.parse(event.responseText);
            for (i = 0; i < jsonArrEventFormatterNames.length; i++) {
                var eventFormatterName = jsonArrEventFormatterNames[i];
                if (eventFormatterName != undefined && eventFormatterName != "") {
                    eventFormatterName = eventFormatterName.trim();
                    eventFormatterSelect.add(new Option(eventFormatterName, eventFormatterName), null);
                }
            }

        }
    });
}

function refreshEventBuilderInfo(eventBuilderSelectId) {
    var eventBuilderSelect = document.getElementById(eventBuilderSelectId);
    new Ajax.Request('../eventbuilder/get_active_event_builders_ajaxprocessor.jsp', {
        method: 'post',
        asynchronous: false,
        onSuccess: function (event) {
            eventBuilderSelect.length = 0;
            // for each property, add a text and input field in a row
            var jsonArrEventBuilderNames = JSON.parse(event.responseText);
            for (i = 0; i < jsonArrEventBuilderNames.length; i++) {
                var eventBuilderName = jsonArrEventBuilderNames[i];
                if (eventBuilderName != undefined && eventBuilderName != "") {
                    eventBuilderName = eventBuilderName.trim();
                    eventBuilderSelect.add(new Option(eventBuilderName, eventBuilderName), null);
                }
            }

        }
    });
}

function refreshStreamDefInfo(streamDefSelectId) {
    var streamDefSelect = document.getElementById(streamDefSelectId);
    new Ajax.Request('../eventstream/get_stream_definitions_ajaxprocessor.jsp', {
        method: 'post',
        asynchronous: false,
        onSuccess: function (event) {
            streamDefSelect.length = 0;
            // for each property, add a text and input field in a row
            var jsonArrStreamDefIds = JSON.parse(event.responseText);
            for (i = 0; i < jsonArrStreamDefIds.length; i++) {
                var streamDefId = jsonArrStreamDefIds[i];
                if (streamDefId != undefined && streamDefId != "") {
                    streamDefId = streamDefId.trim();
                    streamDefSelect.add(new Option(streamDefId, streamDefId), null);
                }
            }
            streamDefSelect.add(new Option("-- Create Stream Definition --", "createStreamDef"), null);
        }
    });
}

