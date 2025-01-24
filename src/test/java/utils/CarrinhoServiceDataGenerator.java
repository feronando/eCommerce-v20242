package utils;

import ecommerce.entity.*;

import java.math.BigDecimal;
import java.util.List;

public class CarrinhoServiceDataGenerator {

    public static Cliente criarCliente() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setTipo(TipoCliente.BRONZE);
        return cliente;
    }

    public static CarrinhoDeCompras criarCarrinhoDeCompras(Cliente cliente, ItemCompra itemCompra) {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setId(1L);
        carrinho.setCliente(cliente);
        carrinho.setItens(List.of(itemCompra));
        return carrinho;
    }

    public static CarrinhoDeCompras criarCarrinhoDeCompras(Cliente cliente) {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setId(1L);
        carrinho.setCliente(cliente);
        carrinho.setItens(List.of());
        return carrinho;
    }

    public static Produto criarProduto() {
        Produto produto = new Produto();
        produto.setId(1L);
        produto.setPreco(BigDecimal.valueOf(100));
        produto.setPeso(5);
        return produto;
    }

    public static ItemCompra criarItemCompra(Produto produto) {
        ItemCompra item = new ItemCompra();
        item.setProduto(produto);
        item.setQuantidade(2L);
        return item;
    }

    public static ItemCompra criarItemCompra() {
        ItemCompra item = new ItemCompra();
        item.setQuantidade(2L);
        return item;
    }
}
