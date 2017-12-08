package com.hp.autonomy.frontend.reports.powerpoint;

public class ChartHasNoWorkBookException extends Exception {
	public ChartHasNoWorkBookException(final String message) {
		super(message);
	}

	public ChartHasNoWorkBookException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
