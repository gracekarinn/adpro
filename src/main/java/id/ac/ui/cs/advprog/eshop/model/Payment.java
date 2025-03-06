package id.ac.ui.cs.advprog.eshop.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
public class Payment {
    private String id;
    private Order order;
    private String method;
    @Setter
    private String status;
    private Map<String, String> paymentData;


    public Payment(String id, Order order, String method, Map<String, String> paymentData) {
        this.id = id;
        this.order = order;
        this.method = method;
        this.paymentData = paymentData;
        this.status = "WAITING";
    }

    public Payment(String id, Order order, String method, Map<String, String> paymentData, String status) {
        this.id = id;
        this.order = order;
        this.method = method;
        this.paymentData = paymentData;
        this.status = status;
    }

    public void validateAndSetStatus() {
        if ("VOUCHER".equals(this.method)) {
            validateVoucherPayment();
        } else if ("BANK_TRANSFER".equals(this.method)) {
            validateBankTransferPayment();
        } else {
            throw new IllegalArgumentException("Unsupported payment method: " + this.method);
        }
    }

    private void validateVoucherPayment() {
        String voucherCode = this.paymentData.get("voucherCode");

        this.status = "REJECTED";

        if (voucherCode == null || voucherCode.length() != 16) {
            return;
        }

        if (!voucherCode.startsWith("ESHOP")) {
            return;
        }

        int digitCount = 0;
        for (char c : voucherCode.toCharArray()) {
            if (Character.isDigit(c)) {
                digitCount++;
            }
        }

        if (digitCount != 8) {
            return;
        }

        this.status = "SUCCESS";
    }

    private void validateBankTransferPayment() {
        String bankName = this.paymentData.get("bankName");
        String referenceCode = this.paymentData.get("referenceCode");

        if (bankName == null || bankName.isEmpty() ||
                referenceCode == null || referenceCode.isEmpty()) {
            this.status = "REJECTED";
            return;
        }

        this.status = "SUCCESS";
    }
}
