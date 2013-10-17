//>>built
define("dojox/dgauges/components/green/CircularLinearGauge",["dojo/_base/lang","dojo/_base/declare","dojo/_base/Color","../utils","../../CircularGauge","../../LinearScaler","../../CircularScale","../../CircularValueIndicator","../../CircularRangeIndicator","../DefaultPropertiesMixin"],function(_1,_2,_3,_4,_5,_6,_7,_8,_9,_a){
return _2("dojox.dgauges.components.green.CircularLinearGauge",[_5,_a],{borderColor:[50,50,50],fillColor:[109,183,19],indicatorColor:[0,0,0],constructor:function(){
var _b=new _6();
this.addElement("background",_1.hitch(this,this.drawBackground));
var _c=new _7();
_c.set("scaler",_b);
_c.set("originX",132);
_c.set("originY",133.5);
_c.set("radius",100);
_c.set("startAngle",120);
_c.set("endAngle",60);
_c.set("orientation","clockwise");
_c.set("labelGap",6);
_c.set("font",{family:"Helvetica",weight:"bold",size:"8pt"});
this.addElement("scale",_c);
var _d=new _8();
_d.set("interactionArea","gauge");
_d.set("value",_b.minimum);
_d.set("indicatorShapeFunc",_1.hitch(this,function(_e,_f){
var l=_f.scale.radius-2;
_e.createPath().moveTo(-20,0).lineTo(-20,-5).lineTo(l,0).lineTo(-20,5).closePath().setFill(this.indicatorColor);
return _e;
}));
_c.addIndicator("indicator",_d);
this.addElement("foreground",_1.hitch(this,this.drawForeground));
},drawBackground:function(g){
var _10=_4.brightness(new _3(this.fillColor),100);
g.createEllipse({cx:132.2528,cy:133.617,rx:132.2528,ry:131.9046}).setFill(this.borderColor);
g.createPath({path:"M260.5056 133.617 C260.5188 179.3085 236.0769 221.5348 196.39 244.3844 C156.703 267.234 107.8027 267.234 68.1158 244.3844 C28.4287 221.5348 3.9866 179.3085 4 133.617 C3.9866 87.9255 28.4287 45.6992 68.1158 22.8496 C107.8027 0 156.703 0 196.39 22.8496 C236.0769 45.6992 260.5188 87.9255 260.5056 133.617 Z"}).setFill({type:"linear",x1:4.00002,y1:5.71243,x2:183.55392,y2:261.52156,colors:[{offset:0,color:[226,226,221]},{offset:0.5,color:[239,239,236]},{offset:1,color:"white"}]});
g.createPath({path:"M132.2358 5.7124 C61.4417 5.7124 4 63.0164 4 133.617 C4 204.2176 61.4417 261.5216 132.2358 261.5216 C203.0297 261.5216 260.5056 204.2176 260.5056 133.617 C260.5056 63.0164 203.0297 5.7124 132.2358 5.7124 ZM132.2358 29.5811 C189.165 29.5811 235.3862 76.19 235.3862 133.617 C235.3862 191.0441 189.165 237.653 132.2358 237.653 C75.3064 237.653 29.1195 191.0441 29.1195 133.617 C29.1195 76.19 75.3064 29.5811 132.2358 29.5811 Z"}).setFill({type:"linear",x1:4.00003,y1:5.71235,x2:4.00003,y2:261.52154,colors:[{offset:0,color:_10},{offset:0.25,color:this.fillColor},{offset:0.5,color:this.fillColor},{offset:0.75,color:this.fillColor},{offset:1,color:_10}]});
g.createPath({path:"M-389.2685 91.1524 C-389.2685 194.5704 -473.1053 278.4072 -576.5233 278.4072 C-679.9412 278.4072 -763.778 194.5704 -763.778 91.1524 C-763.778 -12.2655 -679.9412 -96.1024 -576.5233 -96.1024 C-473.1053 -96.1024 -389.2685 -12.2655 -389.2685 91.1524 Z"}).setTransform({xx:0.1,xy:0.46404,yx:-0.46637,yy:0.1005,dx:147.5,dy:-144.5}).setStroke({color:"white",width:3.04506,style:"Solid",cap:"butt",join:4});
},drawForeground:function(g){
var g1=g.createGroup();
g1.createEllipse({cx:132,cy:133.5,rx:18,ry:18}).setFill(this.fillColor);
g1.createEllipse({cx:132,cy:133.5,rx:17,ry:17}).setFill({type:"linear",x1:116.09304,y1:118.09373,x2:116.09304,y2:149.2857,colors:[{offset:0,color:[255,255,246]},{offset:0.17857,color:[252,251,236]},{offset:0.25755,color:[250,247,230]},{offset:0.77747,color:[246,243,224]},{offset:1,color:[227,209,184]}]});
g.createPath({path:"M235.494 63.7584 C235.494 87.4242 189.2719 51.6605 132.3194 51.6605 C75.367 51.6605 29.1447 87.4242 29.1447 63.7584 C29.1447 40.0927 75.367 2.531 132.3194 2.531 C189.2719 2.531 235.494 40.0927 235.494 63.7584 Z"}).setFill([255,255,255,0.19608]);
}});
});
