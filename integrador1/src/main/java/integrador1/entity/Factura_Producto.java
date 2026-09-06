package integrador1.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Factura_Producto {

    private Integer idFactura;
    private Integer idProducto;
    private Integer cantidad;
}