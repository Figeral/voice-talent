package com.voicetalent.backend.service;

import com.voicetalent.backend.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

  private static final Logger log = LoggerFactory.getLogger(EmailService.class);

  private final AppProperties props;
  private final ObjectProvider<JavaMailSender> mailSenderProvider;

  public EmailService(AppProperties props, ObjectProvider<JavaMailSender> mailSenderProvider) {
    this.props = props;
    this.mailSenderProvider = mailSenderProvider;
  }

  public void sendRegistrationConfirmation(String toEmail, String firstName, String dossierNo) {
    if (!props.mail().enabled()) {
      log.info("Mail disabled — skipping confirmation email to {} (dossier {})", toEmail, dossierNo);
      return;
    }
    JavaMailSender sender = mailSenderProvider.getIfAvailable();
    if (sender == null) {
      log.warn("Mail enabled but no JavaMailSender configured");
      return;
    }
    try {
      SimpleMailMessage msg = new SimpleMailMessage();
      msg.setFrom(props.mail().from());
      msg.setTo(toEmail);
      msg.setSubject("Voice Talent Douala 2026 – Confirmation de candidature (" + dossierNo + ")");
      msg.setText(
          "Bonjour " + firstName + ",\n\n"
              + "Votre inscription à Voice Talent – Édition spéciale Douala 2026 est bien enregistrée.\n"
              + "Votre numéro de dossier : " + dossierNo + "\n\n"
              + "Conservez ce numéro pour suivre l'avancement de votre candidature sur le site officiel.\n"
              + "Le casting en ligne aura lieu le 28 novembre 2026.\n\n"
              + "L'équipe Voice Talent Douala 2026"
      );
      sender.send(msg);
    } catch (Exception ex) {
      log.error("Failed to send confirmation email to {} for dossier {}", toEmail, dossierNo, ex);
    }
  }
}
