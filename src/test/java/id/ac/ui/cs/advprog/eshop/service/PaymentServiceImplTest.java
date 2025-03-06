package id.ac.ui.cs.advprog.eshop.service;

import id.ac.ui.cs.advprog.eshop.model.Order;
import id.ac.ui.cs.advprog.eshop.model.Payment;
import id.ac.ui.cs.advprog.eshop.model.Product;
import id.ac.ui.cs.advprog.eshop.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {
    @InjectMocks
    PaymentServiceImpl paymentService;

    @Mock
    PaymentRepository paymentRepository;

    @Mock
    OrderService orderService;

    private Order order;
    private Payment payment;
    private Map<String, String> voucherData;
    private Map<String, String> bankTransferData;

    @BeforeEach
    void setUp() {
        List<Product> products = new ArrayList<>();
        Product product = new Product();
        product.setProductId("eb558e9f-1c39-460e-8860-71af6af63bd6");
        product.setProductName("Sampo Cap Bambang");
        product.setProductQuantity(2);
        products.add(product);

        order = new Order("13652556-012a-4c07-b546-54eb1396d79b", products, 1708560000L, "Safira Sudrajat");

        voucherData = new HashMap<>();
        voucherData.put("voucherCode", "ESHOP1234ABC5678");

        bankTransferData = new HashMap<>();
        bankTransferData.put("bankName", "BCA");
        bankTransferData.put("referenceCode", "REF123456789");

        payment = new Payment("payment-123", order, "VOUCHER", voucherData);
    }

    @Test
    void testAddPaymentWithVoucher_ValidVoucher() {
        doReturn(payment).when(paymentRepository).save(any(Payment.class));

        Payment result = paymentService.addPayment(order, "VOUCHER", voucherData);

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void testAddPaymentWithVoucher_InvalidVoucher() {
        voucherData.put("voucherCode", "INVALID123");

        doReturn(any(Payment.class)).when(paymentRepository).save(any(Payment.class));

        Payment result = paymentService.addPayment(order, "VOUCHER", voucherData);

        assertNotNull(result);
        assertEquals("REJECTED", result.getStatus());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void testAddPaymentWithBankTransfer_ValidData() {
        doReturn(any(Payment.class)).when(paymentRepository).save(any(Payment.class));

        Payment result = paymentService.addPayment(order, "BANK_TRANSFER", bankTransferData);

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void testAddPaymentWithBankTransfer_InvalidData() {
        bankTransferData.remove("referenceCode");

        doReturn(any(Payment.class)).when(paymentRepository).save(any(Payment.class));

        Payment result = paymentService.addPayment(order, "BANK_TRANSFER", bankTransferData);

        assertNotNull(result);
        assertEquals("REJECTED", result.getStatus());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void testSetStatusToSuccess() {
        Payment paymentToUpdate = new Payment("payment-123", order, "VOUCHER", voucherData, "WAITING");

        doReturn(paymentToUpdate).when(paymentRepository).findById(paymentToUpdate.getId());
        doReturn(paymentToUpdate).when(paymentRepository).save(any(Payment.class));
        doReturn(order).when(orderService).updateStatus(order.getId(), "SUCCESS");

        Payment result = paymentService.setStatus(paymentToUpdate, "SUCCESS");

        assertEquals("SUCCESS", result.getStatus());
        verify(paymentRepository).save(any(Payment.class));
        verify(orderService).updateStatus(order.getId(), "SUCCESS");
    }

    @Test
    void testSetStatusToRejected() {
        Payment paymentToUpdate = new Payment("payment-123", order, "VOUCHER", voucherData, "WAITING");

        doReturn(paymentToUpdate).when(paymentRepository).findById(paymentToUpdate.getId());
        doReturn(paymentToUpdate).when(paymentRepository).save(any(Payment.class));
        doReturn(order).when(orderService).updateStatus(order.getId(), "FAILED");

        Payment result = paymentService.setStatus(paymentToUpdate, "REJECTED");

        assertEquals("REJECTED", result.getStatus());
        verify(paymentRepository).save(any(Payment.class));
        verify(orderService).updateStatus(order.getId(), "FAILED");
    }

    @Test
    void testSetStatusPaymentNotFound() {
        Payment nonExistentPayment = new Payment("non-existent", order, "VOUCHER", voucherData);

        doReturn(null).when(paymentRepository).findById(nonExistentPayment.getId());

        assertThrows(NoSuchElementException.class,
                () -> paymentService.setStatus(nonExistentPayment, "SUCCESS"));

        verify(paymentRepository, never()).save(any(Payment.class));
        verify(orderService, never()).updateStatus(anyString(), anyString());
    }

    @Test
    void testGetPayment() {
        String paymentId = "payment-123";

        doReturn(payment).when(paymentRepository).findById(paymentId);

        Payment result = paymentService.getPayment(paymentId);

        assertNotNull(result);
        assertEquals(paymentId, result.getId());
        verify(paymentRepository).findById(paymentId);
    }

    @Test
    void testGetPaymentNotFound() {
        String paymentId = "45234534";

        doReturn(null).when(paymentRepository).findById(paymentId);

        Payment result = paymentService.getPayment(paymentId);

        assertNull(result);
        verify(paymentRepository).findById(paymentId);
    }

    @Test
    void testGetAllPayments() {
        List<Payment> payments = new ArrayList<>();
        payments.add(payment);

        doReturn(payments).when(paymentRepository).findAll();

        List<Payment> results = paymentService.getAllPayments();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(payment, results.get(0));
        verify(paymentRepository).findAll();
    }
}