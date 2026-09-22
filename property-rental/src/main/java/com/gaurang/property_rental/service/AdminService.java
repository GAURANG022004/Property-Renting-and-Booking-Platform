package com.gaurang.property_rental.service;

import com.gaurang.property_rental.dto.BookingResponse;
import com.gaurang.property_rental.dto.PropertyResponse;
import com.gaurang.property_rental.dto.admin.*;
import com.gaurang.property_rental.model.*;
import com.gaurang.property_rental.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AdminService {
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyImageRepository propertyImageRepository;
    private final BookingRepository bookingRepository;
    private final PropertyCategoryRepository categoryRepository;
    private final ComplaintRepository complaintRepository;
    private final SystemSettingRepository settingRepository;

    public AdminService(UserRepository userRepository, PropertyRepository propertyRepository,
                        PropertyImageRepository propertyImageRepository, BookingRepository bookingRepository,
                        PropertyCategoryRepository categoryRepository, ComplaintRepository complaintRepository,
                        SystemSettingRepository settingRepository) {
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.propertyImageRepository = propertyImageRepository;
        this.bookingRepository = bookingRepository;
        this.categoryRepository = categoryRepository;
        this.complaintRepository = complaintRepository;
        this.settingRepository = settingRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> users() { return userRepository.findAll().stream().map(this::toUser).toList(); }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> owners() { return userRepository.findByRolesContaining("OWNER").stream().map(this::toUser).toList(); }

    @Transactional
    public AdminUserResponse setUserStatus(Long id, boolean active) {
        User user = userRepository.findById(id).orElseThrow(() -> notFound("User"));
        user.setActive(active);
        return toUser(user);
    }

    @Transactional
    public AdminUserResponse setUserRole(Long id, String role) {
        User user = userRepository.findById(id).orElseThrow(() -> notFound("User"));
        if (user.getRoles().contains("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An administrator role cannot be changed here");
        }
        user.setRole(role);
        return toUser(user);
    }

    @Transactional(readOnly = true)
    public List<PropertyResponse> properties() { return propertyRepository.findAll().stream().map(this::toProperty).toList(); }

    @Transactional
    public PropertyResponse moderateProperty(Long id, String status) {
        Property property = propertyRepository.findById(id).orElseThrow(() -> notFound("Property"));
        property.setApprovalStatus(status);
        return toProperty(property);
    }

    @Transactional
    public void deleteProperty(Long id) {
        if (!propertyRepository.existsById(id)) throw notFound("Property");
        propertyImageRepository.deleteByPropertyId(id);
        propertyRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> bookings() { return bookingRepository.findAll().stream().map(this::toBooking).toList(); }

    @Transactional
    public BookingResponse cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> notFound("Booking"));
        booking.cancel();
        return toBooking(booking);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> categories() { return categoryRepository.findAll().stream().map(this::toCategory).toList(); }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        return toCategory(categoryRepository.save(new PropertyCategory(request.name().trim(), request.description())));
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        PropertyCategory category = categoryRepository.findById(id).orElseThrow(() -> notFound("Category"));
        category.update(request.name().trim(), request.description());
        return toCategory(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) throw notFound("Category");
        categoryRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ComplaintResponse> complaints() { return complaintRepository.findAll().stream().map(this::toComplaint).toList(); }

    @Transactional
    public ComplaintResponse resolveComplaint(Long id, ComplaintResolutionRequest request) {
        Complaint complaint = complaintRepository.findById(id).orElseThrow(() -> notFound("Complaint"));
        complaint.resolve(request.status(), request.resolutionNote());
        return toComplaint(complaint);
    }

    @Transactional(readOnly = true)
    public List<SettingResponse> settings() { return settingRepository.findAll().stream().map(s -> new SettingResponse(s.getKey(), s.getValue())).toList(); }

    @Transactional
    public SettingResponse saveSetting(SettingRequest request) {
        SystemSetting setting = settingRepository.findById(request.key().trim())
                .orElseGet(() -> new SystemSetting(request.key().trim(), request.value()));
        setting.setValue(request.value());
        settingRepository.save(setting);
        return new SettingResponse(setting.getKey(), setting.getValue());
    }

    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        List<User> users = userRepository.findAll();
        List<Property> properties = propertyRepository.findAll();
        List<Booking> bookings = bookingRepository.findAll();
        List<Complaint> complaints = complaintRepository.findAll();
        return new DashboardResponse(users.size(), users.stream().filter(User::isActive).count(),
                users.stream().filter(u -> u.getRoles().contains("OWNER")).count(), properties.size(),
                properties.stream().filter(p -> "PENDING".equals(p.getApprovalStatus())).count(), bookings.size(),
                bookings.stream().filter(b -> "CANCELLED".equals(b.getStatus())).count(),
                complaints.stream().filter(c -> "OPEN".equals(c.getStatus())).count());
    }

    private AdminUserResponse toUser(User u) { return new AdminUserResponse(u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(), u.getPhoneNumber(), u.getRoles(), u.isActive()); }
    private PropertyResponse toProperty(Property p) { return new PropertyResponse(p.getId(), p.getTitle(), p.getDescription(), p.getLocation(), p.getPricePerNight(), p.getRating(), p.getApprovalStatus(), p.getOwner().getEmail(), propertyImageRepository.findByPropertyId(p.getId()).stream().map(i -> "/images/" + i.getRelativePath()).toList()); }
    private BookingResponse toBooking(Booking b) { Property p = b.getProperty(); return new BookingResponse(b.getId(), p.getId(), p.getTitle(), p.getLocation(), b.getCheckIn(), b.getCheckOut(), b.getStatus()); }
    private CategoryResponse toCategory(PropertyCategory c) { return new CategoryResponse(c.getId(), c.getName(), c.getDescription()); }
    private ComplaintResponse toComplaint(Complaint c) { return new ComplaintResponse(c.getId(), c.getReportedBy().getEmail(), c.getSubject(), c.getDescription(), c.getStatus(), c.getResolutionNote()); }
    private ResponseStatusException notFound(String resource) { return new ResponseStatusException(HttpStatus.NOT_FOUND, resource + " not found"); }
}
