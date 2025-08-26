package models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Model class for Ingreso (Income) entity
 */
public class Ingreso {
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("descripcion")
    private String descripcion;
    
    @JsonProperty("monto")
    private BigDecimal monto;
    
    @JsonProperty("fuente")
    private String fuente;
    
    @JsonProperty("fecha")
    private LocalDateTime fecha;
    
    @JsonProperty("usuario_id")
    private String usuarioId;
    
    @JsonProperty("recurrente")
    private boolean recurrente;
    
    @JsonProperty("notas")
    private String notas;

    // Constructors
    public Ingreso() {}

    public Ingreso(String descripcion, BigDecimal monto, String fuente, String usuarioId) {
        this.descripcion = descripcion;
        this.monto = monto;
        this.fuente = fuente;
        this.usuarioId = usuarioId;
        this.fecha = LocalDateTime.now();
        this.recurrente = false;
    }

    // Builder pattern for test data creation
    public static class Builder {
        private Ingreso ingreso = new Ingreso();

        public Builder withId(String id) {
            ingreso.id = id;
            return this;
        }

        public Builder withDescripcion(String descripcion) {
            ingreso.descripcion = descripcion;
            return this;
        }

        public Builder withMonto(BigDecimal monto) {
            ingreso.monto = monto;
            return this;
        }

        public Builder withFuente(String fuente) {
            ingreso.fuente = fuente;
            return this;
        }

        public Builder withFecha(LocalDateTime fecha) {
            ingreso.fecha = fecha;
            return this;
        }

        public Builder withUsuarioId(String usuarioId) {
            ingreso.usuarioId = usuarioId;
            return this;
        }

        public Builder withRecurrente(boolean recurrente) {
            ingreso.recurrente = recurrente;
            return this;
        }

        public Builder withNotas(String notas) {
            ingreso.notas = notas;
            return this;
        }

        public Ingreso build() {
            return ingreso;
        }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public String getFuente() { return fuente; }
    public void setFuente(String fuente) { this.fuente = fuente; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public boolean isRecurrente() { return recurrente; }
    public void setRecurrente(boolean recurrente) { this.recurrente = recurrente; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    @Override
    public String toString() {
        return "Ingreso{" +
                "id='" + id + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", monto=" + monto +
                ", fuente='" + fuente + '\'' +
                ", fecha=" + fecha +
                ", usuarioId='" + usuarioId + '\'' +
                ", recurrente=" + recurrente +
                ", notas='" + notas + '\'' +
                '}';
    }
}
