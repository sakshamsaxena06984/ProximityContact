package com.smart.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Contact;
import com.smart.entities.User;

public interface UserRepository extends JpaRepository<User, Integer> {
	
	@Query("select u from User u where u.email= :email")
	public User getUserByUserName(@Param("email") String email);
	
	//deleting records from contact table
	
	@Query("delete from Contact where cId= :cId")
	public Contact deleteByUserId(@Param("cId") Integer cId);

}
