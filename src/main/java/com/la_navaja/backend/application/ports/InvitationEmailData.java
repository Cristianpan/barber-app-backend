package com.la_navaja.backend.application.ports;

/** Datos necesarios para renderizar el correo de invitación a un empleado nuevo. */
public record InvitationEmailData(String firstName, String invitationLink) {}
