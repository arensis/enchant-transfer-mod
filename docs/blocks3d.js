(function(){
function shade(rgb,f){return'rgb('+Math.round(rgb[0]*f)+','+Math.round(rgb[1]*f)+','+Math.round(rgb[2]*f)+')';}
function shadeA(rgb,f,a){return'rgba('+Math.round(rgb[0]*f)+','+Math.round(rgb[1]*f)+','+Math.round(rgb[2]*f)+','+a+')';}

// ===== TRANSFER TABLE =====
!function(){
var S=6,el=document.getElementById('block3d-table');if(!el)return;
var sm={front:.92,back:.72,right:.66,left:.58,top:1,bottom:.4};
var w=document.createElement('div');w.className='world';
w.style.transform='rotateX(-22deg) rotateY(32deg)';el.appendChild(w);
function makeTex(kind){
  var c=document.createElement('canvas');c.width=16;c.height=16;var ctx=c.getContext('2d');
  var px=function(x,y,col){ctx.fillStyle=col;ctx.fillRect(x,y,1,1);};
  if(kind==='gold'){
    ctx.fillStyle='#d8a02e';ctx.fillRect(0,0,16,16);
    var cells=['#e8b545','#d2962a','#dca838','#c88a22','#e0ad40'];
    for(var y=0;y<16;y++)for(var x=0;x<16;x++){if((x+y*3)%5===0)px(x,y,cells[(x*7+y)%cells.length]);}
    ctx.fillStyle='#f4d070';ctx.fillRect(0,0,16,1);ctx.fillRect(0,0,1,16);
    ctx.fillStyle='#fae89a';ctx.fillRect(1,1,5,1);ctx.fillRect(1,1,1,4);
    ctx.fillStyle='#9a6a16';ctx.fillRect(0,15,16,1);ctx.fillRect(15,0,1,16);
    px(4,3,'#fff6c8');px(10,6,'#fff0b0');px(6,11,'#ffeaa0');
  }else if(kind==='red'){
    ctx.fillStyle='#9e1810';ctx.fillRect(0,0,16,16);
    var seed=7,rnd=function(){seed=(seed*1103515245+12345)&0x7fffffff;return seed/0x7fffffff;};
    for(var i=0;i<70;i++){var rx=Math.floor(rnd()*16),ry=Math.floor(rnd()*16),r=rnd();
      px(rx,ry,r<.45?'#6e0c08':r<.78?'#b82418':r<.93?'#d83426':'#ff5a44');}
    ctx.fillStyle='#c43022';ctx.fillRect(0,0,16,1);ctx.fillRect(0,0,1,16);
    ctx.fillStyle='#5a0a06';ctx.fillRect(0,15,16,1);ctx.fillRect(15,0,1,16);
  }else if(kind==='carbon'){
    ctx.fillStyle='#22222a';ctx.fillRect(0,0,16,16);
    var seed2=3,rnd2=function(){seed2=(seed2*1103515245+12345)&0x7fffffff;return seed2/0x7fffffff;};
    for(var i2=0;i2<48;i2++){var cx2=Math.floor(rnd2()*16),cy2=Math.floor(rnd2()*16),r2=rnd2();
      px(cx2,cy2,r2<.5?'#16161c':r2<.85?'#2c2c36':'#3a3a46');}
    ctx.fillStyle='#34343e';ctx.fillRect(0,0,16,1);ctx.fillRect(0,0,1,16);
    ctx.fillStyle='#121218';ctx.fillRect(0,15,16,1);ctx.fillRect(15,0,1,16);
  }
  return c.toDataURL();
}
var TEX={gold:makeTex('gold'),red:makeTex('red'),carbon:makeTex('carbon')};
function makeBox(parent,x,y,z,bw,h,d,colorFn,texUrl){
  var box=document.createElement('div');box.className='box';
  box.style.transform='translate3d('+x*S+'px,'+-y*S+'px,'+z*S+'px)';
  var W=bw*S,H=h*S,D=d*S;
  [{n:'front',t:'translate(-50%,-50%) translateZ('+D/2+'px)',w:W,h:H},
   {n:'back',t:'translate(-50%,-50%) rotateY(180deg) translateZ('+D/2+'px)',w:W,h:H},
   {n:'right',t:'translate(-50%,-50%) rotateY(90deg) translateZ('+W/2+'px)',w:D,h:H},
   {n:'left',t:'translate(-50%,-50%) rotateY(-90deg) translateZ('+W/2+'px)',w:D,h:H},
   {n:'top',t:'translate(-50%,-50%) rotateX(90deg) translateZ('+H/2+'px)',w:W,h:D},
   {n:'bottom',t:'translate(-50%,-50%) rotateX(-90deg) translateZ('+H/2+'px)',w:W,h:D}
  ].forEach(function(f){
    var e=document.createElement('div');e.className='face';
    e.style.width=f.w+'px';e.style.height=f.h+'px';e.style.transform=f.t;
    if(texUrl){var fac=sm[f.n],ov=(fac>=1?0:(1-fac)).toFixed(2);
      e.style.backgroundImage='linear-gradient(rgba(0,0,0,'+ov+'),rgba(0,0,0,'+ov+')),url('+texUrl+')';
      e.style.backgroundSize='cover';e.style.imageRendering='pixelated';
    }else{e.style.background=colorFn(f.n);}
    box.appendChild(e);
  });
  parent.appendChild(box);return box;
}
function makeRing(parent,z,outerHalf,innerHalf,rgb,texUrl){
  var O=outerHalf,I=innerHalf,t=.5,band=O-I;
  var cf=function(n){return shade(rgb,sm[n]);};
  makeBox(parent,0,(I+O)/2,z,2*O,band,t,cf,texUrl);
  makeBox(parent,0,-(I+O)/2,z,2*O,band,t,cf,texUrl);
  makeBox(parent,-(I+O)/2,0,z,band,2*I,t,cf,texUrl);
  makeBox(parent,(I+O)/2,0,z,band,2*I,t,cf,texUrl);
}
function makeRiser(parent,H,z0,z1,rgb,texUrl){
  var midz=(z0+z1)/2,span=z1-z0,t=.4;
  var cf=function(n){return shade(rgb,sm[n]);};
  makeBox(parent,0,H,midz,2*H,t,span,cf,texUrl);
  makeBox(parent,0,-H,midz,2*H,t,span,cf,texUrl);
  makeBox(parent,-H,0,midz,t,2*H,span,cf,texUrl);
  makeBox(parent,H,0,midz,t,2*H,span,cf,texUrl);
}
var GOLD=[210,150,38],RED=[160,26,20],CARBON=[34,34,44];
function buildFunnel(wrapper){
  makeRing(wrapper,8,8,6.5,GOLD,TEX.gold);
  makeRiser(wrapper,6.5,6,8,GOLD,TEX.gold);
  makeRing(wrapper,6,6.5,5,RED,TEX.red);
  makeRiser(wrapper,5,4,6,RED,TEX.red);
  makeRing(wrapper,4,5,3,CARBON,TEX.carbon);
  makeRiser(wrapper,3,2,4,CARBON,TEX.carbon);
  var lit=document.createElement('div');
  lit.style.position='absolute';lit.style.left='0';lit.style.top='0';
  var sz=2*2.8*S;
  lit.style.width=sz+'px';lit.style.height=sz+'px';
  lit.style.transform='translate(-50%,-50%) translateZ('+2.9*S+'px)';
  lit.style.background='radial-gradient(circle at 50% 44%,#ffffff,#cfeeff 34%,rgba(70,175,235,0.92) 82%)';
  lit.style.filter='blur(1px) drop-shadow(0 0 6px #8ce0ff)';
  wrapper.appendChild(lit);
}
function faceWrapper(rot){
  var d=document.createElement('div');d.className='box';d.style.transform=rot;w.appendChild(d);return d;
}
['rotateY(0deg)','rotateY(180deg)','rotateY(90deg)','rotateY(-90deg)','rotateX(-90deg)','rotateX(90deg)']
  .forEach(function(r){buildFunnel(faceWrapper(r));});
var CORE=[70,175,235];
var core=makeBox(w,0,0,0,5.4,5.4,5.4,function(n){return shade(CORE,Math.min(1,sm[n]*1.05));});
core.style.filter='drop-shadow(0 0 5px #2aa6ff)';
}();

// ===== ZINC SMELTER =====
!function(){
var S=4,el=document.getElementById('block3d-smelter');if(!el)return;
var SM={front:.90,back:.70,right:.62,left:.56,top:1,bottom:.38};
var COPPER=[176,106,44],BRASS=[200,148,56],GLASS=[26,56,52],IRON=[72,72,74];
var LAVA=[232,88,24],LAVA_GLOW=[255,140,40];
var w=document.createElement('div');w.className='world';
w.style.transform='rotateX(-22deg) rotateY(30deg) translate3d('+(-8*S)+'px,'+(12*S)+'px,'+(-8*S)+'px)';
el.appendChild(w);
function mkTex(kind){
  var c=document.createElement('canvas');c.width=16;c.height=16;var x=c.getContext('2d');
  if(kind==='copper'){x.fillStyle='#b86a28';x.fillRect(0,0,16,16);[[0,'#c47630'],[1,'#b86a28'],[2,'#c07030'],[3,'#c87a34'],[4,'#b46228'],[5,'#c27232'],[6,'#be6e2e'],[7,'#d08040'],[8,'#c47630'],[9,'#b86228'],[10,'#c47030'],[11,'#bc6a2c'],[12,'#c87a34'],[13,'#b86228'],[14,'#a85e24'],[15,'#b86a28']].forEach(function(a){x.fillStyle=a[1];x.fillRect(0,a[0],16,1);});x.fillStyle='#e89858';x.fillRect(2,2,6,1);x.fillRect(4,5,4,1);x.fillStyle='#d88040';x.fillRect(0,0,16,1);x.fillRect(0,0,1,16);x.fillStyle='#7a3a10';x.fillRect(0,15,16,1);x.fillRect(15,0,1,16);}
  else if(kind==='brass'){x.fillStyle='#c89030';x.fillRect(0,0,16,16);[[0,'#d49a3a'],[1,'#c89030'],[2,'#d09838'],[3,'#d89c3c'],[4,'#c48828'],[5,'#d29638'],[6,'#ce9234'],[7,'#dea040'],[8,'#d49a3a'],[9,'#c68c2c'],[10,'#d49838'],[11,'#cc8e32'],[12,'#d89c3c'],[13,'#c68a2c'],[14,'#be8428'],[15,'#c89030']].forEach(function(a){x.fillStyle=a[1];x.fillRect(0,a[0],16,1);});x.fillStyle='#f0c860';x.fillRect(2,2,6,1);x.fillStyle='#e8b050';x.fillRect(0,0,16,1);x.fillRect(0,0,1,16);x.fillStyle='#8a5c14';x.fillRect(0,15,16,1);x.fillRect(15,0,1,16);}
  else if(kind==='iron'){x.fillStyle='#484848';x.fillRect(0,0,16,16);var s=17,rnd=function(){s=(s*1103515245+12345)&0x7fffffff;return s/0x7fffffff;};for(var i=0;i<55;i++){var px=Math.floor(rnd()*16),py=Math.floor(rnd()*16),v=rnd();x.fillStyle=v<.35?'#383838':v<.7?'#505050':'#5c5c5c';x.fillRect(px,py,1,1);}x.fillStyle='#5e5e5e';x.fillRect(0,0,16,1);x.fillRect(0,0,1,16);x.fillStyle='#2c2c2c';x.fillRect(0,15,16,1);x.fillRect(15,0,1,16);}
  else if(kind==='lava'){x.fillStyle='#c83808';x.fillRect(0,0,16,16);for(var y=0;y<16;y++){var v2=Math.sin(y*0.7)*0.15+0.85;x.fillStyle='rgb('+Math.round(232*v2)+','+Math.round(88*v2+y*3)+','+Math.round(24*v2)+')';x.fillRect(0,y,16,1);}x.fillStyle='#ff9820';x.fillRect(3,4,4,2);x.fillRect(9,8,5,2);x.fillStyle='#ffe870';x.fillRect(4,4,2,1);x.fillRect(10,8,3,1);}
  return c.toDataURL();
}
var TX_C=mkTex('copper'),TX_B=mkTex('brass'),TX_I=mkTex('iron'),TX_L=mkTex('lava');
var copperF=function(n){return shade(COPPER,SM[n]);};copperF.tex=TX_C;
var brassF=function(n){return shade(BRASS,SM[n]);};brassF.tex=TX_B;
var ironF=function(n){return shade(IRON,SM[n]);};ironF.tex=TX_I;
var glassF=function(n){return shadeA(GLASS,SM[n]*1.1,0.55);};
var lavaF=function(n){return shade(LAVA,SM[n]);};lavaF.tex=TX_L;
var lavaGlF=function(n){return shadeA(LAVA_GLOW,SM[n],0.7);};
function makeBox(parent,bx,by,bz,bw,h,d,colorFn){
  var box=document.createElement('div');box.className='box';
  box.style.transform='translate3d('+bx*S+'px,'+-by*S+'px,'+bz*S+'px)';
  var W=bw*S,H=h*S,D=d*S;
  [{n:'front',t:'translate(-50%,-50%) translateZ('+D/2+'px)',w:W,h:H},
   {n:'back',t:'translate(-50%,-50%) rotateY(180deg) translateZ('+D/2+'px)',w:W,h:H},
   {n:'right',t:'translate(-50%,-50%) rotateY(90deg) translateZ('+W/2+'px)',w:D,h:H},
   {n:'left',t:'translate(-50%,-50%) rotateY(-90deg) translateZ('+W/2+'px)',w:D,h:H},
   {n:'top',t:'translate(-50%,-50%) rotateX(90deg) translateZ('+H/2+'px)',w:W,h:D},
   {n:'bottom',t:'translate(-50%,-50%) rotateX(-90deg) translateZ('+H/2+'px)',w:W,h:D}
  ].forEach(function(f){
    var e=document.createElement('div');e.className='face';
    e.style.width=f.w+'px';e.style.height=f.h+'px';e.style.transform=f.t;
    if(colorFn.tex){var fac=SM[f.n],ov=(fac>=1?0:(1-fac)*.85).toFixed(2);
      e.style.backgroundImage='linear-gradient(rgba(0,0,0,'+ov+'),rgba(0,0,0,'+ov+')),url('+colorFn.tex+')';
      e.style.backgroundSize='cover';e.style.imageRendering='pixelated';
    }else{e.style.background=colorFn(f.n);}
    box.appendChild(e);
  });
  parent.appendChild(box);return box;
}
makeBox(w,8,1,8,18,2,18,brassF);
makeBox(w,8,.2,8,16,.6,16,ironF);
var lH=3*0.7;var lv=makeBox(w,8,2+lH/2,8,12,lH,12,lavaF);
lv.style.filter='drop-shadow(0 0 4px rgba(255,100,20,0.5))';
makeBox(w,8,8,8,15,12,15,copperF);
[[-6.5,-6.5],[-6.5,6.5],[6.5,-6.5],[6.5,6.5]].forEach(function(p){
  makeBox(w,8+p[0],8,8+p[1],2.5,12,2.5,brassF);
  [4,8,12].forEach(function(ry){makeBox(w,8+p[0],ry,8+p[1],1.2,1.2,1.2,copperF);});
});
makeBox(w,8,6,16,8,6,1,copperF);
makeBox(w,4.2,8,16.5,.8,1.2,.8,brassF);
makeBox(w,4.2,5,16.5,.8,1.2,.8,brassF);
makeBox(w,11.5,6,16.6,.6,2,.6,ironF);
makeBox(w,8,6,16.8,4,3,.4,glassF);
var glow=makeBox(w,8,6,16.9,3.5,2.5,0.3,lavaGlF);
glow.style.filter='drop-shadow(0 0 4px rgba(255,100,20,0.4))';
makeBox(w,8,5,8,16,.6,16,brassF);
makeBox(w,8,10,8,16,.6,16,brassF);
makeBox(w,8,13,8,16,.6,16,brassF);
makeBox(w,8,14.5,8,17,1.5,17,brassF);
makeBox(w,8,15.5,8,13,1,13,copperF);
makeBox(w,8,16.5,8,5,.8,5,brassF);
makeBox(w,8,19,8,4,5,4,copperF);
makeBox(w,8,18,8,5.5,1,5.5,brassF);
makeBox(w,8,22,8,6,1.5,6,brassF);
makeBox(w,8,23,8,4.5,.8,4.5,copperF);
makeBox(w,-.5,9,8,2,3,2.5,copperF);
makeBox(w,-.5,9,8,2.5,.6,3,brassF);
}();

// ===== INFUSION COIL =====
!function(){
var S=5,el=document.getElementById('block3d-coil');if(!el)return;
var SM={front:.9,back:.72,right:.64,left:.58,top:1,bottom:.42};
var COPPER=[176,106,44],COPPERD=[120,64,26],BRASS=[224,184,80],GLASS=[42,74,68],XP=[168,224,48];
var w=document.createElement('div');w.className='world';
w.style.transform='rotateX(-16deg) rotateY(34deg) translate3d(0,'+0.8*S+'px,0)';
el.appendChild(w);
function mkTex(kind){
  var c=document.createElement('canvas');c.width=16;c.height=16;var x=c.getContext('2d');
  if(kind==='copper'){x.fillStyle='#b86a28';x.fillRect(0,0,16,16);[[0,'#c47630'],[1,'#b86a28'],[2,'#c07030'],[3,'#c87a34'],[4,'#b46228'],[5,'#c27232'],[6,'#be6e2e'],[7,'#d08040'],[8,'#c47630'],[9,'#b86228'],[10,'#c47030'],[11,'#bc6a2c'],[12,'#c87a34'],[13,'#b86228'],[14,'#a85e24'],[15,'#b86a28']].forEach(function(a){x.fillStyle=a[1];x.fillRect(0,a[0],16,1);});x.fillStyle='#e89858';x.fillRect(2,2,6,1);x.fillRect(4,5,4,1);x.fillRect(3,9,5,1);x.fillStyle='#f0a868';x.fillRect(3,2,3,1);x.fillRect(5,5,2,1);x.fillStyle='#d88040';x.fillRect(0,0,16,1);x.fillRect(0,0,1,16);x.fillStyle='#7a3a10';x.fillRect(0,15,16,1);x.fillRect(15,0,1,16);}
  else if(kind==='brass'){x.fillStyle='#c89030';x.fillRect(0,0,16,16);[[0,'#d49a3a'],[1,'#c89030'],[2,'#d09838'],[3,'#d89c3c'],[4,'#c48828'],[5,'#d29638'],[6,'#ce9234'],[7,'#dea040'],[8,'#d49a3a'],[9,'#c68c2c'],[10,'#d49838'],[11,'#cc8e32'],[12,'#d89c3c'],[13,'#c68a2c'],[14,'#be8428'],[15,'#c89030']].forEach(function(a){x.fillStyle=a[1];x.fillRect(0,a[0],16,1);});x.fillStyle='#f0c860';x.fillRect(2,2,6,1);x.fillRect(4,5,4,1);x.fillRect(3,9,5,1);x.fillStyle='#f8d878';x.fillRect(3,2,3,1);x.fillRect(5,5,2,1);x.fillStyle='#e8b050';x.fillRect(0,0,16,1);x.fillRect(0,0,1,16);x.fillStyle='#8a5c14';x.fillRect(0,15,16,1);x.fillRect(15,0,1,16);}
  return c.toDataURL();
}
var TX_C=mkTex('copper'),TX_B=mkTex('brass');
var copperF=function(n){return shade(COPPER,SM[n]);};copperF.tex=TX_C;
var copperDF=function(n){return shade(COPPERD,SM[n]);};copperDF.tex=TX_C;
var brassF=function(n){return shade(BRASS,SM[n]);};brassF.tex=TX_B;
var glassF=function(n){return shadeA(GLASS,SM[n],0.32);};
function makeBox(parent,bx,by,bz,bw,h,d,colorFn){
  var box=document.createElement('div');box.className='box';
  box.style.transform='translate3d('+bx*S+'px,'+-by*S+'px,'+bz*S+'px)';
  var W=bw*S,H=h*S,D=d*S;
  [{n:'front',t:'translate(-50%,-50%) translateZ('+D/2+'px)',w:W,h:H},
   {n:'back',t:'translate(-50%,-50%) rotateY(180deg) translateZ('+D/2+'px)',w:W,h:H},
   {n:'right',t:'translate(-50%,-50%) rotateY(90deg) translateZ('+W/2+'px)',w:D,h:H},
   {n:'left',t:'translate(-50%,-50%) rotateY(-90deg) translateZ('+W/2+'px)',w:D,h:H},
   {n:'top',t:'translate(-50%,-50%) rotateX(90deg) translateZ('+H/2+'px)',w:W,h:D},
   {n:'bottom',t:'translate(-50%,-50%) rotateX(-90deg) translateZ('+H/2+'px)',w:W,h:D}
  ].forEach(function(f){
    var e=document.createElement('div');e.className='face';
    e.style.width=f.w+'px';e.style.height=f.h+'px';e.style.transform=f.t;
    if(colorFn.tex){var fac=SM[f.n],ov=(fac>=1?0:(1-fac)*.85).toFixed(2);
      e.style.backgroundImage='linear-gradient(rgba(0,0,0,'+ov+'),rgba(0,0,0,'+ov+')),url('+colorFn.tex+')';
      e.style.backgroundSize='cover';e.style.imageRendering='pixelated';
    }else{e.style.background=colorFn(f.n);}
    box.appendChild(e);
  });
  parent.appendChild(box);return box;
}
var bodyBaseY=-8,bodyH=9,bodyW=9,bodyD=9,bodyCy=bodyBaseY+bodyH/2;
makeBox(w,0,bodyCy,0,bodyW,bodyH,bodyD,glassF);
var fluidInset=0.6,fluidMaxH=bodyH-1.2,fluidBottom=bodyBaseY+0.6;
var flH=fluidMaxH*0.55,flCy=fluidBottom+flH/2;
makeBox(w,0,flCy,0,bodyW-fluidInset*2,flH,bodyD-fluidInset*2,function(n){
  if(n==='top')return shadeA(XP,1.15,0.95);return shadeA(XP,SM[n],0.8);
});
makeBox(w,0,bodyBaseY+0.3,0,bodyW+0.5,1.4,bodyD+0.5,copperF);
makeBox(w,0,bodyBaseY+bodyH-0.3,0,bodyW+0.4,1.2,bodyD+0.4,copperF);
var half=bodyW/2;
[[half,half],[half,-half],[-half,half],[-half,-half]].forEach(function(p){
  makeBox(w,p[0],bodyCy,p[1],1,bodyH,1,copperF);
});
[[half,half],[-half,half]].forEach(function(p){
  [-2.5,0,2.5].forEach(function(ry){makeBox(w,p[0],bodyCy+ry,p[1],1.2,0.8,1.2,brassF);});
});
var neckBaseY=bodyBaseY+bodyH,neckH=6,neckCy=neckBaseY+neckH/2;
makeBox(w,0,neckCy,0,3,neckH,3,copperF);
[[1.5,1.5],[1.5,-1.5],[-1.5,1.5],[-1.5,-1.5]].forEach(function(p){
  makeBox(w,p[0]*.95,neckCy,p[1]*.95,0.6,neckH,0.6,copperDF);
});
makeBox(w,0,neckBaseY+0.4,0,3.6,0.8,3.6,brassF);
makeBox(w,0,neckBaseY+neckH-0.4,0,3.6,0.8,3.6,brassF);
var capBaseY=neckBaseY+neckH;
makeBox(w,0,capBaseY+1,0,5,2,5,brassF);
makeBox(w,0,capBaseY+2.4,0,1.6,1,1.6,brassF);
[[2,2],[2,-2],[-2,2],[-2,-2]].forEach(function(p){
  makeBox(w,p[0],capBaseY+1,p[1],1,2,1,copperF);
});
var capLight=makeBox(w,0,capBaseY+2.6,0,2,2,2,function(){return'#f0ffe0';});
capLight.style.filter='drop-shadow(0 0 3px #a8e030) drop-shadow(0 0 7px #4dd4ff)';
}();
})();
