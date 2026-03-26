package com.pegadapalli.portal.repository;

import com.pegadapalli.portal.model.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
}
