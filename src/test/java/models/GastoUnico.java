package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Model class for GastoUnico (One-time Expense) entity
 * Demonstrates Builder pattern implementation
 */
public class GastoUnico {
    @JsonIgnore
    private Long id;
    
    @JsonProperty("descripcion")
    private String descripcion;
    
    @JsonProperty("monto")
    private BigDecimal monto;
    
    @JsonProperty("fecha")
    private LocalDate fecha;
    
    @JsonProperty("categoria_gasto_id")
    private Long categoriaGastoId;
    
    @JsonProperty("importancia_gasto_id")
    private Long importanciaGastoId;
    
    @JsonProperty("tipo_pago_id")
    private Long tipoPagoId;

    // Default constructor
    public GastoUnico() {}

    // Constructor with Builder
    private GastoUnico(Builder builder) {
        this.id = builder.id;
        this.descripcion = builder.descripcion;
        this.monto = builder.monto;
        this.fecha = builder.fecha;
        this.categoriaGastoId = builder.categoriaGastoId;
        this.importanciaGastoId = builder.importanciaGastoId;
        this.tipoPagoId = builder.tipoPagoId;
    }

    // Getters
    public Long getId() { return id; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getMonto() { return monto; }
    public LocalDate getFecha() { return fecha; }
    public Long getCategoriaGastoId() { return categoriaGastoId; }
    public Long getImportanciaGastoId() { return importanciaGastoId; }
    public Long getTipoPagoId() { return tipoPagoId; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public void setCategoriaGastoId(Long categoriaGastoId) { this.categoriaGastoId = categoriaGastoId; }
    public void setImportanciaGastoId(Long importanciaGastoId) { this.importanciaGastoId = importanciaGastoId; }
    public void setTipoPagoId(Long tipoPagoId) { this.tipoPagoId = tipoPagoId; }

    // Builder pattern implementation
    public static class Builder {
        private Long id;
        private String descripcion;
        private BigDecimal monto;
        private LocalDate fecha;
        private Long categoriaGastoId;
        private Long importanciaGastoId;
        private Long tipoPagoId;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder descripcion(String descripcion) {
            this.descripcion = descripcion;
            return this;
        }

        public Builder monto(BigDecimal monto) {
            this.monto = monto;
            return this;
        }

        public Builder fecha(LocalDate fecha) {
            this.fecha = fecha;
            return this;
        }

        public Builder categoriaGastoId(Long categoriaGastoId) {
            this.categoriaGastoId = categoriaGastoId;
            return this;
        }

        public Builder importanciaGastoId(Long importanciaGastoId) {
            this.importanciaGastoId = importanciaGastoId;
            return this;
        }

        public Builder tipoPagoId(Long tipoPagoId) {
            this.tipoPagoId = tipoPagoId;
            return this;
        }

        public GastoUnico build() {
            return new GastoUnico(this);
        }
    }

    @Override
    public String toString() {
        return "GastoUnico{" +
                "id=" + id +
                ", descripcion='" + descripcion + '\'' +
                ", monto=" + monto +
                ", fecha=" + fecha +
                ", categoriaGastoId=" + categoriaGastoId +
                ", importanciaGastoId=" + importanciaGastoId +
                ", tipoPagoId=" + tipoPagoId +
                '}';
    }
}
