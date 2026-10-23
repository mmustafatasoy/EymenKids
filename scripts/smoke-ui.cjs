"use strict";
const fs=require("node:fs");
const vm=require("node:vm");
const assert=require("node:assert/strict");
const html=fs.readFileSync("app/src/main/assets/index.html","utf8");
const scripts=[...html.matchAll(/<script(?:\s[^>]*)?>([\s\S]*?)<\/script>/g)].map(m=>m[1]);
assert(scripts.length>0,"No scripts found");
assert.equal((html.match(/id="menuToggle"/g)||[]).length,1,"Expected exactly one menu");
assert(!html.includes('id="splash"'),"Blocking splash must be gone");
assert(!html.includes('id="panel"'),"Legacy dark sidebar must be gone");
assert(html.includes("ui-config.json"),"Remote UI settings unavailable");

class Node {
 constructor(name) {
  this.name=name||"";this.style={setProperty(name,value){this[name]=value}};
  this.value="";this.hidden=false;this.textContent="";this.innerHTML="";this.dataset={};
  this.tagName="BUTTON";this.listeners={};this.attributes={};
  this.children=[];this.classSet=new Set();
  const set=this.classSet;
  this.classList={
   add:n=>set.add(n),remove:n=>set.delete(n),contains:n=>set.has(n),
   toggle:(n,force)=>{const on=force===undefined?!set.has(n):force;if(on)set.add(n);else set.delete(n);return on}
  };
 }
 addEventListener(n,cb){this.listeners[n]=cb}
 appendChild(item){this.children.push(item);return item}
 append(...items){this.children.push(...items)}
 replaceChildren(...items){this.children=items}
 setAttribute(n,v){this.attributes[n]=String(v)}
 getAttribute(n){return this.attributes[n]}
 focus(){}
 scrollIntoView(){}
 click(){if(this.listeners.click)this.listeners.click({target:this,preventDefault(){}})}
}
const cache=new Map();
const lookup=id=>{if(!cache.has(id))cache.set(id,new Node(id));return cache.get(id)};
const document={
 querySelector:lookup,
 getElementById:id=>lookup("#"+id),
 createElement:tag=>{const el=new Node(tag);el.tagName=tag.toUpperCase();return el},
 createTextNode:contents=>({textContent:contents}),
 documentElement:new Node("html"),
 body:new Node("body"),
 addEventListener(n,cb){this[n]=cb}
};
const storage=new Map();
const window={scrollTo(){},location:{href:""},addEventListener(){}};
const context={
 document,window,navigator:{userAgent:"EymenKidsAndroid/1.5"},
 localStorage:{getItem:k=>storage.get(k)||null,setItem:(k,v)=>storage.set(k,v)},
 console,URL,AbortController,
 fetch:async url=>({ok:true,json:async()=>String(url).includes("ui-config")
  ? {schema_version:1,revision:1,palette:{purple:"#8149ed",pink:"#fa579b",blue:"#318cff",mint:"#24c7aa"},heroTitle:"Eğlenceye hazır mısın?",heroSubtitle:"Bir kez indir"}
  : {schema_version:1,videos:[{id:"cu-cu-cu"}]}}),
 setTimeout:()=>1,clearTimeout(){},
};
for(const [i,s] of scripts.entries())new vm.Script(s,{filename:"page-"+i+".js"}).runInNewContext(context);
assert.equal(typeof window.eymenBack,"function","Back key handler missing");
assert(lookup("#featuredGrid").children.length===4,"Featured cards did not initialize");
assert(lookup("#songGrid").children.length>=6,"Songs section did not initialize");
assert(lookup("#cartoonGrid").children.length>=3,"Cartoons section did not initialize");
lookup("#menuToggle").click();
assert(lookup("#drawer").classList.contains("open"),"Drawer cannot open");
assert(lookup("#backdrop").classList.contains("show"),"Drawer backdrop missing");
assert.equal(lookup("#menuToggle").attributes["aria-expanded"],"true");
assert(window.eymenBack(),"Hardware back did not close drawer");
assert(!lookup("#drawer").classList.contains("open"),"Drawer did not close");
lookup("#menuToggle").click();
lookup("#navAbout").click();
assert(lookup("#modalWrap").classList.contains("open"),"About modal did not open");
assert(lookup("#modalTitle").textContent.includes("Hakkında"),"About label incorrect");
assert(window.eymenBack(),"Hardware back did not dismiss About");
assert(!lookup("#modalWrap").classList.contains("open"),"About modal stuck");
lookup("#searchInput").value="vız";
lookup("#searchInput").listeners.input({target:lookup("#searchInput")});
assert(lookup("#searchResults").hidden===false,"Search results hidden");
assert(lookup("#resultGrid").children.length===1,"Search filtering incorrect");
lookup("#searchInput").value="";
lookup("#searchInput").listeners.input({target:lookup("#searchInput")});
assert(lookup("#searchResults").hidden===true,"Search did not reset");
console.log("PASS: colorful home, 1 drawer, about, search, mobile back and offline catalog routes.");
