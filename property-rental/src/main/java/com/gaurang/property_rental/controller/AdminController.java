package com.gaurang.property_rental.controller;

import com.gaurang.property_rental.dto.BookingResponse;
import com.gaurang.property_rental.dto.PropertyResponse;
import com.gaurang.property_rental.dto.admin.*;
import com.gaurang.property_rental.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;
    public AdminController(AdminService adminService) { this.adminService = adminService; }

    @GetMapping("/dashboard") public DashboardResponse dashboard() { return adminService.dashboard(); }
    @GetMapping("/users") public List<AdminUserResponse> users() { return adminService.users(); }
    @GetMapping("/owners") public List<AdminUserResponse> owners() { return adminService.owners(); }
    @PatchMapping("/users/{id}/status") public AdminUserResponse setUserStatus(@PathVariable Long id, @RequestBody UserStatusRequest request) { return adminService.setUserStatus(id, request.active()); }
    @PatchMapping("/users/{id}/role") public AdminUserResponse setUserRole(@PathVariable Long id, @Valid @RequestBody UserRoleRequest request) { return adminService.setUserRole(id, request.role()); }

    @GetMapping("/properties") public List<PropertyResponse> properties() { return adminService.properties(); }
    @PatchMapping("/properties/{id}/approval") public PropertyResponse moderateProperty(@PathVariable Long id, @Valid @RequestBody PropertyModerationRequest request) { return adminService.moderateProperty(id, request.status()); }
    @DeleteMapping("/properties/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteProperty(@PathVariable Long id) { adminService.deleteProperty(id); }

    @GetMapping("/bookings") public List<BookingResponse> bookings() { return adminService.bookings(); }
    @PatchMapping("/bookings/{id}/cancel") public BookingResponse cancelBooking(@PathVariable Long id) { return adminService.cancelBooking(id); }

    @GetMapping("/categories") public List<CategoryResponse> categories() { return adminService.categories(); }
    @PostMapping("/categories") @ResponseStatus(HttpStatus.CREATED) public CategoryResponse createCategory(@Valid @RequestBody CategoryRequest request) { return adminService.createCategory(request); }
    @PutMapping("/categories/{id}") public CategoryResponse updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) { return adminService.updateCategory(id, request); }
    @DeleteMapping("/categories/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteCategory(@PathVariable Long id) { adminService.deleteCategory(id); }

    @GetMapping("/complaints") public List<ComplaintResponse> complaints() { return adminService.complaints(); }
    @PatchMapping("/complaints/{id}") public ComplaintResponse resolveComplaint(@PathVariable Long id, @Valid @RequestBody ComplaintResolutionRequest request) { return adminService.resolveComplaint(id, request); }

    @GetMapping("/settings") public List<SettingResponse> settings() { return adminService.settings(); }
    @PutMapping("/settings") public SettingResponse saveSetting(@Valid @RequestBody SettingRequest request) { return adminService.saveSetting(request); }
}
