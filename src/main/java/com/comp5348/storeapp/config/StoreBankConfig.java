package com.comp5348.storeapp.config;

import lombok.Getter;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

@Log
@Component
@Getter
public class StoreBankConfig {

    private Long storeCustomerId;
    private Long storeAccountId;

    public StoreBankConfig() {
        // 确保商店的银行客户 ID 和账户 ID 与银行应用程序中的一致
        this.storeCustomerId = 1L; // 根据银行应用程序中的实际值替换
        this.storeAccountId = 1L;  // 根据银行应用程序中的实际值替换

        log.info("StoreBankConfig initialized with storeCustomerId=" + storeCustomerId + ", storeAccountId=" + storeAccountId);
    }
}

