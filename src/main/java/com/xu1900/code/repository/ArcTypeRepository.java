package com.xu1900.code.repository;

import com.xu1900.code.entity.ArcType;
import com.xu1900.code.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * 资源类型Respository
 */
public interface ArcTypeRepository extends JpaRepository<ArcType,Integer>, JpaSpecificationExecutor<ArcType> {

}
