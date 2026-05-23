package cl.duoc.ms_combat.service.impl;

import cl.duoc.ms_combat.client.CharacterFeignClient;
import cl.duoc.ms_combat.client.CurrencyFeignClient;
import cl.duoc.ms_combat.dto.CharacterDto;
import cl.duoc.ms_combat.dto.CombatRequestDto;
import cl.duoc.ms_combat.enums.CombatResult;
import cl.duoc.ms_combat.model.Combat;
import cl.duoc.ms_combat.repository.CombatRepository;
import cl.duoc.ms_combat.service.CombatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;


@Service
@RequiredArgsConstructor
public class CombatServiceImpl implements CombatService {

    private final CombatRepository combatRepository;
    private final CharacterFeignClient characterFeignClient;
    private final CurrencyFeignClient currencyFeignClient;

    @Override
    public Combat createScenario(String enemy, Integer capacity, Integer exp, Integer coins) {
        Combat combat = new Combat();
        combat.setEnemy(enemy);
        combat.setMaxParticipants(capacity);
        combat.setBaseExperience(exp);
        combat.setBaseCoins(coins);
        combat.setCurrencyType(combat.getCurrencyType());
        combat.setCombatDate(LocalDateTime.now());
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
        Map<String, Object> currencyRequest = new HashMap<>();
        currencyRequest.put("currencyType", combat.getCurrencyType());
        currencyRequest.put("amount", combat.getCoinsGained());

        try {
            currencyFeignClient.addCurrency(combat.getUserId(), currencyRequest);
        } catch (Exception e) {
            System.out.println("Error al pagar la recompensa" + e.getMessage());
        }
        return combatRepository.save(combat);
    }

    @Override
    public List<Combat> findAll() {
        return combatRepository.findAll();
    }

}
