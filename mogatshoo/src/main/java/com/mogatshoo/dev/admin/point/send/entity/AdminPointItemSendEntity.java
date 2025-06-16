package com.mogatshoo.dev.admin.point.send.entity;

import com.mogatshoo.dev.common.point.entity.AbstractPointOrderHistoryEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "point_order_history")
public class AdminPointItemSendEntity extends AbstractPointOrderHistoryEntity {
	
}
