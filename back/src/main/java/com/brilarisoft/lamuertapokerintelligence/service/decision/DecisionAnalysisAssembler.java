package com.brilarisoft.lamuertapokerintelligence.service.decision;

import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionContext;
import com.brilarisoft.lamuertapokerintelligence.domain.decision.DecisionResult;
import com.brilarisoft.lamuertapokerintelligence.domain.referential.DecisionStatus;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DecisionAnalysisAssembler {

    public DecisionResult success(DecisionContext context, RangeDecisionComputation computation) {
        DecisionResult result = new DecisionResult();
        result.setDecisionContext(context);
        result.setStatus(DecisionStatus.SUCCESS);
        result.setRecommendedAction(computation.action());
        result.setRecommendedSizingValue(computation.sizingValue());
        result.setExplanation(computation.explanation());
        result.setTrace(new ArrayList<>(computation.insights()));
        result.setWarnings(List.of());
        return result;
    }

    public void appendTechnicalDetail(DecisionResult result, String detail) {
        List<String> trace = result.getTrace() == null ? new ArrayList<>() : new ArrayList<>(result.getTrace());
        trace.add(detail);
        result.setTrace(trace);
    }

    public void appendWarning(DecisionResult result, String warning) {
        List<String> warnings = result.getWarnings() == null ? new ArrayList<>() : new ArrayList<>(result.getWarnings());
        warnings.add(warning);
        result.setWarnings(warnings);
    }
}
