package com.ticketmaster.exp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketmaster.exp.MatchType;
import com.ticketmaster.exp.Result;
import com.ticketmaster.exp.model.ExperimentRun;
import com.ticketmaster.exp.repository.ExperimentRunRepository;

import java.util.Objects;

public class ExperimentService {
    private final ObjectMapper objectMapper;
    private final ExperimentRunRepository repository;

    /**
     * Constructs a new ExperimentService.
     * @param repository The repository used for persisting experiment run data.
     */
    public ExperimentService(ExperimentRunRepository repository) {
        this.objectMapper = new ObjectMapper();
        this.repository = repository;
    }

    public <O> void saveResult(Result<O> result) {
        ExperimentRun run = new ExperimentRun();
        run.setExperimentKey(result.getName());
        run.setStartTime(result.getTimestamp());
        // set duration
        run.setControlDurationMs(result.getControlResult().getDuration().toMillis());
        run.setCandidateDurationMs(result.getCandidateResult().getDuration().toMillis());

        // set values
        run.setControlValue(serializeResult(result.getControlResult().getResult().value().orElse(null)));
        run.setCandidateValue(serializeResult(result.getCandidateResult().getResult().value().orElse(null)));

        // set exception details
        run.setControlExceptionDetails(serializeResult(result.getControlResult().getResult().exception().orElse(null)));
        run.setCandidateExceptionDetails(serializeResult(result.getCandidateResult().getResult().exception().orElse(null)));

        run.setMatchType(determineMatchType(result.getControlResult().getResult().value(),
                result.getControlResult().getResult().exception().orElse(null),
                result.getCandidateResult().getResult().value().orElse(null),
                result.getCandidateResult().getResult().exception().orElse(null)));

        repository.save(run);
    }

    private MatchType determineMatchType(Object controlResult, Throwable controlException, Object candidateResult, Throwable candidateException) {
        boolean controlHasException = controlException != null;
        boolean candidateHasException = candidateException != null;

        if (controlHasException && candidateHasException) {
            // Compare exception types and messages
            if (Objects.equals(controlException.getClass(), candidateException.getClass()) &&
                    Objects.equals(controlException.getMessage(), candidateException.getMessage())) {
                return MatchType.EXCEPTION_MATCH;
            } else {
                return MatchType.EXCEPTION_MISMATCH;
            }
        } else if (controlHasException) {
            return MatchType.CONTROL_EXCEPTION;
        } else if (candidateHasException) {
            return MatchType.CANDIDATE_EXCEPTION;
        } else {
            // Compare results
            return Objects.equals(controlResult, candidateResult) ? MatchType.MATCH : MatchType.MISMATCH;
        }
    }

    private String serializeResult(Object result) {
        if (result == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "Error serializing result: " + e.getMessage();
        }
    }
}
