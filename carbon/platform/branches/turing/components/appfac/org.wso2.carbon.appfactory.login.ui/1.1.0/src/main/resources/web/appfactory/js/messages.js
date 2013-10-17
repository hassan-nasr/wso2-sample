messageDisplay = function (params) {
        $('#messageModal').html($('#confirmation-data').html());
        if(params.title == undefined){
            $('#messageModal h3.modal-title').html('API Store');
        }else{
            $('#messageModal h3.modal-title').html(params.title);
        }
        $('#messageModal div.modal-body').html(params.content);
        if(params.buttons != undefined){
            $('#messageModal a.btn-primary').hide();
            for(var i=0;i<params.buttons.length;i++){
                $('#messageModal div.modal-footer').append($('<a class="btn '+params.buttons[i].cssClass+'">'+params.buttons[i].name+'</a>').click(params.buttons[i].cbk));
            }
        }else{
            $('#messageModal a.btn-primary').html('OK').click(function() {
                $('#messageModal').modal('hide');
            });
        }
        $('#messageModal a.btn-other').hide();
        $('#messageModal').modal();
    };
     /*
    usage
    Show info dialog
    jagg.message({content:'foo',type:'info', cbk:function(){alert('Do something here.')} });

    Show warning
    dialog jagg.message({content:'foo',type:'warning', cbk:function(){alert('Do something here.')} });

    Show error dialog
    jagg.message({content:'foo',type:'error', cbk:function(){alert('Do something here.')} });

    Show confirm dialog
    jagg.message({content:'foo',type:'confirm',okCallback:function(){},cancelCallback:function(){}});

    Showing custom popup and registering a call back
    jagg.message({
            type:'custom',
            title:"Select what ever",
            content:'<div><select id="myWhatEver"><option value="foo">flls</option></select></div>',
            buttons:[
                        {cssClass:'btn',name:'Ok',cbk:function(){
                            alert($('#myWhatEver').val());
                            $('#messageModal').modal('hide');
                        }}
                    ]
        });
     */
message = function(params){
        var siteRoot = "";
        if(params.type == "custom"){
            messageDisplay(params);
            return;
        }
        if(params.type == "confirm"){
            if( params.title == undefined ){ params.title = "WSO2 App Factory"}
            messageDisplay({content:params.content,title:params.title ,buttons:[
                {name:"Yes",cssClass:"btn btn-primary",cbk:function() {
                    $('#messageModal').modal('hide');
                    if(typeof params.okCallback == "function") {params.okCallback()};
                }},
                {name:"No",cssClass:"btn",cbk:function() {
                    $('#messageModal').modal('hide');
                    if(typeof params.cancelCallback  == "function") {params.cancelCallback()};
                }}
            ]
            });
            return;
        }
        params.content = '<table class="msg-table"><tr><td class="imageCell"></td><td><span class="messageText">'+params.content+'</span></td></tr></table>';
        var type = "";
        if(params.title == undefined){
            if(params.type == "info"){ type = "Notification"}
            if(params.type == "warning"){ type = "Warning"}
            if(params.type == "error"){ type = "Error"}
        }
        messageDisplay({content:params.content,title:"WSO2 App Factory - " + type,buttons:[
            {name:"OK",cssClass:"btn btn-primary",cbk:function() {
                $('#messageModal').modal('hide');
                if(params.cbk && typeof params.cbk == "function")
	                    params.cbk();
            }}
        ]
        });
    };