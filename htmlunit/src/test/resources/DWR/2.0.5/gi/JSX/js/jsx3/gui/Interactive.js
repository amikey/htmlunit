/*
 * Copyright (c) 2001-2007, TIBCO Software Inc.
 * Use, modification, and distribution subject to terms of license.
 */
jsx3.require("jsx3.gui.HotKey","jsx3.gui.Heavyweight");jsx3.Class.defineInterface("jsx3.gui.Interactive",null,function(j,i){var tb=jsx3.gui.Event;j.JSXBLUR="jsxblur";j.JSXCHANGE="jsxchange";j.JSXCLICK="jsxclick";j.JSXDOUBLECLICK="jsxdblclick";j.JSXFOCUS="jsxfocus";j.JSXKEYDOWN="jsxkeydown";j.JSXKEYPRESS="jsxkeypress";j.JSXKEYUP="jsxkeyup";j.JSXMOUSEDOWN="jsxmousedown";j.JSXMOUSEOUT="jsxmouseout";j.JSXMOUSEOVER="jsxmouseover";j.JSXMOUSEUP="jsxmouseup";j.JSXMOUSEWHEEL="jsxmousewheel";j.FOCUS_STYLE="text-decoration:underline";j.ADOPT="jsxadopt";j.AFTER_APPEND="jsxafterappend";j.AFTER_COMMIT="jsxaftercommit";j.AFTER_EDIT="jsxafteredit";j.AFTER_MOVE="jsxaftermove";j.AFTER_REORDER="jsxafterreorder";j.AFTER_RESIZE="jsxafterresize";j.AFTER_RESIZE_VIEW="jsxafterresizeview";j.AFTER_SORT="jsxaftersort";j.BEFORE_APPEND="jsxbeforeappend";j.BEFORE_DROP="jsxbeforedrop";j.BEFORE_EDIT="jsxbeforeedit";j.BEFORE_MOVE="jsxbeforemove";j.BEFORE_RESIZE="jsxbeforeresize";j.BEFORE_SELECT="jsxbeforeselect";j.BEFORE_SORT="jsxbeforesort";j.CANCEL_DROP="jsxcanceldrop";j.CHANGE="jsxchange";j.CTRL_DROP="jsxctrldrop";j.DESTROY="jsxdestroy";j.DATA="jsxdata";j.DRAG="jsxdrag";j.DROP="jsxdrop";j.EXECUTE="jsxexecute";j.HIDE="jsxhide";j.INCR_CHANGE="jsxincchange";j.MENU="jsxmenu";j.SCROLL="jsxscroll";j.SELECT="jsxselect";j.SHOW="jsxshow";j.SPYGLASS="jsxspy";j.TOGGLE="jsxtoggle";i.UJ=function(a,l){this.doEvent(j.JSXBLUR,{objEVENT:a});};i.gI=function(s,m){this.doEvent(j.JSXCHANGE,{objEVENT:s});};i.IU=function(m,s){this.doEvent(j.JSXCLICK,{objEVENT:m});};i.LH=function(f,c){this.doEvent(j.JSXDOUBLECLICK,{objEVENT:f});};i.SU=function(n,r){this.doEvent(j.JSXFOCUS,{objEVENT:n});};i.DY=function(l,a){var jc=false;if(this.hasHotKey())jc=this.checkHotKeys(l);if(!jc)this.doEvent(j.JSXKEYDOWN,{objEVENT:l});return jc;};i.M0=function(m,s){this.doEvent(j.JSXKEYPRESS,{objEVENT:m});};i.EN=function(r,n){this.doEvent(j.JSXKEYUP,{objEVENT:r});};i.mL=function(f,g){this.doEvent(j.JSXMOUSEDOWN,{objEVENT:f});};i.u2=function(b,k){this.doEvent(j.JSXMOUSEOUT,{objEVENT:b});};i.CL=function(f,c){this.doEvent(j.JSXMOUSEOVER,{objEVENT:f});};i._4=function(c,k){var ub=null;this.doEvent(j.JSXMOUSEUP,{objEVENT:c});if(c.rightButton()&&(ub=this.getMenu())!=null){var B=this.getServer().getJSX(ub);if(B!=null){var Mb=this.doEvent(j.MENU,{objEVENT:c,objMENU:B});if(Mb!==false){if(Mb instanceof Object&&Mb.objMENU instanceof jsx3.gui.Menu)B=Mb.objMENU;B.showContextMenu(c,this);}}}};i.U2=function(d,e){this.doEvent(j.JSXMOUSEWHEEL,{objEVENT:d});};i.setEvent=function(s,p){this.getEvents()[p]=s;return this;};i.getEvents=function(){if(this._jsxbu==null)this._jsxbu={};return this._jsxbu;};i.getEvent=function(h){return this._jsxbu!=null?this._jsxbu[h]:null;};i.hasEvent=function(p){return this._jsxbu!=null&&this._jsxbu[p]!=null&&this._jsxbu[p].match(/\S/);};i.doEvent=function(k,l){var uc=this.getEvent(k);if(typeof(this.publish)=="function")this.publish({subject:k,context:l});return this.eval(uc,l);};i.removeEvent=function(n){if(this._jsxbu!=null)delete this._jsxbu[n];return this;};i.removeEvents=function(){this._jsxbu={};return this;};i.setCanMove=function(f){this.jsxmove=f;return this;};i.getCanMove=function(){return this.jsxmove||jsx3.Boolean.FALSE;};i.setCanDrag=function(o){this.jsxdrag=o;return this;};i.getCanDrag=function(){return this.jsxdrag||jsx3.Boolean.FALSE;};i.setCanDrop=function(o){this.jsxdrop=o;return this;};i.getCanDrop=function(){return this.jsxdrop||jsx3.Boolean.FALSE;};i.setCanSpy=function(q){this.jsxspy=q;return this;};i.getCanSpy=function(){return this.jsxspy||jsx3.Boolean.FALSE;};i.getMenu=function(){return this.jsxmenu;};i.setMenu=function(f){this.jsxmenu=f;return this;};j.s5=[tb.BLUR,tb.CHANGE,tb.CLICK,tb.DOUBLECLICK,tb.FOCUS,tb.KEYDOWN,tb.KEYPRESS,tb.KEYUP,tb.MOUSEDOWN,tb.MOUSEOUT,tb.MOUSEOVER,tb.MOUSEUP,tb.MOUSEWHEEL];j.F5={};j.F5[tb.BLUR]="UJ";j.F5[tb.CHANGE]="gI";j.F5[tb.CLICK]="IU";j.F5[tb.DOUBLECLICK]="LH";j.F5[tb.FOCUS]="SU";j.F5[tb.KEYDOWN]="DY";j.F5[tb.KEYPRESS]="M0";j.F5[tb.KEYUP]="EN";j.F5[tb.MOUSEDOWN]="mL";j.F5[tb.MOUSEOUT]="u2";j.F5[tb.MOUSEOVER]="CL";j.F5[tb.MOUSEUP]="_4";j.F5[tb.MOUSEWHEEL]="U2";j.isBridgeEventHandler=function(f){if(j.sn==null){j.sn={};for(var Y=0;Y<j.s5.length;Y++){j.sn["on"+j.s5[Y]]=true;}}return j.sn[f];};j.Jn="kE";j.zs="_e";i.lM=function(c,f){var Mb={};if((c==null||!c[tb.KEYDOWN])&&(this.hasHotKey()||this.getAlwaysCheckHotKeys()))Mb[tb.KEYDOWN]=true;if((c==null||!c[tb.MOUSEUP])&&this.getMenu())Mb[tb.MOUSEUP]=true;var y=[];var Rb=this.instanceOf(jsx3.gui.Painted);var z=this.getId();for(var Db=0;Db<j.s5.length;Db++){var zc=j.s5[Db];var _="on"+zc;var Jb=[];var Bb=Rb?this.getAttribute(_):null;if(Bb){Jb.push(Bb.replace(/\"/g,"&quot;"));if(!Bb.match(/;\s*$/))Jb.push(";");}var yc=c&&c[zc]||Mb[zc];if(yc){if(typeof(yc)!="string")yc=j.F5[zc];if(f!=null)Jb.push("jsx3."+j.zs+"(event,this,'"+yc+"',"+f+");");else Jb.push("jsx3.GO('"+z+"')."+j.Jn+"(event,this,'"+yc+"');");}if(Jb.length>0){y.push(" "+_+"=\"");y.pushAll(Jb);y.push("\"");}}return y.join("");};i.RX=function(d,q,r){var G="on"+d;var O="";var Qb=false;if(Qb){var C=this.getAttribute(G);if(C){O=O+C;if(!C.match(/;\s*$/))O=O+";";}}var U=r!=null?"jsx3."+j.zs+"(event,this,'"+q+"',"+r+");":"jsx3.GO('"+this.getId()+"')."+j.Jn+"(event,this,'"+q+"');";return " "+G+"=\""+O+U+"\"";};i.kE=function(l,c,k){var bc=this[k];var Z=jsx3.gui.Event.wrap(l);if(bc){bc.call(this,Z,c);}else{throw new jsx3.Exception("no bridge method '"+k+"' for event type '"+Z.getType()+"' on DOM object of id "+this.getId());}};jsx3._e=function(r,n,e,k){var P=n;k=k||Number(0);for(var ec=0;ec<k;ec++)P=P.parentNode;var y=P.getAttribute("id");var cc=jsx3.GO(y);if(cc!=null)cc.kE(r,n,e);else{if(jsx3.html.getElmUpByTagName(n,"body")!=null)throw new jsx3.Exception("No JSX DOM object with id '"+y+"' ("+k+" up from "+n+").");}};j.P3=function(r,n,o,p){var Q=n.ownerDocument;jsx3.gui.Event.preventSelection(Q);var ub=r.getTrueX();var kb=n.offsetLeft;jsx3.EventHelp.constrainY=p;jsx3.EventHelp.xOff=kb-ub;var w=r.getTrueY();var A=n.offsetTop;jsx3.EventHelp.constrainX=o;jsx3.EventHelp.yOff=A-w;jsx3.EventHelp.curDragObject=n;jsx3.EventHelp.FLAG=1;jsx3.EventHelp.beginTrackMouse(r);r.setCapture(n);r.cancelReturn();r.cancelBubble();};j.DM=function(r,n,k){var dc=n.ownerDocument;jsx3.gui.Event.preventSelection(dc);jsx3.EventHelp.startX=r.getTrueX();jsx3.EventHelp.startY=r.getTrueY();jsx3.EventHelp.xOff=n.offsetLeft;jsx3.EventHelp.yOff=n.offsetTop;jsx3.EventHelp.dragRounder=k;jsx3.EventHelp.curDragObject=n;jsx3.EventHelp.FLAG=3;jsx3.EventHelp.beginTrackMouse(r);r.setCapture(n);r.cancelReturn();r.cancelBubble();};i.doBeginMove=function(m,s){if(!m.leftButton()){return;}if(s==null)s=this.getRendered();var wb=s.ownerDocument;var Lc=this.doEvent(j.BEFORE_MOVE,{objEVENT:m});var Ec=Lc===false;if(s!=null&&!Ec){s.style.zIndex=this.getServer().getNextZIndex(jsx3.app.Server.Z_DRAG);jsx3.gui.Event.preventSelection(wb);var Fb=m.getTrueX();var Z=s.style.position=="absolute"?parseInt(s.style.left):s.scrollLeft;if(Lc&&Lc.bCONSTRAINY)jsx3.EventHelp.constrainY=true;jsx3.EventHelp.xOff=Z-Fb;var gc=m.getTrueY();var ic=s.style.position=="absolute"?parseInt(s.style.top):s.scrollTop;if(Lc&&Lc.bCONSTRAINX)jsx3.EventHelp.constrainX=true;jsx3.EventHelp.yOff=ic-gc;jsx3.EventHelp.curDragObject=s;jsx3.EventHelp.FLAG=1;jsx3.EventHelp.beginTrackMouse(m);m.setCapture(s);}};i.doEndMove=function(c,k){if(k==null)k=this.getRendered();if(k!=null){k.style.zIndex=this.getZIndex();c.releaseCapture(k);var Bb=parseInt(k.style.left);var eb=parseInt(k.style.top);this.setLeft(Bb);this.setTop(eb);this.doEvent(j.AFTER_MOVE,{objEVENT:c});}};i.doDrag=function(b,k,q,o){b.cancelAll();if(k==null){k=b.srcElement();while(k!=null&&k.getAttribute("JSXDragId")==null){k=k.parentNode;if(k=k.ownerDocument.getElementsByTagName("body")[0])k=null;}if(k==null)return;}var V=k.getAttribute("JSXDragId");var ib=k.getAttribute("JSXDragType");if(o==null)o={};o.strDRAGID=k.getAttribute("JSXDragId");o.strDRAGTYPE=k.getAttribute("JSXDragType");o.objGUI=k;o.objEVENT=b;if(this.doEvent(j.DRAG,o)===false)return;jsx3.EventHelp.DRAGTYPE=o.strDRAGTYPE;jsx3.EventHelp.DRAGID=o.strDRAGID;if(o.strDRAGIDS instanceof Array)jsx3.EventHelp.DRAGIDS=o.strDRAGIDS;jsx3.EventHelp.JSXID=this;if(q==null)q=jsx3.EventHelp.drag;var Jb=q(k,this,jsx3.EventHelp.DRAGTYPE,jsx3.EventHelp.DRAGID);if(Jb==null){return false;}else{jsx3.EventHelp.dragItemHTML=Jb;jsx3.EventHelp.FLAG=2;jsx3.EventHelp.beginTrackMouse(b);}jsx3.EventHelp.constrainX=false;jsx3.EventHelp.constrainY=false;};i.doDrop=function(n,r,m){if(jsx3.EventHelp.DRAGID!=null){var Yb=jsx3.EventHelp.JSXID;var K=jsx3.EventHelp.DRAGID;var zc=jsx3.EventHelp.DRAGTYPE;var ub={objEVENT:n,objSOURCE:Yb,strDRAGID:K,strDRAGTYPE:zc};if(m==jsx3.EventHelp.ONDROP&&jsx3.gui.isMouseEventModKey(n)){ub.objGUI=n.srcElement();this.doEvent(j.CTRL_DROP,ub);jsx3.EventHelp.reset();}else{if(m==jsx3.EventHelp.ONDROP){ub.objGUI=n.srcElement();this.doEvent(j.DROP,ub);jsx3.EventHelp.reset();}else{if(m==jsx3.EventHelp.ONBEFOREDROP){ub.objGUI=n.toElement();this.doEvent(j.BEFORE_DROP,ub);}else{if(m==jsx3.EventHelp.ONCANCELDROP){ub.objGUI=n.fromElement();this.doEvent(j.CANCEL_DROP,ub);}}}}}};i.doSpyOver=function(r,n,a){var cb=r.getTrueX();var Ab=r.getTrueY();if(this._jsxspytimeout)return;if(a==null)a={};a.objEVENT=r;var Wb=this;this._jsxspytimeout=window.setTimeout(function(){if(Wb.getParent()==null)return;Wb._jsxspytimeout=null;var wc=Wb.doEvent(j.SPYGLASS,a);if(wc)Wb.showSpy(wc,cb,Ab);},jsx3.EventHelp.SPYDELAY);};i.doSpyOut=function(f,g){if(f.toElement()!=null&&f.toElement().className=="jsx30spyglassbuffer")return;window.clearTimeout(this._jsxspytimeout);this._jsxspytimeout=null;j.hideSpy();};i.showSpy=function(g,r,n){if(g!=null){j.hideSpy();g="<span onmouseout=\"if (event.toElement != this.childNodes[0]) jsx3.gui.Interactive.hideSpy();\" class=\"jsx30spyglassbuffer\"><div id=\"_jsxspychild\" class=\"jsx30spyglass\">"+g+"</div></span>";var Ob=new jsx3.gui.Heavyweight("_jsxspy",this);Ob.setHTML(g);Ob.addRule(r,"W",-2,"X");Ob.addRule(r,"E",12,"X");Ob.addRule(null,"W",-24,"X");Ob.addRule(n,"N",-2,"Y");Ob.addRule(n,"S",-6,"Y");Ob.addRule(null,"N",-224,"Y");Ob.setOverflow(jsx3.gui.Block.OVERFLOWEXPAND);Ob.show();var jb=Ob.getRendered();if(jb){var Yb=jb.ownerDocument.getElementsByTagName("body")[0];var S=Yb.offsetHeight-(jb.childNodes[0].offsetHeight+parseInt(jb.style.top));if(S<0)jb.style.top=parseInt(jb.style.top)+S+"px";if(parseInt(jb.style.width)+parseInt(jb.style.left)>Yb.offsetWidth)Ob.applyRules("X");}tb.subscribe(jsx3.gui.Event.MOUSEDOWN,jsx3.gui.Interactive.hideSpy);}};j.hideSpy=function(){if(jsx3.gui.Heavyweight){var hb=jsx3.gui.Heavyweight.GO("_jsxspy");if(hb){hb.destroy();tb.unsubscribe(jsx3.gui.Event.MOUSEDOWN,jsx3.gui.Interactive.hideSpy);}}};i.getSpyStyles=function(g){return this.jsxspystyle?this.jsxspystyle:g?g:null;};i.setSpyStyles=function(a){delete this._jsxem;delete this._jsxrk;delete this.jsxspystylekeys;delete this.jsxspystylevalues;this.jsxspystyle=a;};i.ao=function(){var A={};if(jsx3.util.strEmpty(this.getSpyStyles())&&this.jsxspystylekeys!=null){var Fc=(this.jsxspystylekeys||"").split(/ *; */);var hc=(this.jsxspystylevalues||"").split(/ *; */);for(var u=0;u<Fc.length;u++)A[Fc[u]]=hc[u];}else{var y=this.getSpyStyles(j.FOCUS_STYLE);var ob=/(-\S)/gi;var A={};var Nc=y.split(";");for(var u=0;u<Nc.length;u++){var V=Nc[u]+"";var Cb=V.split(":");if(Cb&&Cb.length==2){var Nb=Cb[0].replace(ob,function(d,c){return c.substring(1).toUpperCase();});A[Nb]=Cb[1];}}}return A;};i.applySpyStyle=function(d){if(this._jsxem==null)this._jsxem=this.ao();if(this._jsxrk==null){this._jsxrk={};for(var bc in this._jsxem)this._jsxrk[bc]=d.style[bc];}for(var bc in this._jsxem)d.style[bc]=this._jsxem[bc];};i.removeSpyStyle=function(e){for(var ub in this._jsxrk)e.style[ub]=this._jsxrk[ub];};i.checkHotKeys=function(e){if(this._jsxkn==null)return false;if(e.isModifierKey())return false;var W=false;var Mb=e.getAttribute("jsxmodal");for(var ac in this._jsxkn){var V=this._jsxkn[ac];if(V instanceof jsx3.gui.HotKey){if(V.isDestroyed()){delete this._jsxkn[ac];continue;}else{if(!V.isEnabled()){continue;}}if(V.isMatch(e)){if(!Mb)V.invoke(this,[e]);W=true;}}}if(W)e.cancelAll();return W;};i.registerHotKey=function(k,d,f,o,c){var yc;if(k instanceof jsx3.gui.HotKey){yc=k;}else{var jb=typeof(k)=="function"?k:this[k];if(!(typeof(jb)=="function"))throw new jsx3.IllegalArgumentException("vntCallback",k);yc=new jsx3.gui.HotKey(jb,d,f,o,c);}if(this._jsxkn==null)this._jsxkn={length:0};var fb=yc.getKey();this._jsxkn.length+=(this._jsxkn[fb]?0:1);this._jsxkn[fb]=yc;return yc;};i.hasHotKey=function(){return this._jsxkn!=null&&this._jsxkn.length>0;};i.setAlwaysCheckHotKeys=function(k){this.jsxalwayscheckhk=k;return this;};i.getAlwaysCheckHotKeys=function(){return this.jsxalwayscheckhk;};i.clearHotKeys=function(){this._jsxkn=null;};j.getVersion=function(){return "3.00.00";};i.isOldEventProtocol=function(){var Lb=this.getServer();return Lb&&Lb.getEnv("EVENTSVERS")<3.1;};});jsx3.Event=jsx3.gui.Interactive;
