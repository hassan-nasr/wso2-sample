//>>built
define("dojox/mobile/TabBarButton",["dojo/_base/connect","dojo/_base/declare","dojo/_base/event","dojo/_base/lang","dojo/dom","dojo/dom-class","dojo/dom-construct","dojo/dom-style","./iconUtils","./_ItemBase","./Badge","./sniff"],function(_1,_2,_3,_4,_5,_6,_7,_8,_9,_a,_b,_c){
return _2("dojox.mobile.TabBarButton",_a,{icon1:"",icon2:"",iconPos1:"",iconPos2:"",selected:false,transition:"none",tag:"li",badge:"",baseClass:"mblTabBarButton",closeIcon:"mblDomButtonWhiteCross",_selStartMethod:"touch",_selEndMethod:"touch",destroy:function(){
if(this.badgeObj){
delete this.badgeObj;
}
this.inherited(arguments);
},inheritParams:function(){
if(this.icon&&!this.icon1){
this.icon1=this.icon;
}
var _d=this.getParent();
if(_d){
if(!this.transition){
this.transition=_d.transition;
}
if(this.icon1&&_d.iconBase&&_d.iconBase.charAt(_d.iconBase.length-1)==="/"){
this.icon1=_d.iconBase+this.icon1;
}
if(!this.icon1){
this.icon1=_d.iconBase;
}
if(!this.iconPos1){
this.iconPos1=_d.iconPos;
}
if(this.icon2&&_d.iconBase&&_d.iconBase.charAt(_d.iconBase.length-1)==="/"){
this.icon2=_d.iconBase+this.icon2;
}
if(!this.icon2){
this.icon2=_d.iconBase||this.icon1;
}
if(!this.iconPos2){
this.iconPos2=_d.iconPos||this.iconPos1;
}
if(_d.closable){
if(!this.icon1){
this.icon1=this.closeIcon;
}
if(!this.icon2){
this.icon2=this.closeIcon;
}
_6.add(this.domNode,"mblTabBarButtonClosable");
}
}
},buildRendering:function(){
this.domNode=this.srcNodeRef||_7.create(this.tag);
if(this.srcNodeRef){
if(!this.label){
this.label=_4.trim(this.srcNodeRef.innerHTML);
}
this.srcNodeRef.innerHTML="";
}
this.labelNode=this.box=_7.create("div",{className:"mblTabBarButtonLabel"},this.domNode);
this.inherited(arguments);
},startup:function(){
if(this._started){
return;
}
this._dragstartHandle=this.connect(this.domNode,"ondragstart",_3.stop);
this._keydownHandle=this.connect(this.domNode,"onkeydown","_onClick");
var _e=this.getParent();
if(_e&&_e.closable){
this._clickCloseHandler=this.connect(this.iconDivNode,"onclick","_onCloseButtonClick");
this._keydownCloseHandler=this.connect(this.iconDivNode,"onkeydown","_onCloseButtonClick");
this.iconDivNode.tabIndex="0";
}
this.inherited(arguments);
if(!this._isOnLine){
this._isOnLine=true;
this.set({icon1:this.icon1,icon2:this.icon2});
}
_5.setSelectable(this.domNode,false);
},onClose:function(e){
_1.publish("/dojox/mobile/tabClose",[this]);
return this.getParent().onCloseButtonClick(this);
},_onCloseButtonClick:function(e){
if(e&&e.type==="keydown"&&e.keyCode!==13){
return;
}
if(this.onCloseButtonClick(e)===false){
return;
}
if(this.onClose()){
this.destroy();
}
},onCloseButtonClick:function(){
},_onClick:function(e){
if(e&&e.type==="keydown"&&e.keyCode!==13){
return;
}
if(this.onClick(e)===false){
return;
}
this.defaultClickAction(e);
},onClick:function(){
},_setIcon:function(_f,n){
if(!this.getParent()){
return;
}
this._set("icon"+n,_f);
if(!this.iconDivNode){
this.iconDivNode=_7.create("div",{className:"mblTabBarButtonIconArea"},this.domNode,"first");
}
if(!this["iconParentNode"+n]){
this["iconParentNode"+n]=_7.create("div",{className:"mblTabBarButtonIconParent mblTabBarButtonIconParent"+n},this.iconDivNode);
}
this["iconNode"+n]=_9.setIcon(_f,this["iconPos"+n],this["iconNode"+n],this.alt,this["iconParentNode"+n]);
this["icon"+n]=_f;
_6.toggle(this.domNode,"mblTabBarButtonHasIcon",_f&&_f!=="none");
},_setIcon1Attr:function(_10){
this._setIcon(_10,1);
},_setIcon2Attr:function(_11){
this._setIcon(_11,2);
},_getBadgeAttr:function(){
return this.badgeObj&&this.badgeObj.domNode.parentNode&&this.badgeObj.domNode.parentNode.nodeType==1?this.badgeObj.getValue():null;
},_setBadgeAttr:function(_12){
if(!this.badgeObj){
this.badgeObj=new _b({fontSize:11});
_8.set(this.badgeObj.domNode,{position:"absolute",top:"0px",right:"0px"});
}
this.badgeObj.setValue(_12);
if(_12){
this.domNode.appendChild(this.badgeObj.domNode);
}else{
if(this.domNode===this.badgeObj.domNode.parentNode){
this.domNode.removeChild(this.badgeObj.domNode);
}
}
},_setSelectedAttr:function(_13){
this.inherited(arguments);
_6.toggle(this.domNode,"mblTabBarButtonSelected",_13);
}});
});
