package com.tradejapan.part3.service;

import com.tradejapan.part1.model.Role;
import com.tradejapan.part1.model.User;
import com.tradejapan.part1.repository.UserRepository;
import com.tradejapan.part2.model.Vehicle;
import com.tradejapan.part2.repository.VehicleRepository;
import com.tradejapan.part3.dto.AdminRef;
import com.tradejapan.part3.dto.InquiryView;
import com.tradejapan.part3.dto.PurchaseView;
import com.tradejapan.part3.dto.ReplyView;
import com.tradejapan.part3.model.Inquiry;
import com.tradejapan.part3.model.InquiryReply;
import com.tradejapan.part3.model.Purchase;
import com.tradejapan.part3.repository.InquiryReplyRepository;
import com.tradejapan.part3.repository.InquiryRepository;
import com.tradejapan.part3.repository.PurchaseRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Turns Inquiry / Purchase entities into views that also carry the customer's name, the
 * vehicle's name and make, the admins involved, and the unread counter.
 */
@Component
public class ViewMapper {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final InquiryReplyRepository replyRepository;
    private final PurchaseRepository purchaseRepository;
    private final InquiryRepository inquiryRepository;

    public ViewMapper(UserRepository userRepository, VehicleRepository vehicleRepository,
                      InquiryReplyRepository replyRepository, PurchaseRepository purchaseRepository,
                      InquiryRepository inquiryRepository) {
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.replyRepository = replyRepository;
        this.purchaseRepository = purchaseRepository;
        this.inquiryRepository = inquiryRepository;
    }

    // ---------- lookups ----------
    private Map<Long, User> usersById(Set<Long> ids) {
        Map<Long, User> map = new HashMap<>();
        if (ids.isEmpty()) return map;
        for (User u : userRepository.findAllById(ids)) map.put(u.getId(), u);
        return map;
    }

    private Map<Long, Vehicle> vehiclesById(Set<Long> ids) {
        Map<Long, Vehicle> map = new HashMap<>();
        if (ids.isEmpty()) return map;
        List<Integer> intIds = new ArrayList<>();
        for (Long id : ids) intIds.add(id.intValue());
        for (Vehicle v : vehicleRepository.findAllById(intIds)) map.put(v.getVehicleId().longValue(), v);
        return map;
    }

    private String fullName(User u) {
        if (u == null) return "Unknown user";
        String name = ((u.getFirstName() == null ? "" : u.getFirstName()) + " "
                + (u.getLastName() == null ? "" : u.getLastName())).trim();
        return name.isEmpty() ? u.getEmail() : name;
    }

    private String vehicleName(Vehicle v) {
        if (v == null) return "Vehicle removed";
        return (v.getMakeName() + " " + v.getModelName() + " " + v.getYear()).trim();
    }

    private String vehicleImage(Vehicle v) {
        if (v == null) return null;
        String urls = v.getImageUrlsString();
        if (urls == null || urls.isBlank()) return null;
        return urls.split(",")[0].trim();
    }

    private boolean isAdminUser(User u) {
        return u != null && (u.getRole() == Role.ROLE_ADMIN || u.getRole() == Role.ROLE_SUPER_ADMIN);
    }

    /** True when the reply was written by the Trade Japan team. Old rows (no flag) are decided by the author's role. */
    private boolean fromTeam(InquiryReply r, Map<Long, User> users) {
        if (r.getFromAdmin() != null) return r.getFromAdmin();
        return isAdminUser(users.get(r.getUserId()));
    }

    // ---------- inquiries ----------
    public List<InquiryView> inquiries(List<Inquiry> inquiries) {
        Set<Long> userIds = new HashSet<>();
        Set<Long> vehicleIds = new HashSet<>();
        Set<Long> inquiryIds = new HashSet<>();
        for (Inquiry i : inquiries) {
            userIds.add(i.getUserId());
            vehicleIds.add(i.getVehicleId());
            inquiryIds.add(i.getInquiryId());
        }

        Map<Long, List<InquiryReply>> repliesByInquiry = new HashMap<>();
        if (!inquiryIds.isEmpty()) {
            for (InquiryReply r : replyRepository.findByInquiryIdIn(inquiryIds)) {
                userIds.add(r.getUserId());
                repliesByInquiry.computeIfAbsent(r.getInquiryId(), k -> new ArrayList<>()).add(r);
            }
        }

        Map<Long, User> users = usersById(userIds);
        Map<Long, Vehicle> vehicles = vehiclesById(vehicleIds);
        Map<Long, Long> purchaseByInquiry = new HashMap<>();
        if (!inquiryIds.isEmpty()) {
            for (Purchase p : purchaseRepository.findByInquiryIdIn(inquiryIds)) {
                purchaseByInquiry.put(p.getInquiryId(), p.getPurchaseId());
            }
        }

        List<InquiryView> result = new ArrayList<>();
        for (Inquiry i : inquiries) {
            List<InquiryReply> mine = repliesByInquiry.getOrDefault(i.getInquiryId(), new ArrayList<>());

            // An admin reply means the admins have seen everything before it.
            long teamReplies = 0;
            LocalDateTime readAt = i.getAdminLastReadAt();
            Map<Long, AdminRef> handled = new LinkedHashMap<>();
            for (InquiryReply r : mine) {
                if (fromTeam(r, users)) {
                    teamReplies++;
                    if (readAt == null || r.getCreatedAt().isAfter(readAt)) readAt = r.getCreatedAt();
                    handled.putIfAbsent(r.getUserId(), new AdminRef(r.getUserId(), fullName(users.get(r.getUserId()))));
                }
            }

            // Unread = the customer's messages that arrived after the admins last looked.
            long unread = readAt == null ? 1 : 0;   // the first message of the inquiry itself
            for (InquiryReply r : mine) {
                if (!fromTeam(r, users) && (readAt == null || r.getCreatedAt().isAfter(readAt))) unread++;
            }

            User u = users.get(i.getUserId());
            Vehicle v = vehicles.get(i.getVehicleId());
            result.add(new InquiryView(
                    i.getInquiryId(), i.getUserId(), fullName(u), u == null ? null : u.getEmail(),
                    i.getVehicleId(), vehicleName(v), v == null ? null : v.getMakeName(), vehicleImage(v),
                    v == null ? null : v.getPriceUsd(),
                    i.getSubject(), i.getMessage(), i.getDestinationCountry(), i.getStatus(),
                    i.getCreatedAt(), i.getUpdatedAt(),
                    teamReplies, unread, new ArrayList<>(handled.values()),
                    purchaseByInquiry.get(i.getInquiryId())));
        }
        return result;
    }

    public InquiryView inquiry(Inquiry inquiry) {
        return inquiries(List.of(inquiry)).get(0);
    }

    public List<ReplyView> replies(List<InquiryReply> replies) {
        Set<Long> userIds = new HashSet<>();
        for (InquiryReply r : replies) userIds.add(r.getUserId());
        Map<Long, User> users = usersById(userIds);

        List<ReplyView> result = new ArrayList<>();
        for (InquiryReply r : replies) {
            User u = users.get(r.getUserId());
            boolean team = fromTeam(r, users);
            result.add(new ReplyView(r.getReplyId(), r.getInquiryId(), r.getUserId(),
                    team ? "Trade Japan Team" : fullName(u),
                    team ? fullName(u) : null,
                    team, r.getMessage(), r.getCreatedAt()));
        }
        return result;
    }

    public ReplyView reply(InquiryReply reply) {
        return replies(List.of(reply)).get(0);
    }

    // ---------- purchases ----------
    public List<PurchaseView> purchases(List<Purchase> purchases) {
        Set<Long> userIds = new HashSet<>();
        Set<Long> vehicleIds = new HashSet<>();
        Set<Long> inquiryIds = new HashSet<>();
        for (Purchase p : purchases) {
            userIds.add(p.getUserId());
            userIds.add(p.getProcessedBy());
            vehicleIds.add(p.getVehicleId());
            if (p.getInquiryId() != null) inquiryIds.add(p.getInquiryId());
        }
        Map<Long, User> users = usersById(userIds);
        Map<Long, Vehicle> vehicles = vehiclesById(vehicleIds);

        // purchases created before the destination column existed take it from their inquiry
        Map<Long, String> destinationByInquiry = new HashMap<>();
        if (!inquiryIds.isEmpty()) {
            for (Inquiry i : inquiryRepository.findAllById(inquiryIds)) {
                destinationByInquiry.put(i.getInquiryId(), i.getDestinationCountry());
            }
        }

        List<PurchaseView> result = new ArrayList<>();
        for (Purchase p : purchases) {
            User buyer = users.get(p.getUserId());
            User admin = users.get(p.getProcessedBy());
            Vehicle v = vehicles.get(p.getVehicleId());
            BigDecimal balance = p.getTotalPrice().subtract(p.getAdvancePaid());
            String destination = p.getDestinationCountry() != null
                    ? p.getDestinationCountry()
                    : (p.getInquiryId() == null ? null : destinationByInquiry.get(p.getInquiryId()));
            result.add(new PurchaseView(
                    p.getPurchaseId(), p.getUserId(), fullName(buyer), buyer == null ? null : buyer.getEmail(),
                    p.getVehicleId(), vehicleName(v), v == null ? null : v.getMakeName(), vehicleImage(v),
                    p.getProcessedBy(), fullName(admin), p.getInquiryId(), destination,
                    p.getInvoiceNo(), p.getInvoiceFileUrl(),
                    p.getTotalPrice(), p.getAdvancePaid(), balance,
                    p.getStatus(), p.getNotes(), p.getCreatedAt()));
        }
        return result;
    }

    public PurchaseView purchase(Purchase purchase) {
        return purchases(List.of(purchase)).get(0);
    }
}
