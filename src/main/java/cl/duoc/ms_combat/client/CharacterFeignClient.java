package cl.duoc.ms_combat.client;

import cl.duoc.ms_combat.dto.CharacterDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "ms-character", url = "http://localhost:8091/api/v1/characters")
public interface CharacterFeignClient {

    @GetMapping("/user/{userId}")
    List<CharacterDto> getCharactersByUserId(@PathVariable("userId") Long userId);

}
