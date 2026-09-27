package com.la_navaja.backend.infrastructure.adapters.mail;

import com.la_navaja.backend.application.ports.EmailDefinition;
import com.la_navaja.backend.application.ports.InvitationEmailData;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class InvitationEmail implements EmailDefinition<InvitationEmailData> {

  @Override
  public String template() {
    return "invitation";
  }

  @Override
  public String subject(InvitationEmailData data) {
    return "Bienvenido/a, " + data.firstName() + " — establece tu contraseña";
  }

  @Override
  public Map<String, Object> toModel(InvitationEmailData data) {
    return Map.of("firstName", data.firstName(), "invitationLink", data.invitationLink());
  }
}
