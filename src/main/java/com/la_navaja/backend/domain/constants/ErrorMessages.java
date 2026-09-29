package com.la_navaja.backend.domain.constants;

public final class ErrorMessages {

  private ErrorMessages() {}

  public static final String INVALID_CREDENTIALS_MESSAGE =
      "El correo o la contraseña son invalidos";
  public static final String UNAUTHORIZED_MESSAGE = "No autorizado";
  public static final String INVALID_REQUEST_MESSAGE = "Solicitud inválida";
  public static final String INTERNAL_ERROR_MESSAGE = "Error interno";
  public static final String EMAIL_ALREADY_EXISTS_MESSAGE = "El correo ya está registrado";
  public static final String FORBIDDEN_MESSAGE = "Acceso denegado";
  public static final String INVITATION_TOKEN_NOT_FOUND_MESSAGE =
      "El token de invitación no ha sido encontrado";
  public static final String INVITATION_TOKEN_INVALID_MESSAGE =
      "El token de invitación no es válido o ha expirado";
  public static final String SERVICE_NAME_ALREADY_EXISTS_MESSAGE =
      "El nombre del servicio ya está registrado";
  public static final String INVALID_STORE_SCHEDULE_MESSAGE = "El horario enviado no es válido";
}
