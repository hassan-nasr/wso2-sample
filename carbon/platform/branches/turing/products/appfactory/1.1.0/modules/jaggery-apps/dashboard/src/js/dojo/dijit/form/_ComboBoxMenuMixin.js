//>>built
define("dijit/form/_ComboBoxMenuMixin",["dojo/_base/array","dojo/_base/declare","dojo/dom-attr","dojo/i18n","dojo/i18n!./nls/ComboBox"],function(_1,_2,_3,_4){
return _2("dijit.form._ComboBoxMenuMixin",null,{_messages:null,postMixInProperties:function(){
this.inherited(arguments);
this._messages=_4.getLocalization("dijit.form","ComboBox",this.lang);
},buildRendering:function(){
this.inherited(arguments);
this.previousButton.innerHTML=this._messages["previousMessage"];
this.nextButton.innerHTML=this._messages["nextMessage"];
},_setValueAttr:function(_5){
this.value=_5;
this.onChange(_5);
},onClick:function(_6){
if(_6==this.previousButton){
this._setSelectedAttr(null);
this.onPage(-1);
}else{
if(_6==this.nextButton){
this._setSelectedAttr(null);
this.onPage(1);
}else{
this.onChange(_6);
}
}
},onChange:function(){
},onPage:function(){
},onClose:function(){
this._setSelectedAttr(null);
},_createOption:function(_7,_8){
var _9=this._createMenuItem();
var _a=_8(_7);
if(_a.html){
_9.innerHTML=_a.label;
}else{
_9.appendChild(_9.ownerDocument.createTextNode(_a.label));
}
if(_9.innerHTML==""){
_9.innerHTML="&#160;";
}
this.applyTextDir(_9,(_9.innerText||_9.textContent||""));
return _9;
},createOptions:function(_b,_c,_d){
this.items=_b;
this.previousButton.style.display=(_c.start==0)?"none":"";
_3.set(this.previousButton,"id",this.id+"_prev");
_1.forEach(_b,function(_e,i){
var _f=this._createOption(_e,_d);
_f.setAttribute("item",i);
_3.set(_f,"id",this.id+i);
this.nextButton.parentNode.insertBefore(_f,this.nextButton);
},this);
var _10=false;
if(_b.total&&!_b.total.then&&_b.total!=-1){
if((_c.start+_c.count)<_b.total){
_10=true;
}else{
if((_c.start+_c.count)>_b.total&&_c.count==_b.length){
_10=true;
}
}
}else{
if(_c.count==_b.length){
_10=true;
}
}
this.nextButton.style.display=_10?"":"none";
_3.set(this.nextButton,"id",this.id+"_next");
},clearResultList:function(){
var _11=this.containerNode;
while(_11.childNodes.length>2){
_11.removeChild(_11.childNodes[_11.childNodes.length-2]);
}
this._setSelectedAttr(null);
},highlightFirstOption:function(){
this.selectFirstNode();
},highlightLastOption:function(){
this.selectLastNode();
},selectFirstNode:function(){
this.inherited(arguments);
if(this.getHighlightedOption()==this.previousButton){
this.selectNextNode();
}
},selectLastNode:function(){
this.inherited(arguments);
if(this.getHighlightedOption()==this.nextButton){
this.selectPreviousNode();
}
},getHighlightedOption:function(){
return this.selected;
}});
});
