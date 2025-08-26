package models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Model class for Gasto (Expense) entity
 */
public class Gasto {
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("descripcion")
    private String descripcion;
    
    @JsonProperty("monto")
    private BigDecimal monto;
    
    @JsonProperty("categoria")
    private String categoria;
    
    @JsonProperty("fecha")
    private LocalDateTime fecha;
    
    @JsonProperty("usuario_id")
    private String usuarioId;
    
    @JsonProperty("metodo_pago")
    private String metodoPago;
    
    @JsonProperty("notas")
    private String notas;

    // Constructors
    public Gasto() {}

    public Gasto(String descripcion, BigDecimal monto, String categoria, String usuarioId) {
        this.descripcion = descripcion;
        this.monto = monto;
        this.categoria = categoria;
        this.usuarioId = usuarioId;
        this.fecha = LocalDateTime.now();
    }

    // Builder pattern for test data creation
    public static class Builder {
        private Gasto gasto = new Gasto();

        public Builder withId(String id) {
            gasto.id = id;
            return this;
        }

        public Builder withDescripcion(String descripcion) {
            gasto.descripcion = descripcion;
            return this;
        }

        public Builder withMonto(BigDecimal monto) {
            gasto.monto = monto;
            return this;
        }

        public Builder withCategoria(String categoria) {
            gasto.categoria = categoria;
            return this;
        }

        public Builder withFecha(LocalDateTime fecha) {
            gasto.fecha = fecha;
            return this;
        }

        public Builder withUsuarioId(String usuarioId) {
            gasto.usuarioId = usuarioId;
            return this;
        }

        public Builder withMetodoPago(String metodoPago) {
            gasto.metodoPago = metodoPago;
            return this;
        }

        public Builder withNotas(String notas) {
            gasto.notas = notas;
            return this;
        }

        public Gasto build() {
            return gasto;
        }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    @Override
    public String toString() {
        return "Gasto{" +
                "id='" + id + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", monto=" + monto +
                ", categoria='" + categoria + '\'' +
                ", fecha=" + fecha +
                ", usuarioId='" + usuarioId + '\'' +
                ", metodoPago='" + metodoPago + '\'' +
                ", notas='" + notas + '\'' +
                '}';
    }
}
