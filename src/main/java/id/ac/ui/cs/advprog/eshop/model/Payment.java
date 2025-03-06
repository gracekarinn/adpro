package id.ac.ui.cs.advprog.eshop.model;

import java.util.Map;

public class Payment {
    private String id;
    private Order order;
    private String method;
    private String status;
    private Map<String, String> paymentData;
}
