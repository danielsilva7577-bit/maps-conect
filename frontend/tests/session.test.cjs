const {test} = require('node:test');
const assert = require('node:assert/strict');
const vm = require('node:vm');
const fs = require('node:fs');
const path = require('node:path');
function context(status = 200) {
  const data = new Map([['token','token'], ['user','{}']]);
  const ctx = vm.createContext({
    window: {location: {origin:'http://localhost:8080',protocol:'http:',port:'8080',pathname:'/api/pages/inicio.html'}},
    localStorage: {getItem:k=>data.get(k),setItem:(k,v)=>data.set(k,v),removeItem:k=>data.delete(k)},
    fetch:async()=>({ok:status===200,status,json:async()=>({message:'Error'})})
  });
  for (const file of ['auth.js','api.js']) vm.runInContext(fs.readFileSync(path.join(__dirname,'../js',file),'utf8'),ctx);
  return {ctx,data};
}
test('403 preserves session',async()=>{
  const {ctx,data}=context(403);
  await assert.rejects(vm.runInContext("API.request('/admin')",ctx));
  assert.equal(data.get('token'),'token');
});
test('401 removes expired session',async()=>{
  const {ctx,data}=context(401);
  await assert.rejects(vm.runInContext("API.request('/admin')",ctx));
  assert.equal(data.has('token'),false);
});
test('page navigation does not duplicate pages',()=>{
  const {ctx}=context();
  assert.equal(vm.runInContext("Auth.resolvePath('pages/perfil.html')",ctx),'../pages/perfil.html');
});
test('corrupt storage does not crash page',()=>{
  const {ctx,data}=context(); data.set('user','undefined');
  assert.equal(vm.runInContext('Auth.getUser()',ctx),null);
  assert.equal(data.has('token'),false);
});
