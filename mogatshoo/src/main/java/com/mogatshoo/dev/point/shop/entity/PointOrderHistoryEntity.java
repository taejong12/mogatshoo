package com.mogatshoo.dev.point.shop.entity;

import com.mogatshoo.dev.common.point.entity.AbstractPointOrderHistoryEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "point_order_history")
public class PointOrderHistoryEntity extends AbstractPointOrderHistoryEntity {

}
