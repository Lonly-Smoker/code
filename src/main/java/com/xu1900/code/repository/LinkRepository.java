package com.xu1900.code.repository;

import com.xu1900.code.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LinkRepository extends JpaRepository<Link,Integer>, JpaSpecificationExecutor<Link> {
}
