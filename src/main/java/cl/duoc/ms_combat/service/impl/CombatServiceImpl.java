package cl.duoc.ms_combat.service.impl;

import cl.duoc.ms_combat.client.CharacterFeignClient;
import cl.duoc.ms_combat.client.CurrencyFeignClient;
import cl.duoc.ms_combat.dto.CharacterDto;
import cl.duoc.ms_combat.dto.CurrencyDto;
import cl.duoc.ms_combat.enums.CombatResult;
import cl.duoc.ms_combat.model.Combat;
import cl.duoc.ms_combat.repository.CombatRepository;
import cl.duoc.ms_combat.service.CombatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class CombatServiceImpl implements CombatService {

    private final CombatRepository combatRepository;
    private final CharacterFeignClient characterFeignClient;
    private final CurrencyFeignClient currencyFeignClient;

    @Override
    public Combat createScenario(String enemy, Integer capacity, Integer exp, Integer coins, String currencyType) {
        log.info("Creando nuevo escenario de combate vs {} con capacidad de {} personajes", enemy, capacity);
        Combat combat = new Combat();
        combat.setEnemy(enemy);
        combat.setMaxParticipants(capacity);
        combat.setBaseExperience(exp);
        combat.setBaseCoins(coins);
        combat.setCombatDate(LocalDateTime.now());
        combat.setCurrencyType(currencyType);
        return combatRepository.save(combat);
    }

    @Override
    public Combat assignTeam(Long combatId, Long userId, List<Long> characterIds) {
        Combat combat = combatRepository.findById(combatId).orElseThrow();

        List<CharacterDto> allUserCharacters = characterFeignClient.getCharactersByUserId(userId);

        List<String> names = new ArrayList<>();
        for (Long id : characterIds) {
            CharacterDto found = allUserCharacters.stream()
                    .filter(c -> c.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Personaje no encontrado"));
            names.add(found.getName());
        }

        combat.setUserId(userId);
        combat.setCharacterIds(characterIds);
        combat.setCharacterNames(names);
        return combatRepository.save(combat);
    }

    @Override
    public Combat playCombat(Long id) {
        Combat combat = combatRepository.findById(id).orElseThrow();
        boolean win = new Random().nextBoolean();

        if (win) {
            combat.setResult(CombatResult.VICTORY);
            combat.setExperienceGained(combat.getBaseExperience());
            combat.setCoinsGained(combat.getBaseCoins());
        } else {
            combat.setResult(CombatResult.DEFEAT);
            combat.setExperienceGained((int) (combat.getBaseExperience() * 0.1));
            combat.setCoinsGained((int) (combat.getBaseCoins() * 0.1));
        }

        if (combat.getUserId() != null) {
            try {
                CurrencyDto payment = new CurrencyDto();
                payment.setCurrencyType(combat.getCurrencyType());
                payment.setAmount(combat.getCoinsGained());

                currencyFeignClient.addCurrency(combat.getUserId(), payment);

                log.info("Premio enviado a ms-currency para el usuario: {}", combat.getUserId());
            } catch (Exception e) {
                log.error("No se pudo entregar el premio: {}", e.getMessage());
            }
        }
        return combatRepository.save(combat);
    }

    @Override
    public List<Combat> findAll() {
        return combatRepository.findAll();
    }
}
