package com.gaurang.property_rental;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import com.gaurang.property_rental.model.Booking;
import com.gaurang.property_rental.model.Property;
import com.gaurang.property_rental.model.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PropertyRentalApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void bookingLocksFullRentAtCreationAndTransitionsToCompleted() {
		User tenant = new User("tenant@example.com", "hash", Set.of("TENANT"));
		Property property = new Property("Rental", "Description", "Pune", 2000, tenant);
		Booking booking = new Booking(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 6), property, tenant);

		property.update("Rental", "Description", "Pune", 3000);
		assertEquals(new BigDecimal("10000.00"), booking.getTotalAmount());

		booking.accept();
		assertEquals("PAYMENT_PENDING", booking.getStatus());
		booking.confirmPayment();
		assertEquals("CONFIRMED", booking.getStatus());
		booking.checkIn();
		assertEquals("ACTIVE", booking.getStatus());
		booking.complete();
		assertEquals("COMPLETED", booking.getStatus());
	}

	@Test
	void instantBookingsAndOwnerApprovalRequestsStartInDifferentStates() {
		User tenant = new User("paths@example.com", "hash", Set.of("TENANT"));
		Property property = new Property("Rental", "Description", "Pune", 2000, tenant);
		LocalDate checkIn = LocalDate.of(2026, 11, 10);
		LocalDate checkOut = LocalDate.of(2026, 11, 12);

		Booking instant = new Booking(checkIn, checkOut, property, tenant, 3, true);
		Booking request = new Booking(checkIn, checkOut, property, tenant, 2, false);

		assertEquals("PAYMENT_PENDING", instant.getStatus());
		assertEquals("INSTANT", instant.getBookingType());
		assertEquals(3, instant.getGuestCount());
		assertEquals("PENDING", request.getStatus());
		assertEquals("REQUEST", request.getBookingType());
		assertEquals(2, request.getGuestCount());
	}

	@Test
	void reactivatingAPropertyRestoresItsPriorApprovalState() {
		User owner = new User("owner@example.com", "hash", Set.of("OWNER"));
		Property property = new Property("Rental", "Description", "Pune", 2000, owner);

		property.deactivate();
		assertEquals("INACTIVE", property.getApprovalStatus());
		property.reactivate();
		assertEquals("APPROVED", property.getApprovalStatus());

		property.setApprovalStatus("REJECTED");
		property.deactivate();
		property.reactivate();
		assertEquals("REJECTED", property.getApprovalStatus());
	}

}
