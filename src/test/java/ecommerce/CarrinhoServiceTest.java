package ecommerce;

import ecommerce.dto.CompraDTO;
import ecommerce.dto.DisponibilidadeDTO;
import ecommerce.dto.EstoqueBaixaDTO;
import ecommerce.dto.PagamentoDTO;
import ecommerce.entity.*;
import ecommerce.external.IEstoqueExternal;
import ecommerce.external.IPagamentoExternal;
import ecommerce.service.CarrinhoDeComprasService;
import ecommerce.service.ClienteService;
import ecommerce.service.CompraService;
import ecommerce.util.exception.ProdutoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static utils.CarrinhoServiceDataGenerator.*;

@ExtendWith(SpringExtension.class)
public class CarrinhoServiceTest {

    private final CompraService compraService
            = new CompraService(null, null, null, null);

    @MockBean
    private IEstoqueExternal estoqueMock;

    @MockBean
    private IPagamentoExternal pagamentoMock;

    @MockBean
    private ClienteService clienteServiceMock;

    @MockBean
    private CarrinhoDeComprasService carrinhoServiceMock;

    /**
     * calcularCustoTotal
     */

    @ParameterizedTest
    @CsvSource({
            // TipoCliente.Bronze
            "BRONZE, 200, 4, 400",    // Sem desconto, frete grátis
            "BRONZE, 200, 6, 412",    // Sem desconto, frete R$12
            "BRONZE, 200, 11, 444",   // Sem desconto, frete R$44
            "BRONZE, 200, 51, 757",   // Sem desconto, frete R$357

            "BRONZE, 300, 4, 540",    // 10% desconto, frete grátis
            "BRONZE, 300, 6, 552",    // 10% desconto, frete R$12
            "BRONZE, 300, 11, 584",   // 10% desconto, frete R$44
            "BRONZE, 300, 51, 897",   // 10% desconto, frete R$357

            "BRONZE, 550, 4, 880",   // 20% desconto, frete grátis
            "BRONZE, 550, 6, 892",   // 20% desconto, frete R$12
            "BRONZE, 550, 11, 924",  // 20% desconto, frete R$44
            "BRONZE, 550, 51, 1237", // 20% desconto, frete R$357

            // TipoCliente.Prata
            "PRATA, 200, 4, 400",     // Sem desconto, frete grátis
            "PRATA, 200, 6, 406",     // Sem desconto, frete R$6 (50% de R$12)
            "PRATA, 200, 11, 422",    // Sem desconto, frete R$22 (50% de R$44)
            "PRATA, 200, 51, 578.5",  // Sem desconto, frete R$178.5 (50% de R$357)

            "PRATA, 300, 4, 540",     // 10% desconto, frete grátis
            "PRATA, 300, 6, 546",     // 10% desconto, frete R$6 (50% de R$12)
            "PRATA, 300, 11, 562",    // 10% desconto, frete R$22 (50% de R$44)
            "PRATA, 300, 51, 718.5",  // 10% desconto, frete R$178.5 (50% de R$357)

            "PRATA, 550, 4, 880",    // 20% desconto, frete grátis
            "PRATA, 550, 6, 886",    // 20% desconto, frete R$6 (50% de R$12)
            "PRATA, 550, 11, 902",   // 20% desconto, frete R$22 (50% de R$44)
            "PRATA, 550, 51, 1058.5",// 20% desconto, frete R$178.5 (50% de R$357)

            // TipoCliente.Ouro
            "OURO, 200, 4, 400",      // Sem desconto, frete grátis
            "OURO, 200, 6, 400",      // Sem desconto, frete grátis
            "OURO, 200, 11, 400",     // Sem desconto, frete grátis
            "OURO, 200, 51, 400",     // Sem desconto, frete grátis

            "OURO, 300, 4, 540",      // 10% desconto, frete grátis
            "OURO, 300, 6, 540",      // 10% desconto, frete grátis
            "OURO, 300, 11, 540",     // 10% desconto, frete grátis
            "OURO, 300, 51, 540",     // 10% desconto, frete grátis

            "OURO, 550, 4, 880",     // 20% desconto, frete grátis
            "OURO, 550, 6, 880",     // 20% desconto, frete grátis
            "OURO, 550, 11, 880",    // 20% desconto, frete grátis
            "OURO, 550, 51, 880"     // 20% desconto, frete grátis
    })
    public void calcularCustoTotalTest(TipoCliente tipoCliente, double custoItens, int pesoTotal, double custoEsperado) {
        Cliente cliente = new Cliente();
        cliente.setTipo(tipoCliente);

        Produto produto = new Produto();
        produto.setPreco(BigDecimal.valueOf(custoItens));
        produto.setPeso(pesoTotal);

        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setCliente(cliente);

        ItemCompra item = new ItemCompra();
        item.setProduto(produto);
        item.setQuantidade(2L);

        carrinho.setItens(List.of(item));

        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho).stripTrailingZeros();

        assertEquals(BigDecimal.valueOf(custoEsperado).stripTrailingZeros(), custoTotal);
    }

    @ParameterizedTest
    @CsvSource({"BRONZE, 1000, 4, 900", "BRONZE, 1001, 4, 800.8", "BRONZE, 500, 4, 500", "BRONZE, 501, 4, 450.9"})
    public void calcularCustoTotalTest_LimitesDesconto(TipoCliente tipoCliente, double custoItens, int pesoTotal, double custoEsperado) {
        Cliente cliente = new Cliente();
        cliente.setTipo(tipoCliente);

        Produto produto = new Produto();
        produto.setPreco(BigDecimal.valueOf(custoItens));
        produto.setPeso(pesoTotal);

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
    @CsvSource({"BRONZE, 500, 5, 500", "BRONZE, 500, 6, 512", "BRONZE, 500, 10, 520", "BRONZE, 500, 11, 544", "BRONZE, 500, 50, 700", "BRONZE, 500, 51, 857",})
    public void calcularCustoTotalTest_LimitesFrete(TipoCliente tipoCliente, double custoItens, int pesoTotal, double custoEsperado) {
        Cliente cliente = new Cliente();
        cliente.setTipo(tipoCliente);

        Produto produto = new Produto();
        produto.setPreco(BigDecimal.valueOf(custoItens));
        produto.setPeso(pesoTotal);

        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setCliente(cliente);

        ItemCompra item = new ItemCompra();
        item.setProduto(produto);
        item.setQuantidade(1L);

        carrinho.setItens(List.of(item));

        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho).stripTrailingZeros();

        assertEquals(BigDecimal.valueOf(custoEsperado).stripTrailingZeros(), custoTotal);
    }

    @Test
    public void calcularCustoTotal_CarrinhoVazio() {
        Cliente cliente = criarCliente();
        CarrinhoDeCompras carrinhoDeCompras = criarCarrinhoDeCompras(cliente);

        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinhoDeCompras).stripTrailingZeros();

        assertEquals(BigDecimal.ZERO.stripTrailingZeros(), custoTotal);
    }

    @Test
    public void calcularCustoTotal_ProdutoNaoEncontrado() {
        Cliente cliente = criarCliente();
        ItemCompra itemCompra = criarItemCompra();
        CarrinhoDeCompras carrinhoDeCompras = criarCarrinhoDeCompras(cliente, itemCompra);

        assertThrows(ProdutoNaoEncontradoException.class, () -> {
            compraService.calcularCustoTotal(carrinhoDeCompras);
        });
    }

    @Test
    public void calcularCustoTotal_CarrinhoComItensVazios() {
        Cliente cliente = criarCliente();
        CarrinhoDeCompras carrinho = criarCarrinhoDeCompras(cliente);

        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho).stripTrailingZeros();

        assertEquals(BigDecimal.ZERO.stripTrailingZeros(), custoTotal);
    }

    /**
     * finalizarCompra
     */

    @ParameterizedTest
    @CsvSource({
            "true, true, true",
            "false, true, true",
            "true, false, true",
            "true, true, false"
    })
    public void finalizarCompraTest(boolean darBaixa, boolean autorizarPagamento, boolean estoqueDisponivel) {
        Cliente cliente = criarCliente();
        Produto produto = criarProduto();
        ItemCompra itemCompra = criarItemCompra(produto);
        CarrinhoDeCompras carrinhoDeCompras = criarCarrinhoDeCompras(cliente, itemCompra);

        when(clienteServiceMock.buscarPorId(1L)).thenReturn(cliente);
        when(carrinhoServiceMock.buscarPorCarrinhoIdEClienteId(1L, cliente)).thenReturn(carrinhoDeCompras);

        when(estoqueMock.verificarDisponibilidade(List.of(1L), List.of(2L)))
                .thenReturn(new DisponibilidadeDTO(estoqueDisponivel, List.of(1L)));

        when(pagamentoMock.autorizarPagamento(1L, 200.0))
                .thenReturn(new PagamentoDTO(autorizarPagamento, 1L));

        when(estoqueMock.darBaixa(List.of(1L), List.of(2L)))
                .thenReturn(new EstoqueBaixaDTO(darBaixa));

        CompraService compraService =
                new CompraService(carrinhoServiceMock, clienteServiceMock, estoqueMock, pagamentoMock);

        if (!(estoqueDisponivel && autorizarPagamento && darBaixa)) {
            assertThrows(IllegalStateException.class, () -> {
                compraService.finalizarCompra(1L, 1L);
            });

            if (!darBaixa) {
                verify(pagamentoMock).cancelarPagamento(1L, 1L);
            }
        } else {
            CompraDTO compraDTO = compraService.finalizarCompra(1L, 1L);

            assertTrue(compraDTO.sucesso());
            assertEquals(1L, compraDTO.transacaoPagamentoId());
            assertEquals("Compra finalizada com sucesso.", compraDTO.mensagem());
        }
    }
}
