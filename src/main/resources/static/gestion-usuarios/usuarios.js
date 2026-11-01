
const aviso = document.querySelector('#aviso');

const escapar = v => String(v ?? '').replace(
    /[&<>"']/g,
    c => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;'
    }[c])
);

let csrf = '';
let admin = false;

async function solicitar(url, opciones = {}) {
    const respuesta = await fetch(url, {
        credentials: 'same-origin',
        ...opciones
    });

    const texto = await respuesta.text();
    let datos = {};

    try {
        datos = JSON.parse(texto);
    } catch (e) {
        datos = {};
    }

    if (!respuesta.ok) {
        throw new Error(
            datos.detail ||
            datos.message ||
            `Error HTTP ${respuesta.status}`
        );
    }

    return datos;
}

async function cargar() {
    const usuarios = await solicitar(
        '/api/gestion-usuarios/vendedores'
    );

    const lista = document.querySelector('#lista');

    lista.innerHTML = usuarios.map(u => `
        <tr>
            <td>${escapar(u.id)}</td>
            <td>${escapar(u.nombre)}</td>
            <td>${escapar(u.correo)}</td>
            <td>${u.activo ? 'Sí' : 'No'}</td>
            <td>
                ${
                    admin
                    ? `
                        <label class="checkbox">
                            <input
                                type="checkbox"
                                data-id="${Number(u.id)}"
                                ${u.puedeCrearUsuarios ? 'checked' : ''}
                                aria-label="Permitir creación de usuarios a ${escapar(u.nombre)}"
                            >
                            Autorizar
                        </label>
                    `
                    : (u.puedeCrearUsuarios ? 'Sí' : 'No')
                }
            </td>
        </tr>
    `).join('') || `
        <tr>
            <td colspan="5">
                Sin vendedores registrados
            </td>
        </tr>
    `;
}

async function iniciar() {
    try {
        const respuestas = await Promise.all([
            solicitar('/api/vendedor/csrf'),
            solicitar('/api/gestion-usuarios/contexto')
        ]);

        csrf = respuestas[0].token;
        admin = respuestas[1].admin === true;

        const permitir = document.querySelector('#permitir');
        const casilla = document.querySelector('#permitir input');

        permitir.hidden = !admin;
        permitir.style.display = admin ? '' : 'none';
        casilla.disabled = !admin;

        await cargar();

    } catch (error) {
        aviso.textContent = error.message;

        document.querySelector(
            '#form-usuarios button'
        ).disabled = true;
    }
}

document.querySelector('#form-usuarios').addEventListener(
    'submit',
    async e => {
        e.preventDefault();

        const formulario = e.currentTarget;
        const boton = formulario.querySelector('button');

        boton.disabled = true;

        const datos = Object.fromEntries(
            new FormData(formulario)
        );

        datos.puedeCrearUsuarios =
            admin &&
            formulario.elements.puedeCrearUsuarios.checked;

        try {
            const resultado = await solicitar(
                '/api/gestion-usuarios/vendedores',
                {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-CSRF-TOKEN': csrf
                    },
                    body: JSON.stringify(datos)
                }
            );

            aviso.textContent = resultado.mensaje;

            formulario.reset();

            await cargar();

        } catch (error) {
            aviso.textContent = error.message;

        } finally {
            boton.disabled = false;
        }
    }
);

document.querySelector('#lista').addEventListener(
    'change',
    async e => {
        if (!admin || !e.target.matches('[data-id]')) {
            return;
        }

        const input = e.target;
        input.disabled = true;

        try {
            const resultado = await solicitar(
                `/api/gestion-usuarios/vendedores/${Number(input.dataset.id)}/permiso`,
                {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-CSRF-TOKEN': csrf
                    },
                    body: JSON.stringify({
                        puedeCrearUsuarios: input.checked
                    })
                }
            );

            aviso.textContent = resultado.mensaje;

        } catch (error) {
            input.checked = !input.checked;
            aviso.textContent = error.message;

        } finally {
            input.disabled = false;
        }
    }
);

iniciar();
