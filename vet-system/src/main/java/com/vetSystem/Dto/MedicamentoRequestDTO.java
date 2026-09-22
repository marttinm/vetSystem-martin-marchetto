package com.vetSystem.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Schema(description = "Datos para registrar o actualizar un medicamento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicamentoRequestDTO {

    @Schema(description = "Nombre comercial", example = "Amoxicilina 500")
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @Schema(description = "Principio activo", example = "Amoxicilina")
    @NotBlank(message = "El principio activo es obligatorio")
    private String principioActivo;

    @Schema(description = "Cantidad disponible", example = "20")
    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @Schema(description = "Precio por unidad", example = "1500.00")
    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio unitario debe ser mayor a 0")
    private BigDecimal precioUnitario;
}
