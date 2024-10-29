package com.somle.amazon.model.enums;

public enum ProcessingStatuses {

    CANCELLED("The report was cancelled. There are two ways a report can be cancelled: an explicit cancellation request before the report starts processing, or an automatic cancellation if there is no data to return."),
    DONE("The report has completed processing."),
    FATAL("The report was aborted due to a fatal error."),
    IN_PROGRESS("The report is being processed."),
    IN_QUEUE("The report has not yet started processing. It may be waiting for another IN_PROGRESS report.");

    private final String description;

    ProcessingStatuses(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

