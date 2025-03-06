package id.ac.ui.cs.advprog.eshop.service;

import id.ac.ui.cs.advprog.eshop.model.Order;
import id.ac.ui.cs.advprog.eshop.model.Payment;
import id.ac.ui.cs.advprog.eshop.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderService orderService;

    @Override
    public Payment addPayment(Order order, String method, Map<String, String> paymentData) {
        // Generate a unique ID for the payment
        String paymentId = UUID.randomUUID().toString();

        // Create a new payment
        Payment payment = new Payment(paymentId, order, method, paymentData);

        // Apply validation rules and set status based on payment method
        if ("VOUCHER".equals(method)) {
            validateVoucherPayment(payment);
        } else if ("BANK_TRANSFER".equals(method)) {
            validateBankTransferPayment(payment);
        }

        // Save the payment
        return paymentRepository.save(payment);
    }

    private void validateVoucherPayment(Payment payment) {
        String voucherCode = payment.getPaymentData().get("voucherCode");

        // Voucher code must be 16 characters long
        if (voucherCode == null || voucherCode.length() != 16) {
            payment.setStatus("REJECTED");
            return;
        }

        // Voucher code must start with "ESHOP"
        if (!voucherCode.startsWith("ESHOP")) {
            payment.setStatus("REJECTED");
            return;
        }

        // Count digits in voucher code
        int digitCount = 0;
        for (char c : voucherCode.toCharArray()) {
            if (Character.isDigit(c)) {
                digitCount++;
            }
        }

        // Voucher code must contain exactly 8 numerical characters
        if (digitCount != 8) {
            payment.setStatus("REJECTED");
            return;
        }

        // If all validations pass
        payment.setStatus("SUCCESS");
    }

    private void validateBankTransferPayment(Payment payment) {
        Map<String, String> paymentData = payment.getPaymentData();
        String bankName = paymentData.get("bankName");
        String referenceCode = paymentData.get("referenceCode");

        // Both bank name and reference code must not be empty or null
        if (bankName == null || bankName.isEmpty() ||
                referenceCode == null || referenceCode.isEmpty()) {
            payment.setStatus("REJECTED");
            return;
        }

        // If all validations pass
        payment.setStatus("SUCCESS");
    }

    @Override
    public Payment setStatus(Payment payment, String status) {
        // Check if the payment exists
        Payment existingPayment = paymentRepository.findById(payment.getId());
        if (existingPayment == null) {
            throw new NoSuchElementException("Payment not found with ID: " + payment.getId());
        }

        // Update the payment status
        existingPayment.setStatus(status);

        // Update the order status based on payment status
        if ("SUCCESS".equals(status)) {
            orderService.updateStatus(existingPayment.getOrder().getId(), "SUCCESS");
        } else if ("REJECTED".equals(status)) {
            orderService.updateStatus(existingPayment.getOrder().getId(), "FAILED");
        }

        // Save the updated payment
        return paymentRepository.save(existingPayment);
    }

    @Override
    public Payment getPayment(String paymentId) {
        return paymentRepository.findById(paymentId);
    }

    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
}