package id.ac.ui.cs.advprog.eshop.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import id.ac.ui.cs.advprog.eshop.model.Payment;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentRepository {
    private Map<String, Payment> payments;

    public PaymentRepository() {
        this.payments = new HashMap<>();
    }

    public Payment findById(String id) {return null;}

    public Payment save(Payment payment) {return null;}

    public List<Payment> findAll() {return null;}

    public List<Payment> findByOrderId(String orderId) {return null;}
}
