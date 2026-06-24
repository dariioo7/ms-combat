package cl.duoc.ms_combat.service.impl;

import cl.duoc.ms_combat.client.CharacterFeignClient;
import cl.duoc.ms_combat.client.CurrencyFeignClient;
import cl.duoc.ms_combat.enums.CombatResult;
import cl.duoc.ms_combat.model.Combat;
import cl.duoc.ms_combat.repository.CombatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CombatServiceImplTest {

    @Mock private CombatRepository combatRepository;
    @Mock private CharacterFeignClient characterFeignClient;
    @Mock private CurrencyFeignClient currencyFeignClient;
    @InjectMocks private CombatServiceImpl service;

    @Test
    void createScenario_valoresNulos_usaValoresPorDefecto() {
        when(combatRepository.save(any(Combat.class))).thenAnswer(inv -> inv.getArgument(0));

        Combat c = service.createScenario("Dragon", 4, null, null, null);

        assertThat(c.getBaseExperience()).isZero();
        assertThat(c.getBaseCoins()).isZero();
        assertThat(c.getCurrencyType()).isEqualTo("GOLD");
    }

    @Test
    void playCombat_recompensaConsistenteConElResultadoYAcredita() {
        Combat c = new Combat();
        c.setUserId(1L);
        c.setBaseExperience(200);
        c.setBaseCoins(100);
        c.setCurrencyType("GOLD");
        when(combatRepository.findById(5L)).thenReturn(Optional.of(c));
        when(combatRepository.save(c)).thenReturn(c);

        Combat res = service.playCombat(5L);

        assertThat(res.getResult()).isIn(CombatResult.VICTORY, CombatResult.DEFEAT);
        if (res.getResult() == CombatResult.VICTORY) {
            assertThat(res.getCoinsGained()).isEqualTo(100);
            assertThat(res.getExperienceGained()).isEqualTo(200);
        } else {
            assertThat(res.getCoinsGained()).isEqualTo(10);
            assertThat(res.getExperienceGained()).isEqualTo(20);
        }
        verify(currencyFeignClient).addCurrency(eq(1L), any());
    }

    @Test
    void playCombat_sinUsuarioAsignado_noAcreditaRecompensa() {
        Combat c = new Combat();
        c.setBaseCoins(100);
        c.setBaseExperience(200);
        when(combatRepository.findById(5L)).thenReturn(Optional.of(c));
        when(combatRepository.save(c)).thenReturn(c);

        service.playCombat(5L);

        verifyNoInteractions(currencyFeignClient);
    }

    @Test
    void playCombat_combateInexistente_lanza404() {
        when(combatRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.playCombat(9L))
                .isInstanceOf(ResponseStatusException.class);
    }
}
