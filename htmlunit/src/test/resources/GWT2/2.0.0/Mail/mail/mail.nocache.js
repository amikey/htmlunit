function mail(){var M='',nb='" for "gwt:onLoadErrorFn"',lb='" for "gwt:onPropertyErrorFn"',Y='"><\/script>',$='#',Lb='.cache.html',ab='/',Fb='20111A518E4E95DA9ABF32428DD52CD6',Gb='38413902DEBCA5E3121EF28B0D49B7FE',Hb='4E0DEBCB8468C288177A61FA0ADD6E2B',Ib='7E71188C76484950D0B066903C433C2E',Jb='7F9F651B9399A0BCF159EC184BBD3F8C',Nb='<script defer="defer">mail.onInjectionDone(\'mail\')<\/script>',X='<script id="',ib='=',_='?',kb='Bad handler "',Mb='DOMContentLoaded',Kb='ECCACD8B266A89FF8F5D665199872CEA',Z='SCRIPT',W='__gwt_marker_mail',bb='base',Q='begin',P='bootstrap',db='clear.cache.gif',hb='content',V='end',zb='gecko',Ab='gecko1_8',R='gwt.codesvr=',S='gwt.hosted=',T='gwt.hybrid',mb='gwt:onLoadErrorFn',jb='gwt:onPropertyErrorFn',gb='gwt:property',Db='hosted.html?mail',yb='ie6',xb='ie8',ob='iframe',cb='img',pb="javascript:''",Cb='loadExternalRefs',N='mail',eb='meta',rb='moduleRequested',U='moduleStartup',wb='msie',fb='name',tb='opera',qb='position:absolute;width:0;height:0;border:none',vb='safari',Eb='selectingPermutation',O='startup',Bb='unknown',sb='user.agent',ub='webkit';var k=window,l=document,m=k.__gwtStatsEvent?function(a){return k.__gwtStatsEvent(a)}:null,n=k.__gwtStatsSessionId?k.__gwtStatsSessionId:null,o,p,q,r=M,s={},t=[],u=[],v=[],w,x;m&&m({moduleName:N,sessionId:n,subSystem:O,evtGroup:P,millis:(new Date).getTime(),type:Q});if(!k.__gwt_stylesLoaded){k.__gwt_stylesLoaded={}}if(!k.__gwt_scriptsLoaded){k.__gwt_scriptsLoaded={}}function y(){var b=false;try{var c=k.location.search;return (c.indexOf(R)!=-1||(c.indexOf(S)!=-1||k.external&&k.external.gwtOnLoad))&&c.indexOf(T)==-1}catch(a){}y=function(){return b};return b}
function z(){if(o&&p){var b=l.getElementById(N);var c=b.contentWindow;if(y()){c.__gwt_getProperty=function(a){return F(a)}}mail=null;c.gwtOnLoad(w,N,r);m&&m({moduleName:N,sessionId:n,subSystem:O,evtGroup:U,millis:(new Date).getTime(),type:V})}}
function A(){var e,f=W,g;l.write(X+f+Y);g=l.getElementById(f);e=g&&g.previousSibling;while(e&&e.tagName!=Z){e=e.previousSibling}function h(a){var b=a.lastIndexOf($);if(b==-1){b=a.length}var c=a.indexOf(_);if(c==-1){c=a.length}var d=a.lastIndexOf(ab,Math.min(c,b));return d>=0?a.substring(0,d+1):M}
;if(e&&e.src){r=h(e.src)}if(r==M){var i=l.getElementsByTagName(bb);if(i.length>0){r=i[i.length-1].href}else{r=h(l.location.href)}}else if(r.match(/^\w+:\/\//)){}else{var j=l.createElement(cb);j.src=r+db;r=h(j.src)}if(g){g.parentNode.removeChild(g)}}
function B(){var b=document.getElementsByTagName(eb);for(var c=0,d=b.length;c<d;++c){var e=b[c],f=e.getAttribute(fb),g;if(f){if(f==gb){g=e.getAttribute(hb);if(g){var h,i=g.indexOf(ib);if(i>=0){f=g.substring(0,i);h=g.substring(i+1)}else{f=g;h=M}s[f]=h}}else if(f==jb){g=e.getAttribute(hb);if(g){try{x=eval(g)}catch(a){alert(kb+g+lb)}}}else if(f==mb){g=e.getAttribute(hb);if(g){try{w=eval(g)}catch(a){alert(kb+g+nb)}}}}}}
function E(a,b){var c=v;for(var d=0,e=a.length-1;d<e;++d){c=c[a[d]]||(c[a[d]]=[])}c[a[e]]=b}
function F(a){var b=u[a](),c=t[a];if(b in c){return b}var d=[];for(var e in c){d[c[e]]=e}if(x){x(a,d,b)}throw null}
var G;function H(){if(!G){G=true;var a=l.createElement(ob);a.src=pb;a.id=N;a.style.cssText=qb;a.tabIndex=-1;l.body.appendChild(a);m&&m({moduleName:N,sessionId:n,subSystem:O,evtGroup:U,millis:(new Date).getTime(),type:rb});a.contentWindow.location.replace(r+J)}}
u[sb]=function(){var b=navigator.userAgent.toLowerCase();var c=function(a){return parseInt(a[1])*1000+parseInt(a[2])};if(b.indexOf(tb)!=-1){return tb}else if(b.indexOf(ub)!=-1){return vb}else if(b.indexOf(wb)!=-1){if(document.documentMode>=8){return xb}else{var d=/msie ([0-9]+)\.([0-9]+)/.exec(b);if(d&&d.length==3){var e=c(d);if(e>=6000){return yb}}}}else if(b.indexOf(zb)!=-1){var d=/rv:([0-9]+)\.([0-9]+)/.exec(b);if(d&&d.length==3){if(c(d)>=1008)return Ab}return zb}return Bb};t[sb]={gecko:0,gecko1_8:1,ie6:2,ie8:3,opera:4,safari:5};mail.onScriptLoad=function(){if(G){p=true;z()}};mail.onInjectionDone=function(){o=true;m&&m({moduleName:N,sessionId:n,subSystem:O,evtGroup:Cb,millis:(new Date).getTime(),type:V});z()};A();var I;var J;if(y()){if(k.external&&(k.external.initModule&&k.external.initModule(N))){k.location.reload();return}J=Db;I=M}B();m&&m({moduleName:N,sessionId:n,subSystem:O,evtGroup:P,millis:(new Date).getTime(),type:Eb});if(!y()){try{E([zb],Fb);E([Ab],Gb);E([yb],Hb);E([xb],Ib);E([tb],Jb);E([vb],Kb);I=v[F(sb)];J=I+Lb}catch(a){return}}var K;function L(){if(!q){q=true;z();if(l.removeEventListener){l.removeEventListener(Mb,L,false)}if(K){clearInterval(K)}}}
if(l.addEventListener){l.addEventListener(Mb,function(){H();L()},false)}var K=setInterval(function(){if(/loaded|complete/.test(l.readyState)){H();L()}},50);m&&m({moduleName:N,sessionId:n,subSystem:O,evtGroup:P,millis:(new Date).getTime(),type:V});m&&m({moduleName:N,sessionId:n,subSystem:O,evtGroup:Cb,millis:(new Date).getTime(),type:Q});l.write(Nb)}
mail();