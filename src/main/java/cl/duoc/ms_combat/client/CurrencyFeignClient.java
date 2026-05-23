package cl.duoc.ms_combat.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ms-currency", url = "http://localhost:8095/api/v1/currency")
public interface CurrencyFeignClient {

    @PostMapping("/add/{userId}")
    void addCurrency(@PathVariable("userId") Long userId, @RequestBody Object dto);
}