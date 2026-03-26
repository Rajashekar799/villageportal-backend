package com.pegadapalli.portal.repository;

import com.pegadapalli.portal.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, Long> {
}
