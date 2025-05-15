package com.mogatshoo.dev.fortune.entity;

import java.time.LocalDate;

import groovy.transform.ToString;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity(name="fortune")
@Table(name="fortune")
@Getter
@Setter
@ToString
public class FortuneEntity {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer fortuneId;
	
	@Column
	private String fortuneContent;
	
	@Column
	private String fortuneItem;
	
	@Column(insertable = false, updatable = false, columnDefinition = "timestamp default current_timestamp")
	private LocalDate fortuneCreate;
	
	@Column(insertable = false, updatable = true, columnDefinition = "timestamp default current_timestamp ON UPDATE current_timestamp")
	private LocalDate fortuneUpdate;
	
}
