package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Model class representing a Débito Automático (Automatic Debit) entity
 * Used for automatic recurring payments and debits
 */
public class DebitoAutomatico {
    @JsonIgnore
    private Long id;
    private String descripcion;
    private BigDecimal monto;
    
    @JsonProperty("dia_de_pago")
    private Integer diaDePago;
    
    @JsonIgnore
    @JsonProperty("mes_de_pago")
    private Integer mesDePago;
    
    @JsonProperty("categoria_gasto_id")
    private Long categoriaGastoId;
    
    @JsonProperty("importancia_gasto_id")
    private Long importanciaGastoId;
    
    @JsonProperty("frecuencia_gasto_id")
    private Long frecuenciaGastoId;
    
    @JsonProperty("tipo_pago_id")
    private Long tipoPagoId;
    
    @JsonProperty("tarjeta_id")
    private Long tarjetaId;
    
    private Boolean activo;
    
    @JsonIgnore
    @JsonProperty("ultima_fecha_generado")
    private String ultimaFechaGenerado;
    
    @JsonIgnore
    @JsonProperty("createdAt")
    private LocalDateTime fechaCreacion;
    
    @JsonIgnore
    @JsonProperty("updatedAt")
    private LocalDateTime fechaActualizacion;
    
    // Default constructor
    public DebitoAutomatico() {}
    
    // Constructor with required fields
    public DebitoAutomatico(String descripcion, BigDecimal monto, Integer diaDePago, 
                           Long categoriaGastoId, Long importanciaGastoId, 
                           Long frecuenciaGastoId, Long tipoPagoId) {
        this.descripcion = descripcion;
        this.monto = monto;
        this.diaDePago = diaDePago;
        this.categoriaGastoId = categoriaGastoId;
        this.importanciaGastoId = importanciaGastoId;
        this.frecuenciaGastoId = frecuenciaGastoId;
        this.tipoPagoId = tipoPagoId;
        this.activo = true; // Default to active
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    
    public Integer getDiaDePago() { return diaDePago; }
    public void setDiaDePago(Integer diaDePago) { this.diaDePago = diaDePago; }
    
    public Integer getMesDePago() { return mesDePago; }
    public void setMesDePago(Integer mesDePago) { this.mesDePago = mesDePago; }
    
    public Long getCategoriaGastoId() { return categoriaGastoId; }
    public void setCategoriaGastoId(Long categoriaGastoId) { this.categoriaGastoId = categoriaGastoId; }
    
    public Long getImportanciaGastoId() { return importanciaGastoId; }
    public void setImportanciaGastoId(Long importanciaGastoId) { this.importanciaGastoId = importanciaGastoId; }
    
    public Long getFrecuenciaGastoId() { return frecuenciaGastoId; }
    public void setFrecuenciaGastoId(Long frecuenciaGastoId) { this.frecuenciaGastoId = frecuenciaGastoId; }
    
    public Long getTipoPagoId() { return tipoPagoId; }
    public void setTipoPagoId(Long tipoPagoId) { this.tipoPagoId = tipoPagoId; }
    
    public Long getTarjetaId() { return tarjetaId; }
    public void setTarjetaId(Long tarjetaId) { this.tarjetaId = tarjetaId; }
    
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    
    public String getUltimaFechaGenerado() { return ultimaFechaGenerado; }
    public void setUltimaFechaGenerado(String ultimaFechaGenerado) { this.ultimaFechaGenerado = ultimaFechaGenerado; }
    
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    
    @Override
    public String toString() {
        return "DebitoAutomatico{" +
                "id=" + id +
                ", descripcion='" + descripcion + '\'' +
                ", monto=" + monto +
                ", diaDePago=" + diaDePago +
                ", categoriaGastoId=" + categoriaGastoId +
                ", importanciaGastoId=" + importanciaGastoId +
                ", frecuenciaGastoId=" + frecuenciaGastoId +
                ", tipoPagoId=" + tipoPagoId +
                ", tarjetaId=" + tarjetaId +
                ", activo=" + activo +
                '}';
    }
    
    /**
     * Builder pattern for creating DebitoAutomatico instances
     */
    public static class Builder {
        private DebitoAutomatico debitoAutomatico;
        
        public Builder() {
            this.debitoAutomatico = new DebitoAutomatico();
        }
        
        public Builder descripcion(String descripcion) {
            this.debitoAutomatico.setDescripcion(descripcion);
            return this;
        }
        
        public Builder monto(BigDecimal monto) {
            this.debitoAutomatico.setMonto(monto);
            return this;
        }
        
        public Builder diaDePago(Integer diaDePago) {
            this.debitoAutomatico.setDiaDePago(diaDePago);
            return this;
        }
        
        public Builder categoriaGastoId(Long categoriaGastoId) {
            this.debitoAutomatico.setCategoriaGastoId(categoriaGastoId);
            return this;
        }
        
        public Builder importanciaGastoId(Long importanciaGastoId) {
            this.debitoAutomatico.setImportanciaGastoId(importanciaGastoId);
            return this;
        }
        
        public Builder frecuenciaGastoId(Long frecuenciaGastoId) {
            this.debitoAutomatico.setFrecuenciaGastoId(frecuenciaGastoId);
            return this;
        }
        
        public Builder tipoPagoId(Long tipoPagoId) {
            this.debitoAutomatico.setTipoPagoId(tipoPagoId);
            return this;
        }
        
        public Builder tarjetaId(Long tarjetaId) {
            this.debitoAutomatico.setTarjetaId(tarjetaId);
            return this;
        }
        
        public Builder activo(Boolean activo) {
            this.debitoAutomatico.setActivo(activo);
            return this;
        }
        
        public DebitoAutomatico build() {
            return this.debitoAutomatico;
        }
    }
}