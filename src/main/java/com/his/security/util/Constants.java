package com.his.security.util;

public class Constants {

    public static class Roles {
        public static final String ADMIN = "Administrador";
        public static final String MEDICO = "Médico";
        public static final String RECEPCIONISTA = "Recepcionista";
    }

    public static class Permisos {
        public static final String PACIENTE_VER = "paciente:ver";
        public static final String PACIENTE_CREAR = "paciente:crear";
        public static final String PACIENTE_EDITAR = "paciente:editar";
        public static final String CITA_VER = "cita:ver";
        public static final String CITA_CREAR = "cita:crear";
        public static final String CITA_EDITAR = "cita:editar";
    }

    public static class Mensajes {
        public static final String ERROR_USUARIO_NO_ENCONTRADO = "¡Lo sentimos! No pudimos encontrar tu usuario :/";
        public static final String ERROR_CREDENCIALES = "¡Lo sentimos! No pudimos validar tus credenciales :/";
        public static final String USUARIO_REGISTRADO = "Usuario registrado correctamente";
        public static final String LOGIN_EXITOSO = "¡Login exitoso!";
        public static final String ERROR_NO_CONTROLADO = "¡Lo sentimos! Ha ocurrido un error no controlado.";
        public static final String INFORMACION_INVALIDA = "¡Lo sentimos! No pudimos validar la información que nos enviaste. Por favor, revisa y vuelve a intentarlo.";
        public static final String USUARIO_EXISTENTE = "¡Lo sentimos! El e-mail que intentas ingresar ya existe. Por favor, revisa y vuelve a intentarlo.";
        public static final String USUARIO_INACTIVO = "¡Lo sentimos! No pudimos encontrar tu información, tal vez tu usuario está desactivado. Por favor, revisa y vuelve a intentarlo.";
        public static final String TOKEN_INVALIDO = "¡Lo sentimos! Tu sesión se ha expirado. Por favor, revisa y vuelve a intentarlo.";
        public static final String REFRESH_TOKEN_INVALIDO = "¡Lo sentimos! No es posible utilizar un refresh token como token de acceso.";
        public static final String LOGOUT_EXITOSO = "¡Excelente! Has cerrado sesión en todos los dispositivos.";
    }

    public enum EstadoUsuario {
        ACTIVO,
        INACTIVO
    }

    public static class Params {
        public static final Integer ACCESS_TOKEN_DURACION = 3600000;
        public static final Integer REFRESH_TOKEN_DURACION = 604800000;
        public static final Integer REFRESH_TOKEN_DIAS = 7;

    }
}

