package com.la_navaja.backend.infrastructure.controllers;

import com.la_navaja.backend.application.dtos.request.SetStoreInfoRequest;
import com.la_navaja.backend.application.dtos.response.SetStoreInfoResponse;
import com.la_navaja.backend.application.services.StoreInfoService;
import com.la_navaja.backend.domain.constants.ErrorMessages;
import com.la_navaja.backend.infrastructure.controllers.advice.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Store")
@RestController
@RequestMapping("/store")
@RequiredArgsConstructor
public class StoreInfoController {

  private final StoreInfoService storeInfoService;

  @Operation(
      summary = "Set the business information of the store",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Store information saved successfully",
        content = @Content(schema = @Schema(implementation = SetStoreInfoResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = ErrorMessages.INVALID_REQUEST_MESSAGE,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = ErrorMessages.UNAUTHORIZED_MESSAGE,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = ErrorMessages.FORBIDDEN_MESSAGE,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = ErrorMessages.INTERNAL_ERROR_MESSAGE,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PutMapping("/info")
  public ResponseEntity<SetStoreInfoResponse> setStoreInfo(
      @Valid @RequestBody SetStoreInfoRequest request) {
    return ResponseEntity.ok(storeInfoService.setStoreInfo(request));
  }
}
