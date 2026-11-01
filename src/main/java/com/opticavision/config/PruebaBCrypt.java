
package com.opticavision.config;

import com.opticavision.service.SeguridadPassword;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PruebaBCrypt implements CommandLineRunner {

    private final SeguridadPassword seguridadPassword;

    public PruebaBCrypt(SeguridadPassword seguridadPassword) {
        this.seguridadPassword = seguridadPassword;
    }

    @Override
    public void run(String... args) {

        System.out.println("==============================");
        System.out.println("PRUEBAS DE SEGURIDAD BCrypt");
        System.out.println("==============================");

        try {

            String passwordCorrecta = "PruebaSegura#2026";
            String passwordIncorrecta = "PasswordIncorrecta";

            // PRUEBA 1: GENERAR HASH
            String hash = seguridadPassword.encriptarPassword(
                    passwordCorrecta
            );

            boolean hashValido = hash.startsWith("$2")
                    && hash.length() == 60;

            System.out.println(
                    (hashValido ? "[OK]" : "[ERROR]")
                    + " 1. Generacion de hash BCrypt"
            );

            // PRUEBA 2: PASSWORD CORRECTA
            boolean accesoCorrecto =
                    seguridadPassword.verificarPassword(
                            passwordCorrecta, hash
                    );

            System.out.println(
                    (accesoCorrecto ? "[OK]" : "[ERROR]")
                    + " 2. Password correcta aceptada"
            );

            // PRUEBA 3: PASSWORD INCORRECTA
            boolean accesoIncorrecto =
                    seguridadPassword.verificarPassword(
                            passwordIncorrecta, hash
                    );

            System.out.println(
                    (!accesoIncorrecto ? "[OK]" : "[ERROR]")
                    + " 3. Password incorrecta rechazada"
            );

            if (hashValido && accesoCorrecto
                    && !accesoIncorrecto) {

                System.out.println("==============================");
                System.out.println("TODAS LAS PRUEBAS EXITOSAS");
                System.out.println("==============================");

            } else {

                System.out.println(
                        "UNA O MAS PRUEBAS FALLARON"
                );
            }

        } catch (Exception e) {

            System.err.println(
                    "[ERROR] Prueba BCrypt: "
                    + e.getClass().getSimpleName()
            );

            System.err.println(e.getMessage());
        }
    }
}
