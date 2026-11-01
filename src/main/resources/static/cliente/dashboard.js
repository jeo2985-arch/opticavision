const $ = s => document.querySelector(s);
const $$ = s => [...document.querySelectorAll(s)];
const escapeHtml = v => String(v ?? "").replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const moneda = v => new Intl.NumberFormat("es-HN",{style:"currency",currency:"HNL"}).format(Number(v||0));
async function obtener(ruta) {
    const r = await fetch('/api/cliente/'+ruta,{credentials:'same-origin'});
    if(!r.ok) throw new Error('No se pudo cargar '+ruta+' (HTTP '+r.status+')');
    return r.json();
}
function tabla(id, registros, columnas) {
    $(id).innerHTML = registros.length ? registros.map(x=>'<tr>'+columnas.map(k=>'<td>'+escapeHtml(x[k])+'</td>').join('')+'</tr>').join('') : '<tr><td colspan="'+columnas.length+'">No hay registros.</td></tr>';
}
async function mostrar(tab) {
    $$('.panel').forEach(p=>p.classList.toggle('hidden',p.id!==tab));
    $$('[data-tab]').forEach(b=>b.classList.toggle('active',b.dataset.tab===tab));
    $('#aviso').textContent='';
    try {
        if(tab==='perfil') {
            const x=await obtener('perfil');
            $('#datos-perfil').textContent=`${x.nombre} | ${x.correo} | DNI: ${x.dni} | Teléfono: ${x.telefono}`;
        }
        if(tab==='catalogo') {
            const datos=await obtener('catalogo');
            $('#datos-catalogo').innerHTML=datos.length?datos.map(x=>`<tr><td>${escapeHtml(x.nombre)}</td><td>${escapeHtml(x.categoria)}</td><td>${escapeHtml(x.marca)}</td><td>${moneda(x.precio)}</td><td>${escapeHtml(x.stock)}</td></tr>`).join(''):'<tr><td colspan="5">Sin productos.</td></tr>';
        }
        if(tab==='mediciones') tabla('#datos-mediciones',await obtener('mediciones'),['fechaMedicion','esferaOD','cilindroOD','esferaOI','cilindroOI','observaciones']);
        if(tab==='compras') {
            const datos=await obtener('compras');
            $('#datos-compras').innerHTML=datos.length?datos.map(x=>`<tr><td>${escapeHtml(x.idVenta)}</td><td>${escapeHtml(x.fechaVenta)}</td><td>${moneda(x.subtotal)}</td><td>${moneda(x.impuesto)}</td><td>${moneda(x.total)}</td><td>${escapeHtml(x.estado)}</td></tr>`).join(''):'<tr><td colspan="6">Sin compras.</td></tr>';
        }
    }catch(e) { $('#aviso').textContent=e.message; }
}
window.addEventListener('DOMContentLoaded',async()=>{
    $$('[data-tab]').forEach(b=>b.addEventListener('click',()=>mostrar(b.dataset.tab)));
    try { $('#csrf').value=(await obtener('csrf')).token; } catch(e) { $('#aviso').textContent=e.message; }
    await mostrar('perfil');
});
