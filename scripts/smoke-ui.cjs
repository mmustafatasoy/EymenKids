"use strict";
const fs = require("node:fs");
const vm = require("node:vm");
const assert = require("node:assert/strict");

const html = fs.readFileSync("app/src/main/assets/index.html", "utf8");
const scripts = [...html.matchAll(/<script(?:\s[^>]*)?>([\s\S]*?)<\/script>/g)].map(match => match[1]);
assert(scripts.length > 0, "Startup scripts not found");

class MockElement {
  constructor() {
    this.style = {};
    this.dataset = {};
    this.hidden = false;
    this.value = "";
    this.checked = false;
    this.innerHTML = "";
    this.textContent = "";
    this.tagName = "BUTTON";
    const classes = new Set();
    this.classList = {
      add(name) { classes.add(name); },
      remove(name) { classes.delete(name); },
      toggle(name, force) {
        if (force === undefined ? !classes.has(name) : force) classes.add(name);
        else classes.delete(name);
      },
      contains(name) { return classes.has(name); }
    };
  }
  addEventListener() {}
  appendChild() {}
  replaceChildren() {}
  focus() {}
  setAttribute() {}
}

const elements = new Map();
function element(selector) {
  if (!elements.has(selector)) elements.set(selector, new MockElement());
  return elements.get(selector);
}
const intervals = [];
const timers = [];
const document = {
  querySelector: element,
  querySelectorAll: selector => [element(selector + "#1"), element(selector + "#2")],
  getElementById: id => element("#" + id),
  createElement: () => new MockElement(),
  addEventListener() {},
};
const window = {
  addEventListener() {},
  scrollTo() {},
  setTimeout: (callback) => { timers.push(callback); return timers.length; },
};
const context = {
  window,
  document,
  navigator: {userAgent: "EymenKidsAndroid/1.4"},
  localStorage: {getItem: () => null, setItem() {}},
  fetch: async () => ({ok: true, json: async () => ({videos: []})}),
  setInterval: (callback, ms) => { intervals.push({callback, ms}); return intervals.length; },
  clearInterval() {},
  setTimeout: (callback) => { timers.push(callback); return timers.length; },
  clearTimeout() {},
  Date,
  console,
  URL,
};

for (const [i, source] of scripts.entries()) {
  new vm.Script(source, {filename: "script-" + (i + 1) + ".js"}).runInNewContext(context);
}
assert.equal(typeof window.eymenBack, "function", "Startup initialization did not finish");
assert(intervals.some(item => item.ms === 95), "Splash animation never started");
for (let i = 0; i < 20; i++) {
  for (const interval of intervals.filter(item => item.ms === 95)) interval.callback();
}
for (const timer of timers) timer();
assert(element("#splash").classList.contains("done"), "Splash did not release the main menu");
assert.equal(element("#progressbar").style.width, "100%", "Splash progress did not finish");
console.log("OK: scripts initialized, listeners installed, splash dismissed.");
