package ecommerce;

import ecommerce.entity.*;
import ecommerce.service.CompraService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CarrinhoServiceTest {

    private final CompraService compraService = new CompraService(null, null, null, null);

    @ParameterizedTest
    @CsvSource({
            // TipoCliente.Bronze
            "BRONZE, 400, 4, 400",    // Sem desconto, frete grátis
            "BRONZE, 400, 6, 412",    // Sem desconto, frete R$12
            "BRONZE, 400, 11, 444",   // Sem desconto, frete R$44
            "BRONZE, 400, 51, 757",   // Sem desconto, frete R$357

            "BRONZE, 600, 4, 540",    // 10% desconto, frete grátis
            "BRONZE, 600, 6, 552",    // 10% desconto, frete R$12
            "BRONZE, 600, 11, 584",   // 10% desconto, frete R$44
            "BRONZE, 600, 51, 897",   // 10% desconto, frete R$357

            "BRONZE, 1100, 4, 880",   // 20% desconto, frete grátis
            "BRONZE, 1100, 6, 892",   // 20% desconto, frete R$12
            "BRONZE, 1100, 11, 924",  // 20% desconto, frete R$44
            "BRONZE, 1100, 51, 1237", // 20% desconto, frete R$357

            // TipoCliente.Prata
            "PRATA, 400, 4, 400",     // Sem desconto, frete grátis
            "PRATA, 400, 6, 406",     // Sem desconto, frete R$6 (50% de R$12)
            "PRATA, 400, 11, 422",    // Sem desconto, frete R$22 (50% de R$44)
            "PRATA, 400, 51, 578.5",  // Sem desconto, frete R$178.5 (50% de R$357)

            "PRATA, 600, 4, 540",     // 10% desconto, frete grátis
            "PRATA, 600, 6, 546",     // 10% desconto, frete R$6 (50% de R$12)
            "PRATA, 600, 11, 562",    // 10% desconto, frete R$22 (50% de R$44)
            "PRATA, 600, 51, 718.5",  // 10% desconto, frete R$178.5 (50% de R$357)

            "PRATA, 1100, 4, 880",    // 20% desconto, frete grátis
            "PRATA, 1100, 6, 886",    // 20% desconto, frete R$6 (50% de R$12)
            "PRATA, 1100, 11, 902",   // 20% desconto, frete R$22 (50% de R$44)
            "PRATA, 1100, 51, 1058.5",// 20% desconto, frete R$178.5 (50% de R$357)

            // TipoCliente.Ouro
            "OURO, 400, 4, 400",      // Sem desconto, frete grátis
            "OURO, 400, 6, 400",      // Sem desconto, frete grátis
            "OURO, 400, 11, 400",     // Sem desconto, frete grátis
            "OURO, 400, 51, 400",     // Sem desconto, frete grátis

            "OURO, 600, 4, 540",      // 10% desconto, frete grátis
            "OURO, 600, 6, 540",      // 10% desconto, frete grátis
            "OURO, 600, 11, 540",     // 10% desconto, frete grátis
            "OURO, 600, 51, 540",     // 10% desconto, frete grátis

            "OURO, 1100, 4, 880",     // 20% desconto, frete grátis
            "OURO, 1100, 6, 880",     // 20% desconto, frete grátis
            "OURO, 1100, 11, 880",    // 20% desconto, frete grátis
            "OURO, 1100, 51, 880"     // 20% desconto, frete grátis
    })
    public void calcularCustoTotalTest(TipoCliente tipoCliente, double custoItens, int pesoTotal, double custoEsperado) {
        // Mock Cliente
        Cliente cliente = new Cliente();
        cliente.setTipo(tipoCliente);

        // Mock Produto
        Produto produto = new Produto();
        produto.setPreco(BigDecimal.valueOf(custoItens));
        produto.setPeso(pesoTotal);

        // Mock CarrinhoDeCompras
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setCliente(cliente);

        ItemCompra item = new ItemCompra();
        item.setProduto(produto);
        item.setQuantidade(1L);

        carrinho.setItens(List.of(item));

        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho).stripTrailingZeros();

        assertEquals(BigDecimal.valueOf(custoEsperado).stripTrailingZeros(), custoTotal);
    }

    @ParameterizedTest
    @CsvSource({
            "BRONZE, 1000, 4, 900",
            "BRONZE, 1001, 4, 800.8",
            "BRONZE, 500, 4, 500",
            "BRONZE, 501, 4, 450.9"
    })
    public void calcularCustoTotalTest_LimitesDesconto(TipoCliente tipoCliente, double custoItens, int pesoTotal, double custoEsperado) {
        // Mock Cliente
        Cliente cliente = new Cliente();
        cliente.setTipo(tipoCliente);

        // Mock Produto
        Produto produto = new Produto();
        produto.setPreco(BigDecimal.valueOf(custoItens));
        produto.setPeso(pesoTotal);

        // Mock CarrinhoDeCompras
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setCliente(cliente);

        ItemCompra item = new ItemCompra();
        item.setProduto(produto);
        item.setQuantidade(1L);

        carrinho.setItens(List.of(item));

        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho).stripTrailingZeros();

        assertEquals(BigDecimal.valueOf(custoEsperado).stripTrailingZeros(), custoTotal);
    }

    @ParameterizedTest
    @CsvSource({
            "BRONZE, 500, 5, 500",
            "BRONZE, 500, 6, 512",
            "BRONZE, 500, 10, 520",
            "BRONZE, 500, 11, 544",
            "BRONZE, 500, 50, 700",
            "BRONZE, 500, 51, 857",
    })
    public void calcularCustoTotalTest_LimitesFrete(TipoCliente tipoCliente, double custoItens, int pesoTotal, double custoEsperado) {
        // Mock Cliente
        Cliente cliente = new Cliente();
        cliente.setTipo(tipoCliente);

        // Mock Produto
        Produto produto = new Produto();
        produto.setPreco(BigDecimal.valueOf(custoItens));
        produto.setPeso(pesoTotal);

        // Mock CarrinhoDeCompras
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setCliente(cliente);

        ItemCompra item = new ItemCompra();
        item.setProduto(produto);
        item.setQuantidade(1L);

        carrinho.setItens(List.of(item));

        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho).stripTrailingZeros();

        assertEquals(BigDecimal.valueOf(custoEsperado).stripTrailingZeros(), custoTotal);
    }


}
