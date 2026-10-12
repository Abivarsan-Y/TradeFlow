package com.tradejapan.cms.controller;

import com.tradejapan.cms.dto.CustomerDetailView;
import com.tradejapan.cms.dto.CustomerView;
import com.tradejapan.cms.dto.UpdateCustomerStatusRequest;
import com.tradejapan.cms.model.Role;
import com.tradejapan.cms.model.User;
import com.tradejapan.cms.repository.UserRepository;
import com.tradejapan.part3.exception.ResourceNotFoundException;
import com.tradejapan.part3.repository.InquiryRepository;
import com.tradejapan.part3.repository.PurchaseRepository;
import com.tradejapan.part3.service.ViewMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manage Customers (Admin / Super Admin; the /api/admin/** rule in SecurityConfig protects it).
 * Admins can look at customers and activate / deactivate them. They cannot edit customer data.
 * Only accounts with the CUSTOMER role are shown here (staff are managed under "Admins").
 */
@RestController
@RequestMapping("/api/admin/customers")
public class CustomerAdminController {

    private final UserRepository userRepository;
    private final InquiryRepository inquiryRepository;
    private final PurchaseRepository purchaseRepository;
    private final ViewMapper viewMapper;

    public CustomerAdminController(UserRepository userRepository, InquiryRepository inquiryRepository,
                                   PurchaseRepository purchaseRepository, ViewMapper viewMapper) {
        this.userRepository = userRepository;
        this.inquiryRepository = inquiryRepository;
        this.purchaseRepository = purchaseRepository;
        this.viewMapper = viewMapper;
    }

    /** All customers, newest first, with how many inquiries and purchases each has. */
    @GetMapping
    public List<CustomerView> list() {
        Map<Long, Long> inquiries = toMap(inquiryRepository.countPerUser());
        Map<Long, Long> purchases = toMap(purchaseRepository.countPerUser());
        List<User> customers = new ArrayList<>(userRepository.findByRole(Role.ROLE_CUSTOMER));
        customers.sort(Comparator.comparing(User::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));

        List<CustomerView> result = new ArrayList<>();
        for (User u : customers) {
            result.add(toView(u, inquiries.getOrDefault(u.getId(), 0L), purchases.getOrDefault(u.getId(), 0L)));
        }
        return result;
    }

    /** Read-only detail: the profile plus the customer's inquiries and purchases. */
    @GetMapping("/{id}")
    public CustomerDetailView detail(@PathVariable Long id) {
        User u = findCustomer(id);
        var inquiries = viewMapper.inquiries(inquiryRepository.findByUserIdOrderByCreatedAtDesc(id));
        var purchases = viewMapper.purchases(purchaseRepository.findByUserIdOrderByCreatedAtDesc(id));
        return new CustomerDetailView(toView(u, inquiries.size(), purchases.size()), inquiries, purchases);
    }

    /** Deactivate (INACTIVE) or reactivate (ACTIVE) a customer. A deactivated customer can no longer log in. */
    @PatchMapping("/{id}/status")
    public CustomerView updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateCustomerStatusRequest request) {
        User u = findCustomer(id);
        u.setStatus(request.status());
        userRepository.save(u);
        return toView(u, inquiryRepository.findByUserIdOrderByCreatedAtDesc(id).size(),
                purchaseRepository.findByUserIdOrderByCreatedAtDesc(id).size());
    }

    private User findCustomer(Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
        if (u.getRole() != Role.ROLE_CUSTOMER) {
            throw new ResourceNotFoundException("Customer not found: " + id);   // admins are not managed here
        }
        return u;
    }

    private Map<Long, Long> toMap(List<Object[]> rows) {
        Map<Long, Long> map = new HashMap<>();
        for (Object[] r : rows) map.put((Long) r[0], (Long) r[1]);
        return map;
    }

    private CustomerView toView(User u, long inquiries, long purchases) {
        return new CustomerView(u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(), u.getWhatsappNo(),
                u.getStatus() == null ? null : u.getStatus().name(),
                u.getAuthProvider() == null ? null : u.getAuthProvider().name(),
                u.getCreatedAt(), inquiries, purchases);
    }
}
