package models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Model class for Compra (Purchase) entity
 * Demonstrates Builder pattern implementation
 */
public class Compra {
    @JsonIgnore
    private String id;
    
    @JsonProperty("descripcion")
    private String descripcion;
    
    @JsonProperty("monto_total")
    private BigDecimal montoTotal;
    
    @JsonProperty("cantidad_cuotas")
    private Integer cantidadCuotas;
    
    @JsonProperty("fecha_compra")
    private LocalDate fechaCompra;
    
    @JsonProperty("categoria_gasto_id")
    private Long categoriaGastoId;
    
    @JsonProperty("importancia_gasto_id")
    private Long importanciaGastoId;
    
    @JsonProperty("tipo_pago_id")
    private Long tipoPagoId;
    
    @JsonProperty("tarjeta_id")
    private Long tarjetaId;
    
    @JsonIgnore
    private Boolean pendienteCuotas;

    // Default constructor
    public Compra() {}

    // Constructor with Builder
    private Compra(Builder builder) {
        this.id = builder.id;
        this.descripcion = builder.descripcion;
        this.montoTotal = builder.montoTotal;
        this.cantidadCuotas = builder.cantidadCuotas;
        this.fechaCompra = builder.fechaCompra;
        this.categoriaGastoId = builder.categoriaGastoId;
        this.importanciaGastoId = builder.importanciaGastoId;
        this.tipoPagoId = builder.tipoPagoId;
        this.tarjetaId = builder.tarjetaId;
        this.pendienteCuotas = builder.pendienteCuotas;
    }

    // Getters
    public String getId() { return id; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getMontoTotal() { return montoTotal; }
    public Integer getCantidadCuotas() { return cantidadCuotas; }
    public LocalDate getFechaCompra() { return fechaCompra; }
    public Long getCategoriaGastoId() { return categoriaGastoId; }
    public Long getImportanciaGastoId() { return importanciaGastoId; }
    public Long getTipoPagoId() { return tipoPagoId; }
    public Long getTarjetaId() { return tarjetaId; }
    public Boolean getPendienteCuotas() { return pendienteCuotas; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setMontoTotal(BigDecimal montoTotal) { this.montoTotal = montoTotal; }
    public void setCantidadCuotas(Integer cantidadCuotas) { this.cantidadCuotas = cantidadCuotas; }
    public void setFechaCompra(LocalDate fechaCompra) { this.fechaCompra = fechaCompra; }
    public void setCategoriaGastoId(Long categoriaGastoId) { this.categoriaGastoId = categoriaGastoId; }
    public void setImportanciaGastoId(Long importanciaGastoId) { this.importanciaGastoId = importanciaGastoId; }
    public void setTipoPagoId(Long tipoPagoId) { this.tipoPagoId = tipoPagoId; }
    public void setTarjetaId(Long tarjetaId) { this.tarjetaId = tarjetaId; }
    public void setPendienteCuotas(Boolean pendienteCuotas) { this.pendienteCuotas = pendienteCuotas; }

    // Builder pattern implementation
    public static class Builder {
        private String id;
        private String descripcion;
        @JsonProperty("monto_total")
        private BigDecimal montoTotal;
        @JsonProperty("cantidad_cuotas")
        private Integer cantidadCuotas;
        @JsonProperty("fecha_compra")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate fechaCompra;
        @JsonProperty("categoria_gasto_id")
        private Long categoriaGastoId;
        @JsonProperty("importancia_gasto_id")
        private Long importanciaGastoId;
        @JsonProperty("tipo_pago_id")
        private Long tipoPagoId;
        @JsonProperty("tarjeta_id")
        private Long tarjetaId;
        @JsonProperty("pendiente_cuotas")
        private Boolean pendienteCuotas;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder descripcion(String descripcion) {
            this.descripcion = descripcion;
            return this;
        }

        public Builder montoTotal(BigDecimal montoTotal) {
            this.montoTotal = montoTotal;
            return this;
        }

        public Builder cantidadCuotas(Integer cantidadCuotas) {
            this.cantidadCuotas = cantidadCuotas;
            return this;
        }

        public Builder fechaCompra(LocalDate fechaCompra) {
            this.fechaCompra = fechaCompra;
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
        
        public Builder pendienteCuotas(Boolean pendienteCuotas) {
            this.pendienteCuotas = pendienteCuotas;
            return this;
        }

        public Compra build() {
            return new Compra(this);
        }
    }

    @Override
    public String toString() {
        return "Compra{" +
                "id=" + id +
                ", descripcion='" + descripcion + '\'' +
                ", montoTotal=" + montoTotal +
                ", cantidadCuotas=" + cantidadCuotas +
                ", fechaCompra=" + fechaCompra +
                ", categoriaGastoId=" + categoriaGastoId +
                ", importanciaGastoId=" + importanciaGastoId +
                ", tipoPagoId=" + tipoPagoId +
                ", tarjetaId=" + tarjetaId +
                ", pendienteCuotas=" + pendienteCuotas +
                '}';
    }
}
