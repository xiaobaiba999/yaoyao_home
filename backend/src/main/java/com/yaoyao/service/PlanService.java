package com.yaoyao.service;

import com.yaoyao.entity.Plan;

import java.util.List;

public interface PlanService {

    List<Plan> list();

    Plan create(Plan plan);

    Plan update(String id, Plan plan);

    void delete(String id);
}
