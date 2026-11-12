package com.ticketmaster.exp;

import com.ticketmaster.exp.publish.MeasurerPublisher;
import com.ticketmaster.exp.util.ReturnChoices;
import com.ticketmaster.exp.util.SameWhens;
import com.ticketmaster.exp.util.Try;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import static com.ticketmaster.exp.util.Selectors.always;

public class ExperimentBuilder<I, O> {
    private final String name;
    private Function<I, O> control;
    private Function<I, O> candidate;
    private Publisher<O> publisher = MeasurerPublisher.DEFAULT;

    private ExecutorService executorService = Executors.newFixedThreadPool(2);  // 2 per experiment (control & candidate)
    private Function<Result<O>, Try<O>> returnChoice = ReturnChoices.alwaysControl();
    private BooleanSupplier doExperimentWhen = always();
    private BiFunction<O, O, Boolean> sameWhen = Objects::equals;
    private BiFunction<Exception, Exception, Boolean> exceptionsSameWhen = SameWhens.classesMatch();

    public ExperimentBuilder(String name) {
        this.name = name;
    }
    public ExperimentBuilder<I, O> control(Function<I, O> control) {
        this.control = control;
        return this;
    }

    public ExperimentBuilder<I, O> candidate(Function<I, O> candidate) {
        this.candidate = candidate;
        return this;
    }
    public ExperimentBuilder<I, O> publishedBy(Publisher<O> publisher) {
        this.publisher = publisher;
        return this;
    }

    public ExperimentBuilder<I, O> returnChoice(Function<Result<O>, Try<O>> returnChoice) {
        this.returnChoice = returnChoice;
        return this;
    }

    public ExperimentBuilder<I, O> doExperimentWhen(BooleanSupplier doExperimentWhen) {
        this.doExperimentWhen = doExperimentWhen;
        return this;
    }
    public ExperimentBuilder<I, O> sameWhen(BiFunction<O, O, Boolean> sameWhen) {
        this.sameWhen = sameWhen;
        return this;
    }
    public ExperimentBuilder<I, O> exceptionsSameWhen(BiFunction<Exception, Exception, Boolean> exceptionsSameWhen) {
        this.exceptionsSameWhen = exceptionsSameWhen;
        return this;
    }
    public ExperimentBuilder<I, O> withExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }
    public Experiment<I, O> build() {
        Experiment<I, O> experiment = new Experiment<>(name);
        experiment.setName(this.name);
        experiment.setControl(this.control);
        experiment.setCandidate(this.candidate);
        experiment.setExecutorService(this.executorService);
        experiment.setReturnChoice(this.returnChoice);
        experiment.setDoExperimentWhen(this.doExperimentWhen);
        experiment.setSameWhen(this.sameWhen);
        experiment.setExceptionsSameWhen(this.exceptionsSameWhen);
        experiment.setPublisher(this.publisher);
        return experiment;
    }
}
