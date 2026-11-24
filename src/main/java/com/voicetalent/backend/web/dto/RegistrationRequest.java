package com.voicetalent.backend.web.dto;

import com.voicetalent.backend.domain.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record RegistrationRequest(
    @NotBlank(message = "Nom requis")
    @Size(max = 80, message = "Nom : 80 caractères maximum")
    String lastName,

    @NotBlank(message = "Prénom requis")
    @Size(max = 80, message = "Prénom : 80 caractères maximum")
    String firstName,

    @Size(max = 80, message = "80 caractères maximum")
    String stageName,

    @NotNull(message = "Date de naissance invalide")
    LocalDate birthDate,

    @NotNull(message = "Indiquez votre sexe")
    Gender gender,

    @NotBlank(message = "Téléphone requis")
    String phone,

    @NotBlank(message = "E-mail requis")
    @Email(message = "Adresse e-mail invalide")
    @Size(max = 160, message = "Adresse e-mail trop longue")
    String email,

    @NotBlank(message = "Ville requis")
    @Size(max = 80, message = "Ville : 80 caractères maximum")
    String city,

    @NotBlank(message = "Quartier requis")
    @Size(max = 80, message = "Quartier : 80 caractères maximum")
    String district,

    @NotBlank(message = "Personne à contacter requis")
    @Size(max = 80, message = "Personne à contacter : 80 caractères maximum")
    String contactName,

    @NotBlank(message = "Téléphone de la personne à contacter requis")
    String contactPhone,

    @NotNull(message = "Ajoutez votre photo de portrait")
    UUID photoUploadId,

    @NotBlank(message = "Genre musical requis")
    @Size(max = 60, message = "Genre musical : 60 caractères maximum")
    String musicGenre,

    @NotBlank(message = "Chanson envisagée requis")
    @Size(max = 150, message = "Chanson envisagée : 150 caractères maximum")
    String songTitle,

    @NotBlank(message = "Parcours musical requis")
    @Size(max = 1000, message = "Parcours musical : 1000 caractères maximum")
    String musicBackground,

    @Valid
    SocialLinks socialLinks,

    @NotNull(message = "Ajoutez votre vidéo de prestation")
    UUID presentationUploadId,

    @AssertTrue(message = "Vous devez accepter le règlement et la politique de confidentialité")
    boolean acceptRules,

    String website,
    String turnstileToken
) {
  public record SocialLinks(
      @Size(max = 200, message = "Lien invalide") String facebook,
      @Size(max = 200, message = "Lien invalide") String instagram,
      @Size(max = 200, message = "Lien invalide") String tiktok,
      @Size(max = 200, message = "Lien invalide") String youtube
  ) {}
}
