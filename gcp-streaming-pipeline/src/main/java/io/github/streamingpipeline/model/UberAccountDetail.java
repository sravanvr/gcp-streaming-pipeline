package io.github.streamingpipeline.model;

import java.util.Map;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UberAccountDetail {
	private UberAccount record;
	private Map<String, String> tags;	
}
