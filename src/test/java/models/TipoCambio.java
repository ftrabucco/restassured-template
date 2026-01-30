package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Model class for TipoCambio (Exchange Rate) entity
 * Represents USD/ARS exchange rate with buy/sell values
 * Maps to backend POST /api/tipo-cambio/manual endpoint
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TipoCambio {

    @JsonProperty("fecha")
    private LocalDate fecha;

    @JsonProperty("valor_compra_usd_ars")
    private BigDecimal valorCompra;

    @JsonProperty("valor_venta_usd_ars")
    private BigDecimal valorVenta;

    @JsonProperty("fuente")
    private String fuente;

    @JsonProperty(value = "activo", access = JsonProperty.Access.READ_ONLY)
    private Boolean activo;

    // Default constructor
    public TipoCambio() {}

    // Constructor with Builder
    private TipoCambio(Builder builder) {
        this.fecha = builder.fecha;
        this.valorCompra = builder.valorCompra;
        this.valorVenta = builder.valorVenta;
        this.fuente = builder.fuente;
    }

    // Getters
    public LocalDate getFecha() { return fecha; }
    public BigDecimal getValorCompra() { return valorCompra; }
    public BigDecimal getValorVenta() { return valorVenta; }
    public String getFuente() { return fuente; }
    public Boolean getActivo() { return activo; }

    // Setters
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public void setValorCompra(BigDecimal valorCompra) { this.valorCompra = valorCompra; }
    public void setValorVenta(BigDecimal valorVenta) { this.valorVenta = valorVenta; }
    public void setFuente(String fuente) { this.fuente = fuente; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    // Builder pattern implementation
    public static class Builder {
        private LocalDate fecha;
        private BigDecimal valorCompra;
        private BigDecimal valorVenta;
        private String fuente;

        public Builder fecha(LocalDate fecha) {
            this.fecha = fecha;
            return this;
        }

        public Builder valorCompra(BigDecimal valorCompra) {
            this.valorCompra = valorCompra;
            return this;
        }

        public Builder valorVenta(BigDecimal valorVenta) {
            this.valorVenta = valorVenta;
            return this;
        }

        public Builder fuente(String fuente) {
            this.fuente = fuente;
            return this;
        }

        public TipoCambio build() {
            return new TipoCambio(this);
        }
    }

    @Override
    public String toString() {
        return "TipoCambio{" +
                "fecha=" + fecha +
                ", valorCompra=" + valorCompra +
                ", valorVenta=" + valorVenta +
                ", fuente='" + fuente + '\'' +
                ", activo=" + activo +
                '}';
    }
}
