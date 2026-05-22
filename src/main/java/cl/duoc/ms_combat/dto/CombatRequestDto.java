package cl.duoc.ms_combat.dto;

import lombok.Data;

import java.util.List;

@Data
public class CombatRequestDto {
    private String enemy;
    private Integer maxParticipants;
    private Integer experienceAwarded;
    private Integer coinsAwarded;
}