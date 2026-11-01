const estado = { productos: [], clientes: [], ventas: [], csrf: "" };
const $ = selector => document.querySelector(selector);
const $$ = selector => [...document.querySelectorAll(selector)];
const texto = valor => String(valor ?? "");
const seguro = valor => texto(valor).replace(/[&<>"']/g, c => ({"&":"&amp;","<":"&lt;",">":"&gt;","\"":"&quot;","'":"&#39;"})[c]);
const dinero = valor => new Intl.NumberFormat("es-HN", {style: "currency", currency: "HNL"}).format(Number(valor || 0));
const notificar = (mensaje, error = false) => {
    const caja = $("#mensaje");
    caja.textContent = mensaje;
    caja.classList.toggle("error", error);
    caja.hidden = false;
    window.scrollTo({ top: 0, behavior: "smooth" });
};
const datosFormulario = formulario => Object.fromEntries(new FormData(formulario).entries());
async function cargarCsrf() {
    const respuesta = await fetch("/api/vendedor/csrf", {credentials: "same-origin"});
    if (!respuesta.ok) throw new Error("No se pudo obtener el token de seguridad.");
    estado.csrf = (await respuesta.json()).token;
    $("#csrf-salir").value = estado.csrf;
}
async function api(ruta, opciones = {}) {
    const metodo = opciones.method || "GET";
    const encabezados = {"Accept": "application/json"};
    if (opciones.body !== undefined) encabezados["Content-Type"] = "application/json";
    if (metodo !== "GET") encabezados["X-CSRF-TOKEN"] = estado.csrf;
    const respuesta = await fetch("/api/vendedor" + ruta, {...opciones, method: metodo, headers: encabezados, credentials: "same-origin"});
    if (respuesta.status === 401 || respuesta.status === 403) {
        if (metodo !== "GET" && respuesta.status === 403) {
            await cargarCsrf();
            throw new Error("La solicitud de seguridad expiró. Intenta guardar nuevamente.");
        }
        throw new Error("La sesión terminó o no tienes permisos para esta operación.");
    }
    let datos;
    try { datos = await respuesta.json(); } catch { datos = {}; }
    if (!respuesta.ok) throw new Error(datos.error || "La operación no pudo completarse.");
    return datos;
}
function mostrarSeccion(id) {
    $$(".seccion").forEach(s => s.classList.toggle("visible", s.id === id));
    $$(".menu-boton").forEach(b => b.classList.toggle("activo", b.dataset.seccion === id));
    $("#mensaje").hidden = true;
    if (id === "inicio") actualizarResumen().catch(e => notificar(e.message, true));
    if (id === "inventario") cargarProductos().catch(e => notificar(e.message, true));
    if (id === "clientes") cargarClientes().catch(e => notificar(e.message, true));
    if (id === "mediciones") cargarClientes().catch(e => notificar(e.message, true));
    if (id === "ventas") Promise.all([cargarClientes(), cargarProductos(), cargarVentas()]).catch(e => notificar(e.message, true));
}
async function actualizarResumen() {
    const r = await api("/resumen");
    $("#k-productos").textContent = r.productos;
    $("#k-stock").textContent = r.stockBajo;
    $("#k-clientes").textContent = r.clientes;
    $("#k-ventas").textContent = r.ventas;
    $("#impuesto-info").textContent = `Impuesto de ejemplo configurado: ${r.impuestoPorcentaje} %. Confirma la tasa aplicable antes de utilizar el sistema en producción.`;
}
async function cargarProductos() {
    estado.productos = await api("/productos");
    dibujarProductos();
    actualizarLineas();
}
function dibujarProductos() {
    const filtro = $("#filtro-productos").value.toLowerCase();
    const filas = estado.productos.filter(p => texto(p.nombre).toLowerCase().includes(filtro) || texto(p.categoria).toLowerCase().includes(filtro));
    $("#tabla-productos").innerHTML = filas.map(p => `<tr><td>${p.idProducto}</td><td>${seguro(p.nombre)}</td><td>${seguro(p.categoria)}</td><td>${dinero(p.precio)}</td><td class="${p.stock < 5 ? "bajo" : ""}">${p.stock}</td><td>${p.activo ? "Activo" : "Inactivo"}</td><td><button class="boton boton-secundario" data-editar-producto="${p.idProducto}" type="button">Editar</button></td></tr>`).join("") || "<tr><td colspan='7'>No hay productos para mostrar.</td></tr>";
}
function limpiarProducto() {
    $("#form-producto").reset();
    $("#form-producto [name=idProducto]").value = "";
    $("#titulo-producto").textContent = "Registrar producto";
}
function editarProducto(id) {
    const p = estado.productos.find(v => Number(v.idProducto) === id);
    if (!p) return;
    const f = $("#form-producto");
    ["idProducto", "nombre", "categoria", "marca", "material", "forma", "precio", "stock", "descripcion"].forEach(k => f.elements[k].value = p[k] ?? "");
    f.elements.activo.checked = !!p.activo;
    $("#titulo-producto").textContent = "Actualizar producto #" + id;
    f.scrollIntoView({behavior: "smooth"});
}
async function guardarProducto(evento) {
    evento.preventDefault();
    const f = evento.currentTarget;
    const d = datosFormulario(f);
    const id = d.idProducto;
    delete d.idProducto;
    d.activo = f.elements.activo.checked;
    d.precio = Number(d.precio);
    d.stock = Number(d.stock);
    try {
        const respuesta = await api(id ? `/productos/${id}` : "/productos", {method: id ? "PUT" : "POST", body: JSON.stringify(d)});
        limpiarProducto();
        await cargarProductos();
        notificar(respuesta.mensaje);
    } catch (e) { notificar(e.message, true); }
}
async function cargarClientes() {
    estado.clientes = await api("/clientes");
    $("#tabla-clientes").innerHTML = estado.clientes.map(c => `<tr><td>${c.idCliente}</td><td>${seguro(c.nombre)}</td><td>${seguro(c.dni)}</td><td>${seguro(c.telefono)}</td><td>${seguro(c.correo)}</td><td><button class="boton boton-secundario" type="button" data-editar-cliente="${c.idCliente}">Editar</button></td></tr>`).join("") || "<tr><td colspan='6'>No hay clientes registrados.</td></tr>";
    for (const selector of ["#medicion-cliente", "#venta-cliente"]) {
        const select = $(selector);
        const anterior = select.value;
        select.innerHTML = `<option value="">Selecciona un cliente</option>` + estado.clientes.map(c => `<option value="${c.idCliente}">${seguro(c.nombre)} · ${seguro(c.dni)}</option>`).join("");
        select.value = anterior;
    }
}
function limpiarCliente() {
    const f = $("#form-cliente");
    f.reset();
    f.elements.idCliente.value = "";
    f.elements.password.required = true;
    $("#campo-password").hidden = false;
    $("#titulo-cliente").textContent = "Registrar cliente";
}
function editarCliente(id) {
    const c = estado.clientes.find(v => Number(v.idCliente) === id);
    if (!c) return;
    const f = $("#form-cliente");
    ["idCliente", "nombre", "dni", "telefono", "correo"].forEach(k => f.elements[k].value = c[k] ?? "");
    f.elements.password.value = "";
    f.elements.password.required = false;
    $("#campo-password").hidden = true;
    $("#titulo-cliente").textContent = "Actualizar cliente #" + id;
    f.scrollIntoView({behavior: "smooth"});
}
async function guardarCliente(evento) {
    evento.preventDefault();
    const f = evento.currentTarget;
    const d = datosFormulario(f);
    const id = d.idCliente;
    delete d.idCliente;
    if (id) delete d.password;
    try {
        const r = await api(id ? `/clientes/${id}` : "/clientes", {method: id ? "PUT" : "POST", body: JSON.stringify(d)});
        limpiarCliente();
        await cargarClientes();
        notificar(r.mensaje);
    } catch (e) { notificar(e.message, true); }
}
async function cargarMediciones() {
    const id = $("#medicion-cliente").value;
    if (!id) { $("#tabla-mediciones").innerHTML = "<tr><td colspan='5'>Selecciona un cliente.</td></tr>"; return; }
    const filas = await api(`/clientes/${id}/mediciones`);
    const bloque = (m, sufijo) => [m[`esfera${sufijo}`], m[`cilindro${sufijo}`], m[`eje${sufijo}`], m[`adicion${sufijo}`]].map(x => seguro(x ?? "—")).join(" / ");
    $("#tabla-mediciones").innerHTML = filas.map(m => `<tr><td>${m.idMedicion}</td><td>${seguro(m.fechaMedicion)}</td><td>${bloque(m,"OD")}</td><td>${bloque(m,"OI")}</td><td>${seguro(m.observaciones)}</td></tr>`).join("") || "<tr><td colspan='5'>No hay mediciones registradas.</td></tr>";
}
async function guardarMedicion(evento) {
    evento.preventDefault();
    const id = $("#medicion-cliente").value;
    if (!id) { notificar("Selecciona un cliente antes de guardar.", true); return; }
    const d = datosFormulario(evento.currentTarget);
    for (const k of ["esferaOD", "cilindroOD", "ejeOD", "adicionOD", "esferaOI", "cilindroOI", "ejeOI", "adicionOI"]) {
        d[k] = d[k] === "" ? null : Number(d[k]);
    }
    try {
        const r = await api(`/clientes/${id}/mediciones`, {method: "POST", body: JSON.stringify(d)});
        evento.currentTarget.reset();
        evento.currentTarget.elements.fechaMedicion.value = new Date().toLocaleDateString("en-CA");
        await cargarMediciones();
        notificar(r.mensaje);
    } catch (e) { notificar(e.message, true); }
}
function opcionesProductos(seleccionado = "") {
    return `<option value="">Selecciona producto</option>` + estado.productos.filter(p => p.activo && p.stock > 0).map(p => `<option value="${p.idProducto}" ${Number(seleccionado) === Number(p.idProducto) ? "selected" : ""}>${seguro(p.nombre)} · ${dinero(p.precio)} · ${p.stock} uds.</option>`).join("");
}
function agregarLinea() {
    const linea = document.createElement("div");
    linea.className = "linea";
    linea.innerHTML = `<label>Producto<select class="linea-producto" required>${opcionesProductos()}</select></label><label>Cantidad<input type="number" class="linea-cantidad" min="1" step="1" value="1" required></label><button type="button" class="boton boton-secundario quitar-linea">Quitar</button>`;
    $("#lineas-venta").append(linea);
}
function actualizarLineas() {
    $$(".linea-producto").forEach(select => {
        const elegido = select.value;
        select.innerHTML = opcionesProductos(elegido);
        select.value = elegido;
    });
}
async function cargarMedicionesVenta() {
    const id = $("#venta-cliente").value;
    const select = $("#venta-medicion");
    select.innerHTML = `<option value="">Sin medición</option>`;
    if (!id) return;
    const filas = await api(`/clientes/${id}/mediciones`);
    select.innerHTML += filas.map(m => `<option value="${m.idMedicion}">#${m.idMedicion} · ${seguro(m.fechaMedicion)}</option>`).join("");
}
async function guardarVenta(evento) {
    evento.preventDefault();
    const cliente = $("#venta-cliente").value;
    const medicion = $("#venta-medicion").value;
    const items = $$("#lineas-venta .linea").map(l => ({idProducto: Number(l.querySelector(".linea-producto").value), cantidad: Number(l.querySelector(".linea-cantidad").value)}));
    if (!cliente || !items.length || items.some(i => !i.idProducto || i.cantidad < 1)) {
        notificar("Selecciona un cliente y al menos un producto válido.", true); return;
    }
    if (new Set(items.map(i => i.idProducto)).size !== items.length) {
        notificar("No repitas el mismo producto en distintas líneas.", true); return;
    }
    if (!confirm("¿Confirmar la venta y descontar las existencias del inventario?")) return;
    try {
        const r = await api("/ventas", {method: "POST", body: JSON.stringify({idCliente: Number(cliente), idMedicion: medicion ? Number(medicion) : null, items})});
        $("#form-venta").reset();
        $("#lineas-venta").innerHTML = "";
        agregarLinea();
        await Promise.all([cargarVentas(), cargarProductos()]);
        notificar(`Venta #${r.idVenta} registrada. Total: ${dinero(r.total)}.`);
    } catch (e) { notificar(e.message, true); }
}
async function cargarVentas() {
    estado.ventas = await api("/ventas");
    $("#tabla-ventas").innerHTML = estado.ventas.map(v => `<tr><td>${v.idVenta}</td><td>${seguro(v.fechaVenta)}</td><td>${seguro(v.cliente)}</td><td>${dinero(v.subtotal)}</td><td>${dinero(v.impuesto)}</td><td>${dinero(v.total)}</td><td>${seguro(v.estado)}</td><td><button class="boton boton-secundario" type="button" data-detalle="${v.idVenta}">Ver</button></td></tr>`).join("") || "<tr><td colspan='8'>No hay ventas registradas.</td></tr>";
}
async function verDetalle(id) {
    try {
        const filas = await api(`/ventas/${id}/detalles`);
        const contenedor = $("#detalle-venta");
        contenedor.hidden = false;
        contenedor.innerHTML = `<strong>Detalle de venta #${id}</strong><ul>${filas.map(f => `<li>${seguro(f.producto)} · ${f.cantidad} × ${dinero(f.precioUnitario)} = ${dinero(f.subtotal)}</li>`).join("")}</ul>`;
        contenedor.scrollIntoView({behavior: "smooth", block: "nearest"});
    } catch (e) { notificar(e.message, true); }
}
document.addEventListener("click", e => {
    const nav = e.target.closest("[data-seccion]");
    const ir = e.target.closest("[data-ir]");
    if (nav) mostrarSeccion(nav.dataset.seccion);
    if (ir) mostrarSeccion(ir.dataset.ir);
    const p = e.target.closest("[data-editar-producto]");
    if (p) editarProducto(Number(p.dataset.editarProducto));
    const c = e.target.closest("[data-editar-cliente]");
    if (c) editarCliente(Number(c.dataset.editarCliente));
    const d = e.target.closest("[data-detalle]");
    if (d) verDetalle(Number(d.dataset.detalle));
    if (e.target.closest(".quitar-linea")) e.target.closest(".linea").remove();
});
$("#form-producto").addEventListener("submit", guardarProducto);
$("#form-cliente").addEventListener("submit", guardarCliente);
$("#form-medicion").addEventListener("submit", guardarMedicion);
$("#form-venta").addEventListener("submit", guardarVenta);
$("#cancelar-producto").addEventListener("click", limpiarProducto);
$("#cancelar-cliente").addEventListener("click", limpiarCliente);
$("#filtro-productos").addEventListener("input", dibujarProductos);
$("#refrescar").addEventListener("click", () => actualizarResumen().catch(e => notificar(e.message, true)));
$("#medicion-cliente").addEventListener("change", () => cargarMediciones().catch(e => notificar(e.message, true)));
$("#venta-cliente").addEventListener("change", () => cargarMedicionesVenta().catch(e => notificar(e.message, true)));
$("#agregar-linea").addEventListener("click", agregarLinea);
$("#form-medicion [name=fechaMedicion]").value = new Date().toLocaleDateString("en-CA");
(async () => {
    try {
        await cargarCsrf();
        await actualizarResumen();
        await Promise.all([cargarProductos(), cargarClientes(), cargarVentas()]);
        agregarLinea();
    } catch (e) { notificar(e.message, true); }
})();
