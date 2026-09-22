package com.gaurang.property_rental.repository;

import com.gaurang.property_rental.model.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, String> { }
