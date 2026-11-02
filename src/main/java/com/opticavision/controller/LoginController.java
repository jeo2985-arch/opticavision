package com.opticavision.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LoginController {

    @GetMapping(value = "/login", produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String login(
            @RequestParam(name = "error", required = false) String error,
            @RequestParam(name = "logout", required = false) String logout,
            HttpServletRequest request
    ) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken == null) {
            throw new IllegalStateException("No se pudo obtener el token CSRF del formulario.");
        }
        String mensaje = "";
        if (error != null) {
            mensaje = "<div class='alerta error'>Correo o contraseña incorrectos.</div>";
        } else if (logout != null) {
            mensaje = "<div class='alerta ok'>Sesión cerrada correctamente.</div>";
        }

        return """
                <!doctype html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width,initial-scale=1">
                    <title>Iniciar sesión | ÓpticaVision</title>
                    <style>
                        :root{
                            --azul:#123b65;
                            --azul-2:#0f3154;
                            --celeste:#147ba5;
                            --fondo:#eef3f8;
                            --texto:#16304c;
                            --muted:#5f738c;
                            --borde:#d8e1eb;
                            --error-bg:#fdecec;
                            --error-text:#ad2f2f;
                            --ok-bg:#e9f8ef;
                            --ok-text:#1d6d3f;
                            --blanco:#ffffff;
                        }
                        *{box-sizing:border-box}
                        body{
                            margin:0;
                            font-family:system-ui,-apple-system,Segoe UI,Roboto,Arial,sans-serif;
                            background:var(--fondo);
                            color:var(--texto);
                        }
                        .barra-superior{
                            background:var(--azul);
                            color:var(--blanco);
                            padding:22px 0;
                            box-shadow:0 2px 10px rgba(16,45,78,.15);
                        }
                        .contenedor{
                            width:min(1180px,92vw);
                            margin:0 auto;
                        }
                        .marca{
                            display:flex;
                            align-items:center;
                            gap:14px;
                            font-weight:800;
                            font-size:2rem;
                            letter-spacing:-.02em;
                        }
                        .marca-icono{
                            font-size:1.8rem;
                            line-height:1;
                        }
                        .hero{
                            min-height:calc(100vh - 92px);
                            display:grid;
                            grid-template-columns:1.1fr .9fr;
                            gap:48px;
                            align-items:center;
                            padding:48px 0 60px;
                        }
                        .presentacion h1{
                            margin:0 0 20px;
                            font-size:clamp(2.5rem,5vw,4.7rem);
                            line-height:1.05;
                            letter-spacing:-.04em;
                        }
                        .presentacion p{
                            font-size:1.3rem;
                            line-height:1.7;
                            color:var(--muted);
                            max-width:650px;
                            margin:0 0 28px;
                        }
                        .acciones-publicas{
                            display:flex;
                            flex-wrap:wrap;
                            gap:14px;
                        }
                        .boton-enlace{
                            display:inline-block;
                            text-decoration:none;
                            background:var(--celeste);
                            color:var(--blanco);
                            padding:14px 22px;
                            border-radius:12px;
                            font-weight:700;
                        }
                        .tarjeta{
                            background:var(--blanco);
                            border:1px solid var(--borde);
                            border-radius:24px;
                            padding:34px;
                            box-shadow:0 20px 45px rgba(18,59,101,.10);
                        }
                        .tarjeta h2{
                            margin:0 0 8px;
                            font-size:2rem;
                        }
                        .tarjeta p{
                            margin:0 0 24px;
                            color:var(--muted);
                        }
                        .campo{margin-bottom:18px}
                        .campo label{
                            display:block;
                            font-weight:700;
                            margin-bottom:8px;
                        }
                        .campo input{
                            width:100%;
                            padding:15px 16px;
                            border:1px solid #c5d3e2;
                            border-radius:12px;
                            font-size:1rem;
                            outline:none;
                            background:#fbfdff;
                        }
                        .campo input:focus{
                            border-color:var(--celeste);
                            box-shadow:0 0 0 4px rgba(20,123,165,.12);
                        }
                        .boton{
                            width:100%;
                            border:none;
                            background:var(--celeste);
                            color:var(--blanco);
                            padding:15px 18px;
                            border-radius:12px;
                            font-size:1.05rem;
                            font-weight:800;
                            cursor:pointer;
                            margin-top:6px;
                        }
                        .alerta{
                            border-radius:12px;
                            padding:14px 16px;
                            margin:0 0 18px;
                            font-weight:600;
                        }
                        .alerta.error{background:var(--error-bg); color:var(--error-text);}
                        .alerta.ok{background:var(--ok-bg); color:var(--ok-text);}
                        .pie{
                            text-align:center;
                            color:#789;
                            padding:10px 0 28px;
                            font-size:1rem;
                        }
                        @media (max-width: 920px){
                            .hero{grid-template-columns:1fr; gap:28px; padding-top:34px}
                            .marca{font-size:1.7rem}
                            .presentacion h1{font-size:clamp(2.2rem,9vw,3.6rem)}
                            .presentacion p{font-size:1.08rem}
                            .tarjeta{padding:24px}
                        }
                    </style>
                </head>
                <body>
                    <header class="barra-superior">
                        <div class="contenedor">
                            <div class="marca">
                                <span class="marca-icono">👓</span>
                                <span>ÓpticaVision</span>
                            </div>
                        </div>
                    </header>

                    <main class="contenedor hero">
                        <section class="presentacion">
                            <h1>Bienvenido a ÓpticaVision</h1>
                            <p>Sistema de gestión de óptica, clientes, inventario, mediciones y ventas.</p>
                            <div class="acciones-publicas">
                                <a class="boton-enlace" href="/">Ir a la portada</a>
                            </div>
                        </section>

                        <section class="tarjeta">
                            <h2>Iniciar sesión</h2>
                            <p>Accede con tu correo y contraseña para ingresar al sistema.</p>
                            %s
                            <form method="post" action="/login">
                                <input type="hidden" name="%s" value="%s">
                                <div class="campo">
                                    <label for="username">Correo</label>
                                    <input id="username" name="username" type="email" autocomplete="username" placeholder="correo@ejemplo.com" required>
                                </div>
                                <div class="campo">
                                    <label for="password">Contraseña</label>
                                    <input id="password" name="password" type="password" autocomplete="current-password" placeholder="Ingresa tu contraseña" required>
                                </div>
                                <button class="boton" type="submit">Entrar al sistema</button>
                            </form>
                        </section>
                    </main>

                    <div class="pie">ÓpticaVision</div>
                </body>
                </html>
                """.formatted(mensaje, csrfToken.getParameterName(), csrfToken.getToken());
    }
}
