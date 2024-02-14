package io.github.streamingpipeline.utils;

public enum ProcessStatus {
	WAIT,
	READY,
	LAUNCH,
	PROCESSING,
	SUCCESSFUL,
	WARNINGS,
	FATAL,
	DUPLICATE,
	OLD,
	LOCKED,
	INVALID_RUNSTREAM,
	UN_KNOWN,
	EXPIRED,
	PUBLISHED
}
