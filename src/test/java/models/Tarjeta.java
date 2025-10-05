package models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Model representing a Tarjeta (Card) - both credit and debit cards
 * Based on MCP server test scenarios for tarjetas endpoint
 * Supports both creation and response scenarios
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Tarjeta {

    @JsonProperty("id")
    private Integer id;

    // MCP format fields
    @JsonProperty("nombre")
    private String nombre;

    @JsonProperty("tipo")
    private String tipo; // "credito" or "debito"

    @JsonProperty("banco")
    private String banco;

    @JsonProperty("dia_mes_cierre")
    private Integer diaMesCierre;

    @JsonProperty("dia_mes_vencimiento")
    private Integer diaMesVencimiento;

    @JsonProperty("permite_cuotas")
    private Boolean permiteCuotas;

    @JsonProperty("usuario_id")
    private Integer usuarioId;

    // Legacy fields for backward compatibility
    @JsonProperty("numero_tarjeta")
    private String numeroTarjeta;

    @JsonProperty("nombre_titular")
    private String nombreTitular;

    @JsonProperty("fecha_vencimiento")
    private String fechaVencimiento;

    @JsonProperty("codigo_seguridad")
    private String codigoSeguridad;

    @JsonProperty("banco_emisor")
    private String bancoEmisor;

    @JsonProperty("tipo_tarjeta")
    private TipoTarjeta tipoTarjeta;

    // Credit card specific fields
    @JsonProperty("limite_credito")
    private BigDecimal limiteCredito;

    @JsonProperty("credito_disponible")
    private BigDecimal creditoDisponible;

    @JsonProperty("fecha_corte")
    private String fechaCorte;

    @JsonProperty("fecha_vencimiento_pago")
    private String fechaVencimientoPago;

    @JsonProperty("tasa_interes")
    private BigDecimal tasaInteres;

    // Debit card specific fields
    @JsonProperty("cuenta_asociada")
    private String cuentaAsociada;

    @JsonProperty("saldo_disponible")
    private BigDecimal saldoDisponible;

    @JsonProperty("limite_diario")
    private BigDecimal limiteDiario;

    // Common fields
    @JsonProperty("estado")
    private EstadoTarjeta estado;

    @JsonProperty("fecha_creacion")
    private String fechaCreacion;

    @JsonProperty("fecha_actualizacion")
    private String fechaActualizacion;

    public enum TipoTarjeta {
        @JsonProperty("CREDITO")
        CREDITO,
        @JsonProperty("DEBITO")
        DEBITO
    }

    public enum EstadoTarjeta {
        @JsonProperty("ACTIVA")
        ACTIVA,
        @JsonProperty("BLOQUEADA")
        BLOQUEADA,
        @JsonProperty("VENCIDA")
        VENCIDA,
        @JsonProperty("CANCELADA")
        CANCELADA
    }

    // Constructors
    public Tarjeta() {}

    public Tarjeta(String numeroTarjeta, String nombreTitular, String fechaVencimiento,
                   String bancoEmisor, TipoTarjeta tipoTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
        this.nombreTitular = nombreTitular;
        this.fechaVencimiento = fechaVencimiento;
        this.bancoEmisor = bancoEmisor;
        this.tipoTarjeta = tipoTarjeta;
        this.estado = EstadoTarjeta.ACTIVA; // Default state
    }

    // Builder pattern for easier test data creation
    public static TarjetaBuilder builder() {
        return new TarjetaBuilder();
    }

    public static class TarjetaBuilder {
        private final Tarjeta tarjeta = new Tarjeta();

        public TarjetaBuilder numeroTarjeta(String numeroTarjeta) {
            tarjeta.numeroTarjeta = numeroTarjeta;
            return this;
        }

        public TarjetaBuilder nombre(String nombre) {
            tarjeta.nombre = nombre;
            return this;
        }

        public TarjetaBuilder tipo(String tipo) {
            tarjeta.tipo = tipo;
            return this;
        }

        public TarjetaBuilder banco(String banco) {
            tarjeta.banco = banco;
            return this;
        }

        public TarjetaBuilder diaMesCierre(Integer diaMesCierre) {
            tarjeta.diaMesCierre = diaMesCierre;
            return this;
        }

        public TarjetaBuilder diaMesVencimiento(Integer diaMesVencimiento) {
            tarjeta.diaMesVencimiento = diaMesVencimiento;
            return this;
        }

        public TarjetaBuilder permiteCuotas(Boolean permiteCuotas) {
            tarjeta.permiteCuotas = permiteCuotas;
            return this;
        }

        public TarjetaBuilder nombreTitular(String nombreTitular) {
            tarjeta.nombreTitular = nombreTitular;
            return this;
        }

        public TarjetaBuilder fechaVencimiento(String fechaVencimiento) {
            tarjeta.fechaVencimiento = fechaVencimiento;
            return this;
        }

        public TarjetaBuilder codigoSeguridad(String codigoSeguridad) {
            tarjeta.codigoSeguridad = codigoSeguridad;
            return this;
        }

        public TarjetaBuilder bancoEmisor(String bancoEmisor) {
            tarjeta.bancoEmisor = bancoEmisor;
            return this;
        }

        public TarjetaBuilder tipoTarjeta(TipoTarjeta tipoTarjeta) {
            tarjeta.tipoTarjeta = tipoTarjeta;
            return this;
        }

        public TarjetaBuilder limiteCredito(BigDecimal limiteCredito) {
            tarjeta.limiteCredito = limiteCredito;
            return this;
        }

        public TarjetaBuilder tasaInteres(BigDecimal tasaInteres) {
            tarjeta.tasaInteres = tasaInteres;
            return this;
        }

        public TarjetaBuilder cuentaAsociada(String cuentaAsociada) {
            tarjeta.cuentaAsociada = cuentaAsociada;
            return this;
        }

        public TarjetaBuilder limiteDiario(BigDecimal limiteDiario) {
            tarjeta.limiteDiario = limiteDiario;
            return this;
        }

        public TarjetaBuilder estado(EstadoTarjeta estado) {
            tarjeta.estado = estado;
            return this;
        }

        public TarjetaBuilder usuarioId(Integer usuarioId) {
            tarjeta.usuarioId = usuarioId;
            return this;
        }

        public Tarjeta build() {
            return tarjeta;
        }
    }

    // Factory methods for common scenarios
    public static Tarjeta createCreditCard(String numeroTarjeta, String nombreTitular,
                                         String fechaVencimiento, String bancoEmisor,
                                         BigDecimal limiteCredito) {
        return builder()
                .numeroTarjeta(numeroTarjeta)
                .nombreTitular(nombreTitular)
                .fechaVencimiento(fechaVencimiento)
                .bancoEmisor(bancoEmisor)
                .tipoTarjeta(TipoTarjeta.CREDITO)
                .limiteCredito(limiteCredito)
                .estado(EstadoTarjeta.ACTIVA)
                .build();
    }

    public static Tarjeta createDebitCard(String numeroTarjeta, String nombreTitular,
                                        String fechaVencimiento, String bancoEmisor,
                                        String cuentaAsociada) {
        return builder()
                .numeroTarjeta(numeroTarjeta)
                .nombreTitular(nombreTitular)
                .fechaVencimiento(fechaVencimiento)
                .bancoEmisor(bancoEmisor)
                .tipoTarjeta(TipoTarjeta.DEBITO)
                .cuentaAsociada(cuentaAsociada)
                .estado(EstadoTarjeta.ACTIVA)
                .build();
    }

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNumeroTarjeta() { return numeroTarjeta; }
    public void setNumeroTarjeta(String numeroTarjeta) { this.numeroTarjeta = numeroTarjeta; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getBanco() { return banco; }
    public void setBanco(String banco) { this.banco = banco; }

    public Integer getDiaMesCierre() { return diaMesCierre; }
    public void setDiaMesCierre(Integer diaMesCierre) { this.diaMesCierre = diaMesCierre; }

    public Integer getDiaMesVencimiento() { return diaMesVencimiento; }
    public void setDiaMesVencimiento(Integer diaMesVencimiento) { this.diaMesVencimiento = diaMesVencimiento; }

    public Boolean getPermiteCuotas() { return permiteCuotas; }
    public void setPermiteCuotas(Boolean permiteCuotas) { this.permiteCuotas = permiteCuotas; }

    public String getNombreTitular() { return nombreTitular; }
    public void setNombreTitular(String nombreTitular) { this.nombreTitular = nombreTitular; }

    public String getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(String fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public String getCodigoSeguridad() { return codigoSeguridad; }
    public void setCodigoSeguridad(String codigoSeguridad) { this.codigoSeguridad = codigoSeguridad; }

    public String getBancoEmisor() { return bancoEmisor; }
    public void setBancoEmisor(String bancoEmisor) { this.bancoEmisor = bancoEmisor; }

    public TipoTarjeta getTipoTarjeta() { return tipoTarjeta; }
    public void setTipoTarjeta(TipoTarjeta tipoTarjeta) { this.tipoTarjeta = tipoTarjeta; }

    public BigDecimal getLimiteCredito() { return limiteCredito; }
    public void setLimiteCredito(BigDecimal limiteCredito) { this.limiteCredito = limiteCredito; }

    public BigDecimal getCreditoDisponible() { return creditoDisponible; }
    public void setCreditoDisponible(BigDecimal creditoDisponible) { this.creditoDisponible = creditoDisponible; }

    public String getFechaCorte() { return fechaCorte; }
    public void setFechaCorte(String fechaCorte) { this.fechaCorte = fechaCorte; }

    public String getFechaVencimientoPago() { return fechaVencimientoPago; }
    public void setFechaVencimientoPago(String fechaVencimientoPago) { this.fechaVencimientoPago = fechaVencimientoPago; }

    public BigDecimal getTasaInteres() { return tasaInteres; }
    public void setTasaInteres(BigDecimal tasaInteres) { this.tasaInteres = tasaInteres; }

    public String getCuentaAsociada() { return cuentaAsociada; }
    public void setCuentaAsociada(String cuentaAsociada) { this.cuentaAsociada = cuentaAsociada; }

    public BigDecimal getSaldoDisponible() { return saldoDisponible; }
    public void setSaldoDisponible(BigDecimal saldoDisponible) { this.saldoDisponible = saldoDisponible; }

    public BigDecimal getLimiteDiario() { return limiteDiario; }
    public void setLimiteDiario(BigDecimal limiteDiario) { this.limiteDiario = limiteDiario; }

    public EstadoTarjeta getEstado() { return estado; }
    public void setEstado(EstadoTarjeta estado) { this.estado = estado; }

    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(String fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    // Utility methods
    public boolean isCreditCard() {
        return TipoTarjeta.CREDITO.equals(this.tipoTarjeta);
    }

    public boolean isDebitCard() {
        return TipoTarjeta.DEBITO.equals(this.tipoTarjeta);
    }

    public boolean isActive() {
        return EstadoTarjeta.ACTIVA.equals(this.estado);
    }

    public String getMaskedCardNumber() {
        if (numeroTarjeta == null || numeroTarjeta.length() < 4) {
            return numeroTarjeta;
        }
        return "**** **** **** " + numeroTarjeta.substring(numeroTarjeta.length() - 4);
    }

    // toString, equals, hashCode
    @Override
    public String toString() {
        return String.format("Tarjeta{id=%d, tipo=%s, numero='%s', titular='%s', banco='%s', estado=%s}",
                id, tipoTarjeta, getMaskedCardNumber(), nombreTitular, bancoEmisor, estado);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tarjeta tarjeta = (Tarjeta) o;
        return Objects.equals(id, tarjeta.id) &&
               Objects.equals(numeroTarjeta, tarjeta.numeroTarjeta) &&
               Objects.equals(nombreTitular, tarjeta.nombreTitular) &&
               tipoTarjeta == tarjeta.tipoTarjeta;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, numeroTarjeta, nombreTitular, tipoTarjeta);
    }
}