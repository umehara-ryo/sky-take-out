package com.sky.service;


public interface ShopService {

    void startOrStop(Integer status);

    Integer getStatus();

}
