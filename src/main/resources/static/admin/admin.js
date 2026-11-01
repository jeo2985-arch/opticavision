async function cargarToken(){const r=await fetch('/api/vendedor/csrf',{credentials:'same-origin'});if(!r.ok)throw Error('No se pudo obtener CSRF');return (await r.json()).token}
cargarToken().then(token=>{const n=document.querySelector('#csrf-salir');if(n)n.value=token}).catch(e=>{const n=document.querySelector('#mensaje');if(n)n.textContent=e.message});
