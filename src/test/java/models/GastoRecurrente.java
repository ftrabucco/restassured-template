package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Model class for GastoRecurrente (Recurring Expense) entity
 * Demonstrates Builder pattern implementation
 */
public class GastoRecurrente {
    @JsonIgnore
    private Long id;
    
    @JsonProperty("descripcion")
    private String descripcion;
    
    @JsonProperty("monto")
    private BigDecimal monto;
    
    @JsonProperty("dia_de_pago")
    private Integer diaDePago;
    
    @JsonProperty("fecha_inicio")
    private LocalDate fechaInicio;
    
    @JsonProperty("frecuencia_gasto_id")
    private Long frecuenciaGastoId;
    
    @JsonProperty("categoria_gasto_id")
    private Long categoriaGastoId;
    
    @JsonProperty("importancia_gasto_id")
    private Long importanciaGastoId;
    
    @JsonProperty("tipo_pago_id")
    private Long tipoPagoId;
    
    @JsonProperty("tarjeta_id")
    private Long tarjetaId;
    
    @JsonProperty("activo")
    private Boolean activo;
    
    @JsonIgnore
    private LocalDate ultimaFechaGenerado;

    // Default constructor
    public GastoRecurrente() {}

    // Constructor with Builder
    private GastoRecurrente(Builder builder) {
        this.id = builder.id;
        this.descripcion = builder.descripcion;
        this.monto = builder.monto;
        this.diaDePago = builder.diaDePago;
        this.fechaInicio = builder.fechaInicio;
        this.frecuenciaGastoId = builder.frecuenciaGastoId;
        this.categoriaGastoId = builder.categoriaGastoId;
        this.importanciaGastoId = builder.importanciaGastoId;
        this.tipoPagoId = builder.tipoPagoId;
        this.tarjetaId = builder.tarjetaId;
        this.activo = builder.activo;
        this.ultimaFechaGenerado = builder.ultimaFechaGenerado;
    }

    // Getters
    public Long getId() { return id; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getMonto() { return monto; }
    public Integer getDiaDePago() { return diaDePago; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public Long getFrecuenciaGastoId() { return frecuenciaGastoId; }
    public Long getCategoriaGastoId() { return categoriaGastoId; }
    public Long getImportanciaGastoId() { return importanciaGastoId; }
    public Long getTipoPagoId() { return tipoPagoId; }
    public Long getTarjetaId() { return tarjetaId; }
    public Boolean getActivo() { return activo; }
    public LocalDate getUltimaFechaGenerado() { return ultimaFechaGenerado; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public void setDiaDePago(Integer diaDePago) { this.diaDePago = diaDePago; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public void setFrecuenciaGastoId(Long frecuenciaGastoId) { this.frecuenciaGastoId = frecuenciaGastoId; }
    public void setCategoriaGastoId(Long categoriaGastoId) { this.categoriaGastoId = categoriaGastoId; }
    public void setImportanciaGastoId(Long importanciaGastoId) { this.importanciaGastoId = importanciaGastoId; }
    public void setTipoPagoId(Long tipoPagoId) { this.tipoPagoId = tipoPagoId; }
    public void setTarjetaId(Long tarjetaId) { this.tarjetaId = tarjetaId; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public void setUltimaFechaGenerado(LocalDate ultimaFechaGenerado) { this.ultimaFechaGenerado = ultimaFechaGenerado; }

    // Builder pattern implementation
    public static class Builder {
        private Long id;
        private String descripcion;
        private BigDecimal monto;
        private Integer diaDePago;
        private LocalDate fechaInicio;
        private Long frecuenciaGastoId;
        private Long categoriaGastoId;
        private Long importanciaGastoId;
        private Long tipoPagoId;
        private Long tarjetaId;
        private Boolean activo;
        private LocalDate ultimaFechaGenerado;

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

        public Builder diaDePago(Integer diaDePago) {
            this.diaDePago = diaDePago;
            return this;
        }

        public Builder fechaInicio(LocalDate fechaInicio) {
            this.fechaInicio = fechaInicio;
            return this;
        }

        public Builder frecuenciaGastoId(Long frecuenciaGastoId) {
            this.frecuenciaGastoId = frecuenciaGastoId;
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

        public Builder tarjetaId(Long tarjetaId) {
            this.tarjetaId = tarjetaId;
            return this;
        }

        public Builder activo(Boolean activo) {
            this.activo = activo;
            return this;
        }

        public Builder ultimaFechaGenerado(LocalDate ultimaFechaGenerado) {
            this.ultimaFechaGenerado = ultimaFechaGenerado;
            return this;
        }

        public GastoRecurrente build() {
            return new GastoRecurrente(this);
        }
    }

    @Override
    public String toString() {
        return "GastoRecurrente{" +
                "id=" + id +
                ", descripcion='" + descripcion + '\'' +
                ", monto=" + monto +
                ", diaDePago=" + diaDePago +
                ", fechaInicio=" + fechaInicio +
                ", frecuenciaGastoId=" + frecuenciaGastoId +
                ", categoriaGastoId=" + categoriaGastoId +
                ", importanciaGastoId=" + importanciaGastoId +
                ", tipoPagoId=" + tipoPagoId +
                ", tarjetaId=" + tarjetaId +
                ", activo=" + activo +
                ", ultimaFechaGenerado=" + ultimaFechaGenerado +
                '}';
    }
}
