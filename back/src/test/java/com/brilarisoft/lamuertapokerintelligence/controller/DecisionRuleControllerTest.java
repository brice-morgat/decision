package com.brilarisoft.lamuertapokerintelligence.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.brilarisoft.lamuertapokerintelligence.domain.referential.ActionType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.GameType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Position;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.RuleOperator;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.ScenarioType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.SizingType;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.Street;
import com.brilarisoft.lamuertapokerintelligence.domain.rule.ConditionValueType;
import com.brilarisoft.lamuertapokerintelligence.domain.rule.RuleConditionType;
import com.brilarisoft.lamuertapokerintelligence.dto.rule.DecisionRuleDetailDto;
import com.brilarisoft.lamuertapokerintelligence.dto.rule.RuleActionDto;
import com.brilarisoft.lamuertapokerintelligence.dto.rule.RuleConditionDto;
import com.brilarisoft.lamuertapokerintelligence.exception.RestExceptionHandler;
import com.brilarisoft.lamuertapokerintelligence.service.DecisionRuleService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class DecisionRuleControllerTest {

    @Mock
    private DecisionRuleService decisionRuleService;

    @Test
    void createRuleRejectsInvalidPayload() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new DecisionRuleController(decisionRuleService))
                .setControllerAdvice(new RestExceptionHandler())
                .build();

        mockMvc.perform(post("/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "profileId": null,
                                  "name": "",
                                  "actions": []
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRuleReturnsCreated() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new DecisionRuleController(decisionRuleService))
                .setControllerAdvice(new RestExceptionHandler())
                .build();

        when(decisionRuleService.createRule(any())).thenReturn(new DecisionRuleDetailDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Flop c-bet",
                "",
                GameType.CASH,
                Street.FLOP,
                ScenarioType.FLOP_CBET_SPOT,
                Position.BTN,
                Position.BB,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                false,
                10,
                null,
                List.of(new RuleConditionDto(RuleConditionType.HERO_WAS_PREFLOP_AGGRESSOR, RuleOperator.EQUALS, ConditionValueType.BOOLEAN, "true", null, 0)),
                List.of(new RuleActionDto(ActionType.BET, SizingType.POT_PERCENT, java.math.BigDecimal.valueOf(50), 0, null)),
                Instant.now(),
                Instant.now()
        ));

        mockMvc.perform(post("/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "profileId": "%s",
                                  "name": "Flop c-bet",
                                  "description": "",
                                  "gameType": "CASH",
                                  "street": "FLOP",
                                  "scenarioType": "FLOP_CBET_SPOT",
                                  "heroPosition": "BTN",
                                  "villainPosition": "BB",
                                  "enabled": true,
                                  "stopOnMatch": false,
                                  "priority": 10,
                                  "conditions": [
                                    {
                                      "conditionType": "HERO_WAS_PREFLOP_AGGRESSOR",
                                      "operator": "EQUALS",
                                      "valueType": "BOOLEAN",
                                      "expectedValue": "true",
                                      "conditionOrder": 0
                                    }
                                  ],
                                  "actions": [
                                    {
                                      "actionType": "BET",
                                      "sizingType": "POT_PERCENT",
                                      "sizingValue": 50,
                                      "executionOrder": 0
                                    }
                                  ]
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isCreated());
    }
}
