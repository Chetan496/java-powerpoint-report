package com.hp.autonomy.frontend.reports.powerpoint;

public class SlideHasNoChartException extends Exception {
	
	public SlideHasNoChartException(final String message) {
		super(message);
	}

	public SlideHasNoChartException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
